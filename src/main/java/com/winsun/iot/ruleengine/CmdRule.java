package com.winsun.iot.ruleengine;

import com.alibaba.fastjson.JSONObject;
import com.winsun.iot.command.CmdFactory;
import com.winsun.iot.command.CmdMsg;
import com.winsun.iot.command.CmdRuleInfo;
import com.winsun.iot.command.EnumQoS;
import com.winsun.iot.device.DeviceConnManager;
import com.winsun.iot.domain.CmdResult;
import com.winsun.iot.utils.PathUtil;
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
        cmdStatus = cmdMsg.getStatus();
        this.cmdMsg = cmdMsg;
        this.cmdMsgList.add(new CmdRuleInfo(cmdMsg));
    }

    public CmdResult<CmdRuleInfo> processCmdMsg(CmdRuleInfo cmdMsg) {
        if (this.cmdMsgList.size() == 0) {
            //默认第一条数据
            this.cmdMsg = cmdMsg.getCmdMsg();
            cmdStatus = cmdMsg.getCmdMsg().getStatus();
        }
        this.cmdMsgList.add(cmdMsg);

        int stage = cmdMsg.getCmdMsg().getStatus().getCode();
        if(stage<=this.cmdStatus.getCode()){
            return new CmdResult<>(-1, false, "重复的stage", null);
        }
        cmdStatus = cmdMsg.getCmdMsg().getStatus();

        int newStage = stage + 1;
        if (newStage > this.cmdMsg.getQos().getCode()) {
            return new CmdResult<>(-1, false, "无效的指令信息", null);
        }
        String topic = this.cmdMsg.getTopic();
        String gatewayId = PathUtil.getPathLast(topic);
        JSONObject resp = CmdFactory.buildBizCmdResp(cmdMsg.getBizId(), EnumCmdStatus.parseOf(newStage), true);
        CmdMsg msg = new CmdMsg(topic,
                resp, EnumQoS.ExtractOnce);
        msg.setBizId(cmdMsg.getBizId());
        msg.setGatewayId(gatewayId);

        this.cmdMsgList.add(cmdMsg);
        cmdStatus = EnumCmdStatus.parseOf(newStage);
        return new CmdResult<>(0, true, null, new CmdRuleInfo(msg));
    }

    public boolean isComplete() {
        switch (cmdMsg.getQos()) {
            case Once:
                return this.cmdStatus == EnumCmdStatus.Stage_0;
            case AtleastOnce:
                return this.cmdStatus == EnumCmdStatus.Stage_1;
            case ExtractOnce:
                return this.cmdStatus == EnumCmdStatus.Stage_2;
        }
        return false;
    }

    /**
     * 指令闭环完成时执行
     */
    public boolean done() {
        if (iscomplete) {
            return false;
        }
        logger.info("the biz command is complete {}", this.cmdMsg.getBizId());
        return true;
    }

    private CmdRuleInfo getLastMsg() {
        return this.cmdMsgList.size() > 0 ? this.cmdMsgList.get(cmdMsgList.size() - 1) : null;
    }
}
