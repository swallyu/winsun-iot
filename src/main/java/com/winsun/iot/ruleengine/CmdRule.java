package com.winsun.iot.ruleengine;

import com.winsun.iot.command.CmdMsg;
import com.winsun.iot.command.EnumQoS;
import com.winsun.iot.device.DeviceConnManager;

import java.util.ArrayList;
import java.util.List;

public class CmdRule {

    //指令闭环实现
    private EnumQoS cmdQos;

    private DeviceConnManager connManager;

    private CmdMsg cmdMsg;

    private List<CmdRuleInfo> cmdMsgList = new ArrayList<>();

    public CmdRule(CmdMsg cmdMsg) {
        this.cmdMsg = cmdMsg;
    }

    public void execute(){

        connManager.sendCmd(cmdMsg);
    }

    public static class CmdRuleInfo {
        private CmdMsg cmdMsg;
        //业务标识,在进行指令往返时需要使用此标识标记一次业务
        private String bizId;

        private EnumCmdStatus status;

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
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
}
