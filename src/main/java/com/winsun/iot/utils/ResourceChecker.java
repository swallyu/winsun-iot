package com.winsun.iot.utils;

import com.winsun.iot.utils.functions.Action;
import com.winsun.iot.utils.functions.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

public class ResourceChecker {

    private static Map<String,CheckTask> taskList = new HashMap<>();

    private static ScheduledExecutorService service = Executors.newScheduledThreadPool(5, new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setName("resource-checker");
            return t;
        }
    });

    /**
     *
     * @param retryInterval second
     * @param action
     */
    public static void addCheckTask(int retryInterval, Action<Boolean> action){
        CheckTask task = new CheckTask(UUID.randomUUID().toString(),retryInterval,action);

        taskList.put(task.taskId,task);
        ScheduledFuture<?> f = service.scheduleAtFixedRate(task,0,retryInterval, TimeUnit.SECONDS);
        task.setFuture(f);
    }

    private static class CheckTask implements Runnable{
        private static final Logger logger = LoggerFactory.getLogger(ResourceChecker.class);
        private String taskId;
        private int retryInterval=10;

        private Action<Boolean> action;
        private ScheduledFuture<?> future;

        private boolean running = false;
        @Override
        public void run() {
            try{
                if(running){
                    return;
                }
                running=true;
                logger.info("res check");
                boolean ret = action.execute();
                if(ret){
                    logger.info("resource check suc");
                    future.cancel(true);
                }
            }catch (Exception exc){
                logger.error(exc.getMessage(),exc);
            }finally {
                running=false;
            }
        }

        public CheckTask(String taskId,int retryInterval, Action<Boolean> action) {
            this.retryInterval = retryInterval;
            this.action = action;
            this.taskId = taskId;
        }

        public int getRetryInterval() {
            return retryInterval;
        }

        public Action<Boolean> getAction() {
            return action;
        }

        public void setFuture(ScheduledFuture<?> future) {
            this.future = future;
        }

        public ScheduledFuture<?> getFuture() {
            return future;
        }

    }
}
