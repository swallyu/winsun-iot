package com.winsun.iot.device;

import com.winsun.iot.command.CmdMsg;
import com.winsun.iot.command.CmdRuleInfo;
import com.winsun.iot.config.Config;
import com.winsun.iot.mqtt.MqttConfig;
import com.winsun.iot.mqtt.MqttServer;
import com.winsun.iot.ruleengine.CmdRule;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class DeviceConnManager implements Consumer<CmdMsg> {
    private static final Logger logger = LoggerFactory.getLogger(DeviceConnManager.class);
    private MqttServer mqttServer;

    private CommandServer commandServer;

    private Config config;

    private Map<String, CmdQueue> gatewayCmdQueue = new ConcurrentHashMap<>();
    private Map<String, CmdRule> cmdRuleInfoMap = new HashMap<>();

    private int cmdTimeInterval = 300;
    private int destroySecond = 180;

    private ScheduledExecutorService service;

    public DeviceConnManager(Config config) {
        this.config = config;
        service = Executors.newScheduledThreadPool(2, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("mqttserver-cmd-thread");
                return t;
            }
        });
    }

    public void init() {
        this.mqttServer = new MqttServer(new MqttConfig(config.MqttBroker(), config.MqttUserName(), config.MqttPassword()));
        this.commandServer = this.mqttServer;
        this.commandServer.setReceiveMsgConsumer(this);
    }

    public void start() {
        this.mqttServer.start();
        service.scheduleAtFixedRate(new CmdSender(), 0, 50, TimeUnit.MILLISECONDS);
    }

    public void sendCmd(CmdRuleInfo cmdMsg) {
        CmdQueue queue = gatewayCmdQueue.computeIfAbsent(cmdMsg.getCmdMsg().getGatewayId(), k -> new CmdQueue(cmdTimeInterval, destroySecond));
        queue.offerQueue(cmdMsg);

        //
        CmdRule cmdRule = cmdRuleInfoMap.computeIfAbsent(cmdMsg.getBizId(),k->new CmdRule(cmdMsg.getCmdMsg()));
        cmdRule.processCmdMsg(cmdMsg);
    }

    @Override
    public void accept(CmdMsg cmdMsg) {
        String bizId = cmdMsg.getBizId();
        CmdRule cmdRule = this.cmdRuleInfoMap.get(bizId);
        if (cmdRule != null) {
            cmdRule.processCmdMsg(new CmdRuleInfo(cmdMsg));
            if(cmdRule.isComplete()){
                cmdRule.done();
            }
        }
    }

    private class CmdSender implements Runnable {

        @Override
        public void run() {

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
                        sendSuc = mqttServer.publish(msg);
                    } catch (MqttException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
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
            Duration duration = Duration.between(LocalDateTime.now(), lastTime);
            return duration.toMillis() > millsecondInterval;
        }

        public boolean canDestroy() {
            Duration duration = Duration.between(LocalDateTime.now(), lastTime);
            return duration.toMillis() > destroySecond * 1000 && this.cmdQueue.isEmpty();
        }
    }
}
