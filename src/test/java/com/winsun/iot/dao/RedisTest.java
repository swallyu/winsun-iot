package com.winsun.iot.dao;

import com.winsun.iot.iocmodule.Ioc;
import com.winsun.iot.persistence.redis.RedisService;
import org.junit.Test;

public class RedisTest {

    @Test
    public void testSet() throws Exception {
        RedisService redisService = Ioc.getInjector().getInstance(RedisService.class);

        redisService.set("aa","bb");

        for (;;){
            Thread.sleep(10*1000);
        }
    }
}
