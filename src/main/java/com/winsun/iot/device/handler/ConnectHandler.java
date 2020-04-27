package com.winsun.iot.device.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.inject.Inject;
import com.winsun.iot.command.CmdHandler;
import com.winsun.iot.command.CmdMsg;
import com.winsun.iot.constcode.EventCode;
import com.winsun.iot.dao.CommonDao;
import com.winsun.iot.device.DeviceLifeRecycleListener;
import com.winsun.iot.device.DeviceManager;
import com.winsun.iot.domain.LogDeviceEvents;
import com.winsun.iot.logdata.LogDataService;
import com.winsun.iot.utils.functions.Function;
import com.winsun.iot.persistence.PersistenceService;
import com.winsun.iot.utils.PathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * 网关上下线
 */
public class ConnectHandler implements CmdHandler {

    public static final String TOPIC_CONNECT = "$SYS/brokers/emqx@127.0.0.1/clients/+/connected";
    public static final String TOPIC_DISCONNECT = "$SYS/brokers/emqx@127.0.0.1/clients/+/disconnected";

    public static final int QOS = 1;

    private static final Logger logger = LoggerFactory.getLogger(ConnectHandler.class);

    private ScheduledExecutorService scheduledExecutorService;
    @Inject
    private PersistenceService persistenceService;
    @Inject
    private CommonDao dao;
    @Inject
    private DeviceManager deviceManager;

    @Inject
    private LogDataService logDataService;

    public ConnectHandler() {
        this.scheduledExecutorService = Executors.newScheduledThreadPool(2, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("connect-handler-thread");
                return t;
            }
        });
    }

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
        if(isonline){
            //如果设备上线，需要推迟一段时间更新二维码。
            this.scheduledExecutorService.schedule(new Runnable() {
                @Override
                public void run() {
                    deviceManager.invokeOnline(gateway);
                }
            },5, TimeUnit.SECONDS);
        }
        LogDeviceEvents events = new LogDeviceEvents();
        events.setBaseId(gateway);
        events.setEventName("GatewayConnState");
        events.setTime(LocalDateTime.now());
        events.setValue(status+"");

        logDataService.addData(EventCode.EVENT_DEVICE_NETSTATE, JSON.toJSONString(events));

        persistenceService.addAction(new Function() {
            @Override
            public void execute() {
                dao.addEvent(events);
            }
        });
    }

}
