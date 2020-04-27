package com.winsun.iot.iocmodule;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.winsun.iot.config.Config;
import com.winsun.iot.dao.CommonDao;
import com.winsun.iot.device.DeviceManager;
import com.winsun.iot.http.HttpIocModule;
import com.winsun.iot.logdata.LogDataService;
import com.winsun.iot.persistence.PersistenceBatchService;
import com.winsun.iot.persistence.PersistenceService;
import com.winsun.iot.persistence.redis.RedisService;
import com.winsun.iot.persistence.redis.RedisServiceImpl;
import com.winsun.iot.schedule.ScheduleService;
import com.winsun.iot.utils.FileUtils;

public class IotModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new HttpIocModule());
        bind(DeviceManager.class).toInstance(new DeviceManager());
    }

    @Provides
    @Singleton
    RedisService redisService(Config config) {
        RedisService service = new RedisServiceImpl(config.RedisUrl());
        return service;
    }

    @Provides
    @Singleton
    Config config() {
        return Config.load();
    }

    @Provides
    @Singleton
    ScheduleService scheduleService() {
        ScheduleService config = new ScheduleService();
        return config;
    }

    @Provides
    @Singleton
    PersistenceService persistenceService() {
        PersistenceService service = new PersistenceService();
        return service;
    }
    @Provides
    @Singleton
    PersistenceBatchService persistenceBatchService(CommonDao commonDao) {
        PersistenceBatchService service = new PersistenceBatchService(commonDao);
        return service;
    }

    @Provides
    @Singleton
    LogDataService logDataService(RedisService redisService){
        LogDataService dataService = new LogDataService(redisService);
        return dataService;
    }
}
