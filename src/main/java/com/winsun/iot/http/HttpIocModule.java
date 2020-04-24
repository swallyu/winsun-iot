package com.winsun.iot.http;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.winsun.iot.config.Config;
import com.winsun.iot.http.common.HttpMap;
import com.winsun.iot.http.common.HttpController;
import com.winsun.iot.http.common.HttpHandlerFactory;
import com.winsun.iot.utils.ClassScanner;
import com.winsun.iot.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HttpIocModule extends AbstractModule {

    private static final Logger logger = LoggerFactory.getLogger(HttpIocModule.class);

    @Override
    protected void configure() {
        Multibinder<HttpController> multibinder =
                Multibinder.newSetBinder(binder(), HttpController.class);

        initInstance(multibinder);
    }

    @Provides
    HttpHandlerFactory handlerFactory(Set<HttpController> controllerSet) {
        HttpHandlerFactory factory = new HttpHandlerFactory(controllerSet);
        return factory;
    }

    private void initInstance(Multibinder<HttpController> multibinder) {
        try {
            Config config = Config.load();

            String[] pkgNames = config.getHttpHandlerPkg();
            Set<String> clzNameSet = new HashSet<>();
            for (String pkgName : pkgNames) {
                ClassScanner scanner = new ClassScanner(pkgName);
                List<String> nameList = new ArrayList<>();
                scanner.doScan(nameList);
                clzNameSet.addAll(nameList);
            }

            for (String clzName : clzNameSet) {
                Class<?> clz = Class.forName(clzName);
                HttpMap handler = clz.getAnnotation(HttpMap.class);
                if (handler != null) {
                    Object handlerInst = clz.getDeclaredConstructor().newInstance();

                    if (handlerInst instanceof HttpController) {
                        multibinder.addBinding().to((Class<? extends HttpController>) clz).in(Singleton.class);
                    }
                }
            }
        } catch (Exception exc) {
            logger.error(exc.getMessage(), exc);
        }
    }
}
