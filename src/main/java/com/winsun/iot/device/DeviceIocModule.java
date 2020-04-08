package com.winsun.iot.device;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.winsun.iot.command.biz.BizCmdHandler;
import com.winsun.iot.config.Config;
import com.winsun.iot.device.handler.ConnectHandler;
import com.winsun.iot.device.handler.EventHandler;
import com.winsun.iot.device.handler.ResponseHandler;
import com.winsun.iot.device.handler.SensorHandler;

public class DeviceIocModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(EventHandler.class).toInstance(new EventHandler());
        bind(ConnectHandler.class).toInstance(new ConnectHandler());
        bind(SensorHandler.class).toInstance(new SensorHandler());
        bind(BizCmdHandler.class).toInstance(new BizCmdHandler());
        bind(ResponseHandler.class).toInstance(new ResponseHandler());
    }

    @Provides
    @Singleton
    DeviceConnManager connManager(Config config ,BizCmdHandler handler) {
        DeviceConnManager connManager = new DeviceConnManager(config,handler);
        return connManager;
    }
}
