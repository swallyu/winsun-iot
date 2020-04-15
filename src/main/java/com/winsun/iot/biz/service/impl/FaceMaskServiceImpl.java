package com.winsun.iot.biz.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.winsun.iot.biz.service.BizService;
import com.winsun.iot.biz.service.FaceMaskService;
import com.winsun.iot.command.CmdCallback;
import com.winsun.iot.command.EnumQoS;
import com.winsun.iot.device.DeviceManager;
import com.winsun.iot.domain.CmdResult;
import com.winsun.iot.persistence.redis.RedisService;
import com.winsun.iot.ruleengine.CmdRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class FaceMaskServiceImpl implements FaceMaskService {
    private static final Logger logger = LoggerFactory.getLogger(FaceMaskServiceImpl.class);
    private static final String msgType = "control";
    private static final String topic = "/E2ES/GateWay/Control";

    private static final String REDIS_DEVICE_TOKEN="deviceToken";
    @Inject
    private DeviceManager dm;

    @Inject
    private BizService bizService;

    @Inject
    private RedisService redisService;

    @Override
    public CmdResult<String> sellFaceMak(String baseId, String ticket) {
        JSONObject cmdObj = new JSONObject();
        cmdObj.put("msgType", "sell");
        cmdObj.put("location", 1);

        String currentTicket = redisService.hget(REDIS_DEVICE_TOKEN,baseId);

        if(!Objects.equals(currentTicket,ticket)){
            CmdResult<String> msg=new CmdResult<String>(2001,false,"无效的二维码");
            return msg;
        }
        JSONObject msg = new JSONObject();
        msg.put("deviceId",baseId);
        msg.put("ticket",ticket);

        CmdResult<String> result = dm.invokeCmd(topic, EnumQoS.ExtractOnce, msgType, baseId, cmdObj,
                new SellInnerCmdCallback());
        bizService.startBiz(result.getData(), baseId, msg.toJSONString(),
                msgType, EnumQoS.AtleastOnce.getCode());

        return result;
    }

    @Override
    public CmdResult<String> updateQrCode(String bizMsg, String url) {
        String[] tmp = bizMsg.split(",");
        if(tmp.length<2){
            CmdResult<String> msg=new CmdResult<String>(-1,false,"请求数据错误");
            return msg;
        }

        String deviceId = tmp[0];
        String token = tmp[1];
        redisService.hset(REDIS_DEVICE_TOKEN,deviceId,token);

        JSONObject cmdObj = new JSONObject();
        cmdObj.put("msgType", "updateQRC");
        cmdObj.put("data", url);


        CmdResult<String> result = dm.invokeCmd(topic, EnumQoS.ExtractOnce, msgType, deviceId, cmdObj,
                new UpdateQrCodeInnerCmdCallback());
        bizService.startBiz(result.getData(), deviceId, cmdObj.toJSONString(),
                msgType, EnumQoS.AtleastOnce.getCode());

        return result;
    }

    private class SellInnerCmdCallback implements CmdCallback {

        @Override
        public void complete(String bizId, CmdRule cmdMsg) {
            bizService.complete(bizId, cmdMsg);
            logger.info("update qrcode sell success {}", JSON.toJSONString(cmdMsg.getCmdMsg(),true));
        }
    }

    private class UpdateQrCodeInnerCmdCallback implements CmdCallback {

        @Override
        public void complete(String bizId, CmdRule cmdMsg) {
            bizService.complete(bizId, cmdMsg);
            logger.info("update qrcode success {}",
                    JSON.toJSONString(cmdMsg.getCmdMsg(),true));
        }
    }
}
