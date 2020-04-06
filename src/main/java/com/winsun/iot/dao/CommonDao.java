package com.winsun.iot.dao;

import com.winsun.iot.domain.DeviceList;
import com.winsun.iot.domain.LogDeviceEvents;

import java.util.List;

public interface CommonDao {

    int addEvent(LogDeviceEvents record);

    List<DeviceList> listDevices();
}