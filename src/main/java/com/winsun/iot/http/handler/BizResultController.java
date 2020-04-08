package com.winsun.iot.http.handler;

import com.alibaba.fastjson.JSONObject;
import com.google.inject.Inject;
import com.winsun.iot.biz.domain.BizInfo;
import com.winsun.iot.biz.service.BizService;
import com.winsun.iot.http.common.*;

@HttpMap(value = "/biz/query",method = HttpMethod.Post)
public class BizResultController implements HttpController {

    @Inject
    private BizService bizService;

    @Override
    public void execute(HttpRequestWrapper request, HttpResponse resp) {
        JSONObject jo = request.getBodyAsJson();

        String bizId = jo.getString("bizId");

        BizInfo info = bizService.getById(bizId);

        resp.write(0,"",info);
    }
}
