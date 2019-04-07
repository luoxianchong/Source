package org.ten.server.http;

/**
 * Created by ing on 2019-03-24.
 */
public abstract  class Servlet {

    public abstract void doGet(Request request,Response response) throws Exception;
    public abstract void doPost(Request request,Response response) throws Exception;
    public abstract void service(Request request,Response response) throws Exception;
}
