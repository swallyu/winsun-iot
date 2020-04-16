package com.winsun.iot.biz;

import com.google.inject.AbstractModule;
import com.google.inject.Exposed;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.winsun.iot.biz.service.BizService;
import com.winsun.iot.biz.service.FaceMaskService;
import com.winsun.iot.biz.service.impl.BizServiceImpl;
import com.winsun.iot.biz.service.impl.FaceMaskServiceImpl;
import com.winsun.iot.config.Config;
import com.winsun.iot.device.DeviceManager;
import com.winsun.iot.persistence.redis.RedisService;

public class BizIocModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(BizService.class).to(BizServiceImpl.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    public FaceMaskService faceMaskService(DeviceManager dm,
                                           BizService bizService,
                                           RedisService redisService,
                                           Config config){
        FaceMaskService faceMaskService = new FaceMaskServiceImpl(dm, bizService, redisService, config);
        return faceMaskService;
    }
}
