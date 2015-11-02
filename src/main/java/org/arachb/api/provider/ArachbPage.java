package org.arachb.api.provider;

import java.io.IOException;

public interface ArachbPage {
	
	public String generateHTML() throws IOException;
	
	public String generatejson() throws IOException;



}
