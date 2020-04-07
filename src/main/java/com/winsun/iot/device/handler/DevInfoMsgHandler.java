package com.winsun.iot.device.handler;

import com.alibaba.fastjson.JSONArray;
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

public class DevInfoMsgHandler implements CmdHandler {

    public static final String TOPIC = "/E2ES/GateWay/DevInfoMsg";

    public static final int QOS = 1;

    private static final Logger logger = LoggerFactory.getLogger(DevInfoMsgHandler.class);

    @Inject
    private PersistenceService persistenceService;
    @Inject
    private CommonDao dao;
    @Inject
    private DeviceManager deviceManager;

    public DevInfoMsgHandler() {
    }

    public DevInfoMsgHandler(PersistenceService persistenceService, CommonDao dao, DeviceManager deviceManager) {
        this.persistenceService = persistenceService;
        this.dao = dao;
        this.deviceManager = deviceManager;
    }

    /**
     * 说明：OptParameter：设备受控制后响应操作，如果操作失败就不回复
     * {
     * "detail" : {
     * "devobjs" : [ {
     * "baseid" : "22B80B0077E33303DE110000",
     * "params" : {
     * "0001" : 1,.
     * "0002" : 1,
     * "0003" : 1
     * }
     * } ],
     * "Info_Type" : "Parameter"
     * },
     * "Msg_Type" : "devInfo"
     * }
     *
     * @param topic
     * @param data
     */
    @Override
    public void execute(String topic, CmdMsg data) {

        JSONObject msg = JSONObject.parseObject(data.getData());
        String msgtype = msg.getString("Msg_Type");
        JSONObject detail = msg.getJSONObject("detail");
        if (msgtype != null && detail != null) {
            switch (msgtype) {
                case "devInfo": {
                    String infotype = detail.getString("Info_Type");
                    switch (infotype) {
                        case "Parameter": {
                            if (!detail.getString("devobjs").startsWith("[")) break;
                            JSONArray devobjs = detail.getJSONArray("devobjs");

                            for (int i = 0; i < devobjs.size(); i++) {
                                String baseid = devobjs.getJSONObject(i).getString("baseid");
                                JSONObject params = devobjs.getJSONObject(i).getJSONObject("params");
                                for (String paramname : params.keySet()) {
                                    deviceManager.updateControlRealityP(baseid, paramname, params.getString(paramname));
                                }
                            }
                            break;
                        }
                    }
                    break;
                }
            }
        }

    }

}
