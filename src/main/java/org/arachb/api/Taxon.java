package org.arachb.api;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
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
	


	public void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {

		final OutputStream os = response.getOutputStream();

		String name = request.getQueryString();
		System.out.println("raw string is: |" + name);
		name = name.substring("taxon=".length()).trim();
		final int pos = name.indexOf('+');
		if (pos>-1){
			name = name.substring(0,pos)+ ' ' + name.substring(pos+1);
		}
		if (!validateTaxonName(name)){
			Util.returnError(os);
			os.flush();
			os.close();
			return;			
		}
		response.setContentType(Util.SPARQLMIMETYPE);
		final File baseDir = new File(Util.ADUNAHOME);
		Repository repo = null;
		RepositoryConnection con = null;
		final LocalRepositoryManager manager = new LocalRepositoryManager(baseDir);
		try {
			manager.initialize();
			repo = manager.getRepository(Util.REPONAME);
			con = repo.getConnection();
			List <String> NameList = buildSubClassClosure(name,con); 
			final String ethogramQueryString = getName2EthogramQuery(name);
			List<TupleQueryResult> resultList = new ArrayList<TupleQueryResult>();
			resultList = Util.tryAndAccumulateQueryResult(resultList, ethogramQueryString, con);
			System.out.println("Result list is " + resultList);
			if (resultList.isEmpty()){
				final String taxonIdQueryString = getName2TaxonIdQuery(name);
				if (!Util.tryQuery(taxonIdQueryString,con,os)){
					Util.returnError(os);
				}
			}
			else{
				Util.jsonFormatResultList(resultList,os);
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
    	
	/**
	 * 
	 * @param name either monomial or binomial (only one space)
	 * @return true if valid
	 * 
	 */
    public boolean validateTaxonName(String name){
    	return (name.split(" ").length<=2);  //TODO be more selective (also consider common names)
    }
    
    
    String getName2EthogramQuery(String name){
    	final StringBuilder b = new StringBuilder();
    	b.append(Util.OBOPREFIX);
        b.append("SELECT ?taxon_name ?behavior ?anatomy ?publication ?pubid\n");
        b.append("WHERE {?taxon rdfs:label \"%s\"^^xsd:string . \n"); 
        b.append("       ?r1 <http://www.w3.org/2002/07/owl#someValuesFrom> ?taxon . \n");
        b.append("       ?res1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> ?r1 . \n"); 
        b.append("       ?n3 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> ?res1 . \n");
        b.append("       ?n3 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> ?o4 . \n");
        b.append("       ?o4 rdfs:label ?anatomy . \n");
        b.append("       ?s5 <http://www.w3.org/2002/07/owl#intersectionOf> ?n3 . \n");
        b.append("       ?s6 <http://www.w3.org/2002/07/owl#someValuesFrom> ?s5 . \n");
        b.append("       ?s7 ?p7 ?s6 . \n");
        b.append("       ?s8 ?p8 ?s7 . \n");
        b.append("       ?s8 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> ?o9 . \n");
        b.append("       ?o9 rdfs:label ?behavior . \n");
        b.append("       ?s10 ?p108 ?s8 . \n");
        b.append("       ?s11 <http://www.w3.org/2002/07/owl#someValuesFrom> ?s10 . \n");
        b.append("       ?s12 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> ?s11 . \n");
        b.append("       ?s13 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> ?s12 . \n");
       	b.append("       ?s15 <http://www.w3.org/2002/07/owl#intersectionOf> ?s13 . \n");
       	b.append("       ?s16 ?p1615 ?s15 . \n");
       	b.append("       ?s16 obo:BFO_0000050 ?pubid . \n");
       	b.append("       ?pubid rdfs:label ?publication . \n");
       	b.append("       ?taxon rdfs:label ?taxon_name . } \n");
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
    
    List<String> buildSubClassClosure(String name, RepositoryConnection con) throws RepositoryException, MalformedQueryException, QueryEvaluationException{
    	final List<String> result = new ArrayList<String>();
    	String queryStr = getName2SubClassQuery(name);
    	System.out.println("subclass query is " + queryStr);
		List<TupleQueryResult>resultList = new ArrayList<TupleQueryResult>(); 
		resultList = Util.tryAndAccumulateQueryResult(resultList, queryStr, con);
		System.out.println("Result count = " + resultList.size());
		try {
			for(TupleQueryResult rn : resultList){
				while (rn.hasNext()){
					BindingSet foo = rn.next();
					Binding b = foo.getBinding("child_name");
					//Binding b2 = foo.getBinding("p2");
					System.out.println("child is " + b.getValue()); // + "; prediate = " + b2.getValue());
				}
			}
		} catch (QueryEvaluationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return result;
    }
    
    String getName2SubClassQuery(String name){
    	final StringBuilder b = new StringBuilder();
    	b.append(Util.OBOPREFIX);
    	b.append("SELECT ?child_name \n");
    	b.append("WHERE {?taxon_id rdfs:label \"%s\"^^xsd:string . \n");
    	b.append("       ?child_id rdfs:subClassOf ?taxon_id .\n");
    	b.append("       ?child_id rdfs:label ?child_name . }\n");
    	return String.format(b.toString(), name);
    }
    
}
