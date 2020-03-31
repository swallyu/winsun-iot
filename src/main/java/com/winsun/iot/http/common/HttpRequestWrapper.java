package com.winsun.iot.http.common;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HttpRequestWrapper {
    private static final Logger logger = LoggerFactory.getLogger(HttpRequestWrapper.class);
    private HttpRequest request;

    private com.winsun.iot.http.common.HttpMethod method;

    private Map<String,String> params = new HashMap<>();
    private String body;

    public static HttpRequestWrapper parse(HttpRequest request) {
        HttpRequestWrapper wrapper = new HttpRequestWrapper();

        wrapper.request = request;
        FullHttpRequest fullHttpRequest = (FullHttpRequest)request;
        QueryStringDecoder decoderQuery = new QueryStringDecoder(request.uri());
        Map<String, List<String>> uriAttributes = decoderQuery.parameters();
        for (Map.Entry<String, List<String>> attr : uriAttributes.entrySet()) {
            wrapper.params.put(attr.getKey(),String.join(",",attr.getValue()));
        }
        if(Objects.equals(request.method(), HttpMethod.GET)){
            wrapper.method = com.winsun.iot.http.common.HttpMethod.Get;
        }else if(Objects.equals(request.method(), HttpMethod.POST)){
            wrapper.method = com.winsun.iot.http.common.HttpMethod.Post;
            ByteBuf byteBuf = fullHttpRequest.content();
            String content = byteBuf.toString(CharsetUtil.UTF_8);
            wrapper.body = content;
        }else{
            logger.error("the request method [{}] is not supported ",request.method().name());
            return null;
        }
        return wrapper;
    }

    public com.winsun.iot.http.common.HttpMethod getMethod() {
        return method;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public String getBody() {
        return body;
    }

    public String getUri(){
        return request.uri();
    }
}
