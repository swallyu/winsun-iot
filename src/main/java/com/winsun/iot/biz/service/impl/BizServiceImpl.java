package com.winsun.iot.biz.service.impl;

import com.winsun.iot.biz.domain.BizInfo;
import com.winsun.iot.biz.service.BizService;
import com.winsun.iot.dao.LogDeviceCtrlMapper;
import com.winsun.iot.domain.LogDeviceCtrl;
import com.winsun.iot.ruleengine.CmdRule;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class BizServiceImpl implements BizService {

    @Inject
    private LogDeviceCtrlMapper logDeviceCtrlMapper;

    private Map<String,BizInfo> bizInfoMap = new HashMap<>();
    @Override
    public BizInfo getById(String bizId) {

        return bizInfoMap.get(bizId);
    }

    @Override
    public void startBiz(String bizId, String baseId, String cmd, String cmdType, String msgType, int qos) {
        LogDeviceCtrl entity = new LogDeviceCtrl();
        entity.setBaseId(baseId);
        entity.setCmdMsg(cmd);
        entity.setCmdType(cmdType);
        entity.setMsgType(msgType);
        entity.setComplete(false);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        entity.setExecStep(0);
        entity.setQos(qos);
        entity.setResult(false);
        entity.setSig(bizId);
        this.logDeviceCtrlMapper.insert(entity);

        bizInfoMap.put(bizId,new BizInfo(bizId));
    }

    @Override
    public void complete(String bizId, CmdRule cmdMsg) {
        BizInfo info = bizInfoMap.get(bizId);
        if(info==null){
            return;
        }
        this.logDeviceCtrlMapper.updateStatus(bizId,true,cmdMsg.isResult(),LocalDateTime.now());
        info.setFinish(true,cmdMsg.isResult());
    }
}
