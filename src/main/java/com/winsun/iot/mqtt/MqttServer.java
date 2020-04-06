package com.winsun.iot.mqtt;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.winsun.iot.command.CmdMsg;
import com.winsun.iot.command.CmdRuleInfo;
import com.winsun.iot.command.CommandHandler;
import com.winsun.iot.command.EnumQoS;
import com.winsun.iot.device.CommandServer;
import com.winsun.iot.exception.SendException;
import com.winsun.iot.utils.Const;
import com.winsun.iot.utils.DateTimeUtils;
import com.winsun.iot.utils.tree.TriaTree;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MqttServer implements MqttCallback, CommandServer {
    private static final Logger logger = LoggerFactory.getLogger(MqttServer.class);
    private MqttConfig config;
    private MqttConnectOptions options;
    private MqttClient client;
    private String clientidPrefix = "server_";

    private Consumer<CmdMsg> msgConsumer;

    private TriaTree<CommandHandler> cmdtree = new TriaTree<>();
    private List<CommandHandler> commandList = new ArrayList<>();

    public MqttServer(MqttConfig config) {
        this.config = config;
        this.options = getOptions();
    }

    public void addCommand(CommandHandler commandHandler) {
        this.commandList.add(commandHandler);
        this.cmdtree.add(commandHandler.getTopic(), commandHandler);
        if (client != null && client.isConnected()) {
            subcribeTopic();
        }
    }

    public void connect() {
        try {
            String id_tmp = DateTimeUtils.formatFullStr(LocalDateTime.now());
            logger.info("connect with cliendid :" + clientidPrefix + id_tmp);
            client = new MqttClient(config.getBroker(), clientidPrefix + id_tmp, new MemoryPersistence());
        } catch (MqttException e) {
            logger.error(e.getMessage(), e);
            return;
        }

        try {
            client.setCallback(this);
            client.connect(options);
            //订阅消息
            if (client.isConnected()) {
                subcribeTopic();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void subcribeTopic() {
        if (commandList.size() == 0) {
            return;
        }
        String[] topcicFilter = new String[commandList.size()];
        int[] qos = new int[commandList.size()];
        for (int i = 0; i < commandList.size(); i++) {
            topcicFilter[i] = commandList.get(i).getTopic();
            qos[i] = commandList.get(i).getQos().getCode();
        }
        try {
            IMqttToken token = this.client.subscribeWithResponse(topcicFilter, qos);
            token.waitForCompletion(5 * Const.SECOND);
            if (!token.isComplete()) {
                logger.error("subscibr topic {} fail,{}", String.join(",", topcicFilter),
                        token.getException().getMessage());
            }
        } catch (MqttException e) {
            logger.error(e.getMessage(), e);
        }

    }

    private MqttConnectOptions getOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setUserName(config.getUsername());
        options.setPassword(config.getPassword().toCharArray());
        // 设置超时时间
        options.setConnectionTimeout(10);
        // 设置会话心跳时间
        options.setKeepAliveInterval(20);
        options.setAutomaticReconnect(true);
        return options;
    }

    @Override
    public void connectionLost(Throwable throwable) {
        logger.error("mqtt server disconnect ",throwable);
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {

        String content = new String(mqttMessage.getPayload());
        JSONObject jo = JSON.parseObject(content);
        String signature = jo.getString("signature");
        String actiontype = jo.getString("actiontype");
        CmdMsg msg = new CmdMsg(topic, content,
                EnumQoS.valueOf(mqttMessage.getQos()));
        msg.setBizId(signature);
        msg.setActionType(actiontype);

        receive(msg);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }

    @Override
    public void receive(CmdMsg msg) {
        //
        if (this.msgConsumer != null) {
            this.msgConsumer.accept(msg);
        }
        String topic = msg.getTopic();
        List<CommandHandler> cmdInfo = cmdtree.getValue(topic);
        for (CommandHandler commandHandler : cmdInfo) {
            try{
                commandHandler.getHandler().execute(topic, msg);
            }catch (Exception exc){
                logger.error(exc.getMessage(),exc);
            }
        }
    }

    public void start() {
        this.connect();
    }

    @Override
    public void setReceiveMsgConsumer(Consumer<CmdMsg> cmdMsg) {
        this.msgConsumer = cmdMsg;
    }

    public boolean canSend() {
        return this.client.isConnected();
    }

    public boolean publish(CmdRuleInfo ruleInfo) throws MqttException {
        CmdMsg msg = ruleInfo.getCmdMsg();
        return publish(msg.getTopic(), msg.getData(), msg.getQos().getCode());
    }

    public boolean publish(String topic, String msg, int qos) throws SendException {
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setPayload(msg.getBytes());
        mqttMessage.setQos(qos);
        mqttMessage.setRetained(false);
        MqttTopic mqttTopic = this.client.getTopic(topic);
        if (mqttTopic != null) {
            MqttToken token = null;
            try {
                token = mqttTopic.publish(mqttMessage);
                token.waitForCompletion();
                return token.isComplete();

            } catch (MqttException e) {
                throw new SendException(e.getMessage(), e);
            }
        }
        logger.error("topic is not exits {}", topic);
        return false;
    }

    public MqttConfig getConfig() {
        return config;
    }
}
