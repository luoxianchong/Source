package org.ten.server.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.List;
import java.util.Map;

/**
 * Created by ing on 2019-03-24.
 */
public class Request {
    private ChannelHandlerContext ctx;
    private HttpRequest request;

    public Request(ChannelHandlerContext ctx, HttpRequest requset) {
        this.ctx=ctx;
        this.request=requset;
    }

    public String getURI(){
        return request.uri();
    }

    public String getMethod(){
        return request.method().name();
    }

    public Map<String,List<String>> getParameters(){
        QueryStringDecoder decoder=new QueryStringDecoder(request.uri());
        return decoder.parameters();
    }

    public String getParameter(String name){
        Map<String,List<String>> param=getParameters();
        List<String> value=param.get(name);

        return value==null?null:value.get(0);
    }
}
