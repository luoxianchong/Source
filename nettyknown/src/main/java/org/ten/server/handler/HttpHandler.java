package org.ten.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import org.ten.server.http.Request;
import org.ten.server.http.Response;
import org.ten.server.servlets.HttpServlet;

/**
 * Created by ing on 2019-03-24.
 */
public class HttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    protected void channelRead0(ChannelHandlerContext context, FullHttpRequest request) throws Exception {
        Request req=new Request(context,  request);
        Response res=new Response(context, request);
        HttpServlet.class.newInstance().doGet(req,res);
    }
}
