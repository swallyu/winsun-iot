package com.winsun.iot.device;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.inject.Inject;
import com.winsun.iot.command.*;
import com.winsun.iot.command.biz.BizCmdHandler;
import com.winsun.iot.config.Config;
import com.winsun.iot.dao.CommonDao;
import com.winsun.iot.device.handler.*;
import com.winsun.iot.domain.CmdResult;
import com.winsun.iot.domain.DeviceInfo;
import com.winsun.iot.domain.SysDevices;
import com.winsun.iot.iocmodule.Ioc;
import com.winsun.iot.persistence.redis.RedisService;
import com.winsun.iot.ruleengine.EnumCmdStatus;
import com.winsun.iot.schedule.ScheduleService;
import com.winsun.iot.utils.DateTimeUtils;
import com.winsun.iot.utils.HttpClientUtil;
import com.winsun.iot.utils.RandomString;
import com.winsun.iot.utils.functions.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DeviceManager {

    private static final Logger logger = LoggerFactory.getLogger(DeviceManager.class);

    @Inject
    private DeviceConnManager connManager;

    @Inject
    private CommonDao commonDao;

    @Inject
    private Config config;

    @Inject
    private BizCmdHandler bizCmdHandler;

    @Inject
    private ScheduleService scheduleService;

    private Map<String, DeviceInfo> deviceInfoMap = new ConcurrentHashMap<>();

    private Map<String,DeviceLifeRecycleListener> lifeRecycleListenerMap = new HashMap<>();

    public void start() {
        RedisService redisService = Ioc.getInjector().getInstance(RedisService.class);
        scheduleService.startTask("heart-beat", 10 * 1000, new Function() {
            @Override
            public void execute() {
                logger.info("send heart beat");
                connManager.sendRawCmd("/E2ES/HeartBeat","1",0);
            }
        });

        loadDevice();

        connManager.addCommand(new CommandHandler(EventHandler.TOPIC, EnumQoS.valueOf(EventHandler.QOS),
                Ioc.getInjector().getInstance(EventHandler.class)));

        connManager.addCommand(new CommandHandler(SensorHandler.TOPIC, EnumQoS.valueOf(SensorHandler.QOS),
                Ioc.getInjector().getInstance(SensorHandler.class)));

        connManager.addCommand(new CommandHandler(ConnectHandler.TOPIC_CONNECT, EnumQoS.valueOf(ConnectHandler.QOS),
                Ioc.getInjector().getInstance(ConnectHandler.class)));

        connManager.addCommand(new CommandHandler(ConnectHandler.TOPIC_DISCONNECT, EnumQoS.valueOf(ConnectHandler.QOS),
                Ioc.getInjector().getInstance(ConnectHandler.class)));

        connManager.addCommand(new CommandHandler(DevInfoMsgHandler.TOPIC, EnumQoS.valueOf(DevInfoMsgHandler.QOS),
                Ioc.getInjector().getInstance(DevInfoMsgHandler.class)));

        connManager.addCommand(new CommandHandler(GetInfoHandler.TOPIC, EnumQoS.valueOf(GetInfoHandler.QOS),
                Ioc.getInjector().getInstance(GetInfoHandler.class)));

        connManager.addCommand(new CommandHandler(ResponseHandler.TOPIC, EnumQoS.valueOf(ResponseHandler.QOS),
                Ioc.getInjector().getInstance(ResponseHandler.class)));

        connManager.start();

        loadGatewayStatusFromMqtt();
    }

    private void loadDevice() {
        List<SysDevices> devices = commonDao.listDevices();
        for (SysDevices device : devices) {
            DeviceInfo info = new DeviceInfo(device);
            deviceInfoMap.put(info.getBaseId(), info);
        }
        logger.info("load device size {}", devices.size());
    }

    public boolean updateDeviceStatus(String baseid, Boolean status) {
        DeviceInfo info = deviceInfoMap.get(baseid);
        if (info != null) {
            if (info.isOnline() == status) {
                return false;
            }
            info.setOnline(status);

            return true;
        }
        return false;
    }

    public void loadGatewayStatusFromMqtt() {

        int pageSize = 10000;
        String apiUrl = config.MqttApiHost() + "/api/v3/connections";
        String username = config.MqttApiUser();
        String password = config.MqttApiPwd();
        Map<String, Object> mapparameters = new HashMap<String, Object>();

        int currentPage = 1;
        while (true) {
            mapparameters.put("_page", currentPage);
            mapparameters.put("_limit", pageSize);
            currentPage++;
            try {
                String res = HttpClientUtil.doGet(apiUrl, mapparameters, username, password);
                JSONObject jo = JSONObject.parseObject(res);
                if (jo == null) {
                    continue;
                }
                JSONArray items = jo.getJSONArray("items");
                if (items == null) {
                    items = jo.getJSONArray("data");
                }
                if (items == null) {
                    break;
                }
                if (items != null) {
                    for (int i = 0; i < items.size(); i++) {
                        String deviceId = items.getJSONObject(i).getString("client_id");
                        DeviceInfo info = this.deviceInfoMap.get(deviceId);
                        if (info != null) {
                            info.login();
                            for (DeviceLifeRecycleListener value : lifeRecycleListenerMap.values()) {
                                value.online(deviceId);
                            }
                        }
                    }
                }
                if (items.size() == 0) {
                    logger.info("处理设备状态完成");
                    break;
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                break;
            }
        }
    }

    /**
     * 周期传感器数据
     *
     * @param baseid
     * @param stssensordata
     * @return
     */
    public boolean receiveStsSensorDataFromMqtt(String baseid, JSONObject stssensordata) {
        DeviceInfo deviceInfo = deviceInfoMap.get(baseid);
        if (deviceInfo == null) {
            return false;
        }
        if (stssensordata == null) {
            logger.error("，设备:" + baseid + "当前时间 " + DateTimeUtils.formatFullStr(LocalDateTime.now()) + "，数据为空:");
            return false;
        }
        boolean saveSuc = deviceInfo.updateStsSensorDataAndPull2DB(stssensordata);
        if (!deviceInfo.isOnline()) {
            if (this.isDevOnLine(deviceInfo.getBaseId())) {
                deviceInfo.setOnline(true);
            }
        }
        return true;
    }

    /**
     * 实时传感器数据
     *
     * @param baseid
     * @param stssensordata
     */
    public boolean receiveRealitySensorDataFromMqtt(String baseid, JSONObject stssensordata) {
        DeviceInfo deviceInfo = deviceInfoMap.get(baseid);
        if (deviceInfo == null) {
            return false;
        }
        if (stssensordata == null) {
            logger.error("，设备:" + baseid + "当前时间 " + DateTimeUtils.formatFullStr(LocalDateTime.now()) + "，数据为空:");
            return false;
        }
        boolean saveSuc = deviceInfo.updateRealitySensorDataAndPull2DB(stssensordata);
        if (!deviceInfo.isOnline()) {
            if (this.isDevOnLine(deviceInfo.getBaseId())) {
                deviceInfo.setOnline(true);
            }
        }
        return true;
    }

    private boolean isDevOnLine(String gatewayid) {
        DeviceInfo info = this.deviceInfoMap.get(gatewayid);
        if (info != null) {
            return info.isOnline();
        }
        return false;
    }

    public boolean updateControlRealityP(String baseid, String parameter, String val) {
        DeviceInfo dobj = this.deviceInfoMap.get(baseid);
        if (dobj != null) {
            return dobj.updateControlRealityP(parameter, val);
        } else {
            return false;
        }
    }

    public DeviceInfo getDeviceObj(String baseid) {
        return this.deviceInfoMap.get(baseid);
    }

    public CmdResult<String> invokeCmd(String topic, EnumQoS qos,
                                       String msgtype, String baseId, JSONObject cmdObj,
                                       CmdCallback callback, int timeout) {
        String dstTopic = topic + "/" + baseId;

        String sig = RandomString.getRandomString(16);

        JSONObject ringMsgobj = CmdFactory.buildBizCmd(sig,qos,msgtype,cmdObj);

        CmdMsg msg = new CmdMsg(dstTopic, ringMsgobj, qos);
        msg.setBizId(sig);
        msg.setGatewayId(baseId);
        msg.setStatus(EnumCmdStatus.Stage_0);
        connManager.sendCmd(new CmdRuleInfo(msg),callback,timeout);

        CmdResult<String> result = new CmdResult<>(0, true, "发送控制命令成功", sig);
        return result;
    }

    public boolean invokeCmd(CmdRuleInfo data, CmdCallback callback) {
        connManager.sendCmd(data,callback, 10);
        return true;
    }

    public void addLifeRecycleListener(String name, DeviceLifeRecycleListener listener) {
        this.lifeRecycleListenerMap.put(name, listener);
    }

    public void invokeOnline(String gateway) {
        if(this.deviceInfoMap.containsKey(gateway)){
            for (DeviceLifeRecycleListener value : lifeRecycleListenerMap.values()) {
                value.online(gateway);
            }
        }
    }
}
