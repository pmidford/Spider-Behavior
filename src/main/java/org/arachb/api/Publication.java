package org.arachb.api;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.TupleQueryResultHandlerException;
import org.eclipse.rdf4j.query.resultio.QueryResultFormat;
import org.eclipse.rdf4j.query.resultio.QueryResultIO;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultWriter;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;
import org.eclipse.rdf4j.repository.manager.LocalRepositoryManager;

public class Publication extends HttpServlet {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	final static private String QUERY =
			"prefix obo:<https://purl.obolibrary.org/obo/> select ?publication WHERE{?publication rdf:type obo:IAO_0000312 .}";

	private final static File BASEDIR = new File(Util.RDF4JHOME);

	final static private String USERHOME = System.getProperty("user.home");
	final static private String baseURI = "https://arachb.org/arachb/arachb.owl";

	private static final Logger log = Logger.getLogger(Taxon.class);


	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    		final OutputStream os = response.getOutputStream();
            response.setContentType("application/json");
			Repository repo = null;
			RepositoryConnection con = null;
			log.error("In publication doGet");
    		LocalRepositoryManager manager = new LocalRepositoryManager(BASEDIR);
    		try {
				manager.init();
				repo = manager.getRepository(Util.REPONAME);
				con = repo.getConnection();
    			TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, QUERY);
				try (TupleQueryResult result = tupleQuery.evaluate()) {
					QueryResultFormat jsonFormat =
							QueryResultIO.getWriterFormatForMIMEType("application/sparql-results+json").orElse(null);
					if (jsonFormat != null) {
						TupleQueryResultWriter jsonResults = (TupleQueryResultWriter) QueryResultIO.createWriter(jsonFormat, os);
						jsonResults.startQueryResult(result.getBindingNames());
						while (result.hasNext()) {
							jsonResults.handleSolution(result.next());
						}
						jsonResults.endQueryResult();
					}
				}
			} catch (RepositoryException | RepositoryConfigException | MalformedQueryException |
					 QueryEvaluationException | TupleQueryResultHandlerException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} finally {
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
