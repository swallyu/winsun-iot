package com.winsun.iot.command;

public class CommandHandler {
    private String topic;
    private CmdHandler handler;
    private EnumQoS qos;

    public CommandHandler(String topic, EnumQoS qos, CmdHandler handler) {
        this.topic = topic;
        this.handler = handler;
        this.qos = qos;
    }

    public String getTopic() {
        return topic;
    }

    public CmdHandler getHandler() {
        return handler;
    }

    public EnumQoS getQos() {
        return qos;
    }
}
