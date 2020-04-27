package com.winsun.iot.logdata;

import com.alibaba.fastjson.JSONObject;
import com.winsun.iot.persistence.redis.RedisService;
import com.winsun.iot.utils.FileUtils;
import com.winsun.iot.utils.ResourceChecker;
import com.winsun.iot.utils.functions.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

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

    private ExecutorService executorService;

    private File eventFilePath;

    public LogDataService(RedisService redisService) {
        eventFilePath = new File(FileUtils.getProjectPath() + "/events");
        logger.info("event save path : {}",eventFilePath.getAbsolutePath());
        if (!eventFilePath.exists()) {
            eventFilePath.mkdirs();
        }

        this.redisService = redisService;
        executorService = Executors.newCachedThreadPool(new ThreadFactory() {
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
        executorService.execute(checker);
    }

    private static class SaveLogDataToFileChecker implements Runnable {
        private String streamName;
        private String fileName;
        private RedisService redisService;

        private File saveFileName;
        private LocalDateTime lastUpdateTime;

        public SaveLogDataToFileChecker(String streamName, RedisService redisService) {
            this.streamName = streamName;
            this.redisService = redisService;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    List<String> datas = redisService.lpopRange(streamName, BATCH_SIZE);
                    if (datas.size() == 0) {
                        for (String data : datas) {
                            logger.info(data);
                        }
                        Thread.sleep(1000);
                    }
                } catch (Exception exc) {
                    logger.error(exc.getMessage());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
