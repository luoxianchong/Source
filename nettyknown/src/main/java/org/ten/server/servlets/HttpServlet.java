package org.ten.server.servlets;

import org.ten.server.http.Request;
import org.ten.server.http.Response;
import org.ten.server.http.Servlet;

/**
 * Created by ing on 2019-03-24.
 */
public class HttpServlet  extends Servlet{


    public void doGet(Request request, Response response)throws Exception {
        System.out.println(request.getParameters());
        response.write(request.getParameter("key"));
    }

    public void doPost(Request request, Response response) throws Exception {
        doGet(request,response);
    }

    public void service(Request request, Response response) throws Exception {

    }
}
