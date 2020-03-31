package com.winsun.iot.mqtt;

import com.winsun.iot.command.CmdMsg;
import com.winsun.iot.command.CommandHandler;
import com.winsun.iot.command.EnumQoS;
import com.winsun.iot.utils.Const;
import com.winsun.iot.utils.DateTimeUtils;
import com.winsun.iot.utils.tree.TriaTree;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MqttServer implements MqttCallback {
    private static final Logger logger = LoggerFactory.getLogger(MqttServer.class);
    private MqttConfig config;
    private MqttConnectOptions options;
    private MqttClient client;
    private String clientidPrefix ="server_";

    private TriaTree<CommandHandler> cmdtree = new TriaTree<>();
    private List<CommandHandler> commandList = new ArrayList<>();

    public MqttServer(MqttConfig config) {
        this.config = config;
        this.options = getOptions();
    }

    public void addCommand(CommandHandler commandHandler){
        this.commandList.add(commandHandler);
        this.cmdtree.add(commandHandler.getTopic(), commandHandler);
        if(client.isConnected()){
            subcribeTopic();
        }
    }

    public void connect(){
        try {
            String id_tmp = DateTimeUtils.formatFullStr(LocalDateTime.now());
            logger.info("connect with cliendid :"+clientidPrefix+id_tmp);
            client = new MqttClient(config.getBroker(), clientidPrefix+id_tmp, new MemoryPersistence());
        } catch (MqttException e) {
            logger.error(e.getMessage(),e);
            return;
        }

        try {
            client.setCallback(this);
            client.connect(options);
            //订阅消息
            if(client.isConnected())
            {
                subcribeTopic();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void subcribeTopic() {
        String[] topcicFilter = new String[commandList.size()];
        int[] qos = new int[commandList.size()];
        for (int i = 0; i < commandList.size(); i++) {
            topcicFilter[i]= commandList.get(i).getTopic();
            qos[i]= commandList.get(i).getQos().getCode();
        }
        try {
            IMqttToken token = this.client.subscribeWithResponse(topcicFilter,qos);
            token.waitForCompletion(5* Const.SECOND);
            if(!token.isComplete()){
                logger.error("subscibr topic {} fail,{}",String.join(",",topcicFilter),
                        token.getException().getMessage());
            }
        } catch (MqttException e) {
            logger.error(e.getMessage(),e);
        }

    }

    private MqttConnectOptions getOptions(){
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

    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        //
        List<CommandHandler> cmdInfo = cmdtree.getValue(topic);
        CmdMsg msg = new CmdMsg(topic, new String(mqttMessage.getPayload()),
                EnumQoS.valueOf(mqttMessage.getQos()));
        for (CommandHandler commandHandler : cmdInfo) {
            commandHandler.getHandler().execute(topic,msg);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }

    public void start() {

        this.connect();
    }
}
