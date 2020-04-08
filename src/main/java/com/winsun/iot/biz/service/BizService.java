package com.winsun.iot.biz.service;

import com.winsun.iot.biz.domain.BizInfo;
import com.winsun.iot.ruleengine.CmdRule;

public interface BizService {

    BizInfo getById(String bizId);

    void startBiz(String bizId);

    void complete(String bizId, CmdRule cmdMsg);
}
