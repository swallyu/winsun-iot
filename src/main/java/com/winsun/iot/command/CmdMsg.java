package com.winsun.iot.command;

public class CmdMsg {

    private String topic;
    private String data;
    private EnumQoS qos;

    private String gatewayId;

    public CmdMsg(String topic, String data, EnumQoS qos, String gatewayId) {
        this.topic = topic;
        this.data = data;
        this.qos = qos;
        this.gatewayId = gatewayId;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public String getTopic() {
        return topic;
    }

    public String getData() {
        return data;
    }

    public EnumQoS getQos() {
        return qos;
    }
}
