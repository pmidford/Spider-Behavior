package org.arachb.api;

import org.apache.log4j.Logger;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.query.resultio.QueryResultFormat;
import org.eclipse.rdf4j.query.resultio.QueryResultIO;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultWriter;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;
import org.eclipse.rdf4j.repository.manager.LocalRepositoryManager;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class CurationStatus extends HttpServlet {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	final static private String QUERY =
			"prefix obo:<http://purl.obolibrary.org/obo/> " +
					"SELECT ?publication ?publication_id " +
					"WHERE { ?publication_id rdf:type obo:IAO_0000312. " +
                     "?publication_id rdfs:label ?publication}";

	private final static File BASEDIR = new File(Util.RDF4JHOME);

	final static private String USERHOME = System.getProperty("user.home");
	final static private String baseURI = "https://arachb.org/arachb/arachb.owl";



	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		final OutputStream os = response.getOutputStream();
		response.setContentType("application/json");
		Repository repo = null;
		RepositoryConnection con = null;
		getServletContext().log("In curation status doGet");
		File baseDir = Util.getBaseDir(getServletContext());
		LocalRepositoryManager manager = new LocalRepositoryManager(baseDir);

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
			StackTraceElement[] st = e.getStackTrace();
			String stCl = st[0].getClassName();
			String stMtd = st[0].getMethodName();
			getServletContext().log("Error: " + e.getLocalizedMessage());
			getServletContext().log("In: " + stCl + "." + stMtd);
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
