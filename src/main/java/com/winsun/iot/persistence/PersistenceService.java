package com.winsun.iot.persistence;

import com.winsun.iot.utils.functions.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class PersistenceService implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(PersistenceService.class);
    private Queue<Function> sqlOptCmd = new ArrayDeque<>();

    private ScheduledExecutorService executorService;
    private static final int checkPeriod = 3;

    private static final int maxBuffer = 1000;
    private static final int maxTime = 60; //second
    private LocalDateTime lastCheckTime;

    public void start() {
        executorService = Executors.newScheduledThreadPool(1, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("persistence-thread");
                return thread;
            }
        });
        executorService.scheduleAtFixedRate(this, 0, checkPeriod, TimeUnit.SECONDS);
        lastCheckTime = LocalDateTime.now();
    }

    public void addAction(Function func){
        this.sqlOptCmd.offer(func);
    }

    @Override
    public void run() {
        logger.info("check data for save {}",this.sqlOptCmd.size());
        Duration duration = Duration.between(lastCheckTime,LocalDateTime.now());

        if (sqlOptCmd.size() > maxBuffer || duration.toMillis() / 1000 > maxTime) {
            int execSize = 0;
            try {
                logger.info("save data to database ");
                int sqlexcqueuesize = sqlOptCmd.size();

                while (true) {
                    Function consumer = sqlOptCmd.poll();
                    if(consumer==null){
                        break;
                    }
                    consumer.execute();

                    execSize++;
                    if(execSize>sqlexcqueuesize){
                        break;
                    }
                }
            } catch (Exception exc) {
                logger.error(exc.getMessage(), exc);
            }finally {
                logger.info("save data to database finish {}",execSize);
                lastCheckTime=LocalDateTime.now();
            }
        }
    }
}
