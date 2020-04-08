package com.winsun.iot.device.handler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.inject.Inject;
import com.winsun.iot.command.CmdHandler;
import com.winsun.iot.command.CmdMsg;
import com.winsun.iot.dao.CommonDao;
import com.winsun.iot.device.DeviceManager;
import com.winsun.iot.domain.LogDeviceEvents;
import com.winsun.iot.persistence.Function;
import com.winsun.iot.persistence.PersistenceService;
import com.winsun.iot.utils.PathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 网关上下线
 */
public class ConnectHandler implements CmdHandler {

    public static final String TOPIC_CONNECT = "$SYS/brokers/emqx@127.0.0.1/clients/+/connected";
    public static final String TOPIC_DISCONNECT = "$SYS/brokers/emqx@127.0.0.1/clients/+/disconnected";

    public static final int QOS = 1;

    private static final Logger logger = LoggerFactory.getLogger(ConnectHandler.class);

    @Inject
    private PersistenceService persistenceService;
    @Inject
    private CommonDao dao;
    @Inject
    private DeviceManager deviceManager;

    /**
     * 事件格式
     *
     * @param topic
     * @param data
     */
    @Override
    public void execute(String topic, CmdMsg data) {

        String gateway = PathUtil.getPath(topic, 4);
        JSONObject msg = data.getData();

        boolean isonline = true;
        if(topic.contains("disconnected")){
            isonline = false;
        }else{
            isonline=true;
        }
        final boolean status = isonline;
        deviceManager.updateDeviceStatus(gateway,isonline);

        persistenceService.addAction(new Function() {
            @Override
            public void execute() {
                LogDeviceEvents events = new LogDeviceEvents();
                events.setBaseId(gateway);
                events.setEventName("GatewayConnState");
                events.setTime(LocalDateTime.now());
                events.setValue(status+"");
                dao.addEvent(events);
            }
        });
    }
}
