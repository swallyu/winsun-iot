package com.winsun.iot.logdata;

import com.alibaba.fastjson.JSONObject;
import com.winsun.iot.persistence.redis.RedisService;
import com.winsun.iot.utils.FileUtils;
import com.winsun.iot.utils.ResourceChecker;
import com.winsun.iot.utils.ZipUtil;
import com.winsun.iot.utils.functions.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 日志数据处理
 */
public class LogDataService {
    private static final Logger logger = LoggerFactory.getLogger(LogDataService.class);

    //先保存在 redis中，再保存在文件中。
    //data->redis->files
    private RedisService redisService;

    private static final String REDIS_LOGSTREAM_KEY = "logDataStreamName";

    private Set<String> streamNames = new HashSet<>();

    private Map<String, SaveLogDataToFileChecker> checkerMap = new ConcurrentHashMap<>();

    private static final int BATCH_SIZE = 10000;

    private ScheduledExecutorService executorService;

    private File eventFilePath;

    public LogDataService(RedisService redisService) {
        eventFilePath = new File(FileUtils.getProjectPath() + "/events");
        logger.info("event save path : {}", eventFilePath.getAbsolutePath());
        if (!eventFilePath.exists()) {
            eventFilePath.mkdirs();
        }

        this.redisService = redisService;
        executorService = Executors.newScheduledThreadPool(20,new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("logdata-save-thread");
                return t;
            }
        });
        ResourceChecker.addCheckTask(10, new Action<Boolean>() {
            @Override
            public Boolean execute() {
                try {
                    streamNames = redisService.sget(REDIS_LOGSTREAM_KEY);
                    for (String streamName : streamNames) {
                        startTask(streamName);
                    }
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        });
    }

    public void addData(String streamName, String data) {
        if (!streamNames.contains(streamName)) {
            redisService.sadd(REDIS_LOGSTREAM_KEY, streamName);
            startTask(streamName);
        }
        this.redisService.lpush(streamName, data);
    }

    private void startTask(String streamName) {
        if (checkerMap.containsKey(streamName)) {
            return;
        }
        SaveLogDataToFileChecker checker = new SaveLogDataToFileChecker(streamName, redisService);
        checkerMap.put(streamName, checker);
        executorService.scheduleAtFixedRate(checker,0,1,TimeUnit.SECONDS);
    }

    private class SaveLogDataToFileChecker implements Runnable, Closeable {
        private String streamName;
        private RedisService redisService;

        private File saveFileName;
        private File saveFilePath;
        private LocalDateTime lastUpdateTime;
        private int period = 60; //一分钟保存一次

        private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HH");

        private String currentFileName;
        private BufferedWriter bw;
        private AtomicBoolean running = new AtomicBoolean(false);

        private ReadWriteLock lock = new ReentrantReadWriteLock();

        public SaveLogDataToFileChecker(String streamName, RedisService redisService) {
            this.saveFilePath = new File(eventFilePath,streamName);
            if(!this.saveFilePath.exists()){
                this.saveFilePath.mkdirs();
            }
            this.streamName = streamName;
            this.redisService = redisService;
            this.lastUpdateTime = LocalDateTime.now();
            this.currentFileName = getDataFile();
            this.saveFileName = new File(saveFilePath, currentFileName);
            try {
                this.bw = new BufferedWriter(new FileWriter(this.saveFileName));
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }

        @Override
        public void run() {
            boolean lockret = lock.writeLock().tryLock();
            if(!lockret){
                return;
            }
            try {
                String dataFile = getDataFile();
                if (!Objects.equals(this.currentFileName, dataFile)) {
                    changeFile();
                }
                long datalength = redisService.llen(streamName);
                logger.info("event {},length {}", streamName, datalength);
                if (datalength >= BATCH_SIZE || LocalDateTime.now().isAfter(this.lastUpdateTime.plusSeconds(period))) {
                    List<String> datas = redisService.lpopRange(streamName, BATCH_SIZE);
                    if (datas.size() != 0) {
                        writeData(datas);
                    }
                    this.lastUpdateTime = LocalDateTime.now();
                }
                Thread.sleep(1000);
            } catch (Exception exc) {
                logger.error(exc.getMessage());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } finally {
                this.lock.writeLock().unlock();
            }
        }

        @Override
        public void close() throws IOException {
            if (bw != null) {
                bw.flush();
                bw.close();
            }
        }

        private String getDataFile() {
            return streamName + "-" + getFileExtension(this.lastUpdateTime) + ".json";
        }

        private String getFileExtension(LocalDateTime time) {
            String ext = time.format(formatter);
            int v = time.getMinute() / 5 * 5;
            ext = ext + String.format("%02d", v);
            return ext;
        }

        private void writeData(List<String> data) throws Exception {
            for (String value : data) {
                bw.write(value);
                bw.newLine();
            }
            bw.flush();
            compressFile(currentFileName);
        }

        private void changeFile() throws Exception {
            BufferedWriter oldBw = this.bw;
            String oldFilName = this.currentFileName;

            this.currentFileName = getDataFile();
            this.saveFileName = new File(saveFilePath, currentFileName);
            try {
                this.bw = new BufferedWriter(new FileWriter(this.saveFileName));
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }

            if (oldBw != null) {
                oldBw.flush();
                oldBw.close();
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    compressFile(oldFilName);
                }
            }).start();
        }

        private void compressFile(String file){
            ZipUtil.compress(new File(saveFilePath,file),
                    new File(saveFilePath,file+".zip"));
        }

    }
}
