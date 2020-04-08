package com.winsun.iot.biz.service.impl;

import com.winsun.iot.biz.domain.BizInfo;
import com.winsun.iot.biz.service.BizService;
import com.winsun.iot.ruleengine.CmdRule;

import java.util.HashMap;
import java.util.Map;

public class BizServiceImpl implements BizService {

    private Map<String,BizInfo> bizInfoMap = new HashMap<>();
    @Override
    public BizInfo getById(String bizId) {
        return bizInfoMap.get(bizId);
    }

    @Override
    public void startBiz(String bizId) {
        bizInfoMap.put(bizId,new BizInfo(bizId));
    }

    @Override
    public void complete(String bizId, CmdRule cmdMsg) {
        BizInfo info = bizInfoMap.get(bizId);
        if(info==null){
            return;
        }
        info.setFinish(true,cmdMsg.isResult());
    }
}
