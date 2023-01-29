package org.arachb.api.provider;

import java.io.IOException;

public interface ArachbPage {
	
	String generateHTML() throws IOException;
	
	String generatejson() throws IOException;



}
