package org.arachb.api;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
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
			List<TupleQueryResult> resultList = new ArrayList<TupleQueryResult>();
			for (String child_name : NameList){
				final String ethogramQueryString = getName2EthogramQuery(child_name);
				resultList = Util.tryAndAccumulateQueryResult(resultList, ethogramQueryString, con);
			}
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
			catch (NullPointerException e){
				System.out.println("Error trying to close null repository");
				e.printStackTrace();
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
    
    final static String ETHOGRAMQUERYBASE = Util.OBOPREFIX +
           "SELECT ?taxon_name ?behavior ?anatomy ?publication ?pubid \n" +
           "WHERE {?r1 owl:someValuesFrom <%s> . \n" +
           "       ?res1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> ?r1 . \n" +
           "       ?n3 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> ?res1 . \n" +
           "       ?n3 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> ?o4 . \n" +
           "       ?o4 rdfs:label ?anatomy . \n" +
           "       ?s5 owl:intersectionOf ?n3 . \n" +
           "       ?s6 owl:someValuesFrom ?s5 . \n" +
           "       ?s7 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> ?s6 . \n" +
           "       ?s8 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> ?s7 . \n" +
           "       ?s8 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> ?o9 . \n" +
           "       ?o9 rdfs:label ?behavior . \n" +
           "       ?s10 owl:intersectionOf ?s8 . \n" +
           "       ?s11 <http://www.w3.org/2002/07/owl#someValuesFrom> ?s10 . \n" +
           "       ?s12 <http://www.w3.org/1999/02/22-rdf-syntax-ns#first> ?s11 . \n" +
           "       ?s13 <http://www.w3.org/1999/02/22-rdf-syntax-ns#rest> ?s12 . \n" +
           "       ?s15 <http://www.w3.org/2002/07/owl#intersectionOf> ?s13 . \n" +
           "       ?s16 rdf:type ?s15 . \n" +
           "       ?s16 obo:BFO_0000050 ?pubid . \n" +
           "       ?pubid rdfs:label ?publication . \n" +
           "       <%s> rdfs:label ?taxon_name . } \n";
    		
   
    String getName2EthogramQuery(String id){
        return String.format(ETHOGRAMQUERYBASE,id,id);
    }
    
    final static String NAME2TAXONQUERYBASE = 
        	"SELECT ?taxon_id \n" +
        	"WHERE {?taxon_id rdfs:label \"%s\"^^xsd:string . } \n";
    		
    
    String getName2TaxonIdQuery(String name){
    	return String.format(NAME2TAXONQUERYBASE, name);
    }
    
    List<String> buildSubClassClosure(String name, RepositoryConnection con) throws RepositoryException, MalformedQueryException, QueryEvaluationException{
    	final List<String> result = new ArrayList<String>();
		final String taxonIdQueryString = getName2TaxonIdQuery(name);
		List<TupleQueryResult>queryResults = new ArrayList<TupleQueryResult>(); 
		queryResults = Util.tryAndAccumulateQueryResult(queryResults, taxonIdQueryString, con);
		String rootId = getOneResult(queryResults,"taxon_id");
    	final Deque<String> worklist = new ArrayDeque<String>();
    	worklist.add(rootId);
    	while(worklist.peek() != null){
    		final String nextId = worklist.poll();
    		result.add(nextId);
    		final String taxonSubClassQueryString = getName2SubClassQuery(nextId);
    		queryResults.clear(); 
    		queryResults = Util.tryAndAccumulateQueryResult(queryResults, taxonSubClassQueryString, con);
    		try{
    			for(TupleQueryResult rn : queryResults){
    				while (rn.hasNext()){
    					final BindingSet bSet = rn.next();
    					final Binding b = bSet.getBinding("child_id");
    					if (b != null){
    						final String child_id = b.getValue().stringValue();
    						worklist.add(child_id);
    					}
    				}
    			}
    		} catch (QueryEvaluationException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    	}
    	return result;
    }

    
    private String getOneResult(List<TupleQueryResult> queryResults, String bindingKey){
    	try {
    		for(TupleQueryResult rn : queryResults){
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
    
    final static String NAME2SUBCLASSQUERYBASE = 
        	"SELECT ?child_id \n" + 
        	"WHERE {?child_id rdfs:subClassOf <%s> . } \n";
    		
    
    String getName2SubClassQuery(String id){
    	return String.format(NAME2SUBCLASSQUERYBASE, id);
    }
    
    
}
