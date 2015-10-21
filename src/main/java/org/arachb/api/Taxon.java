package org.arachb.api;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

	private final static File BASEDIR = new File(Util.ADUNAHOME);



	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String path = request.getRequestURI();
		path = path.substring(path.indexOf("/taxon"));
		String taxonName = getTaxonFromQuery(request);

		final OutputStream os = response.getOutputStream();
		response.setContentType(Util.SPARQLMIMETYPE);

		if (!validateTaxonName(taxonName)){
			Util.noResultsError(os);
			os.flush();
			os.close();
			return;
		}

		switch (path){
		case "/taxon/events":
			getTaxonEvents(taxonName,os);
			break;
		case "/taxon":
			getTaxonGeneralClaims(taxonName,os);
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


	void getTaxonGeneralClaims(String taxonName, OutputStream os) throws ServletException, IOException{

		Repository repo = null;
		RepositoryConnection con = null;
		final LocalRepositoryManager manager = new LocalRepositoryManager(BASEDIR);
		try {
			manager.initialize();
			repo = manager.getRepository(Util.REPONAME);
			con = repo.getConnection();
			List<TupleQueryResult> resultList = new ArrayList<TupleQueryResult>();
			final String ethogramQueryString = getName2GeneralQuery(taxonName);
			resultList = Util.tryAndAccumulateQueryResult(resultList, ethogramQueryString, con);
			if (resultList.isEmpty()){
				final String taxonIdQueryString = getName2TaxonNameAndId(taxonName);
				if (!Util.queryToOutput(taxonIdQueryString,con,os)){
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
			cleanupResources(repo,con);
		}
		os.flush();
		os.close();
	}


	void getTaxonEvents(String taxonName, OutputStream os) throws ServletException, IOException{

		Repository repo = null;
		RepositoryConnection con = null;
		final LocalRepositoryManager manager = new LocalRepositoryManager(BASEDIR);
		try {
			manager.initialize();
			repo = manager.getRepository(Util.REPONAME);
			con = repo.getConnection();
			//List <String> NameList = buildSubClassClosure(taxonName,con);
			List<TupleQueryResult> resultList = new ArrayList<TupleQueryResult>();

			final String eventQueryString = getName2EventQuery(taxonName);
			System.out.println("event query = " + eventQueryString);
			resultList = Util.tryAndAccumulateQueryResult(resultList, eventQueryString, con);

			if (resultList.isEmpty()){
				final String taxonIdQueryString = getName2TaxonNameAndId(taxonName);
				if (!Util.queryToOutput(taxonIdQueryString,con,os)){
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
			cleanupResources(repo, con);
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


    String getName2GeneralQuery(String name){
    	SparqlBuilder b = SparqlBuilder.startSparqlWithOBO();
    	b.addText("SELECT ?taxon ?child_taxon ?behavior ?behavior_id ?anatomy ?anatomy_id ?publication ?pubid %n", true);
		String line1 = String.format("WHERE { ?parent_taxon rdfs:label \"%s\"^^xsd:string . %n",name);
		b.addText(line1,true);
		b.addClause("?child_taxon rdfs:subClassOf* ?parent_taxon",true);
    	b.addClause("?r1 owl:someValuesFrom ?child_taxon",true);
    	b.addClause("?res1 rdf:first ?r1",true);
    	b.addClause("?n3 rdf:rest ?res1",true);
    	b.addClause("?n3 rdf:first ?anatomy_id",true);
    	b.addClause("?anatomy_id rdfs:label ?anatomy",true);
    	b.addClause("?s5 owl:intersectionOf ?n3",true);
    	b.addClause("?s6 owl:someValuesFrom ?s5",true);
    	b.addClause("?s7 rdf:first ?s6",true);
    	b.addClause("?s8 rdf:rest ?s7",true);
    	b.addClause("?s8 rdf:first ?behavior_id",true);
        b.addClause("?behavior_id rdfs:label ?behavior",true);
    	b.addClause("?s10 owl:intersectionOf ?s8",true);   //should be intersectionOf?
    	b.addClause("?s11 owl:someValuesFrom ?s10 ",true);
    	b.addClause("?s12 rdf:first ?s11",true);
    	b.addClause("?s13 rdf:rest ?s12",true);
    	b.addClause("?s13 rdf:first <http://purl.obolibrary.org/obo/IAO_0000300>", true);
    	b.addClause("?s15 owl:intersectionOf ?s13",true);
        b.addClause("?s16 rdf:type ?s15",true);
    	b.addClause("?s16 obo:BFO_0000050 ?pubid",true);
    	b.addClause("?pubid rdfs:label ?publication",true);
    	b.addClause("?child_taxon rdfs:label ?taxon",true);
    	b.addText("} %n", true);
    	b.debug();
    	return b.finish();
    }


    String getName2TaxonIdQuery(String name){
    	SparqlBuilder b = SparqlBuilder.startSparql();
    	b.addText("SELECT ?taxon_id %n");
    	String line2 = String.format("WHERE {?taxon_id rdfs:label \"%s\"^^xsd:string . } %n", name);
    	b.addText(line2);
    	return b.finish();
    }

    final static String NAME2TAXONNAMEANDIDQUERYBASE =
        	"SELECT ?taxon_name ?taxon_id %n" +
        	"WHERE {?taxon_id rdfs:label \"%s\"^^xsd:string . %n" +
        	"       ?taxon_id rdfs:label ?taxon_name . } %n";

    String getName2TaxonNameAndId(String name){
    	return String.format(NAME2TAXONNAMEANDIDQUERYBASE, name);
    }

    private String selectLine =
    		"SELECT ?taxon ?child_taxon ?individual ?subject ?anatomy ?anatomy_id ?behavior ?behavior_id ?narrative ?narrative_id ?publication ?pub_id %n";

    String getName2EventQuery(String name){
    	SparqlBuilder b = SparqlBuilder.startSparqlWithOBO();
		b.addText(selectLine);
		String line1 = String.format("WHERE { ?parent_taxon rdfs:label \"%s\"^^xsd:string . %n",name);
		b.addText(line1);
		b.addClause("?child_taxon rdfs:subClassOf* ?parent_taxon");
		b.addClause("?subject rdf:type ?child_taxon");
        b.addClause("?subject rdfs:label ?individual",true);
        b.addClause("?anatomy_id obo:BFO_0000050 ?subject",true);
        b.addClause("?anatomy_id rdfs:label ?anatomy",true);
        b.addClause("?event obo:RO_0002218 ?anatomy_id",true);
        b.addClause("?event rdf:type ?i1",true);
        b.addClause("?i1 owl:intersectionOf ?i2",true);
        b.addClause("?i2 rdf:first ?behavior_id",true);
        b.addClause("?behavior_id rdfs:label ?behavior",true);
        b.addClause("?event obo:BFO_0000050 ?narrative_id",true);
        b.addClause("?narrative_id rdfs:label ?narrative",true);
        b.addClause("?n3 obo:IAO_0000219 ?event",true);
        b.addClause("?n3 obo:BFO_0000050 ?pub_id",true);
        b.addClause("?pub_id rdfs:label ?publication",true);
		b.addClause("?child_taxon rdfs:label ?taxon",true);
		b.addText("    FILTER NOT EXISTS { ?subject rdf:type ?other .");
        b.addText("        ?other rdfs:subClassOf ?child_taxon .");
        b.addText("        FILTER(?other != ?child_taxon)");
        b.addText("    }");
    	b.addText("} %n");
    	b.debug();
    	return b.finish();
    }




    String buildClass(String name, RepositoryConnection con) throws RepositoryException, MalformedQueryException, QueryEvaluationException{
    	final String taxonIdQueryString = getName2TaxonIdQuery(name);
    	List<TupleQueryResult>queryResults = new ArrayList<TupleQueryResult>();
    	queryResults = Util.tryAndAccumulateQueryResult(queryResults, taxonIdQueryString, con);
    	return Util.getOneResult(queryResults,"taxon_id");
    }



    //cleanup methods

    private void cleanupResources(Repository repo, RepositoryConnection con){
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

}

