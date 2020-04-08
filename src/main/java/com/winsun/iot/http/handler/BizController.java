package com.winsun.iot.http.handler;

import com.alibaba.fastjson.JSONObject;
import com.google.inject.Inject;
import com.winsun.iot.command.EnumQoS;
import com.winsun.iot.device.DeviceManager;
import com.winsun.iot.domain.CmdResult;
import com.winsun.iot.http.common.HttpMap;
import com.winsun.iot.http.common.HttpRequestWrapper;
import com.winsun.iot.http.common.HttpResponse;

/**
 * 业务闭环
 */
@HttpMap("/biz/control")
public class BizController implements HttpController {

    private static final String topic="/E2ES/GateWay/Control";
    private static final String msgType="control";
    @Inject
    private DeviceManager dm;

    @Override
    public void execute(HttpRequestWrapper request, HttpResponse resp) {

        JSONObject obj = request.getBodyAsJson();
        String baseId = obj.getString("baseId");
        JSONObject cmdObj = obj.getJSONObject("detail");

        CmdResult<Object> result = dm.invokeCmd(topic, EnumQoS.AtleastOnce,msgType,baseId,cmdObj);

        resp.write(result);
    }
}
