package org.ten.server.http;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.internal.StringUtil;

import java.io.UnsupportedEncodingException;

/**
 * Created by ing on 2019-03-24.
 */
public class Response {

    private ChannelHandlerContext ctx;
    private HttpRequest request;
    public Response(ChannelHandlerContext ctx, HttpRequest requset) {
        this.ctx=ctx;
        this.request=requset;
    }


    public void write(String responseContent) throws UnsupportedEncodingException {
        if(StringUtil.isNullOrEmpty(responseContent)){return ;}
        try {
            FullHttpResponse response =
                    new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                            HttpResponseStatus.OK,
                            Unpooled.wrappedBuffer(responseContent.getBytes("utf-8"))
                    );
            HttpHeaders headers = response.headers();
            headers.set(HttpHeaderNames.CONTENT_TYPE, "application/json");
            headers.set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            headers.set(HttpHeaderNames.EXPIRES, 0);
            if (HttpUtil.isKeepAlive(request)) {
                headers.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            }
            ctx.write(response);
        }finally {
            ctx.flush();
        }
    }

}
