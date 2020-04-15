package com.winsun.iot.http.handler.facemask;

import com.alibaba.fastjson.JSONObject;
import com.google.inject.Inject;
import com.winsun.iot.biz.service.BizService;
import com.winsun.iot.biz.service.FaceMaskService;
import com.winsun.iot.command.CmdCallback;
import com.winsun.iot.command.EnumQoS;
import com.winsun.iot.device.DeviceManager;
import com.winsun.iot.domain.CmdResult;
import com.winsun.iot.http.common.*;
import com.winsun.iot.ruleengine.CmdRule;

/**
 * 业务闭环
 */
@HttpMap(value = "/biz/facemask/sell",method = HttpMethod.Post)
public class BizFaceMaskController implements HttpController {


    @Inject
    private FaceMaskService faceMaskService;

    @Override
    public void execute(HttpRequestWrapper request, HttpResponse resp) {

        JSONObject obj = request.getBodyAsJson();
        String token = obj.getString("token");

        String[] data = token.split(",");
        if(data.length<2){
            CmdResult<String> msg=new CmdResult<String>(-1,false,"请求数据错误");
            resp.write(msg);
            return;
        }
        CmdResult<String> result = faceMaskService.sellFaceMak(data[0],data[1]);
        resp.write(result);
    }

}
