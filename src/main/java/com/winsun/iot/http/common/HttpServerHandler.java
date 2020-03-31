package com.winsun.iot.http.common;

import com.winsun.iot.http.handler.ErrorController;
import com.winsun.iot.http.handler.HttpController;
import com.winsun.iot.http.handler.HttpHandlerFactory;
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
        HttpController controller = Ioc.getInjector().getInstance(HttpHandlerFactory.class).match(wrapper.getUri());
        if(controller!=null){

        }else{
            controller = new ErrorController(404);
        }
        controller.execute(wrapper,response);
    }
}
