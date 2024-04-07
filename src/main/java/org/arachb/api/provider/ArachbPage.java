package org.arachb.api.provider;

import javax.servlet.ServletContext;

public interface ArachbPage{
	
	String generateHTML(ServletContext servletContext);
	
	String generatejson(ServletContext servletContext);



}
