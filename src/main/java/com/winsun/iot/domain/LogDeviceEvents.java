package com.winsun.iot.domain;

import java.time.LocalDateTime;
import java.util.Date;

public class LogDeviceEvents {
    /**
    * 
    */
    private Long id;

    /**
    * 
    */
    private LocalDateTime time;

    /**
    * 
    */
    private String baseId;

    /**
    * 
    */
    private String eventName;

    /**
    * 
    */
    private String value;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public String getBaseId() {
        return baseId;
    }

    public void setBaseId(String baseId) {
        this.baseId = baseId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}