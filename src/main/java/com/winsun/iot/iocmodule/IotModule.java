package com.winsun.iot.iocmodule;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.winsun.iot.config.Config;
import com.winsun.iot.device.DeviceManager;
import com.winsun.iot.http.HttpIocModule;
import com.winsun.iot.http.handler.HttpController;
import com.winsun.iot.http.handler.HttpHandlerFactory;
import com.winsun.iot.persistence.PersistenceBatchService;
import com.winsun.iot.persistence.PersistenceService;
import com.winsun.iot.schedule.ScheduleService;
import com.winsun.iot.utils.FileUtils;

import java.util.Set;

public class IotModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new HttpIocModule());
        bind(DeviceManager.class).toInstance(new DeviceManager());
    }

    @Provides
    @Singleton
    Config config() {
        Config config = new Config();
        config.load(FileUtils.readContent("application.properties"));
        return config;
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
    PersistenceBatchService persistenceBatchService() {
        PersistenceBatchService service = new PersistenceBatchService();
        return service;
    }
}
