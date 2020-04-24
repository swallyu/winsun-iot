package com.winsun.iot.biz;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.winsun.iot.biz.service.BizService;
import com.winsun.facemask.service.FaceMaskService;
import com.winsun.iot.biz.service.impl.BizServiceImpl;
import com.winsun.facemask.service.impl.FaceMaskServiceImpl;
import com.winsun.iot.config.Config;
import com.winsun.iot.device.DeviceManager;
import com.winsun.iot.persistence.redis.RedisService;

public class BizIocModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(BizService.class).to(BizServiceImpl.class).in(Singleton.class);
    }

}
