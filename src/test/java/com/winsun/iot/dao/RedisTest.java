package com.winsun.iot.dao;

import com.winsun.iot.iocmodule.Ioc;
import com.winsun.iot.persistence.redis.RedisService;
import org.junit.Test;

public class RedisTest {

    @Test
    public void testSet() throws Exception {
        RedisService redisService = Ioc.getInjector().getInstance(RedisService.class);

        for (;;){
           boolean ret =  redisService.isConnect();
           if(ret){
               break;
           }
           Thread.sleep(2000);
        }

        redisService.set("aa","bb");

        for (;;){
            Thread.sleep(10*1000);
        }
    }
}
