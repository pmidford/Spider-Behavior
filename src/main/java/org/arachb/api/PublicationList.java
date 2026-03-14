package org.arachb.api;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

public class PublicationList extends HttpServlet {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	final static private String QUERY =
			"prefix obo:<http://purl.obolibrary.org/obo/> " +
					"SELECT ?publication ?publication_id ?curated_statements " +
					"WHERE {?publication_id rdf:type obo:IAO_0000312. " +
					"?publication_id rdfs:label ?publication. " +
					"?curated_statements obo:BFO_0000050 ?publication_id} ";

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		final OutputStream os = response.getOutputStream();
		response.setContentType("application/json");
		Repository repo = null;
		RepositoryConnection con = null;
		//getServletContext().log("In publication list doGet");
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
				StackTraceElement[] st = e.getStackTrace();
				String stCl = st[0].getClassName();
				String stMtd = st[0].getMethodName();
				getServletContext().log("Error while trying to close repository:");
				getServletContext().log(e.getLocalizedMessage());
				getServletContext().log("In: " + stCl + "." + stMtd);
			}
		}
		os.flush();
		os.close();
	}

}
