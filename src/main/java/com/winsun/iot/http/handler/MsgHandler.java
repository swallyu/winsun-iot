package com.winsun.iot.http.handler;

import com.winsun.iot.http.HttpMap;
import com.winsun.iot.http.HttpRequestWrapper;
import com.winsun.iot.http.HttpResponse;

@HttpMap("/test")
public class MsgHandler implements HttpController {
    @Override
    public void execute(HttpRequestWrapper request, HttpResponse resp) {

        resp.setStatusCode(200);
        resp.write("sssss");
    }
}
