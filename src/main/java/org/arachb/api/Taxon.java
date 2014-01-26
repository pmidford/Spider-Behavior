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

	final static private String OBOPREFIX = "prefix obo:<http://purl.obolibrary.org/obo/> ";

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	

    		String name = request.getQueryString();
    		System.out.println("raw string is: |" + name);
    		name = name.substring("taxon=".length());
			final int pos = name.indexOf('+');
    		if (pos>-1){
    			name = name.substring(0,pos)+ ' ' + name.substring(pos+1);
    		}
    		String name2TaxonQuery = getName2TaxonQuery(name);
    		System.out.println("taxon query = \n" + name2TaxonQuery);
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
    			TupleQuery taxonQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, name2TaxonQuery);
  			  	TupleQueryResult taxonResult = taxonQuery.evaluate();
  			  	//String taxonValue = null;
  			  	//while(taxonResult.hasNext()){
  			  	//	taxonValue = taxonResult.next().getValue("taxon").stringValue();
  			  	//}
  			  	//System.out.println("Taxon value is " + taxonValue);
  			  	//String taxon2partQuery = getTaxon2partQuery(taxonValue);
  			  	//TupleQuery partQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, taxon2partQuery);
  			  	//TupleQueryResult partResult = partQuery.evaluate();
    			TupleQueryResultFormat jsonFormat = QueryResultIO.getWriterFormatForMIMEType("application/sparql-results+json");
    			TupleQueryResultWriter jsonResults = QueryResultIO.createWriter(jsonFormat, os);
    			jsonResults.startQueryResult(taxonResult.getBindingNames());
    			while(taxonResult.hasNext()){
    		        jsonResults.handleSolution(taxonResult.next());
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
    
    String getName2TaxonQuery(String name){
        String query = OBOPREFIX +
        		"SELECT ?tname ?pubid ?aname ?bname \n"+ 
        		"              WHERE {?taxon rdfs:label \"%s\"^^xsd:string . \n" +
        		"                     ?r1 <http://www.w3.org/2002/07/owl#someValuesFrom> ?taxon . \n" +
        		"                     ?res1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> ?r1 . \n" +
                "                     ?res1 ?p2 ?n1 . \n" +
        		"                     ?n3 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> ?res1 . \n" +
                "                     ?n3 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> ?o4 . \n" +
        		"                     ?o4 rdfs:label ?aname . \n " +
        		"                     ?s5 <http://www.w3.org/2002/07/owl#intersectionOf> ?n3 . \n" +
                "                     ?s6 <http://www.w3.org/2002/07/owl#someValuesFrom> ?s5 . \n" +
        		"                     ?s7 ?p7 ?s6 . \n" +
                "                     ?s8 ?p8 ?s7 . \n" +
        		"                     ?s8 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> ?o9 . \n" +
                "                     ?o9 rdfs:label ?bname . \n " +
                "                     ?s10 ?p108 ?s8 . \n" +
        		"                     ?s11 <http://www.w3.org/2002/07/owl#someValuesFrom> ?s10 . \n" +
                "                     ?s12 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> ?s11 . \n" +
        		"                     ?s13 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> ?s12 . \n " +
                "                     ?s15 <http://www.w3.org/2002/07/owl#intersectionOf> ?s13 . \n " +
        		"                     ?s16 ?p1615 ?s15 . \n " +
                "                     ?s16 <http://purl.obolibrary.org/obo/BFO_0000050> ?pubid . \n " +
        		"                     ?taxon rdfs:label ?tname . \n " +
                "                     ?s13 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> <http://purl.obolibrary.org/obo/IAO_0000300> . \n " +
                "                     ?s12 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> <http://www.w3.org/1999/02/22-rdf-syntax-ns#nil> . \n" +
        		"                     ?s10 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> . \n" +
                "                     ?s6 <http://www.w3.org/2002/07/owl#onProperty> <http://purl.obolibrary.org/obo/BFO_0000057> . \n" +
        		"                     ?r1 <http://www.w3.org/2002/07/owl#onProperty> <http://purl.obolibrary.org/obo/BFO_0000050> . } \n";
        String result = String.format(query,name);
        return result;

    }
    
    String getTaxon2partQuery(String taxonURI){
    	String query = OBOPREFIX + "SELECT ?s ?p WHERE {?s ?p <%s> .}";
    	String result = String.format(query, taxonURI);
    	return result;
    }
}
