package com.winsun.iot.iocmodule;

import com.google.inject.AbstractModule;
import com.winsun.iot.device.DeviceManager;
import com.winsun.iot.http.HttpIocModule;

public class IotModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new HttpIocModule());
        bind(DeviceManager.class).toInstance(new DeviceManager());
    }
}
