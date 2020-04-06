package com.winsun.iot.device;

import com.google.inject.Inject;
import com.winsun.iot.command.CommandHandler;
import com.winsun.iot.command.EnumQoS;
import com.winsun.iot.device.handler.EventHandler;
import com.winsun.iot.iocmodule.Ioc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceManager {

    private static final Logger logger = LoggerFactory.getLogger(DeviceManager.class);

    @Inject
    private DeviceConnManager connManager;

    public void start(){

        connManager.addCommand(new CommandHandler(EventHandler.TOPIC, EnumQoS.valueOf(EventHandler.QOS),
                Ioc.getInjector().getInstance(EventHandler.class)));
        connManager.start();
//        List<DeviceList> deviceLists = deviceDao.listAll();
//        for (DeviceList deviceList : deviceLists) {
//            System.out.println(deviceList.getId());
//        }
//        logger.info("device size {}",deviceLists.size());
    }
}
