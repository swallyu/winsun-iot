package com.winsun.iot.biz.service;

import com.winsun.iot.biz.domain.BizInfo;
import com.winsun.iot.ruleengine.CmdRule;

public interface BizService {

    BizInfo getById(String bizId);

    void startBiz(String bizId, String baseId, String cmd, String cmdType, String msgType, int qos);

    void complete(String bizId, CmdRule cmdMsg);
}
