package com.winsun.iot.http;

import com.winsun.iot.http.handler.ErrorController;
import com.winsun.iot.http.handler.HttpController;
import com.winsun.iot.http.handler.HttpHandlerFactory;
import com.winsun.iot.iocmodule.Ioc;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

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
