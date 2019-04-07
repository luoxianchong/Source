package org.ten.bio;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by ing on 2019-03-21.
 */
public class BIOServer {

    ServerSocket serverSocket=null;

    public BIOServer(int port) throws IOException {
        serverSocket=new ServerSocket(port);
    }

    public static void main(String[] args) {
        try {
            new BIOServer(88).listener();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listener() throws IOException {
        while (true){
            Socket socket= this.serverSocket.accept();
            InputStream in=socket.getInputStream();
            byte[] buffer=new byte[1024];
            int len=0;
            while ((len=in.read(buffer))>0){
                if("over".equals(buffer.toString())) break;
                else  System.out.printf(new String(buffer));
            }
            OutputStream out=socket.getOutputStream();
            out.write("copy that".getBytes());
            out.flush();
        }

    }
}
