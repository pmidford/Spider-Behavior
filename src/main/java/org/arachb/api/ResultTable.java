package org.arachb.api;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.TupleQueryResultHandlerException;
import org.eclipse.rdf4j.query.resultio.QueryResultFormat;
import org.eclipse.rdf4j.query.resultio.QueryResultIO;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultWriter;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;

public class ResultTable {

	private final List<TupleQueryResult> contents;
	private final HttpServletResponse response;
	private static final Logger log = Logger.getLogger(ResultTable.class);

	
	public ResultTable(HttpServletResponse r){
		response = r;
		contents = new ArrayList<>();
	}

	public List<TupleQueryResult> getContents(){
		return contents;
	}


	public boolean isEmpty(){
		return contents.isEmpty();
	}


	public void tryAndAccumulateQueryResult(String queryString,RepositoryConnection con)
			throws RepositoryException, MalformedQueryException, QueryEvaluationException{
		final TupleQuery tQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		final TupleQueryResult qResult = tQuery.evaluate();
		if (qResult.hasNext()){  //worth saving?
			contents.add(qResult);
		} 
	}


	final static QueryResultFormat JSONFORMAT = QueryResultIO.getWriterFormatForMIMEType(Util.SPARQLMIMETYPE).orElse(null);

	public void jsonFormatResultList() throws IOException{
		final ServletOutputStream os = response.getOutputStream();
		final TupleQueryResultWriter jsonResults = (TupleQueryResultWriter) QueryResultIO.createWriter(JSONFORMAT, os);
		try {
			TupleQueryResult r = contents.get(0);
			jsonResults.startQueryResult(r.getBindingNames());
			for(TupleQueryResult rn : contents){
				while (rn.hasNext()){
					jsonResults.handleSolution(rn.next());
				}
			}
			jsonResults.endQueryResult();
		} catch (QueryEvaluationException e) {
			response.setStatus(500);
			PrintStream ps = new PrintStream(os);
			e.printStackTrace(ps);
			log.error("Query evaluation problem",e);
		}
		catch (TupleQueryResultHandlerException e) {
			response.setStatus(500);
			PrintStream ps = new PrintStream(os);
			e.printStackTrace(ps);
			log.error("Json formatting problem",e);
		}
	}

	
	public String htmlFormatPairedColumns(String[] queryKeys) throws IOException, QueryEvaluationException {
		final String ls = System.lineSeparator();
		log.info("columns checkpoint 1, query_keys= " + Arrays.toString(queryKeys));
		StringBuilder result = new StringBuilder();
		result.append("<table class=\"table\">").append(ls);
		List<String> headers = new ArrayList<>();
		for (int i=0;i < queryKeys.length;i+=2){
			int next = i+1;
			log.info("query: " + next);
			headers.add(queryKeys[next]);
		}
		log.info("columns checkpoint 2");
		StringBuilder headerRow = new StringBuilder();
		headerRow.append("   <tr>").append(ls);
		for (String name : headers){
			headerRow.append(String.format("    <th>%s</th>\n",name));
			log.info("columns checkpoint 2x: " + name);
		}
		headerRow.append("   </tr>").append(ls);
		log.info("Header row is: " + headerRow);
		result.append(headerRow);
		try {
			for(TupleQueryResult rn : contents){
				while (rn.hasNext()){
					final BindingSet bSet = rn.next();
					StringBuilder rowBuilder = new StringBuilder(100);
					result.append("   <tr>").append(ls);
					for (int i=0; i<queryKeys.length;i+=2){
						log.info("Trying key: " + queryKeys[i] + " and link: " + queryKeys[i+1]);
						final Binding nameBinding = bSet.getBinding(queryKeys[i]);
						final Binding linkBinding = bSet.getBinding(queryKeys[i+1]);
						if (nameBinding != null && linkBinding != null){
							String name = nameBinding.getValue().stringValue();
							String link = linkBinding.getValue().stringValue();
							rowBuilder.append(String.format("      <td><a href='%s'>%s</a></td>\n",name,link));
						}
						else if (nameBinding != null){
							String name = nameBinding.getValue().stringValue();
							rowBuilder.append(String.format("      <td>%s</td>\n", name));
						}
						else{
							rowBuilder.append("      <td></td>").append(ls);
						}
					}
					log.info("table row is " + rowBuilder);
					result.append(rowBuilder).append("   </tr>").append(ls);
				}
			}
		}
		finally{
			result.append("</table>").append(ls);
		}
		return result.toString();
	}


	void jsonFormatSingleResult() throws IOException{
		final ServletOutputStream os = response.getOutputStream();
		final TupleQueryResultWriter jsonResults = (TupleQueryResultWriter) QueryResultIO.createWriter(JSONFORMAT, os);
		try{
			TupleQueryResult r = contents.get(0);
			jsonResults.startQueryResult(r.getBindingNames());
			while(r.hasNext()){
				jsonResults.handleSolution(r.next());
			}
			jsonResults.endQueryResult();  
		} catch (QueryEvaluationException e) {
			response.setStatus(500);
			PrintStream ps = new PrintStream(os);
			e.printStackTrace(ps);
			log.error("Query evaluation problem",e);
		}
		catch (TupleQueryResultHandlerException e) {
			response.setStatus(500);
			PrintStream ps = new PrintStream(os);
			e.printStackTrace(ps);
			log.error("Json formatting problem",e);
		}
	}



	public String getOneResult(String bindingKey){
		try {
			for(TupleQueryResult rn : contents){
				if (rn.hasNext()){
					final BindingSet bSet = rn.next();
					final Binding b = bSet.getBinding(bindingKey);
					if (b!= null){
						return b.getValue().stringValue();
					}
					else {
						return null;
					}
				}
				else{
					return null;
				}
			}
		}
		catch (QueryEvaluationException e){
			return null;
		}
		return null;
	}


    public void noResultsError(String query) throws IOException{
		final PrintStream ps = new PrintStream(response.getOutputStream());
		final String error_str = String.format("No results Unexpected empty result from query %s", query);
		ps.printf("{error: %s}", error_str);

    }


}

