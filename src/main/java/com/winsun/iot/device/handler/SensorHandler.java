package com.winsun.iot.device.handler;

import com.alibaba.fastjson.JSONObject;
import com.google.inject.Inject;
import com.winsun.iot.command.CmdHandler;
import com.winsun.iot.command.CmdMsg;
import com.winsun.iot.dao.CommonDao;
import com.winsun.iot.device.DeviceManager;
import com.winsun.iot.persistence.PersistenceService;
import com.winsun.iot.utils.DateTimeUtils;
import com.winsun.iot.utils.PathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

public class SensorHandler implements CmdHandler {

    public static final String TOPIC = "/E2ES/GateWay/Sensor/+";

    public static final int QOS = 1;

    private static final Logger logger = LoggerFactory.getLogger(SensorHandler.class);

    @Inject
    private PersistenceService persistenceService;
    @Inject
    private CommonDao dao;
    @Inject
    private DeviceManager deviceManager;

    public SensorHandler() {
    }

    public SensorHandler(PersistenceService persistenceService, CommonDao dao, DeviceManager deviceManager) {
        this.persistenceService = persistenceService;
        this.dao = dao;
        this.deviceManager = deviceManager;
    }

    /**
     * 事件格式
     * {
     * "detail" : {
     * "devobjs" : [ {
     * "baseid" : "00124B001A545673",
     * "value" : false
     * } ],
     * "eventname" : "NetworkState"
     * },
     * "Msg_Type" : "devEvents"
     * }
     *
     * @param topic
     * @param data
     */
    @Override
    public void execute(String topic, CmdMsg data) {
        String gateway = PathUtil.getPath(topic, 4);
        JSONObject msg = data.getData();

        JSONObject datatemp = msg.getJSONObject("data");
        String baseid = datatemp.getString("baseid");

        if (baseid == null) {
            logger.error("数据错误: {}，\n{}",topic,data.getData());
            return;
        }
        if (datatemp.containsKey("period")) {
            boolean ret = deviceManager.receiveStsSensorDataFromMqtt(baseid, datatemp);
            if (!ret) {
                logger.error("设备 " + baseid + " " + DateTimeUtils.formatFullSecond(LocalDateTime.now())
                        + " 数据\n" + msg.toJSONString());
            }
        } else {
            deviceManager.receiveRealitySensorDataFromMqtt(baseid, datatemp);
        }
    }

}
