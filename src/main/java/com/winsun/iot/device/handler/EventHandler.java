package com.winsun.iot.device.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.inject.Inject;
import com.winsun.iot.command.CmdHandler;
import com.winsun.iot.command.CmdMsg;
import com.winsun.iot.dao.CommonDao;
import com.winsun.iot.domain.LogDeviceEvents;
import com.winsun.iot.persistence.Function;
import com.winsun.iot.persistence.PersistenceService;
import com.winsun.iot.utils.PathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class EventHandler implements CmdHandler {

    public static final String TOPIC = "/E2ES/GateWay/Events/+";

    public static final int QOS = 1;

    private static final Logger logger = LoggerFactory.getLogger(EventHandler.class);

    private PersistenceService persistenceService;

    private CommonDao dao;

    public EventHandler(PersistenceService persistenceService, CommonDao dao) {
        this.persistenceService = persistenceService;
        this.dao = dao;
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
        JSONObject msg = JSONObject.parseObject(data.getData());

        String Msg_Type = msg.getString("Msg_Type");
        switch (Msg_Type) {
            case "devEvents": {
                JSONObject detail = msg.getJSONObject("detail");
                Object deviceObjs = detail.getObject("devobjs", Object.class);
                if (!(deviceObjs instanceof JSONArray)) {
                    return;
                }
                //不论所有事件，保存到数据库
                JSONArray devobjs = (JSONArray) deviceObjs;
                String gwid = gateway;

                for (int i = 0; i < devobjs.size(); i++) {
                    String baseid = devobjs.getJSONObject(i).getString("baseid");
                    if ("localdev".equals(baseid)) {
                        baseid = gwid;
                    }
                    final String baseIdValue = baseid;
                    String value = devobjs.getJSONObject(i).getString("value");
                    String eventname = detail.getString("eventname");
                    persistenceService.addAction(new Function() {
                        @Override
                        public void execute() {
                            LogDeviceEvents events = new LogDeviceEvents();
                            events.setBaseId(baseIdValue);
                            events.setEventName(detail.getString("eventname"));
                            events.setTime(LocalDateTime.now());
                            events.setValue(value);
                            dao.addEvent(events);
                        }
                    });
                }
                break;
            }
        }
    }
}
