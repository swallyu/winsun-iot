package com.winsun.iot.mqtt;

public class MqttConfig {
    private String broker;
    private String username;
    private String password;

    public MqttConfig(String broker, String username, String password) {
        this.broker = broker;
        this.username = username;
        this.password = password;
    }

    public String getBroker() {
        return broker;
    }

    public void setBroker(String broker) {
        this.broker = broker;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
