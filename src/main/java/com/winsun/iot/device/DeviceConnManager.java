package com.winsun.iot.device;

import com.alibaba.fastjson.JSONObject;
import com.google.inject.Inject;
import com.winsun.iot.command.CmdCallback;
import com.winsun.iot.command.CmdMsg;
import com.winsun.iot.command.CmdRuleInfo;
import com.winsun.iot.command.CommandHandler;
import com.winsun.iot.command.biz.BizCmdHandler;
import com.winsun.iot.config.Config;
import com.winsun.iot.mqtt.MqttConfig;
import com.winsun.iot.mqtt.MqttServer;
import com.winsun.iot.ruleengine.CmdRule;
import com.winsun.iot.utils.MsgConsumer;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class DeviceConnManager {
    private static final Logger logger = LoggerFactory.getLogger(DeviceConnManager.class);
    private MqttServer mqttServer;

    private CommandServer commandServer;


    private Map<String, CmdQueue> gatewayCmdQueue = new ConcurrentHashMap<>();


    private int cmdTimeInterval = 300;
    private int destroySecond = 180;

    private boolean isinit = false;
    private ScheduledExecutorService service;

    private BizCmdHandler bizCmdHandler;

    private Config config;

    public DeviceConnManager(Config config,BizCmdHandler handler) {
        this.config = config;
        this.bizCmdHandler = handler;
        service = Executors.newScheduledThreadPool(2, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("mqttserver-cmd-thread");
                return t;
            }
        });
    }

    private void init() {
        this.mqttServer = new MqttServer(new MqttConfig(config.MqttBroker(), config.MqttUserName(), config.MqttPassword()));
        for (CommandHandler cmdHandler : cmdHandlers) {
            this.mqttServer.addCommand(cmdHandler);
        }
        this.commandServer = this.mqttServer;
        this.commandServer.setReceiveMsgConsumer(bizCmdHandler.getConsumer());
    }

    private List<CommandHandler> cmdHandlers = new ArrayList<>();

    public void addCommand(CommandHandler handler) {
        this.cmdHandlers.add(handler);
    }

    public void start() {
        if (!isinit) {
            init();
        }
        logger.info("start mqtt server {}", this.mqttServer.getConfig().getBroker());
        this.mqttServer.start();
        service.scheduleAtFixedRate(new CmdSender(), 0, 50, TimeUnit.MILLISECONDS);
    }

    public void sendCmd(CmdRuleInfo cmdMsg, CmdCallback cmdCallback) {
        CmdQueue queue = gatewayCmdQueue.computeIfAbsent(cmdMsg.getCmdMsg().getGatewayId(),
                k -> new CmdQueue(cmdTimeInterval, destroySecond));
        queue.offerQueue(cmdMsg);

        CmdRule cmdRule = bizCmdHandler.addCmdRule(cmdMsg,cmdCallback);

    }

    public void sendRawCmd(String topic, String msg, int qos) {
        if(mqttServer!=null&&mqttServer.isconnect()){
            mqttServer.publish(topic,msg,qos);
        }
    }


    private class CmdSender implements Runnable {

        @Override
        public void run() {

            try {
                Iterator<Map.Entry<String, CmdQueue>> iter = gatewayCmdQueue.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, CmdQueue> queue = iter.next();
                    if (queue.getValue().canDestroy()) {
                        iter.remove();
                    }
                    if (queue.getValue().canPullAndSend() && mqttServer.canSend()) {
                        boolean sendSuc = false;
                        try {
                            CmdRuleInfo msg = queue.getValue().pull();
                            if (msg != null) {
                                sendSuc = mqttServer.publish(msg);
                            }
                        } catch (MqttException e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            } catch (Exception exc) {
                logger.error(exc.getMessage(), exc);
            }
        }
    }

    private void publish(String topic, String msg, int qos) {
        mqttServer.publish(topic, msg, qos);
    }

    private static class CmdQueue {
        private LocalDateTime lastTime;
        private Queue<CmdRuleInfo> cmdQueue = new ArrayBlockingQueue<>(100);

        private int millsecondInterval = 300;
        private int destroySecond = 180;

        public CmdQueue(int millsecondInterval, int destroySecond) {
            this.millsecondInterval = millsecondInterval;
            this.destroySecond = destroySecond;
            this.lastTime = LocalDateTime.now();
        }

        public void offerQueue(CmdRuleInfo cmdMsg) {
            this.cmdQueue.offer(cmdMsg);
        }

        public CmdRuleInfo pull() {
            return this.cmdQueue.poll();
        }

        /**
         * is time to send command
         *
         * @return
         */
        public boolean canPullAndSend() {
            Duration duration = Duration.between(lastTime, LocalDateTime.now());
            return duration.toMillis() > millsecondInterval;
        }

        public boolean canDestroy() {
            Duration duration = Duration.between(lastTime, LocalDateTime.now());
            return duration.toMillis() > destroySecond * 1000 && this.cmdQueue.isEmpty();
        }
    }
}
