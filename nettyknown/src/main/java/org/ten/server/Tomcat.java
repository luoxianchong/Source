package org.ten.server;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.ten.server.handler.TomcatHandler;

/**
 * 使用netty实现一个tomcat
 */
public class Tomcat {

    public void start(int port) throws InterruptedException {
        //主线程
        EventLoopGroup mainGroup=new NioEventLoopGroup();
        //从线上
        EventLoopGroup workerGroup=new NioEventLoopGroup();

        try {
            ServerBootstrap server = new ServerBootstrap();
            server.group(mainGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    //从线程的Handler
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel client) throws Exception {
                            //无锁化的串行编程
                            //业务逻辑，编码器
                            client.pipeline().addLast(new HttpResponseEncoder());
                            //解码器
                            client.pipeline().addLast(new HttpRequestDecoder());

                            client.pipeline().addLast(new TomcatHandler());
                        }
                    })
                    //配置信息
                    .option(ChannelOption.SO_BACKLOG, 128) //主线程配置
                    .childOption(ChannelOption.SO_KEEPALIVE, true);//从线程配置。
            ChannelFuture f = server.bind(port).sync();
            System.out.println("tomact started");
            f.channel().closeFuture().sync();
        }finally {
            mainGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        try {
            //new Tomcat().start(88);

        }catch (Exception e){
            e.printStackTrace();
        }

        System.out.println(Integer.toBinaryString(4));
    }

    private static boolean isPowerOfTwo(int val) {
        return (val & -val) == val;
    }

}
