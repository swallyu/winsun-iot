package com.winsun.iot.iocmodule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.winsun.iot.biz.BizIocModule;
import com.winsun.iot.device.DeviceIocModule;

public class Ioc {

    private static Injector injector;

    static {
        injector = Guice.createInjector(
                new MybatisXmlModule(),
                new IotModule(),
                new DeviceIocModule(),
                new BizIocModule());
    }

    public static Injector getInjector() {
        return injector;
    }
}
