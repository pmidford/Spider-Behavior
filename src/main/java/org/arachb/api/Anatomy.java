package org.arachb.api;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Anatomy extends HttpServlet {

	@Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {


            PrintWriter out = response.getWriter();
            out.println("{'Test works'}");
            out.flush();
            out.close();
        }

}
