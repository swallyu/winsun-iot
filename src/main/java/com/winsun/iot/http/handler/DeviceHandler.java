package com.winsun.iot.http.handler;

import com.winsun.iot.http.common.*;

@HttpMap(value = "/device/list",method = HttpMethod.Post)
public class DeviceHandler implements HttpController {
    @Override
    public void execute(HttpRequestWrapper request, HttpResponse resp) {

    }
}
