package com.winsun.iot.command.biz;

import com.alibaba.fastjson.JSONObject;
import com.google.inject.Inject;
import com.winsun.iot.command.CmdCallback;
import com.winsun.iot.command.CmdMsg;
import com.winsun.iot.command.CmdRuleInfo;
import com.winsun.iot.device.DeviceManager;
import com.winsun.iot.domain.CmdResult;
import com.winsun.iot.ruleengine.CmdRule;
import com.winsun.iot.ruleengine.EnumCmdStatus;
import com.winsun.iot.utils.MsgConsumer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 指令签名处理
 */
public class BizCmdHandler {
    private Map<String, CmdRule> cmdRuleInfoMap = new HashMap<>();

    private MsgConsumer consumer = new InnerMsgConsumer();

    @Inject
    private DeviceManager dm;

    public BizCmdHandler() {
    }

    public CmdRule addCmdRule(CmdRuleInfo cmdMsg, CmdCallback callback) {
        return cmdRuleInfoMap.computeIfAbsent(cmdMsg.getBizId(), k -> new CmdRule(cmdMsg.getCmdMsg(),callback));
    }

    public MsgConsumer getConsumer() {
        return consumer;
    }

    private class InnerMsgConsumer implements MsgConsumer {
        @Override
        public void after(CmdMsg cmdMsg) {
            String bizId = cmdMsg.getBizId();
            if (bizId == null) {
                return;
            }
            CmdRule cmdRule = cmdRuleInfoMap.get(bizId);
            if (cmdRule != null) {
                CmdResult<CmdRuleInfo> ret = cmdRule.processCmdMsg(new CmdRuleInfo(cmdMsg));
                if(ret.isResult()){
                    if (ret.getData() != null) {
                        boolean send = dm.invokeCmd(ret.getData(),null);
                        if (send && cmdRule.isComplete()) {
                            cmdRule.done();
                            cmdRuleInfoMap.remove(bizId);
                        }
                    } else if (cmdRule.isComplete()) {
                        cmdRule.done();
                        cmdRuleInfoMap.remove(bizId);
                    }
                }
            }
        }

        @Override
        public void before(CmdMsg cmdMsg) {
            JSONObject data = cmdMsg.getData();
            String signature = data.getString("sig");
            if (signature != null) {
                int stage = data.getInteger("stage");
                cmdMsg.setStatus(EnumCmdStatus.parseOf(stage));
                cmdMsg.setBizId(signature);
            }
        }
    }

}
