package com.winsun.iot.http.common;

import com.alibaba.fastjson.JSONObject;
import com.winsun.iot.iocmodule.Ioc;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {
    private static Logger logger = LoggerFactory.getLogger(HttpServerHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        HttpRequestWrapper wrapper = HttpRequestWrapper.parse((HttpRequest) msg);
        HttpResponse response = new HttpResponse(ctx);
        HttpController controller = Ioc.getInjector().getInstance(HttpHandlerFactory.class).match(wrapper.getUri(), wrapper.getMethod());
        if (controller == null) {
            controller = new ErrorController(404,"Page Not Found");

        }
        try {
            controller.execute(wrapper, response);
        } catch (Exception exc) {
            logger.error("execute invoke fail {} ,{}", wrapper.getUri(), controller.getClass().getName());
            logger.error(exc.getMessage(), exc);

            JSONObject data = new JSONObject();
            data.put("code", -1);
            data.put("msg", exc.getMessage());
            response.setStatusCode(500);
            response.write(data);

        }

    }
}
