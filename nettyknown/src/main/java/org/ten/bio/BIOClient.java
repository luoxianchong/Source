package org.ten.bio;

import java.io.*;
import java.net.Socket;

/**
 * Created by ing on 2019-03-22.
 */
public class BIOClient {

    public static void main(String[] args) throws IOException {
        Socket socket=new Socket("127.0.0.1",88);

       OutputStream out = socket.getOutputStream();
       byte[] buffer=new byte[512];
       int len=System.in.read(buffer);

       while (len>0){
           out.write(buffer);

           if("over".equals(buffer)) break;
       }

       InputStream in=socket.getInputStream();
       byte[] bytes=new byte[512];

       in.read(bytes);
        System.out.printf(bytes.toString());

       out.close();
       in.close();
       socket.close();
    }
}

