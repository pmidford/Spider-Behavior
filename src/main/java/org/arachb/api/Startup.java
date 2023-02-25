package org.arachb.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;
import org.eclipse.rdf4j.repository.config.RepositoryImplConfig;
import org.eclipse.rdf4j.repository.manager.LocalRepositoryManager;
import org.eclipse.rdf4j.repository.sail.config.SailRepositoryConfig;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.sail.config.SailImplConfig;
import org.eclipse.rdf4j.sail.memory.config.MemoryStoreConfig;


public class Startup extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger log = Logger.getLogger(Startup.class);

	final static private String RDF4JHOME = Util.USERHOME+"/.rdf4j/";
	final static private String baseURI = "https://arachb.org/arachb/arachb.owl";

	
	@Override
    public void init(ServletConfig config) throws ServletException{
		log.warn("Starting init process");
		log.warn("RDF4JHOME = " + RDF4JHOME);
		File baseDir = new File(RDF4JHOME);
		String repositoryId = "test1";
		LocalRepositoryManager manager = new LocalRepositoryManager(baseDir);
		Repository repo = null;
		RepositoryConnection con = null;
		try {
			manager.init();
			repo = manager.getRepository(repositoryId);
			log.warn("Found repo " + repo + " from id repositoryId");
			boolean needsLoading = false;
			if (repo == null){
				boolean persist = true;
				SailImplConfig backendConfig = new MemoryStoreConfig(persist);

				 
				// create a configuration for the repository implementation
				RepositoryImplConfig repositoryTypeSpec = new SailRepositoryConfig(backendConfig);
				RepositoryConfig repConfig = new RepositoryConfig(repositoryId, repositoryTypeSpec);
				manager.addRepositoryConfig(repConfig);
				repo = manager.getRepository(repositoryId);
				needsLoading = true;
				con = repo.getConnection();
			}
			else {
			    con = repo.getConnection();
			    long size = con.size();
				log.warn("Found repo " + repo + " of size " + size + " from id repositoryId");
			    if (size <1000) {
			    	//fake, need to load
			    	needsLoading = true;
			    }
			    	
			}
			if (needsLoading){

				URL loadURL = Startup.class.getClassLoader().getResource("arachb.owl");
				log.warn("Load url is " + loadURL);
				URLConnection loadConnection = loadURL.openConnection();
				InputStream loadStream = loadConnection.getInputStream();
			    
			    log.warn("Size before loading: " + con.size());
			    con.add(loadStream, baseURI, RDFFormat.RDFXML);
			    log.warn("Size after loading: " + con.size());
			}
				
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RDFParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RepositoryConfigException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			try {
				if (con != null){
					con.close();
				}
				if (repo != null){ 
					repo.shutDown();
			    }
			} catch (RepositoryException e) {
				System.out.println("Error shutting down repository");
				e.printStackTrace();
			}
		}

    }

}
