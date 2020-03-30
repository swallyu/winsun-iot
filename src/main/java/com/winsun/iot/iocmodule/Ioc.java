package com.winsun.iot.iocmodule;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class Ioc {

    private static Injector injector;

    static {
        injector = Guice.createInjector(
                new MybatisXmlModule(),
                new IotModule());
    }

    public static Injector getInjector() {
        return injector;
    }
}
