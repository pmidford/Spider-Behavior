package org.arachb.api;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;
import org.eclipse.rdf4j.repository.manager.LocalRepositoryManager;



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

	private final static File BASEDIR = new File(Util.RDF4JHOME);
	private static final Logger log = Logger.getLogger(Taxon.class);



	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		getServletContext().log("Got doGet for Taxon servlet");

		String path = request.getRequestURI();
		path = path.substring(path.indexOf("/taxon"));
		String taxonName = getTaxonFromQuery(request);
		getServletContext().log("Taxonname is " + taxonName);

		response.setContentType(Util.SPARQLMIMETYPE);

		if (!validateTaxonName(taxonName)){
			final PrintStream ps = new PrintStream(response.getOutputStream());
			String errorStr = String.format("{\"error\": \"Failed to validate Taxon Name %s\"}", taxonName);
			response.getOutputStream().write(errorStr.getBytes(StandardCharsets.UTF_8));
		}
		else{
			log.warn("path: " +  path);
			switch (path){
			case "/taxon/events":
				getTaxonEvents(taxonName,response);
				break;
			case "/taxon":
				getTaxonGeneralClaims(taxonName,response);
				break;
			default:
				response.getOutputStream().write(('"' + "Path is: " + path + '"').getBytes(StandardCharsets.UTF_8));
			}
		}
		response.getOutputStream().flush();
		response.getOutputStream().close();
	}


	void getTaxonGeneralClaims(String taxonName, HttpServletResponse response) throws IOException{
		Repository repo = null;
		RepositoryConnection con = null;
		log.warn("BASEDIR= " + BASEDIR);
		getServletContext().log("BASEDIR= " + BASEDIR);
		final LocalRepositoryManager manager = new LocalRepositoryManager(BASEDIR);
		try {
			manager.init();
			repo = manager.getRepository(Util.REPONAME);
			con = repo.getConnection();
			final String ethogramQueryString = getName2GeneralQuery(taxonName);
			getServletContext().log("Query String " + ethogramQueryString);
			ResultTable resultTable = new ResultTable(response);
			resultTable.tryAndAccumulateQueryResult(ethogramQueryString, con);
			if (!resultTable.isEmpty()){
				getServletContext().log("First query was successful");
				resultTable.jsonFormatResultList();
			}
			else{
				getServletContext().log("First query was empty");
				final String taxonIdQueryString = getName2TaxonNameAndId(taxonName);
				resultTable.tryAndAccumulateQueryResult(taxonIdQueryString, con);
				if (!resultTable.isEmpty()){
					resultTable.jsonFormatResultList();
				}
				else{
					getServletContext().log("Second query was empty");
					resultTable.noResultsError(taxonIdQueryString);
				}
			}
		} catch (RepositoryException | RepositoryConfigException | MalformedQueryException e) {
			//TODO - make these return meaningful JSON strings
			//response.setStatus(400);
			log.error(e.toString());
			e.printStackTrace();
		} catch (QueryEvaluationException e) {
			// TODO Auto-generated catch block
			//response.setStatus(400);
			e.printStackTrace();
		}
		finally {
			cleanupResources(repo,con);
		}
		response.getOutputStream().flush();
		response.getOutputStream().close();
	}


	void getTaxonEvents(String taxonName, HttpServletResponse response) throws IOException{
		Repository repo = null;
		RepositoryConnection con = null;
		final LocalRepositoryManager manager = new LocalRepositoryManager(BASEDIR);
		try {
			manager.init();
			repo = manager.getRepository(Util.REPONAME);
			con = repo.getConnection();
			ResultTable resultTable = new ResultTable(response);

			final String eventQueryString = getName2EventQuery(taxonName);
			resultTable.tryAndAccumulateQueryResult(eventQueryString, con);
			if (!resultTable.isEmpty()){
				resultTable.jsonFormatResultList();
			}
			else{
				final String taxonIdQueryString = getName2TaxonNameAndId(taxonName);
				resultTable.tryAndAccumulateQueryResult(taxonIdQueryString, con);
				if (!resultTable.isEmpty()){
					resultTable.jsonFormatResultList();
				}
				else{
					resultTable.noResultsError(taxonIdQueryString);
				}
			}
		} catch (RepositoryException | RepositoryConfigException | MalformedQueryException | QueryEvaluationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//response.setStatus(400);
		} finally {
			cleanupResources(repo, con);
		}
		response.getOutputStream().flush();
		response.getOutputStream().close();
	}

	private String getTaxonFromQuery(HttpServletRequest request){
		String name = request.getQueryString();
		name = name.substring("taxon=".length()).trim();
		StringBuilder b = new StringBuilder();  //remove when upgrade to java 8 happens
		for (String Component : name.split("\\+")){
			b.append(Component);
			b.append(" ");
		}
		return b.toString().trim();
	}


	/**
	 *
	 * @param name either monomial or binomial (only one space)
	 * @return true if valid
	 *
	 */
    public boolean validateTaxonName(String name) {
		Pattern pattern = Pattern.compile("%3A|%7B|%7D|\\{|\\}", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(name);
		if (matcher.find()) {
			return false;
		}
		else {
			return (name.split(" ").length <= 2);  //TODO be more selective (also consider common names)
		}
    }


    String getName2GeneralQuery(String name){
    	SparqlBuilder b = SparqlBuilder.startSparqlWithOBO();
		b.addText("SELECT ?taxon ?child_taxon ?behavior ?behavior_id ?anatomy ?anatomy_id ?publication ?pubid \n", true);
		String line1 = String.format("WHERE { ?parent_taxon rdfs:label \"%s\"^^xsd:string . \n",name);
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
    	b.addText("} \n", true);
    	//b.debug();
    	return b.finish();
    }


    final static String NAME2TAXONNAMEANDIDQUERYBASE =
        	"SELECT ?taxon_name ?taxon_id \n" +
        	"WHERE {?taxon_id rdfs:label \"%s\"^^xsd:string . \n" +
        	"       ?taxon_id rdfs:label ?taxon_name . } \n";

    String getName2TaxonNameAndId(String name){
    	return String.format(NAME2TAXONNAMEANDIDQUERYBASE, name);
    }


    String getName2EventQuery(String name){
		final String selectLine =
				"SELECT ?taxon ?child_taxon ?individual ?subject ?anatomy ?anatomy_id ?behavior ?behavior_id ?narrative ?narrative_id ?publication ?pub_id \n";
    	SparqlBuilder b = SparqlBuilder.startSparqlWithOBO();
		b.addText(selectLine);
		String line1 = String.format("WHERE { ?parent_taxon rdfs:label \"%s\"^^xsd:string . \n",name);
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
    	b.addText("} \n");
    	//b.debug();
    	return b.finish();
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

