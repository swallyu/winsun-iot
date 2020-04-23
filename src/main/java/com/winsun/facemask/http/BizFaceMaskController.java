package com.winsun.facemask.http;

import com.alibaba.fastjson.JSONObject;
import com.google.inject.Inject;
import com.winsun.iot.biz.domain.SellInfo;
import com.winsun.facemask.service.FaceMaskService;
import com.winsun.iot.domain.CmdResult;
import com.winsun.iot.http.common.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 业务闭环
 */
@HttpMap(value = "/biz/facemask/sell",method = HttpMethod.Post)
public class BizFaceMaskController implements HttpController {

    private static final Logger logger = LoggerFactory.getLogger(BizFaceMaskController.class);

    @Inject
    private FaceMaskService faceMaskService;

    @Override
    public void execute(HttpRequestWrapper request, HttpResponse resp) {

        logger.info("receive sell cmd :{}",request.getBody());

        JSONObject obj = request.getBodyAsJson();
        String token = obj.getString("sellToken");
        JSONObject qrcode = obj.getJSONObject("qrcode");
        String qrCodeToken = qrcode.getString("token");
        String qrCodeUrl = qrcode.getString("url");

        SellInfo info = new SellInfo(token, qrCodeToken, qrCodeUrl);
        String[] data = token.split(",");
        if(data.length<2){
            CmdResult<String> msg=new CmdResult<String>(-1,false,"请求数据错误");
            resp.write(msg);
            return;
        }
        CmdResult<String> result = faceMaskService.sellFaceMak(info);
        resp.write(result);
    }

}
