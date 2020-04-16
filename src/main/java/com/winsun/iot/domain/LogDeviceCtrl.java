package com.winsun.iot.domain;

import java.time.LocalDateTime;
import java.util.Date;

public class LogDeviceCtrl {
    /**
    * 
    */
    private Integer id;

    /**
    * 设备信息
    */
    private String baseId;

    /**
    * 指令信息
    */
    private String cmdType;

    private String msgType;

    /**
    * 指令签名
    */
    private String sig;

    /**
    * 指令信息
    */
    private String cmdMsg;

    /**
    * 
    */
    private Integer execStep;

    /**
    * 
    */
    private Integer qos;

    /**
    * 
    */
    private boolean complete;

    /**
    * 
    */
    private Boolean result;

    /**
    * 
    */
    private LocalDateTime createTime;

    /**
    * 
    */
    private LocalDateTime updateTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBaseId() {
        return baseId;
    }

    public void setBaseId(String baseId) {
        this.baseId = baseId;
    }

    public String getCmdType() {
        return cmdType;
    }

    public void setCmdType(String cmdType) {
        this.cmdType = cmdType;
    }

    public String getSig() {
        return sig;
    }

    public void setSig(String sig) {
        this.sig = sig;
    }

    public String getCmdMsg() {
        return cmdMsg;
    }

    public void setCmdMsg(String cmdMsg) {
        this.cmdMsg = cmdMsg;
    }

    public Integer getExecStep() {
        return execStep;
    }

    public void setExecStep(Integer execStep) {
        this.execStep = execStep;
    }

    public Integer getQos() {
        return qos;
    }

    public void setQos(Integer qos) {
        this.qos = qos;
    }

    public boolean getComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public Boolean getResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }
}