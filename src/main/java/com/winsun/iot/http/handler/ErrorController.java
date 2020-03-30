package com.winsun.iot.http.handler;

import com.winsun.iot.http.HttpRequestWrapper;
import com.winsun.iot.http.HttpResponse;

public class ErrorController implements HttpController {
    private int statusCode;
    private String msg;
    public ErrorController(int statusCode) {
        this.statusCode = statusCode;
        this.msg="";
    }

    public ErrorController(int statusCode, String msg) {
        this.statusCode = statusCode;
        this.msg = msg;
    }

    @Override
    public void execute(HttpRequestWrapper request, HttpResponse resp) {
        resp.setStatusCode(this.statusCode);
        resp.write(msg);
    }
}
