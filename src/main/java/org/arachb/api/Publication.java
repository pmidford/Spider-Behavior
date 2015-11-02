package org.arachb.api;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

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
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.manager.LocalRepositoryManager;

public class Publication extends HttpServlet {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	final static private String QUERY = 
    		"prefix obo:<http://purl.obolibrary.org/obo/> select ?publication WHERE{?publication rdf:type obo:IAO_0000312 .}";

	final static private String USERHOME = System.getProperty("user.home");
	final static private String ADUNAHOME = USERHOME+"/.aduna/";
	final static private String baseURI = "http://arachb.org/arachb/arachb.owl";

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	

    		final OutputStream os = response.getOutputStream();
            response.setContentType("application/json");
    		File baseDir = new File(ADUNAHOME);
			String repositoryId = "test1";
			Repository repo = null;
			RepositoryConnection con = null;
    		LocalRepositoryManager manager = new LocalRepositoryManager(baseDir);
    		try {
    			manager.initialize();
    			repo = manager.getRepository(repositoryId);
    		    con = repo.getConnection();
    			TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, QUERY);
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
    				if (con != null){
    					con.close();
    				}
    				if (repo != null){
    					repo.shutDown();
    				}
    			}
    			catch (RepositoryException e){
    				System.out.println("Error while trying to close repository");
    				e.printStackTrace();
    			}
    		}
            os.flush();
            os.close();
        }

}
