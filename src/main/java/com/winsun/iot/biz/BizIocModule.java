package com.winsun.iot.biz;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.winsun.iot.biz.service.BizService;
import com.winsun.iot.biz.service.impl.BizServiceImpl;

public class BizIocModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(BizService.class).to(BizServiceImpl.class).in(Singleton.class);
    }
}
