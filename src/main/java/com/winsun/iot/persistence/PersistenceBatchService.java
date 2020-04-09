package com.winsun.iot.persistence;

import com.google.inject.Inject;
import com.winsun.iot.config.Config;
import com.winsun.iot.dao.CommonDao;
import com.winsun.iot.utils.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class PersistenceBatchService {
    private static final Logger logger = LoggerFactory.getLogger(PersistenceBatchService.class);

    private ScheduledExecutorService executorService;
    private static final int checkPeriod = 3;

    private static final int maxBuffer = 100000;
    private static final int operat1timemax=10000;

    private static final int maxTime = 60; //second

    private Map<String,BatchTask> taskMap = new HashMap<>();

    private CommonDao commonDao;

    public PersistenceBatchService(CommonDao commonDao) {
        this.commonDao = commonDao;
    }

    public void start() {
        executorService = Executors.newScheduledThreadPool(1, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("persistence-thread");
                return thread;
            }
        });
    }

    public void addTask(String tableName, String[] fields){
        BatchTask task = taskMap.get(tableName);
        if(task==null){
            task = new BatchTask(tableName,fields);
            ScheduledFuture<?> future =  executorService.scheduleAtFixedRate(task, 0, checkPeriod, TimeUnit.SECONDS);
            task.setFuture(future);
            taskMap.put(tableName,task);
        }
    }

    public void addDataToTask(String tableName,String data){
        BatchTask task = taskMap.get(tableName);
        if(task!=null){
            task.addData(data);
        }
    }

    public class BatchTask implements Runnable{
        private String tableName;
        private String[] fields;
        private LocalDateTime lastCheckTime;

        private Queue<String> queue = new ArrayDeque<>(10000);
        private ScheduledFuture<?> future;

        public BatchTask(String tableName, String[] fields) {
            this.tableName = tableName;
            this.fields = fields;
            this.lastCheckTime = LocalDateTime.now();
        }

        private AtomicBoolean running = new AtomicBoolean();

        @Override
        public void run() {
            if(running.get()){
                return;
            }
            running.set(true);
//            logger.info("check data for save {}： {}",this.tableName,this.queue.size());
            Duration duration = Duration.between(lastCheckTime,LocalDateTime.now());

            if (queue.size() > maxBuffer || duration.toMillis() / 1000 > maxTime) {
                int execSize = 0;
                try {
                    logger.info("save data to database {},",this.tableName);
                    int sqlexcqueuesize = queue.size();

                    File tmpFiledir = new File(Config.getMySqlLoadFilePath());
                    tmpFiledir.mkdirs();

                    int lineCount=0;
                    if (sqlexcqueuesize > 0) {
                        File file = new File(tmpFiledir, tableName +  "-" +
                                DateTimeUtils.formatDatePlain(LocalDateTime.now()) + ".txt");
                        String begin=null;
                        String end=null;

                        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                            while (true) {
                                if (lineCount >= operat1timemax || lineCount >= sqlexcqueuesize) {
                                    break;
                                }
                                String sqlstr = this.queue.poll();
                                bw.write(sqlstr);
                                bw.newLine();
                                lineCount++;
                                if(begin==null){
                                    begin=sqlstr;
                                }
                                end=sqlstr;
                            }
                        } catch (IOException e) {
                            logger.error(e.getMessage(), e);
                        }
                        logger.info("data file {} , lines {} ,\n{}\n{}",file.getAbsolutePath(),lineCount,begin,end);
                        String excusqlstr = MessageFormat.format("load data local infile \"{0}\" into table {1}({2})",
                                file.getAbsolutePath().replaceAll("\\\\", "/"), tableName,
                                String.join(",", fields));

                        commonDao.executeRawSql(excusqlstr);
                    }
                } catch (Exception exc) {
                    logger.error(exc.getMessage(), exc);
                }finally {
                    logger.info("save data to database finish {} :{}",this.tableName,execSize);
                    lastCheckTime=LocalDateTime.now();
                }
            }
            running.set(false);
        }

        public void addData(String data) {
            this.queue.offer(data);
        }

        public void setFuture(ScheduledFuture<?> future) {
            this.future = future;
        }

        public ScheduledFuture<?> getFuture() {
            return future;
        }
    }

}
