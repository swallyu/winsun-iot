package com.winsun.iot.dao;

import com.winsun.iot.domain.LogDeviceEvents;
import com.winsun.iot.iocmodule.Ioc;

import java.time.LocalDateTime;

import static org.junit.Assert.*;

public class CommonDaoTest {

    @org.junit.Test
    public void addEvent() {

        CommonDao dao = Ioc.getInjector().getInstance(CommonDao.class);
        LogDeviceEvents events = new LogDeviceEvents();
        events.setBaseId("123456");
        events.setEventName("test");
        events.setTime(LocalDateTime.now());
        events.setValue("111");
        int ret = dao.addEvent(events);

        System.out.println("ret="+ret+"\t"+events.getId());
    }
}