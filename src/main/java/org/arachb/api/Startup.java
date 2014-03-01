package org.arachb.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.repository.manager.LocalRepositoryManager;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.sail.config.SailImplConfig;
import org.openrdf.sail.memory.config.MemoryStoreConfig;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.sail.config.SailRepositoryConfig;


public class Startup extends HttpServlet {

	final static private String USERHOME = System.getProperty("user.home");
	final static private String ADUNAHOME = USERHOME+"/.aduna/";
	final static private String baseURI = "http://arachb.org/arachb/arachb.owl";

	
	@Override
    public void init(ServletConfig config) throws ServletException{
		System.out.println("Starting init process");
		File baseDir = new File(ADUNAHOME);
		String repositoryId = "test1";
		LocalRepositoryManager manager = new LocalRepositoryManager(baseDir);
		Repository repo = null;
		RepositoryConnection con = null;
		try {
			manager.initialize();
			repo = manager.getRepository(repositoryId);
			System.out.println("Found repo " + repo + " from id repositoryId");
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
				System.out.println("Found repo " + repo + " of size " + size + " from id repositoryId");
			    if (size <1000) {
			    	//fake, need to load
			    	needsLoading = true;
			    }
			    	
			}
			if (needsLoading){

				URL loadURL = Startup.class.getClassLoader().getResource("arachb.owl");
				URLConnection loadConnection = loadURL.openConnection();
				InputStream loadStream = loadConnection.getInputStream();
			    
			    System.out.println("Size before loading: " + con.size());
			    con.add(loadStream, baseURI, RDFFormat.RDFXML);
			    System.out.println("Size after loading: " + con.size());
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
