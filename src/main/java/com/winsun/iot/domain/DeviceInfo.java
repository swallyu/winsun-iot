package com.winsun.iot.domain;

import com.alibaba.fastjson.JSONObject;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DeviceInfo {

    private SysDevices device;

    private DeviceSensorData sensorData=new DeviceSensorData(1);

    private ConcurrentHashMap<String,String> controlp = new ConcurrentHashMap<String,String>();			//设备目标控制参数，控制参数主要是on/off
    private ConcurrentHashMap<String,String> controlrealityp = new ConcurrentHashMap<String,String>();			//设备实际控制参数，控制参数主要是on/off
    private Set<String> paireddevices = new HashSet<>();

    private boolean online;

    public DeviceInfo(SysDevices device) {
        this.device = device;
    }

    public SysDevices getDevice() {
        return device;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public String getBaseId() {
        return this.device.getBaseId();
    }

    public void login() {
        this.setOnline(true);
    }

    public boolean updateStsSensorDataAndPull2DB(JSONObject stssensordata) {
        return sensorData.updateStsDataAndPull2DB(LocalDateTime.now(), getBaseId(), stssensordata);
    }

    public boolean updateRealitySensorDataAndPull2DB(JSONObject stssensordata) {
        return sensorData.updateDataAndPull2DB(LocalDateTime.now(),getBaseId(),stssensordata);
    }

    public boolean updateControlRealityP(String parameter, String val) {
        if(!controlp.containsKey(parameter)){
            return false;
        }
        controlrealityp.put(parameter,val);
        return true;
    }

    public Set<String> getPaireddevices() {
        return paireddevices;
    }

    public void setDevice(SysDevices devices) {
        this.device = devices;
    }
}
