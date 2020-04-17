package com.winsun.iot.command.biz;

import com.alibaba.fastjson.JSONObject;
import com.google.inject.Inject;
import com.winsun.iot.biz.service.BizService;
import com.winsun.iot.command.CmdCallback;
import com.winsun.iot.command.CmdMsg;
import com.winsun.iot.command.CmdRuleInfo;
import com.winsun.iot.command.EnumQoS;
import com.winsun.iot.device.DeviceManager;
import com.winsun.iot.domain.CmdResult;
import com.winsun.iot.ruleengine.CmdRule;
import com.winsun.iot.ruleengine.EnumCmdStatus;
import com.winsun.iot.utils.MsgConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

/**
 * 指令签名处理
 */
public class BizCmdHandler {
    private Map<String, CmdRule> cmdRuleInfoMap = new HashMap<>();

    private MsgConsumer consumer = new InnerMsgConsumer();

    private ScheduledExecutorService executorService;

    private static final Logger logger = LoggerFactory.getLogger(BizCmdHandler.class);
    @Inject
    private DeviceManager dm;

    @Inject
    private BizService bizService;

    public BizCmdHandler() {
        executorService = Executors.newScheduledThreadPool(2, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("check-bizcmd-timeout");
                return t;
            }
        });
        executorService.scheduleAtFixedRate(new CmdTimeOutChecker(), 0, 1, TimeUnit.SECONDS);
    }

    public CmdRule addCmdRule(CmdRuleInfo cmdMsg, CmdCallback callback) {
        return cmdRuleInfoMap.computeIfAbsent(cmdMsg.getBizId(), k -> new CmdRule(cmdMsg.getCmdMsg(), callback));
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
                if (ret.isResult()) {
                    if (ret.getData() != null) {
                        boolean send = dm.invokeCmd(ret.getData(), null);
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

                if (data.containsKey("qos")) {
                    int qos = data.getInteger("qos");
                    cmdMsg.setQos(EnumQoS.valueOf(qos));
                }
            }
        }
    }

    private class CmdTimeOutChecker implements Runnable {

        @Override
        public void run() {
            LocalDateTime checkTime = LocalDateTime.now();

            Set<String> bizIdSet = new HashSet<>();
            bizIdSet.addAll(cmdRuleInfoMap.keySet());
            for (String bizId : bizIdSet) {
                CmdRule value = cmdRuleInfoMap.get(bizId);
                Duration d = Duration.between(value.getLastUpdateTime(), checkTime);

                if (value.getTimeOut() > 0
                        && d.toMillis() / 1000 > value.getTimeOut()
                        && !value.isComplete()) {

                    CmdMsg timeOutmsg = value.getNeedResendMsg();
                    if (timeOutmsg != null) {
                        cmdRuleInfoMap.remove(bizId);
                        cmdRuleInfoMap.put(value.getBizId(), value);
                        value.updateResendTimes(LocalDateTime.now());
                        bizService.updateBizInfo(bizId,timeOutmsg.getData().toJSONString(),value.getBizId());
                        dm.invokeCmd(new CmdRuleInfo(timeOutmsg), null);
                    }
                }
            }
        }
    }
}
