package com.winsun.iot.biz.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.winsun.iot.biz.domain.SellInfo;
import com.winsun.iot.biz.service.BizService;
import com.winsun.iot.biz.service.FaceMaskService;
import com.winsun.iot.command.CmdCallback;
import com.winsun.iot.command.EnumQoS;
import com.winsun.iot.config.Config;
import com.winsun.iot.device.DeviceLifeRecycleListener;
import com.winsun.iot.device.DeviceManager;
import com.winsun.iot.domain.CmdResult;
import com.winsun.iot.persistence.redis.RedisService;
import com.winsun.iot.ruleengine.CmdRule;
import com.winsun.iot.utils.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class FaceMaskServiceImpl implements FaceMaskService, DeviceLifeRecycleListener {

    private static final Logger logger = LoggerFactory.getLogger(FaceMaskServiceImpl.class);
    private static final String cmdType = "control";
    private static final String topic = "/E2ES/GateWay/Control";

    private static final String REDIS_DEVICE_TOKEN = "deviceToken";

    private DeviceManager dm;

    private BizService bizService;

    private RedisService redisService;

    private Config config;

    public FaceMaskServiceImpl(DeviceManager dm, BizService bizService, RedisService redisService, Config config) {
        this.dm = dm;
        this.bizService = bizService;
        this.redisService = redisService;
        this.config = config;
        dm.addLifeRecycleListener("facemask", this);
    }

    @Override
    public CmdResult<String> sellFaceMak(SellInfo sellInfo) {
        JSONObject cmdObj = new JSONObject();
        cmdObj.put("msgType", "sell");
        cmdObj.put("location", 1);

        String currentTicket = redisService.hget(REDIS_DEVICE_TOKEN, sellInfo.getBaseId());

        if (!Objects.equals(currentTicket, sellInfo.getTicket())) {
            CmdResult<String> msg = new CmdResult<String>(2001, false, "无效的二维码");
            return msg;
        }

        CmdResult<String> result = dm.invokeCmd(topic, EnumQoS.ExtractOnce, cmdType, sellInfo.getBaseId(), cmdObj,
                new SellInnerCmdCallback(sellInfo), 0);

        bizService.startBiz(result.getData(), sellInfo.getBaseId(), JSON.toJSONString(sellInfo),
                cmdType,"sell", EnumQoS.ExtractOnce.getCode());

        return result;
    }

    @Override
    public CmdResult<String> updateQrCode(String bizMsg, String url) {
        String[] tmp = bizMsg.split(",");
        if (tmp.length < 2) {
            CmdResult<String> msg = new CmdResult<String>(-1, false, "请求数据错误");
            return msg;
        }

        String deviceId = tmp[0];
        String token = tmp[1];
        redisService.hset(REDIS_DEVICE_TOKEN, deviceId, token);

        JSONObject cmdObj = new JSONObject();
        cmdObj.put("msgType", "updateQRC");
        cmdObj.put("data", url);

        CmdResult<String> result = dm.invokeCmd(topic, EnumQoS.ExtractOnce, cmdType, deviceId, cmdObj,
                new UpdateQrCodeInnerCmdCallback(),10);
        bizService.startBiz(result.getData(), deviceId, cmdObj.toJSONString(),
                cmdType, "updateQRC", EnumQoS.ExtractOnce.getCode());

        return result;
    }

    @Override
    public void online(String deviceId) {
        String url = config.getBizServerReq("/online");
        JSONObject obj = new JSONObject();
        obj.put("baseId", deviceId);
        JSONObject retObj = HttpClientUtil.doPostJson(url, obj.toJSONString());
        logger.info("receive:\n{}", retObj);
        String qrCodeToken = retObj.getString("token");
        String qrCodeUrl = retObj.getString("url");
        updateQrCode(qrCodeToken, qrCodeUrl);
    }

    @Override
    public void offline(String deviceId) {

    }

    private class SellInnerCmdCallback implements CmdCallback {
        private SellInfo sellInfo;

        public SellInnerCmdCallback(SellInfo sellInfo) {
            this.sellInfo = sellInfo;
        }

        @Override
        public void complete(String bizId, CmdRule cmdMsg) {
            bizService.complete(bizId, cmdMsg);
            reportStatus(cmdMsg.isResult());

            updateQrCode(sellInfo.getQrCodeToken(), sellInfo.getQrCodeUrl());

            logger.info("update qrcode sell success {}", JSON.toJSONString(cmdMsg.getCmdMsg(), true));
        }

        private void reportStatus(boolean result) {
            String url = config.getBizServerReq("/status");
            JSONObject obj = new JSONObject();
            obj.put("token", sellInfo.getRawToken());
            obj.put("result", result ? 1 : 0);
            String retObj = HttpClientUtil.doPost(url, obj.toJSONString());
            logger.info("report status \n{}", retObj);

        }
    }


    private class UpdateQrCodeInnerCmdCallback implements CmdCallback {

        @Override
        public void complete(String bizId, CmdRule cmdMsg) {
            bizService.complete(bizId, cmdMsg);
            logger.info("update qrcode success {}",
                    JSON.toJSONString(cmdMsg.getCmdMsg(), true));
        }
    }
}
