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



/**
 * This servlet should handle URIs starting with api.arachb.org/taxon
 * @author pmidford
 *
 */
public class Taxon extends HttpServlet {
	
	final static private String baseURI = "http://arachb.org/arachb/arachb.owl";


	public void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {

		final OutputStream os = response.getOutputStream();

		String name = request.getQueryString();
		//System.out.println("raw string is: |" + name);
		name = name.substring("taxon=".length());
		final int pos = name.indexOf('+');
		if (pos>-1){
			name = name.substring(0,pos)+ ' ' + name.substring(pos+1);
		}
		name = name.trim();
		if (!validateTaxonName(name)){
			Util.returnError(os);
			os.flush();
			os.close();
			return;			
		}
		response.setContentType("application/sparql-results+json");
		File baseDir = new File(Util.ADUNAHOME);
		String repositoryId = "test1";
		Repository repo = null;
		RepositoryConnection con = null;
		LocalRepositoryManager manager = new LocalRepositoryManager(baseDir);
		try {
			manager.initialize();
			repo = manager.getRepository(repositoryId);
			con = repo.getConnection();
			String ethogramQueryString = getName2EthogramQuery(name);
			//System.out.println("ethogram query = \n" + ethogramQueryString);
			if (!Util.tryQuery(ethogramQueryString,con,os)){
				String taxonIdQueryString = getName2TaxonIdQuery(name);
				if (!Util.tryQuery(taxonIdQueryString,con,os)){
					Util.returnError(os);
				}
			}
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
    	
    public boolean validateTaxonName(String name){
    	String[]nameList = name.split(" ");
    	return (nameList.length<=3);
    }
    
    
    String getName2EthogramQuery(String name){
    	final StringBuilder b = new StringBuilder();
    	b.append(Util.OBOPREFIX);
        b.append("SELECT ?taxon_name ?behavior ?anatomy ?pubid\n");
        b.append("WHERE {?taxon rdfs:label \"%s\"^^xsd:string . \n"); 
        b.append("       ?r1 <http://www.w3.org/2002/07/owl#someValuesFrom> ?taxon . \n");
        b.append("       ?res1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> ?r1 . \n"); 
        b.append("       ?n3 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> ?res1 . \n");
        b.append("       ?n3 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> ?o4 . \n");
        b.append("       ?o4 rdfs:label ?anatomy . \n ");
        b.append("       ?s5 <http://www.w3.org/2002/07/owl#intersectionOf> ?n3 . \n");
        b.append("       ?s6 <http://www.w3.org/2002/07/owl#someValuesFrom> ?s5 . \n");
        b.append("       ?s7 ?p7 ?s6 . \n");
        b.append("       ?s8 ?p8 ?s7 . \n");
        b.append("       ?s8 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> ?o9 . \n");
        b.append("       ?o9 rdfs:label ?behavior . \n ");
        b.append("       ?s10 ?p108 ?s8 . \n");
        b.append("       ?s11 <http://www.w3.org/2002/07/owl#someValuesFrom> ?s10 . \n");
        b.append("       ?s12 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> ?s11 . \n");
        b.append("       ?s13 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> ?s12 . \n ");
       	b.append("       ?s15 <http://www.w3.org/2002/07/owl#intersectionOf> ?s13 . \n ");
       	b.append("       ?s16 ?p1615 ?s15 . \n ");
       	b.append("       ?s16 obo:BFO_0000050 ?pubid . \n ");
       	b.append("       ?taxon rdfs:label ?taxon_name . \n ");
       	b.append("       ?s13 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> obo:IAO_0000300 . \n ");
       	b.append("       ?s12 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> <http://www.w3.org/1999/02/22-rdf-syntax-ns#nil> . \n");
       	b.append("       ?s10 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> . \n");
       	b.append("       ?s6 <http://www.w3.org/2002/07/owl#onProperty> obo:BFO_0000057 . \n");
       	b.append("       ?r1 <http://www.w3.org/2002/07/owl#onProperty> obo:BFO_0000050 . } \n");
        return String.format(b.toString(),name);
    }
    
    String getName2TaxonIdQuery(String name){
    	final StringBuilder b = new StringBuilder();
    	b.append(Util.OBOPREFIX);
    	b.append("SELECT ?taxon_name ?taxon_id \n");
        b.append("WHERE {?taxon_id rdfs:label \"%s\"^^xsd:string . \n");
        b.append("       ?taxon_id rdfs:label ?taxon_name . }\n ");
    	return String.format(b.toString(), name);
    }
}
