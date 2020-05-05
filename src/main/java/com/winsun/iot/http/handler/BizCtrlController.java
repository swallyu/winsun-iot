package com.winsun.iot.http.handler;

import com.alibaba.fastjson.JSONObject;
import com.google.inject.Inject;
import com.winsun.iot.biz.domain.BizInfo;
import com.winsun.iot.biz.service.BizService;
import com.winsun.iot.command.CmdCallback;
import com.winsun.iot.command.EnumQoS;
import com.winsun.iot.device.DeviceManager;
import com.winsun.iot.domain.CmdResult;
import com.winsun.iot.http.common.*;
import com.winsun.iot.ruleengine.CmdRule;

/**
 * 业务闭环
 */
@HttpMap(value = "/biz/control",method = HttpMethod.Post)
public class BizCtrlController implements HttpController {

    private static final String topic="/E2ES/GateWay/Control";
    private static final String msgType="control";
    @Inject
    private DeviceManager dm;

    @Inject
    private BizService bizService;

    @Override
    public void execute(HttpRequestWrapper request, HttpResponse resp) {

        JSONObject obj = request.getBodyAsJson();
        String baseId = obj.getString("baseId");
        JSONObject cmdObj = obj.getJSONObject("detail");

        CmdResult<BizInfo> result = dm.invokeCmd(topic, EnumQoS.ExtractOnce,msgType,baseId,cmdObj,new InnerCmdCallback(), 0, false, false);
        bizService.startBiz(result.getData().getBizId(), 0, baseId, obj.toString(), msgType, "control", EnumQoS.AtleastOnce.getCode());

        resp.write(result);
    }

    private class InnerCmdCallback implements CmdCallback {

        @Override
        public void complete(String bizId, CmdRule cmdMsg) {
            bizService.complete(bizId,cmdMsg);
        }
    }
}
