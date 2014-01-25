package org.arachb.api;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.QueryResultIO;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriter;
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
	
	final static private String USERHOME = System.getProperty("user.home");
	final static private String ADUNAHOME = USERHOME+"/.aduna/";
	final static private String baseURI = "http://arachb.org/arachb/arachb.owl";

	

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	

    		String name = request.getQueryString();
    		System.out.print("raw string is: |" + name);
    		name = name.substring("taxon=".length());
			final int pos = name.indexOf('+');
    		if (pos>-1){
    			name = name.substring(0,pos)+ ' ' + name.substring(pos+1);
    		}
    		String finalQuery = getStringLiteralQuery(name);
    		System.out.print("SPARQL query = " + finalQuery);
    		final OutputStream os = response.getOutputStream();
            response.setContentType("application/sparql-results+json");
    		File baseDir = new File(ADUNAHOME);
			String repositoryId = "test1";
			Repository repo = null;
			RepositoryConnection con = null;
    		LocalRepositoryManager manager = new LocalRepositoryManager(baseDir);
    		try {
    			manager.initialize();
    			repo = manager.getRepository(repositoryId);
    		    con = repo.getConnection();
    			TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, finalQuery);
  			  	TupleQueryResult result = tupleQuery.evaluate();
    			TupleQueryResultFormat jsonFormat = QueryResultIO.getWriterFormatForMIMEType("application/sparql-results+json");
    			TupleQueryResultWriter jsonResults = QueryResultIO.createWriter(jsonFormat, os);
    			jsonResults.startQueryResult(result.getBindingNames());
    			while(result.hasNext()){
    		        jsonResults.handleSolution(result.next());
    			}
    			jsonResults.endQueryResult();  
    		} catch (RepositoryException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} catch (RepositoryConfigException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MalformedQueryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (QueryEvaluationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TupleQueryResultHandlerException e) {
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
            os.flush();
            os.close();
    
    }
    
    String getStringLiteralQuery(String name){
        String query = 
        		"prefix obo:<http://purl.obolibrary.org/obo/> select ?taxon WHERE{?taxon rdfs:label \"%s\"^^xsd:string .}";
        String result = String.format(query,name);
        return result;

    }
}
