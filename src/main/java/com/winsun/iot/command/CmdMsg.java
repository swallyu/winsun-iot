package com.winsun.iot.command;

public class CmdMsg {

    private String topic;
    private String data;
    private EnumQoS qos;

    public CmdMsg(String topic, String data, EnumQoS qos) {
        this.topic = topic;
        this.data = data;
        this.qos = qos;
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
