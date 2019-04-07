package org.ten.nio;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by ing on 2019-03-22.
 */
public class NioServer {

    private Selector selector=null;
    private InetSocketAddress socketAddress=null;
    public  NioServer(int port) throws IOException {
        ServerSocketChannel  server=ServerSocketChannel.open();//开启服务
        socketAddress=new InetSocketAddress(port);
        server.bind(socketAddress).configureBlocking(false);

        server.register(selector=Selector.open(), SelectionKey.OP_ACCEPT);//启动seletor并注册到服务中

    }


    public void listener() throws IOException {
        while (true){
            int wait =selector.select();
            if (wait==0)continue;
            Set<SelectionKey> selectionKeys= selector.selectedKeys();

            Iterator<SelectionKey> iterator=selectionKeys.iterator();
            while (iterator.hasNext()){
                woker(iterator.next());
                iterator.remove();
            }

        }
    }

    private void woker(SelectionKey key) throws IOException {
        ByteBuffer buffer=ByteBuffer.allocate(1024);
        if(key.isAcceptable()){
            ServerSocketChannel server= (ServerSocketChannel) key.channel();
            SocketChannel clientChannel=server.accept();
            clientChannel.configureBlocking(false);
            clientChannel.register(selector,SelectionKey.OP_READ);
        }else if(key.isReadable()){
            SocketChannel client= (SocketChannel) key.channel();
            int len=client.read(buffer);
            if (len>0){
                buffer.flip();
                String content=new String(buffer.array(),0,len);
                System.out.println(content);
                client.register(selector,SelectionKey.OP_WRITE);
                buffer.clear();
            }
        }else if(key.isWritable()){
            SocketChannel server= (SocketChannel) key.channel();

            ByteBuffer response=buffer.wrap("copy that！".getBytes());
            if(response!=null)
                server.write(buffer);
            else
                server.close();
        }

    }


    public static void main(String[] args) throws IOException {
        new NioServer(88).listener();
    }
}
