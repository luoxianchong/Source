package org.ten.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by ing on 2019-03-23.
 */
public class AioServer {

    private  int port=80;
    public  AioServer(int port){
        this.port=port;
    }

    public  void listener(){
        try {
            AsynchronousServerSocketChannel  serverSocket=AsynchronousServerSocketChannel.open();
            serverSocket.bind(new InetSocketAddress(port));
            serverSocket.accept(new String(), new CompletionHandler<AsynchronousSocketChannel, String>() {


                public void completed(AsynchronousSocketChannel client, String attachment) {
                    ByteBuffer buffer=ByteBuffer.allocate(1024);

                    Future<Integer> f=client.read(buffer);

                    try {
                        if(f.get()>0){
                            buffer.flip();
                            System.out.println(new String(buffer.array()));
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }

                public void failed(Throwable exc, String attachment) {

                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
