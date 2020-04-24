package com.winsun.facemask;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.winsun.facemask.service.FaceMaskService;
import com.winsun.facemask.service.impl.FaceMaskServiceImpl;
import com.winsun.iot.biz.service.BizService;
import com.winsun.iot.command.annotation.PluginIoc;
import com.winsun.iot.config.Config;
import com.winsun.iot.device.DeviceManager;
import com.winsun.iot.persistence.redis.RedisService;

@PluginIoc
public class FaceMaskIocModule extends AbstractModule {

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
