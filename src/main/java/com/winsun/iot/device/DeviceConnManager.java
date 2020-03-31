package com.winsun.iot.device;

import com.google.inject.Inject;
import com.winsun.iot.config.Config;
import com.winsun.iot.mqtt.MqttConfig;
import com.winsun.iot.mqtt.MqttServer;

public class DeviceConnManager {
    private MqttServer mqttServer;

    private Config config;

    public DeviceConnManager(Config config) {
        this.config=config;
    }

    public void init() {
        this.mqttServer = new MqttServer(new MqttConfig(config.MqttBroker(),config.MqttUserName(),config.MqttPassword()));
    }

    public void start(){
        this.mqttServer.start();
    }
}
