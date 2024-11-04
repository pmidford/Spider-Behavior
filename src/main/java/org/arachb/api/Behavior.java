package org.arachb.api;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;
import org.eclipse.rdf4j.repository.manager.LocalRepositoryManager;

/**
 * This servlet should handle urls beginning with api.arachb.org/behavior
 * @author pmidford
 *
 */
public class Behavior extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	final Logger logger = LoggerFactory.getLogger(Behavior.class);

	@Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {


		final ServletContext context = getServletContext();

		String name = request.getQueryString();
		name = name.substring("behavior=".length()).trim();
		final String[] components = name.split("\\+");
		if (components.length>1){
			StringBuilder b = new StringBuilder();
			for (String c : components){
				b.append(c);
				b.append(' ');
			}
			name = b.toString().trim();  //remove final space
		}
		if (!validateBehaviorName(name)){
			final PrintStream ps = new PrintStream(response.getOutputStream());
			response.setStatus(500);
			String errorStr = "Failed to validate behavior " + name;
			ps.print(errorStr);
			logger.debug(errorStr);
			return;
		}
		response.setContentType(Util.SPARQLMIMETYPE);
		Repository repo = null;
		RepositoryConnection con = null;
		File baseDir = Util.getBaseDir(getServletContext());

		LocalRepositoryManager manager = new LocalRepositoryManager(baseDir);

		try {
			context.log("About to query");
			manager.init();
			repo = manager.getRepository(Util.REPONAME);
			con = repo.getConnection();
			String behaviorQueryString = getName2BehaviorReportQuery(name);
			ResultTable result = new ResultTable(response);
			result.tryAndAccumulateQueryResult(behaviorQueryString,con);
			if (!result.isEmpty()){
				result.jsonFormatResultList(context);
			}
			else{
				String taxonIdQueryString = getName2BehaviorIdQuery(name);
				result.tryAndAccumulateQueryResult(taxonIdQueryString, con);
				if (!result.isEmpty()){
					result.jsonFormatResultList(context);
				}
				else{
					result.noResultsError(taxonIdQueryString);
				}
			}
		} catch (RepositoryException | RepositoryConfigException | MalformedQueryException | QueryEvaluationException e) {
			// TODO Auto-generated catch block
			context.log("Exception in behavior query", e);
		} finally {
			try{
				if (con != null){
					con.close();
				}
				if (repo != null){
					repo.shutDown();
				}
			}
			catch (NullPointerException | RepositoryException e){
				context.log("Error closing repository after behavior query", e);
			}
		}
    }
    
    

    /**
     * 
     * @param name identifies a behavior name (term label?)
     * @return true if a behavior name
     */
    public boolean validateBehaviorName(String name){
		Pattern pattern = Pattern.compile("%3A|%7B|%7D|\\{|\\}", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(name);
		if (matcher.find())
			return false;
		else {
			return (name.split(" ").length <= 3);  //TODO be more selective (also consider common names)
		}
    }



	public String getName2BehaviorReportQuery(String name){
    	SparqlBuilder b = SparqlBuilder.startSparql();
    	String selectLine = "SELECT ?behavior ?taxon ?anatomy\n";
    	b.addText(selectLine);
    	String whereLine =
    			String.format("WHERE {?behavior_id rdfs:label \"%s\"^^xsd:string .\n", name);
    	b.addText(whereLine);
    	b.addClause("?behavior_id rdfs:label ?behavior", true);
    	b.addClause("?s8 rdf:first ?behavior_id",true);
    	b.addClause("?s8 rdf:rest ?s7",true);
    	b.addClause("?s7 rdf:first ?s6",true);
    	b.addClause("?s6 owl:someValuesFrom ?s5",true);
    	b.addClause("?s5 owl:intersectionOf ?s4",true);
    	b.addClause("?s4 rdf:first ?anatomy_id",true);
    	b.addClause("?s4 rdf:rest ?s2",true);
    	b.addClause("?s2 rdf:first ?s1",true);
    	b.addClause("?s1 owl:someValuesFrom ?taxon_id",true);
    	b.addClause("?taxon_id rdfs:label ?taxon",true);
    	b.addClause("?anatomy_id rdfs:label ?anatomy",true);
    	b.addText("}");
    	//b.debug();
    	return b.finish();
    }
    
    final static String NAME2BEHAVIORIDBASE = SparqlBuilder.OBOPREFIX +
        	"SELECT ?behavior_name ?behavior_id \n" +
            "WHERE {?behavior_id rdfs:label \"%s\"^^xsd:string . \n" +
            "       ?behavior_id rdfs:label ?behavior_name . }\n ";
    		
    
    String getName2BehaviorIdQuery(String name){
    	return String.format(NAME2BEHAVIORIDBASE, name);
    }


}
