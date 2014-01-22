package org.arachb.api;

import java.io.File;
import java.io.IOException;

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

	
	
	@Override
    public void init(ServletConfig config) throws ServletException{
		System.out.println("Starting init process");
		File file = new File("/Users/pmidford/Projects/arachtools/owlbuilder/test.owl");
		File baseDir = new File("/Users/pmidford/temp/sesame/");
		String repositoryId = "test-db";
		LocalRepositoryManager manager = new LocalRepositoryManager(baseDir);
		String baseURI = "http://arachb.org/arachb/arachb.owl";
		Repository repo = null;
		RepositoryConnection con = null;
		try {
			manager.initialize();
			 
			try{
				repo = manager.getRepository(repositoryId);
			}
			catch (RepositoryException e){
				// create a configuration for the SAIL stack
				boolean persist = true;
				SailImplConfig backendConfig = new MemoryStoreConfig(persist);

				 
				// create a configuration for the repository implementation
				RepositoryImplConfig repositoryTypeSpec = new SailRepositoryConfig(backendConfig);
				RepositoryConfig repConfig = new RepositoryConfig(repositoryId, repositoryTypeSpec);
				manager.addRepositoryConfig(repConfig);
				repo = manager.getRepository(repositoryId);
			    con = repo.getConnection();

			    System.out.println("Size before loading: " + con.size());
			    con.add(file, baseURI, RDFFormat.RDFXML);
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
