package com.winsun.iot.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.io.UnsupportedEncodingException;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;

public class HttpResponse {
    private ChannelHandlerContext ctx;

    private int statusCode;

    public HttpResponse(ChannelHandlerContext ctx) {
        this.ctx=ctx;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void write(String msg) {
        byte[] bs= null;
        try {
            bs = msg.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        ByteBuf buf = Unpooled.wrappedBuffer(bs);


        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.valueOf(statusCode),
                buf);
        response.headers().set(CONTENT_TYPE, "application/json");//text/plain
        response.headers().setInt(CONTENT_LENGTH, buf.readableBytes());
        response.headers().set("Access-Control-Allow-Origin","*"); // 跨域
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
