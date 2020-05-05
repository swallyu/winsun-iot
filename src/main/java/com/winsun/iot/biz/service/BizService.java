package com.winsun.iot.biz.service;

import com.alibaba.fastjson.JSONObject;
import com.winsun.iot.biz.domain.BizInfo;
import com.winsun.iot.domain.LogDeviceCtrl;
import com.winsun.iot.ruleengine.CmdRule;

public interface BizService {

    BizInfo getById(String bizId);

    void startBiz(String bizId,long logId, String baseId, String cmd, String cmdType, String msgType, int qos);

    LogDeviceCtrl getLogInfo(String bizId);

    void complete(String bizId, CmdRule cmdMsg);

    void updateResendBizInfo(String bizId, String cmd, String newBizId);

    void processMissTask(String deviceId, String bizId, String topic, JSONObject data);

    void registerHandler(String name, ProcessService service);

}
