package com.winsun.iot.dao;

import com.google.common.base.Stopwatch;
import com.winsun.facemask.service.FaceMaskService;
import com.winsun.iot.domain.LogDeviceCtrl;
import com.winsun.iot.http.HttpServer;
import com.winsun.iot.iocmodule.Ioc;
import com.winsun.iot.persistence.PersistenceBatchService;
import com.winsun.iot.persistence.PersistenceService;
import com.winsun.iot.persistence.redis.RedisService;
import com.winsun.iot.utils.DateTimeUtils;
import org.apache.commons.beanutils.BeanUtils;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RedisTest {

    @Test
    public void testSet() throws Exception {

        long duration = TimeUnit.MINUTES.toSeconds(30);
//        Ioc.getInjector().getInstance(PersistenceService.class).start();
//        Ioc.getInjector().getInstance(PersistenceBatchService.class).start();
//        Ioc.getInjector().getInstance(FaceMaskService.class);

        RedisService redisService = Ioc.getInjector().getInstance(RedisService.class);

        for (; ; ) {
            boolean ret = redisService.isConnect();
            if (ret) {
                break;
            }
            Thread.sleep(100);
        }

        String key = "testqueue";

        Stopwatch sw = Stopwatch.createStarted();
        for (int i = 0; i < 100000; i++) {
            redisService.lpush(key, System.currentTimeMillis() + "");
        }

        System.out.println(sw.elapsed(TimeUnit.MILLISECONDS));
        sw.reset();
        sw.start();
        int times = 0;
        while (true){
            List<String> value = redisService.lpopRange(key, 1000);
            times++;
            if(value.size()==0){
                break;
            }
            System.out.println("times:+"+times+"\t"+value.size());
        }

        System.out.println(sw.elapsed(TimeUnit.MILLISECONDS));

//        String[] data ="0123456789".split("");
//        for (String datum : data) {
//            redisService.lpush(key,datum);
//        }
//
//        List<String> value = redisService.lpopRange(key,5);
//        System.out.println("get data=====");
//        for (String s : value) {
//            System.out.println(s);
//        }
//
//        value = redisService.lgetAll(key);
//        System.out.println("get data all=====");
//        for (String s : value) {
//            System.out.println(s);
//        }
//
////        redisService.set("aa","bb");
//        LogDeviceCtrl entity = new LogDeviceCtrl();
//        entity.setCmdMsg("ssss");
////        entity.setUpdateTime(LocalDateTime.now());
//        Map<String,String> properties =  BeanUtils.describe(entity);
//
//        LocalDateTime time = DateTimeUtils.parseDefaultTime(properties.get("updateTime"));
//
//        System.out.println("ss");
    }
}
