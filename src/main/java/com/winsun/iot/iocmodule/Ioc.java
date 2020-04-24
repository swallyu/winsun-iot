package com.winsun.iot.iocmodule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.winsun.iot.biz.BizIocModule;
import com.winsun.iot.command.annotation.PluginIoc;
import com.winsun.iot.config.Config;
import com.winsun.iot.device.DeviceIocModule;
import com.winsun.iot.http.common.HttpController;
import com.winsun.iot.http.common.HttpMap;
import com.winsun.iot.utils.ClassScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Ioc {
    private static final Logger logger = LoggerFactory.getLogger(Ioc.class);

    private static Injector injector;

    static {
        List<Module> modules = new ArrayList<>();
        modules.add(new MybatisXmlModule());
        modules.add(new IotModule());
        modules.add(new DeviceIocModule());
        modules.add(new BizIocModule());

        List<Module> plugins = searchPluginModule();
        modules.addAll(plugins);
        injector = Guice.createInjector(modules);
    }

    public static Injector getInjector() {
        return injector;
    }

    private static List<Module> searchPluginModule() {
        List<Module> modules = new ArrayList<>();
        try {

            Config config = Config.load();

            String[] pkgNames = config.getPluginPkg();
            Set<String> clzNameSet = new HashSet<>();
            for (String pkgName : pkgNames) {
                ClassScanner scanner = new ClassScanner(pkgName);
                List<String> nameList = new ArrayList<>();
                scanner.doScan(nameList);
                clzNameSet.addAll(nameList);
            }

            for (String clzName : clzNameSet) {
                Class<?> clz = Class.forName(clzName);
                PluginIoc module = clz.getAnnotation(PluginIoc.class);
                if (module != null) {
                    Object handlerInst = clz.getDeclaredConstructor().newInstance();
                    if (handlerInst instanceof Module) {
                        modules.add((Module) handlerInst);
                    }
                }
            }
        } catch (Exception exc) {
            logger.error(exc.getMessage(), exc);
        }
        return modules;
    }
}
