package com.winsun.iot.http.handler;

import com.winsun.iot.http.HttpRequestWrapper;
import com.winsun.iot.http.HttpResponse;

public interface HttpController {
    void execute(HttpRequestWrapper request, HttpResponse resp);
}
