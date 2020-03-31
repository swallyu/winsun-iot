package com.winsun.iot.http.handler;

import com.winsun.iot.http.common.HttpRequestWrapper;
import com.winsun.iot.http.common.HttpResponse;

public interface HttpController {
    void execute(HttpRequestWrapper request, HttpResponse resp);
}
