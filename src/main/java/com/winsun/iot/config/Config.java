package com.winsun.iot.config;

import com.winsun.iot.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

public class Config {

    private static final Logger logger = LoggerFactory.getLogger(Config.class);
    private Properties prop;

    public static String getMySqlLoadFilePath() {
        return FileUtils.getProjectPath()+"/batch-data";
    }

    public void load(Properties propConfig) {
        prop = propConfig;
    }

    public void load(String propConfig) {
        prop = new Properties();
        try {
            prop.load(new StringReader(propConfig));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public int getHttpServerPort() {
        return getInt("http.server.port", "2323");
    }

    public String MqttBroker() {
        return getString("mqtt.broker", "");
    }

    public String MqttUserName() {
        return getString("mqtt.username", "");
    }

    public String MqttPassword() {
        return getString("mqtt.password", "");
    }

    public String MqttApiHost(){
        return getString("mqtt.api_host", "");
    }
    public String MqttApiUser(){
        return getString("mqtt.dashboard_user", "");
    }
    public String MqttApiPwd(){
        return getString("mqtt.dashboard_passwd", "");
    }

    private int getInt(String key, String defaultValue) {
        String value = prop.getProperty(key, defaultValue);
        return Integer.valueOf(value);
    }

    private String getString(String key, String defaultValue) {
        String value = prop.getProperty(key, defaultValue);
        return value;
    }
}
