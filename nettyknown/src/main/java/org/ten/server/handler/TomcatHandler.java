package org.ten.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpRequest;
import org.ten.server.http.Request;
import org.ten.server.http.Response;
import org.ten.server.servlets.HttpServlet;

/**
 * Created by ing on 2019-03-24.
 */
public class TomcatHandler  extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if(msg instanceof HttpRequest){
            HttpRequest req=(HttpRequest) msg;

            Request request=new Request(ctx,  req);
            Response response=new Response(ctx,  req);

            HttpServlet.class.newInstance().doGet(request,response);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception{

    }
}
