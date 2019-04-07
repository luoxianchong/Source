package org.ten.redis;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by ing on 2018/4/15.
 */
public class RedisClient {

    private static final String HOST="192.168.99.1";//192.168.139.128
    private static final int PORT=6379;

    private static  Socket socket=null;


    public static void main(String[] args) throws IOException {
        socket=new Socket(HOST,PORT);
        new Thread(new Write(socket)).start();
        new Thread(new Read(socket)).start();

    }


    static class Write implements  Runnable{
        Socket socket;
        public  Write(Socket socket){
            this.socket=socket;
        }

        public void run() {
            Scanner scanner=null;
            OutputStream out= null;
            try {
                while (true) {

                    out = socket.getOutputStream();
                    scanner = new Scanner(System.in);
                    out.write((scanner.nextLine() + "\r\n").getBytes());
                    out.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                     if(out!=null)
                         out.close();
                     if(scanner!=null)
                          scanner.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        }
    }


    static class  Read implements Runnable{

        Socket socket=null;
        public  Read(Socket socket){
            this.socket=socket;
        }

        public void run() {
            InputStream in= null;
            try {
                while (true) {
                    in = socket.getInputStream();
                    BufferedInputStream buf = new BufferedInputStream(in);
                    byte[] bytes = new byte[1024];
                    buf.read(bytes);
                    System.out.printf("redis>" + new String(bytes));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    if(in!=null){
                        in.close();
                    }
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
