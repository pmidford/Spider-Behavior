package org.arachb.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

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

	final static private String baseURI = "https://arachb.org/arachb/arachb.owl";

	
	@Override
    public void init(ServletConfig config) throws ServletException{
		super.init(config);
		File baseDir = Util.getBaseDir(getServletContext());
		String repositoryId = "test1";
		LocalRepositoryManager manager = new LocalRepositoryManager(baseDir);
		Repository repo = null;
		RepositoryConnection con = null;
		try {
			manager.init();
			repo = manager.getRepository(repositoryId);
			getServletContext().log("Found repo " + repo + " from id repositoryId");
			boolean needsLoading = false;
			if (repo == null){
				boolean persist = true;
				SailImplConfig backendConfig = new MemoryStoreConfig(persist);


				// create a configuration for the repository implementation
				RepositoryImplConfig repositoryTypeSpec = new SailRepositoryConfig(backendConfig);
				RepositoryConfig repConfig = new RepositoryConfig(repositoryId, repositoryTypeSpec);
				File dataDir = manager.getRepositoryDir(repConfig.getID());
				//getServletContext().log("dataDir is  " + dataDir);
				System.out.println("dataDir is  " + dataDir);
				if (!dataDir.exists()) {
					boolean mkResult = dataDir.mkdirs();
					if (!mkResult){
						throw new RuntimeException("Tried to create dirs for " + dataDir);
					}
				}

				manager.addRepositoryConfig(repConfig);
				repo = manager.getRepository(repositoryId);
				needsLoading = true;
				con = repo.getConnection();
			}
			else {
			    con = repo.getConnection();
			    long size = con.size();
				getServletContext().log("Found repo " + repo + " of size " + size + " from id repositoryId");
			    if (size <1000) {
			    	//fake, need to load
			    	needsLoading = true;
			    }

			}
			if (needsLoading){

				URL loadURL = Startup.class.getClassLoader().getResource("arachb.owl");
				ClassLoader cl = Startup.class.getClassLoader();
				getServletContext().log("Context loader is " + cl);
				getServletContext().log("Load url is " + loadURL);
				if (loadURL == null){
					throw new ServletException("Can't find KB file to load into server, loadURL is null");
				}
				URLConnection loadConnection = loadURL.openConnection();
				InputStream loadStream = loadConnection.getInputStream();

				getServletContext().log("Size before loading: " + con.size());
			    con.add(loadStream, baseURI, RDFFormat.RDFXML);
				getServletContext().log("Size after loading: " + con.size());
			}

		} catch (RepositoryException | RDFParseException | IOException | RepositoryConfigException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
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
