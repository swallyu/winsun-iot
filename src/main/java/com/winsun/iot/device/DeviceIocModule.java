package com.winsun.iot.device;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.winsun.iot.config.Config;
import com.winsun.iot.dao.CommonDao;
import com.winsun.iot.device.handler.ConnectHandler;
import com.winsun.iot.device.handler.EventHandler;
import com.winsun.iot.device.handler.SensorHandler;
import com.winsun.iot.persistence.PersistenceService;
import com.winsun.iot.utils.FileUtils;

public class DeviceIocModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(EventHandler.class).toInstance(new EventHandler());
        bind(ConnectHandler.class).toInstance(new ConnectHandler());
        bind(SensorHandler.class).toInstance(new SensorHandler());
    }

    @Provides
    @Singleton
    DeviceConnManager connManager(Config config ) {
        DeviceConnManager connManager = new DeviceConnManager(config);
        return connManager;
    }
}
