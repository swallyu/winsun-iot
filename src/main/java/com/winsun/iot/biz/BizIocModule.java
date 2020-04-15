package com.winsun.iot.biz;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.winsun.iot.biz.service.BizService;
import com.winsun.iot.biz.service.FaceMaskService;
import com.winsun.iot.biz.service.impl.BizServiceImpl;
import com.winsun.iot.biz.service.impl.FaceMaskServiceImpl;

public class BizIocModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(BizService.class).to(BizServiceImpl.class).in(Singleton.class);
        bind(FaceMaskService.class).to(FaceMaskServiceImpl.class).in(Singleton.class);
    }
}
