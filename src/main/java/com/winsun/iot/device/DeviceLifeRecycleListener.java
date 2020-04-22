package com.winsun.iot.device;

public interface DeviceLifeRecycleListener {

    void online(String deviceId);
    void offline(String deviceId);
    void processMissTask(String baseId);
}
