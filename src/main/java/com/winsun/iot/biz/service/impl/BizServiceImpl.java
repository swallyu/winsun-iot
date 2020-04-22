package com.winsun.iot.biz.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.winsun.iot.biz.domain.BizInfo;
import com.winsun.iot.biz.service.BizService;
import com.winsun.iot.dao.LogDeviceCtrlMapper;
import com.winsun.iot.device.DeviceLifeRecycleListener;
import com.winsun.iot.device.DeviceManager;
import com.winsun.iot.domain.LogDeviceCtrl;
import com.winsun.iot.ruleengine.CmdRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BizServiceImpl implements BizService {
    private static final Logger logger = LoggerFactory.getLogger(BizServiceImpl.class);
    @Inject
    private LogDeviceCtrlMapper logDeviceCtrlMapper;

    private Map<String, BizInfo> bizInfoMap = new HashMap<>();

    @Inject
    private DeviceManager deviceManager;

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

        bizInfoMap.put(bizId, new BizInfo(bizId));
    }

    public void updateResendBizInfo(String bizId, String cmd, String newBizId) {
        LogDeviceCtrl entity = logDeviceCtrlMapper.selectByBizId(bizId);
        if (entity != null) {
            entity.setSig(newBizId);
            entity.setCmdMsg(cmd);
            entity.setRetryTimes(entity.getRetryTimes() + 1);
            bizInfoMap.remove(bizId);
            bizInfoMap.put(newBizId, new BizInfo(bizId));
            this.logDeviceCtrlMapper.updateByPrimaryKey(entity);
        }
    }

    @Override
    public void precessMissTask(String bizId, String topic, JSONObject data) {
        LogDeviceCtrl ctrl = getLogInfo(bizId);
        if(Objects.equals(ctrl.getMsgType(),"sell")){
            //设备上执行指令重试，重复resp，实际上之前已经处理，则需要使用最新的二维码更新。
            logger.info("device rectrl {}",bizId);
//            deviceManager.
        }
    }

    @Override
    public LogDeviceCtrl getLogInfo(String bizId) {
        LogDeviceCtrl entity = logDeviceCtrlMapper.selectByBizId(bizId);
        return entity;
    }

    @Override
    public void complete(String bizId, CmdRule cmdMsg) {
        BizInfo info = bizInfoMap.get(bizId);
        if (info == null) {
            return;
        }
        this.logDeviceCtrlMapper.updateStatus(bizId, true, cmdMsg.isResult(), LocalDateTime.now());
        info.setFinish(true, cmdMsg.isResult());
        bizInfoMap.remove(bizId);
    }
}
