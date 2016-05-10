package org.arachb.api;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PublicOntology extends HttpServlet {

	final static String PUBLIC_ONTOLOGY_REDIRECT = "http://bit.ly/1WZ2koJ";


	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.sendRedirect(PUBLIC_ONTOLOGY_REDIRECT);
	}
}
