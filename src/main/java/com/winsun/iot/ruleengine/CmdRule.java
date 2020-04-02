package com.winsun.iot.ruleengine;

import com.winsun.iot.command.CmdMsg;
import com.winsun.iot.command.CmdRuleInfo;
import com.winsun.iot.command.EnumQoS;
import com.winsun.iot.device.DeviceConnManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CmdRule {
    private static final Logger logger = LoggerFactory.getLogger(CmdRule.class);
    //指令闭环实现
    private EnumQoS cmdQos;

    private DeviceConnManager connManager;

    private CmdMsg cmdMsg;

    private boolean iscomplete;

    private List<CmdRuleInfo> cmdMsgList = new ArrayList<>();

    public CmdRule(CmdMsg cmdMsg) {
        this.cmdMsg = cmdMsg;
    }


    public void addCmdMsg(CmdRuleInfo cmdMsg) {
        this.cmdMsgList.add(cmdMsg);
    }

    public boolean isComplete() {
        return iscomplete;
    }

    /**
     * 指令闭环完成时执行
     */
    public void done() {
        logger.info("the biz command is complete {}",this.cmdMsg.getBizId());
    }
}
