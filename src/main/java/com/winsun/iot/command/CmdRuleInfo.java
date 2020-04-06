package com.winsun.iot.command;

import com.winsun.iot.ruleengine.EnumCmdStatus;

import java.time.LocalDateTime;

public class CmdRuleInfo {
    private CmdMsg cmdMsg;
    //业务标识,在进行指令往返时需要使用此标识标记一次业务
    private String bizId;

    private LocalDateTime time;

    private EnumCmdStatus status;

    public CmdRuleInfo() {
    }

    public CmdRuleInfo(CmdMsg cmdMsg) {
        this.cmdMsg = cmdMsg;
        this.bizId = cmdMsg.getBizId();
        this.time = LocalDateTime.now();
    }

    public CmdMsg getCmdMsg() {
        return cmdMsg;
    }

    public void setCmdMsg(CmdMsg cmdMsg) {
        this.cmdMsg = cmdMsg;
    }

    public String getBizId() {
        return bizId;
    }

    public void setBizId(String bizId) {
        this.bizId = bizId;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public CmdRuleInfo clone(EnumCmdStatus status,LocalDateTime dateTime) {
        CmdRuleInfo info = new CmdRuleInfo();
        info.bizId = this.bizId;
        info.status = status;
        CmdMsg cmdMsg = new CmdMsg(this.cmdMsg.getTopic(), this.cmdMsg.getData(),
                this.cmdMsg.getQos(), this.cmdMsg.getGatewayId());
        info.setCmdMsg(cmdMsg);
        info.time = dateTime;
        return info;
    }
}
