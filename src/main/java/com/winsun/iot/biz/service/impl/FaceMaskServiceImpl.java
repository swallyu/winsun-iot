package com.winsun.iot.biz.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.winsun.iot.biz.domain.BizInfo;
import com.winsun.iot.biz.domain.SellInfo;
import com.winsun.iot.biz.service.BizService;
import com.winsun.iot.biz.service.FaceMaskService;
import com.winsun.iot.command.CmdCallback;
import com.winsun.iot.command.EnumQoS;
import com.winsun.iot.config.Config;
import com.winsun.iot.device.DeviceLifeRecycleListener;
import com.winsun.iot.device.DeviceManager;
import com.winsun.iot.domain.CmdResult;
import com.winsun.iot.domain.LogDeviceCtrl;
import com.winsun.iot.persistence.redis.RedisService;
import com.winsun.iot.ruleengine.CmdRule;
import com.winsun.iot.utils.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class FaceMaskServiceImpl implements FaceMaskService, DeviceLifeRecycleListener {

    private static final Logger logger = LoggerFactory.getLogger(FaceMaskServiceImpl.class);
    private static final String cmdType = "control";
    private static final String topic = "/E2ES/GateWay/Control";

    private static final String REDIS_DEVICE_TOKEN = "deviceToken";

    private DeviceManager dm;

    private BizService bizService;

    private RedisService redisService;

    private Config config;

    private ScheduledExecutorService scheduledExecutorService;

    //deviceid->qrcode
    private Map<String, QrCodeInfo> qrCodeInfoMap = new HashMap<>();

    public FaceMaskServiceImpl(DeviceManager dm, BizService bizService, RedisService redisService, Config config) {
        this.dm = dm;
        this.bizService = bizService;
        this.redisService = redisService;
        this.config = config;
        dm.addLifeRecycleListener("facemask", this);
        this.scheduledExecutorService = Executors.newScheduledThreadPool(2, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("resend-qrcode");
                return t;
            }
        });
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
                new SellInnerCmdCallback(sellInfo), 5, false);

        bizService.startBiz(result.getData(), sellInfo.getBaseId(), JSON.toJSONString(sellInfo),
                cmdType, "sell", EnumQoS.ExtractOnce.getCode());

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
                new UpdateQrCodeInnerCmdCallback(), 10, false);
        String bizId = result.getData();

        QrCodeInfo qrCodeInfo =
                qrCodeInfoMap.computeIfAbsent(deviceId, key -> new QrCodeInfo(deviceId, bizMsg, url, bizId));
        qrCodeInfo.setDeviceId(deviceId);
        qrCodeInfo.setMsg(bizMsg);
        qrCodeInfo.setUrl(url);
        qrCodeInfo.setBizId(bizId);
        qrCodeInfo.setUpdateTime(LocalDateTime.now());

        bizService.startBiz(bizId, deviceId, cmdObj.toJSONString(),
                cmdType, "updateQRC", EnumQoS.ExtractOnce.getCode());

        return result;
    }

    @Override
    public void resendQrCode(String baseId) {
        logger.info("device reuse response ,should resend qrcode {}",baseId);
        QrCodeInfo qrCodeInfo = qrCodeInfoMap.get(baseId);
        if(qrCodeInfo!=null){
            updateQrCode(qrCodeInfo.msg, qrCodeInfo.url);
        }
    }

    @Override
    public void processMissTask(String baseId) {
        resendQrCode(baseId);
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
    public void offline(String baseId) {

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

            if (cmdMsg.isResult()) {
                logger.info("sell success {}", JSON.toJSONString(cmdMsg.getCmdMsg(), true));
            } else {
                logger.info("sell fail {}", JSON.toJSONString(cmdMsg.getCmdMsg(), true));

            }
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
            if (cmdMsg.isResult()) {
                logger.info("update qrcode success {}",
                        JSON.toJSONString(cmdMsg.getCmdMsg(), true));
            } else {
                //如果更新二维码失败，则重新发送
                LogDeviceCtrl info = bizService.getLogInfo(bizId);
                if (info != null) {
                    //延时1分钟重发二维码
                    scheduledExecutorService.schedule(new Runnable() {
                        @Override
                        public void run() {
                            String baseId = info.getBaseId();
                            logger.info("delay resend qrcode {}",baseId);
                            QrCodeInfo qrCodeInfo = qrCodeInfoMap.get(baseId);
                            updateQrCode(qrCodeInfo.msg, qrCodeInfo.url);
                        }
                    }, 1, TimeUnit.MINUTES);

                }
                logger.info("update qrcode fail {}",
                        JSON.toJSONString(cmdMsg.getCmdMsg(), true));
            }

        }
    }

    public static class QrCodeInfo {
        private String deviceId;
        private String msg;
        private String url;

        private String bizId;
        private LocalDateTime updateTime;

        public QrCodeInfo(String deviceId, String msg, String url, String bizId) {
            this.deviceId = deviceId;
            this.msg = msg;
            this.url = url;
            this.bizId = bizId;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getBizId() {
            return bizId;
        }

        public void setBizId(String bizId) {
            this.bizId = bizId;
        }

        public void setUpdateTime(LocalDateTime updateTime) {
            this.updateTime = updateTime;
        }

        public LocalDateTime getUpdateTime() {
            return updateTime;
        }
    }
}
