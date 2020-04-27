package com.winsun.iot.device.handler;

import com.alibaba.fastjson.JSONObject;
import com.google.inject.Inject;
import com.winsun.iot.command.CmdHandler;
import com.winsun.iot.command.CmdMsg;
import com.winsun.iot.dao.CommonDao;
import com.winsun.iot.device.DeviceManager;
import com.winsun.iot.persistence.PersistenceService;
import com.winsun.iot.utils.PathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResponseHandler implements CmdHandler {

    public static final String TOPIC = "/E2ES/GateWay/Response/+";

    public static final int QOS = 1;

    private static final Logger logger = LoggerFactory.getLogger(ResponseHandler.class);

    @Inject
    private PersistenceService persistenceService;
    @Inject
    private CommonDao dao;
    @Inject
    private DeviceManager deviceManager;

    public ResponseHandler() {
    }

    public ResponseHandler(PersistenceService persistenceService, CommonDao dao, DeviceManager deviceManager) {
        this.persistenceService = persistenceService;
        this.dao = dao;
        this.deviceManager = deviceManager;
    }

    /**
     * 事件格式
     * {
     * "sig":"58r4g35f4ds3",
     * "stage":0,
     * "result":false
     * }
     *
     * @param topic
     * @param data
     */
    @Override
    public void execute(String topic, CmdMsg data) {
//        try {
//
//            String gateway = PathUtil.getPath(topic, 4);
//            JSONObject msg = data.getData();
//            String sig = msg.getString("sig");
//            int stage = msg.getInteger("stage");
//            boolean result = msg.getBoolean("result");
//        } catch (Exception e) {
//            logger.error(e.getMessage(),e);
//            logger.error(e.getMessage(),data.getData().toJSONString());
//        }
    }
}
