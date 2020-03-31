package com.winsun.iot.device;

import com.google.inject.Inject;
import com.winsun.iot.dao.DeviceDao;
import com.winsun.iot.domain.DeviceList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DeviceManager {

    private static final Logger logger = LoggerFactory.getLogger(DeviceManager.class);
    @Inject
    private DeviceDao deviceDao;
    @Inject
    private DeviceConnManager connManager;

    public void start(){
        connManager.start();
        List<DeviceList> deviceLists = deviceDao.listAll();
        for (DeviceList deviceList : deviceLists) {
            System.out.println(deviceList.getId());
        }
        logger.info("device size {}",deviceLists.size());
    }
}
