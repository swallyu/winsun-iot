package com.winsun.iot.schedule;

import java.util.function.Consumer;

public class SchduleTask implements Runnable {

    private String name;
    private int period;
    private Consumer<Object> consumer;

    public SchduleTask(String name, int period) {
        this.name = name;
        this.period = period;
    }

    public void setConsumer(Consumer<Object> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void run() {
        if(consumer!=null){
            consumer.accept(null);
        }
    }
}
