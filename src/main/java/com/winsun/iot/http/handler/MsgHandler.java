package com.winsun.iot.http.handler;

import com.winsun.iot.http.common.HttpMap;
import com.winsun.iot.http.common.HttpRequestWrapper;
import com.winsun.iot.http.common.HttpResponse;

@HttpMap("/test")
public class MsgHandler implements HttpController {
    @Override
    public void execute(HttpRequestWrapper request, HttpResponse resp) {

        resp.setStatusCode(200);
        resp.write("sssss");
    }
}
