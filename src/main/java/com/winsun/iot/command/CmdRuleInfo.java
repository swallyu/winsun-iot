package com.winsun.iot.command;

import com.winsun.iot.ruleengine.EnumCmdStatus;

public class CmdRuleInfo {
    private CmdMsg cmdMsg;
    //业务标识,在进行指令往返时需要使用此标识标记一次业务
    private String bizId;

    private EnumCmdStatus status;

    public CmdRuleInfo() {
    }

    public CmdRuleInfo(CmdMsg cmdMsg) {
        this.cmdMsg = cmdMsg;
        this.bizId = cmdMsg.getBizId();
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
}
