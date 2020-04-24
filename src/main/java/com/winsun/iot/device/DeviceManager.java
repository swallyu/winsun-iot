package com.winsun.iot.device;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.inject.Inject;
import com.winsun.iot.command.*;
import com.winsun.iot.command.biz.BizCmdHandler;
import com.winsun.iot.config.Config;
import com.winsun.iot.constcode.MsgCode;
import com.winsun.iot.dao.CommonDao;
import com.winsun.iot.dao.SysDevicesMapper;
import com.winsun.iot.device.handler.*;
import com.winsun.iot.domain.CmdResult;
import com.winsun.iot.domain.DeviceInfo;
import com.winsun.iot.domain.SysDevices;
import com.winsun.iot.iocmodule.Ioc;
import com.winsun.iot.persistence.redis.RedisService;
import com.winsun.iot.ruleengine.EnumCmdStatus;
import com.winsun.iot.schedule.ScheduleService;
import com.winsun.iot.utils.BizResult;
import com.winsun.iot.utils.DateTimeUtils;
import com.winsun.iot.utils.HttpClientUtil;
import com.winsun.iot.utils.RandomString;
import com.winsun.iot.utils.functions.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DeviceManager {

    private static final Logger logger = LoggerFactory.getLogger(DeviceManager.class);

    private static final String REDIS_DEVICE_CACHE_KEY="deviceInfo";

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

    @Inject
    private SysDevicesMapper devicesMapper;

    @Inject
    private RedisService redisService;

    private Map<String, DeviceInfo> deviceInfoMap = new ConcurrentHashMap<>();

    private Map<String, DeviceLifeRecycleListener> lifeRecycleListenerMap = new HashMap<>();

    public void start() {
        RedisService redisService = Ioc.getInjector().getInstance(RedisService.class);
        scheduleService.startTask("heart-beat", 10 * 1000, new Function() {
            @Override
            public void execute() {
                logger.info("send heart beat");
                connManager.sendRawCmd("/E2ES/HeartBeat", "1", 0);
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
            this.redisService.hset(REDIS_DEVICE_CACHE_KEY,device.getBaseId(), JSON.toJSONString(device));
        }
        logger.info("load device size {}", devices.size());
    }

    private void saveToRedis(SysDevices devices){
        redisService.hset(REDIS_DEVICE_CACHE_KEY,devices.getBaseId(),JSON.toJSONString(devices));
    }

    private void removeFromRedis(String baseId){
        redisService.hdel(REDIS_DEVICE_CACHE_KEY,baseId);
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
                    currentPage--;
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
                                       CmdCallback callback, int timeout, boolean resendUseNewSig, boolean invokeIfNotOnline) {
        DeviceInfo info = getDeviceObj(baseId);
        if(info==null){
            return new CmdResult<String>(MsgCode.DEVICE_NOT_EXITS,false,"设备不存在",null);
        }
        if(!invokeIfNotOnline&&!info.isOnline()){
            return new CmdResult<String>(MsgCode.DEVICE_OFFLINE,false,"设备掉线",null);
        }

        String dstTopic = topic + "/" + baseId;

        String sig = RandomString.getRandomString(16);

        JSONObject ringMsgobj = CmdFactory.buildBizCmd(sig, qos, msgtype, cmdObj);

        CmdMsg msg = new CmdMsg(dstTopic, ringMsgobj, qos);
        msg.setBizId(sig);
        msg.setGatewayId(baseId);
        msg.setStatus(EnumCmdStatus.Stage_0);

        connManager.sendCmd(new CmdRuleInfo(msg), callback, timeout, resendUseNewSig);

        CmdResult<String> result = new CmdResult<>(0, true, "发送控制命令成功", sig);
        return result;
    }

    public boolean invokeCmd(CmdRuleInfo data, CmdCallback callback) {
        connManager.sendCmd(data, callback, 10, false);
        return true;
    }

    public boolean invokeCmd(CmdRuleInfo data, CmdCallback callback, int timeout, boolean resendUseNewSig) {
        connManager.sendCmd(data, callback, timeout, resendUseNewSig);
        return true;
    }

    public void addLifeRecycleListener(String name, DeviceLifeRecycleListener listener) {
        this.lifeRecycleListenerMap.put(name, listener);
    }

    public void invokeOnline(String gateway) {
        if (this.deviceInfoMap.containsKey(gateway)) {
            for (DeviceLifeRecycleListener value : lifeRecycleListenerMap.values()) {
                value.online(gateway);
            }
        }
    }

    public void reload() {
        loadDevice();
    }

    public boolean sendRawCmd(String data, int qos, String topic) {
        this.connManager.sendRawCmd(topic, data, qos);
        return true;
    }

    public Collection<DeviceInfo> getDeviceObjList() {
        return this.deviceInfoMap.values();
    }

    public BizResult<Boolean> deleteDevice(String deviceId) {
        int ret = this.devicesMapper.deleteDevices(deviceId);
        if (ret > 0) {
            this.deviceInfoMap.remove(deviceId);
            removeFromRedis(deviceId);
        }
        return BizResult.Success(true);
    }

    public BizResult<Boolean> createDevice(DeviceInfo deviceInfo) {
        DeviceInfo info = this.deviceInfoMap.get(deviceInfo.getBaseId());
        if (info != null) {

            return new BizResult<Boolean>(MsgCode.DEVICE_EXITS,
                    "增加设备 " + deviceInfo.getBaseId() + " 失败，设备已存在", false);
        } else {
            SysDevices device = deviceInfo.getDevice();
            device.setCreateTime(LocalDateTime.now());
            device.setModifiedTime(LocalDateTime.now());

            int ret = this.devicesMapper.insert(deviceInfo.getDevice());
            if (ret > 0) {
                this.deviceInfoMap.put(device.getBaseId(), new DeviceInfo(device));
                saveToRedis(device);
                return BizResult.Success("新增设备 " + deviceInfo.getBaseId() + " 成功", true);
            }
            return BizResult.Success("新增设备 " + deviceInfo.getBaseId() + " 失败", false);
        }
    }

    public BizResult<Boolean> updateDevice(DeviceInfo deviceInfo) {
        DeviceInfo info = this.deviceInfoMap.get(deviceInfo.getBaseId());
        if (info != null) {
            SysDevices device = deviceInfo.getDevice();
            device.setModifiedTime(LocalDateTime.now());

            int ret = this.devicesMapper.updateByBaseIdSelective(deviceInfo.getDevice());
            if (ret > 0) {
                SysDevices devices = this.devicesMapper.selectByBaseId(deviceInfo.getBaseId());
                info.setDevice(devices);
                saveToRedis(devices);
                return BizResult.Success("更新设备 " + deviceInfo.getBaseId() + " 成功", true);
            }
            return BizResult.Success("更新设备 " + deviceInfo.getBaseId() + " 失败", false);

        } else {
            return new BizResult<Boolean>(MsgCode.DEVICE_NOT_EXITS,
                    "更新设备 " + deviceInfo.getBaseId() + " 失败，设备不存在", false);
        }
    }
}
