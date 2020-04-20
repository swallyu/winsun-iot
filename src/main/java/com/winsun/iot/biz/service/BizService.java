package com.winsun.iot.biz.service;

import com.winsun.iot.biz.domain.BizInfo;
import com.winsun.iot.domain.LogDeviceCtrl;
import com.winsun.iot.ruleengine.CmdRule;

public interface BizService {

    BizInfo getById(String bizId);

    void startBiz(String bizId, String baseId, String cmd, String cmdType, String msgType, int qos);

    LogDeviceCtrl getLogInfo(String bizId);

    void complete(String bizId, CmdRule cmdMsg);

    void updateResendBizInfo(String bizId, String cmd, String newBizId);
}
