package com.winsun.facemask.http;

import com.alibaba.fastjson.JSONObject;
import com.google.inject.Inject;
import com.winsun.facemask.service.FaceMaskService;
import com.winsun.iot.domain.CmdResult;
import com.winsun.iot.http.common.*;

/**
 * 业务闭环
 */
@HttpMap(value = "/biz/facemask/update-qrcode",method = HttpMethod.Post)
public class BizUpdateQrCodeController implements HttpController {


    @Inject
    private FaceMaskService faceMaskService;

    @Override
    public void execute(HttpRequestWrapper request, HttpResponse resp) {

        JSONObject obj = request.getBodyAsJson();
        String token=obj.getString("token");
        String url=obj.getString("url");

        CmdResult<String> result = faceMaskService.updateQrCode(token,url);
        resp.write(result);
    }
}
