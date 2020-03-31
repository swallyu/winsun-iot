package com.winsun.iot.device;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.winsun.iot.config.Config;
import com.winsun.iot.utils.FileUtils;

public class DeviceIocModule extends AbstractModule {
    @Override
    protected void configure() {
    }

    @Provides
    @Singleton
    DeviceConnManager connManager(Config config ) {
        DeviceConnManager connManager = new DeviceConnManager(config);
        connManager.init();
        return connManager;
    }
}
