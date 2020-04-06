package com.winsun.iot.command;

import java.util.concurrent.CompletableFuture;

public class CmdMsg {

    private String topic;
    private String data;
    private EnumQoS qos;

    private String gatewayId;

    private CompletableFuture<CmdMsg> receiveFuture;
    private String bizId;
    private String actionType;

    public CmdMsg(String topic, String data, EnumQoS qos) {
        this.topic = topic;
        this.data = data;
        this.qos = qos;
    }

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

    public CompletableFuture<CmdMsg> getReceiveFuture() {
        return receiveFuture;
    }

    public void setReceiveFuture(CompletableFuture<CmdMsg> receiveFuture) {
        this.receiveFuture = receiveFuture;
    }

    public void setBizId(String bizId) {
        this.bizId = bizId;
    }

    public String getBizId() {
        return bizId;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getActionType() {
        return actionType;
    }
}
