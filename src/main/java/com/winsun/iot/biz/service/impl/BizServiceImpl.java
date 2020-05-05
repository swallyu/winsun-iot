package com.winsun.iot.biz.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.winsun.iot.biz.domain.BizInfo;
import com.winsun.iot.biz.service.BizService;
import com.winsun.iot.biz.service.ProcessService;
import com.winsun.iot.dao.LogDeviceCtrlMapper;
import com.winsun.iot.device.DeviceManager;
import com.winsun.iot.domain.LogDeviceCtrl;
import com.winsun.iot.persistence.redis.RedisService;
import com.winsun.iot.ruleengine.CmdRule;
import com.winsun.iot.utils.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class BizServiceImpl implements BizService {
    private static final Logger logger = LoggerFactory.getLogger(BizServiceImpl.class);
    @Inject
    private LogDeviceCtrlMapper logDeviceCtrlMapper;

    private Map<String, BizInfo> bizInfoMap = new HashMap<>();

    @Inject
    private DeviceManager deviceManager;

    @Inject
    private RedisService redisService;

    private static final String REDIS_BIZ_CACHE_KEY = "bizInfo_";

    private Map<String, ProcessService> processServiceMap = new HashMap<>();

    @Override
    public BizInfo getById(String bizId) {
        BizInfo bizInfo = bizInfoMap.get(bizId);
        if (bizInfo == null) {
            return getBizInfo(bizId);
        }
        return bizInfoMap.get(bizId);
    }

    @Override
    public void startBiz(String bizId, long logId, String baseId, String cmd, String cmdType, String msgType, int qos) {
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
        entity.setLogId(logId);
        this.logDeviceCtrlMapper.insert(entity);

        bizInfoMap.put(bizId, new BizInfo(bizId,logId));

        redisService.hset(REDIS_BIZ_CACHE_KEY + bizId, entity, 30, TimeUnit.MINUTES);
    }

    public void updateResendBizInfo(String bizId, String cmd, String newBizId) {
        LogDeviceCtrl entity = logDeviceCtrlMapper.selectByBizId(bizId);
        if (entity != null) {
            entity.setSig(newBizId);
            entity.setCmdMsg(cmd);
            entity.setRetryTimes(entity.getRetryTimes() + 1);
            bizInfoMap.remove(bizId);
            bizInfoMap.put(newBizId, new BizInfo(bizId,entity.getLogId()));
            this.logDeviceCtrlMapper.updateByPrimaryKey(entity);

            redisService.hset(REDIS_BIZ_CACHE_KEY + bizId, entity, 30, TimeUnit.MINUTES);
        }
    }

    @Override
    public void processMissTask(String deviceId, String bizId, String topic, JSONObject data) {
        LogDeviceCtrl ctrl = getLogInfo(bizId);
        //设备上执行指令重试，重复resp，实际上之前已经处理，则需要使用最新的二维码更新。

        for (ProcessService value : processServiceMap.values()) {
            value.processMissTask(deviceId, bizId, topic, data);
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

        Map<String, String> updateInfo = new HashMap<>();
        updateInfo.put("complete", "true");
        updateInfo.put("result", cmdMsg.isResult() + "");
        updateInfo.put("update_time", cmdMsg.isResult() + "");
        redisService.hset(REDIS_BIZ_CACHE_KEY + bizId, updateInfo, 30, TimeUnit.MINUTES);
    }

    private BizInfo getBizInfo(String bizId) {

        Map<String, String> value = redisService.hgetAll(REDIS_BIZ_CACHE_KEY + bizId);
        if (value.size() == 0) {
            LogDeviceCtrl ctrl = logDeviceCtrlMapper.selectByBizId(bizId);
            if (ctrl != null) {
                redisService.hset(REDIS_BIZ_CACHE_KEY + bizId, ctrl, 30, TimeUnit.MINUTES);
            }else{
                //如果缓存命中失败，缓存5分钟，
                redisService.hset(REDIS_BIZ_CACHE_KEY+bizId,"hasvalue","false",5,TimeUnit.MINUTES);
            }
        }
        value = redisService.hgetAll(REDIS_BIZ_CACHE_KEY + bizId);
        if (value.size() > 1) {
            LocalDateTime startTime = DateTimeUtils.parseDefaultTime(value.get("createTime"));
            LocalDateTime finishTime = DateTimeUtils.parseDefaultTime(value.get("updateTime"));
            long logId = Long.valueOf(value.get("logId"));
            BizInfo info = new BizInfo(bizId, logId);

            info.setFinish(Boolean.valueOf(value.get("complete")), Boolean.valueOf(value.get("result")));
            if (info.isFinish() && finishTime != null) {
                info.setFinishTime(finishTime);
            }
            return info;
        }
        return null;
    }

    @Override
    public void registerHandler(String name, ProcessService service) {
        this.processServiceMap.put(name, service);
    }
}
