package org.arachb.api;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.manager.LocalRepositoryManager;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;


/**
 * This servlet should handle URIs starting with api.arachb.org/taxon
 * @author pmidford
 *
 */
public class Taxon extends HttpServlet {
	
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

    		String foo = request.getQueryString();
            PrintWriter out = response.getWriter();
      		File baseDir = new File("/Users/pmidford/temp/sesame/");
			String repositoryId = "test-db";
			Repository repo = null;
			RepositoryConnection con = null;
    		LocalRepositoryManager manager = new LocalRepositoryManager(baseDir);
    		try {
    			manager.initialize();
    			repo = manager.getRepository(repositoryId);
    		    con = repo.getConnection();
    		    System.out.println("Size after loading: " + con.size());
    		    con.close();
    			repo.shutDown();
    		} catch (RepositoryException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} catch (RepositoryConfigException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		finally {
    			try{
    				con.close();
    				repo.shutDown();
    			}
    			catch (RepositoryException e){
    				System.out.println("Error while trying to close repository");
    				e.printStackTrace();
    			}
    		}

            out.println("{'Test works' " + foo + " }");
            out.flush();
            out.close();
        }
    
    

}
