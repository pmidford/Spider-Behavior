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
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		
		String path = request.getRequestURI();
		switch (path){
		case "/taxon/events":
			getTaxonEvents(request,response);
			break;
		case "/taxon":
			getTaxonGeneralClaims(request,response);
			break;
		default:
			final StringBuilder msgBuffer = new StringBuilder();
			msgBuffer.append('"').append("Path is: ").append(path).append('"');
			response.getOutputStream().write(msgBuffer.toString().getBytes("UTF-8"));
		}
		response.getOutputStream().flush();
		response.getOutputStream().close();
		return;
	}
	
	
	public void getTaxonGeneralClaims(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException{	
		final OutputStream os = response.getOutputStream();
		String name = getTaxonFromQuery(request);
		if (!validateTaxonName(name)){
			Util.noResultsError(os);
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
				final String ethogramQueryString = getName2GeneralQuery(child_name);
				resultList = Util.tryAndAccumulateQueryResult(resultList, ethogramQueryString, con);
			}
			if (resultList.isEmpty()){
				final String taxonIdQueryString = getName2TaxonNameAndId(name);
				if (!Util.tryQuery(taxonIdQueryString,con,os)){
					Util.noResultsError(os);
				}
			}
			else{
				Util.jsonFormatResultList(resultList,os);
			}
		} catch (RepositoryException e) {  //TODO - make these return meaningful JSON strings
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
				if (con != null){
					con.close();
				}
				if (repo != null){
					repo.shutDown();
				}
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

	
	public void getTaxonEvents(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException{	

		final OutputStream os = response.getOutputStream();
		String name = getTaxonFromQuery(request);

		if (!validateTaxonName(name)){
			Util.noResultsError(os);
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
			String name1 = buildClass(name,con); //buildSubClassClosure(name,con); 
			List<TupleQueryResult> resultList = new ArrayList<TupleQueryResult>();
			final String eventQueryString = getName2EventQuery(name1);
			System.out.println("event query = " + eventQueryString);
			resultList = Util.tryAndAccumulateQueryResult(resultList, eventQueryString, con);
			if (resultList.isEmpty()){
				System.out.println("No results");
				final String taxonIdQueryString = getName2TaxonNameAndId(name);
				if (!Util.tryQuery(taxonIdQueryString,con,os)){
					Util.noResultsError(os);
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
				if (con != null){
					con.close();
				}
				if (repo != null){
					repo.shutDown();
				}
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
	
	private String getTaxonFromQuery(HttpServletRequest request){
		String name = request.getQueryString();
		name = name.substring("taxon=".length()).trim();
		final int pos = name.indexOf('+');
		if (pos>-1){
			name = name.substring(0,pos)+ ' ' + name.substring(pos+1);
		}
		return name;
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
    
/*    final static String TAXONGENERALQUERY = Util.OBOPREFIX +
          // "SELECT ?taxon_name ?behavior ?anatomy ?publication ?pubid %n" +
    	"SELECT ?taxon_name ?o9 ?p1 ?s8 ?p3 ?s7 ?p2 ?s6 ?p1 ?s5 ?n3 ?o4 ?p8 ?p10 ?r2 ?r1 ?p11 ?r20 ?r21 ?res1 ?p4 ?anatomy %n" +
           "WHERE {?r1 owl:someValuesFrom <%s> .  %n" +
           "       <%s> rdfs:label ?taxon_name . %n" +
           "       ?r1 ?p10 ?r2 . %n " +
           "       ?r2 ?p11 ?r20 . %n" +
           "       ?res1 rdf:first ?r1 . %n " +
           "       ?res1 rdf:rest ?r21 . %n" + 
           "       ?n3 rdf:rest ?res1 .  %n " +
           "       ?n3 rdf:first ?o4 .  %n" +
           "       ?o4 rdfs:label ?anatomy . %n" +
           "       ?s5 owl:intersectionOf ?n3 . %n" +
           "       ?s6 ?p1 ?s5 . %n" +
           "       ?s7 ?p2 ?s6 . %n" +
           "       ?s8 ?p3 ?s7 . %n " +
           "       ?s8 ?p4 ?o9 . }%n";
         /*  "       ?o9 rdfs:label ?behavior . %n" +
           "       ?s10 owl:intersectionOf ?s8 . %n" +
           "       ?s11 owl:someValuesFrom ?s10 . %n" +
           "       ?s12 rdf:first ?s11 . %n" +
           "       ?s13 rdf:rest ?s12 . %n" +
           "       ?s15 owl:intersectionOf ?s13 . %n" +
           "       ?s16 rdf:type ?s15 . %n" +
           "       ?s16 obo:BFO_0000050 ?pubid . %n" +
           "       ?pubid rdfs:label ?publication . %n" +
           "       <%s> rdfs:label ?taxon_name . } %n";  */
    		
   
    String getName2GeneralQuery(String id){
    	SparqlBuilder b = SparqlBuilder.startSparql();
    	b.addText(Util.OBOPREFIX, true);
    	b.addText("%n",true);
    	b.addText("SELECT ?taxon_name ?behavior ?anatomy ?publication ?pubid %n", true);
    	String line1 = String.format("WHERE {?r1 owl:someValuesFrom <%s> .  %n",id);
    	b.addText(line1,true);
    	b.addClause("?res1 rdf:first ?r1",true);
    	b.addClause("?n3 rdf:rest ?res1",true);
    	b.addClause("?n3 rdf:first ?o4",true);
    	b.addClause("?o4 rdfs:label ?anatomy",true);
    	b.addClause("?s5 owl:intersectionOf ?n3",true);
    	b.addClause("?s6 owl:someValuesFrom ?s5",true);
    	b.addClause("?s7 rdf:first ?s6",true);
    	b.addClause("?s8 rdf:rest ?s7",true);
    	b.addClause("?s8 rdf:first ?o9",true);
        b.addClause("?o9 rdfs:label ?behavior",true);
    	b.addClause("?s10 owl:intersectionOf ?s8",true);   //should be intersectionOf?
    	b.addClause("?s11 owl:someValuesFrom ?s10 ",true);
    	b.addClause("?s12 rdf:first ?s11",true);
    	b.addClause("?s13 rdf:rest ?s12",true);
    	b.addClause("?s13 rdf:first <http://purl.obolibrary.org/obo/IAO_0000300>", true);
    	b.addClause("?s15 owl:intersectionOf ?s13",true);
        b.addClause("?s16 rdf:type ?s15",true);
    	b.addClause("?s16 obo:BFO_0000050 ?pubid",true);
    	b.addClause("?pubid rdfs:label ?publication",true);
    	String line2 = String.format("<%s> rdfs:label ?taxon_name",id);
    	b.addText(line2,true); 
    	b.addText("} %n", true);
    	String foo = b.finish();
    	System.out.println("Hit getName2GeneralQuery : " + foo);
    	return foo;
    }
    
    final static String NAME2TAXONQUERYBASE = 
        	"SELECT ?taxon_id %n" +
        	"WHERE {?taxon_id rdfs:label \"%s\"^^xsd:string . } %n";
    		
    
    String getName2TaxonIdQuery(String name){
    	return String.format(NAME2TAXONQUERYBASE, name);
    }
    
    final static String NAME2TAXONNAMEANDIDQUERYBASE =
        	"SELECT ?taxon_name ?taxon_id %n" +
        	"WHERE {?taxon_id rdfs:label \"%s\"^^xsd:string . %n" +
        	"       ?taxon_id rdfs:label ?taxon_name . } %n";
    		
    String getName2TaxonNameAndId(String name){
    	return String.format(NAME2TAXONNAMEANDIDQUERYBASE, name);
    }

    
    final static String TAXONEVENTQUERY = Util.OBOPREFIX +
    		"SELECT ?subject ?label ?p2 ?event ?n1 %n" +
	        "WHERE { ?subject rdf:type <%s> .  %n" +
	        "        ?subject rdfs:label ?label .  %n" +
	        "        ?n1 obo:BFO_0000050 ?subject .  %n" +
            "        ?n1 obo:RO_0000056 ?n2. }";     //should be RO_000056
    String getName2EventQuery(String id){
        return String.format(TAXONEVENTQUERY, id);
    }

    
    
    List<String> buildSubClassClosure(String name, RepositoryConnection con) throws RepositoryException, MalformedQueryException, QueryEvaluationException{
    	final List<String> result = new ArrayList<String>();
    	final String taxonIdQueryString = getName2TaxonIdQuery(name);
    	List<TupleQueryResult>queryResults = new ArrayList<TupleQueryResult>(); 
    	queryResults = Util.tryAndAccumulateQueryResult(queryResults, taxonIdQueryString, con);
    	String rootId = getOneResult(queryResults,"taxon_id");
    	if (rootId != null){
    		final String nextId = rootId;
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
    						result.add(child_id);
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

    
    String buildClass(String name, RepositoryConnection con) throws RepositoryException, MalformedQueryException, QueryEvaluationException{
    	final String taxonIdQueryString = getName2TaxonIdQuery(name);
    	List<TupleQueryResult>queryResults = new ArrayList<TupleQueryResult>(); 
    	queryResults = Util.tryAndAccumulateQueryResult(queryResults, taxonIdQueryString, con);
    	return getOneResult(queryResults,"taxon_id");
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
        	"SELECT ?child_id %n" + 
        	"WHERE {?child_id rdfs:subClassOf* <%s> . } %n";
    		
    
    String getName2SubClassQuery(String id){
    	return String.format(NAME2SUBCLASSQUERYBASE, id);
    }
    
}
    
