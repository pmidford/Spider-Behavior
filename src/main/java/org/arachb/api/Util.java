package org.arachb.api;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.QueryResultIO;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriter;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

public class Util {
	
	final static String USERHOME = System.getProperty("user.home");
	final static String ADUNAHOME = USERHOME+"/.aduna/";
	final static String OBOPREFIX = "prefix obo:<http://purl.obolibrary.org/obo/> ";
	final static String SPARQLMIMETYPE = "application/sparql-results+json";
	final static String BASEURI = "http://arachb.org/arachb/arachb.owl";
	final static String REPONAME = "test1";


	public static boolean tryQuery(String queryString, RepositoryConnection con, OutputStream os) 
			throws RepositoryException, MalformedQueryException, QueryEvaluationException{
		final TupleQuery tQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		long startTime = System.nanoTime();
		final TupleQueryResult qResult = tQuery.evaluate();
		long endTime = System.nanoTime();
		long duration = endTime - startTime;
		//System.out.println("Query " + queryString + " \n\n took " + duration + " nanosec");
		if (qResult.hasNext()){
			jsonFormatResult(qResult,os);
			return true;
		}
		else
			return false;
	}
	
	public static List<TupleQueryResult> tryAndAccumulateQueryResult(List<TupleQueryResult> results,
			                                                         String queryString, 
			                                                         RepositoryConnection con)
	       throws RepositoryException, MalformedQueryException, QueryEvaluationException{
		final TupleQuery tQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		long startTime = System.nanoTime();
		final TupleQueryResult qResult = tQuery.evaluate();
		long endTime = System.nanoTime();
		long duration = endTime - startTime;
		//System.out.println("Query " + queryString + " \n\n took " + duration + " nanosec");
		if (qResult.hasNext()){  //worth saving?
			results.add(qResult);
		}
		return results;
	}
	
    public static void jsonFormatResult(TupleQueryResult r,OutputStream os){
		final TupleQueryResultFormat jsonFormat = QueryResultIO.getWriterFormatForMIMEType(SPARQLMIMETYPE);
		final TupleQueryResultWriter jsonResults = QueryResultIO.createWriter(jsonFormat, os);
		try{
			jsonResults.startQueryResult(r.getBindingNames());
			while(r.hasNext()){
				jsonResults.handleSolution(r.next());
			}
			jsonResults.endQueryResult();  
		} catch (QueryEvaluationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (TupleQueryResultHandlerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public static void jsonFormatResultList(List<TupleQueryResult> results, OutputStream os){
		final TupleQueryResultFormat jsonFormat = QueryResultIO.getWriterFormatForMIMEType(SPARQLMIMETYPE);
		final TupleQueryResultWriter jsonResults = QueryResultIO.createWriter(jsonFormat, os);
		try {
			TupleQueryResult r = results.get(0);
			jsonResults.startQueryResult(r.getBindingNames());
			for(TupleQueryResult rn : results){
				while (rn.hasNext()){
					jsonResults.handleSolution(rn.next());
				}
			}
			jsonResults.endQueryResult();
		} catch (QueryEvaluationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (TupleQueryResultHandlerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	
    public static void returnError(OutputStream os) throws IOException{
		final StringBuilder msgBuffer = new StringBuilder();
		msgBuffer.append('"');
		msgBuffer.append("no results");
		msgBuffer.append('"');
		os.write(msgBuffer.toString().getBytes("UTF-8"));
    }

}
