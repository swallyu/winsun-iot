package com.winsun.iot.schedule;

import com.winsun.iot.utils.functions.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class SchduleTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(SchduleTask.class);

    private String name;
    private int period;

    private Function consumer;

    public SchduleTask(String name, int period) {
        this.name = name;
        this.period = period;
    }

    public void setConsumer(Function consumer) {
        this.consumer = consumer;
    }

    @Override
    public void run() {
        try{
            if(consumer!=null){
                consumer.execute();
            }
        }catch (Exception exc){
            logger.error(exc.getMessage(),exc);
        }
    }
}
