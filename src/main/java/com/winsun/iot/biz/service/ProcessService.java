package com.winsun.iot.biz.service;

import com.alibaba.fastjson.JSONObject;

public interface ProcessService {
    void processMissTask(String deviceId, String bizId, String topic, JSONObject data);
}
