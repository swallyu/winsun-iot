package com.winsun.iot.iocmodule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.winsun.iot.device.DeviceIocModule;

public class Ioc {

    private static Injector injector;

    static {
        injector = Guice.createInjector(
                new MybatisXmlModule(),
                new IotModule(),
                new DeviceIocModule());
    }

    public static Injector getInjector() {
        return injector;
    }
}
