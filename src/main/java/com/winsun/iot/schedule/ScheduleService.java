package com.winsun.iot.schedule;

import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class ScheduleService {

    private Map<String,SchduleTask> taskMap= new ConcurrentHashMap<>();

    private ScheduledExecutorService scheduledExecutorService;

    public ScheduleService() {
        scheduledExecutorService = Executors.newScheduledThreadPool(100, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("schedule-service");
                return t;
            }
        });
    }

    /**
     *
     * @param name
     * @param period millsceond
     * @param consumer
     */
    public void startTask(String name, int period, Consumer<Object> consumer){
        SchduleTask task = taskMap.computeIfAbsent(name,k->new SchduleTask(name,period));
        task.setConsumer(consumer);

        scheduledExecutorService.scheduleAtFixedRate(task,
                0,period, TimeUnit.MILLISECONDS);
    }
}
