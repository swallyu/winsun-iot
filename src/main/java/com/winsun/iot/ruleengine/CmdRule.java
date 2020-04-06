package com.winsun.iot.ruleengine;

import com.winsun.iot.command.CmdMsg;
import com.winsun.iot.command.CmdRuleInfo;
import com.winsun.iot.command.EnumQoS;
import com.winsun.iot.device.DeviceConnManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CmdRule {
    private static final Logger logger = LoggerFactory.getLogger(CmdRule.class);

    public static final String ACTION_TYPE_SEND = "send";
    public static final String ACTION_TYPE_COMPLETE = "complete";
    public static final String ACTION_TYPE_FAIL = "fail";
    public static final String ACTION_TYPE_ACK = "ack";
    public static final String ACTION_TYPE_ACK_CONFIRM = "ack_confirm";

    //指令闭环实现
    private EnumQoS cmdQos;

    private DeviceConnManager connManager;

    private CmdMsg cmdMsg;

    private boolean iscomplete;

    private List<CmdRuleInfo> cmdMsgList = new ArrayList<>();

    private EnumCmdStatus cmdStatus;

    public CmdRule(CmdMsg cmdMsg) {
        this.cmdMsg = cmdMsg;
    }

    public CmdRuleInfo processCmdMsg(CmdRuleInfo cmdMsg) {
        this.cmdMsgList.add(cmdMsg);
        switch (cmdMsg.getCmdMsg().getActionType()) {
            case ACTION_TYPE_SEND:
                this.cmdStatus = EnumCmdStatus.Send;
                break;
            case ACTION_TYPE_COMPLETE:
                this.iscomplete = true;
                this.cmdStatus = EnumCmdStatus.Complete;
                break;
            case ACTION_TYPE_FAIL:
                this.cmdStatus = EnumCmdStatus.Fail;
                break;
            case ACTION_TYPE_ACK:
                //接收到设备的ACK指令后，需要回复指令确认
                this.cmdStatus = EnumCmdStatus.Ack;
                //生成确认指令
                CmdRuleInfo ri = cmdMsg.clone(EnumCmdStatus.AckConfirm, LocalDateTime.now());
                return ri;
            case ACTION_TYPE_ACK_CONFIRM:
                this.cmdStatus = EnumCmdStatus.AckConfirm;
                break;
        }
        return null;
    }

    public boolean isComplete() {
        return this.cmdStatus == EnumCmdStatus.Complete || this.cmdStatus == EnumCmdStatus.Fail;
    }

    /**
     * 指令闭环完成时执行
     */
    public boolean done() {
        if(iscomplete){
            return false;
        }
        logger.info("the biz command is complete {}", this.cmdMsg.getBizId());
        return true;
    }

    private CmdRuleInfo getLastMsg() {
        return this.cmdMsgList.size() > 0 ? this.cmdMsgList.get(cmdMsgList.size() - 1) : null;
    }
}
