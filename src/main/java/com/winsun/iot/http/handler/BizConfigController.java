package com.winsun.iot.http.handler;

import com.alibaba.fastjson.JSONObject;
import com.google.inject.Inject;
import com.winsun.iot.biz.service.BizService;
import com.winsun.iot.command.CmdCallback;
import com.winsun.iot.command.CmdMsg;
import com.winsun.iot.command.EnumQoS;
import com.winsun.iot.device.DeviceManager;
import com.winsun.iot.domain.CmdResult;
import com.winsun.iot.http.common.*;
import com.winsun.iot.ruleengine.CmdRule;

/**
 * 业务闭环
 */
@HttpMap(value = "/biz/config",method = HttpMethod.Post)
public class BizConfigController implements HttpController {

    private static final String topic="/E2ES/GateWay/config";
    private static final String msgType="config";
    @Inject
    private DeviceManager dm;
    @Inject
    private BizService bizService;

    @Override
    public void execute(HttpRequestWrapper request, HttpResponse resp) {

        JSONObject obj = request.getBodyAsJson();
        String baseId = obj.getString("baseId");
        JSONObject cmdObj = obj.getJSONObject("detail");

        CmdResult<String> result = dm.invokeCmd(topic, EnumQoS.AtleastOnce,msgType,baseId,cmdObj,new InnerCmdCallback());
        bizService.startBiz(result.getData());
        resp.write(result);
    }

    private class InnerCmdCallback implements CmdCallback{

        @Override
        public void complete(String bizId, CmdRule cmdMsg) {

        }
    }
}
