package org.arachb.api;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.servlet.ServletException;
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
 * This servlet should handle urls beginning with api.arachb.org/behavior
 * @author pmidford
 *
 */
public class Behavior extends HttpServlet {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static Logger log = Logger.getLogger(Behavior.class);


	@Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

		final OutputStream os = response.getOutputStream();

		String name = request.getQueryString();
		//System.out.println("raw string is: |" + name);
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
			String errorStr = "Failed to validate behavior Name %s"; 
			ps.printf(errorStr, name);
			log.error(String.format(errorStr, name));
			return;
		}
		response.setContentType(Util.SPARQLMIMETYPE);
		Repository repo = null;
		RepositoryConnection con = null;
		LocalRepositoryManager manager = new LocalRepositoryManager(new File(Util.RDF4JHOME));

		try {
			System.out.println("About to query");
			manager.initialize();
			repo = manager.getRepository(Util.REPONAME);
			con = repo.getConnection();
			String behaviorQueryString = getName2BehaviorReportQuery(name);
			ResultTable result = new ResultTable(response);
			result.tryAndAccumulateQueryResult(behaviorQueryString,con);
			if (!result.isEmpty()){
				result.jsonFormatResultList();
			}
			else{
				String taxonIdQueryString = getName2BehaviorIdQuery(name);
				result.tryAndAccumulateQueryResult(taxonIdQueryString, con);
				if (!result.isEmpty()){
					result.jsonFormatResultList();
				}
				else{
					result.noResultsError(behaviorQueryString);
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
    
    

    /**
     * 
     * @param name
     * @return true if a behavior name
     */
    public boolean validateBehaviorName(String name){
    	String[]nameList = name.split(" ");   //TODO better validation
    	return (nameList.length<=3);
    }

    
    
    String getName2BehaviorReportQuery(String name){
    	SparqlBuilder b = SparqlBuilder.startSparql();
    	String selectLine = "SELECT ?behavior ?taxon ?anatomy%n";
    	b.addText(selectLine);
    	String whereLine =
    			String.format("WHERE {?behavior_id rdfs:label \"%s\"^^xsd:string . %n", name);
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
    	b.addText("} %n");
    	b.debug();
    	return b.finish();
    }
    
    final static String NAME2BEHAVIORIDBASE = SparqlBuilder.OBOPREFIX +
        	"SELECT ?behavior_name ?behavior_id %n" +
            "WHERE {?behavior_id rdfs:label \"%s\"^^xsd:string . %n" +
            "       ?behavior_id rdfs:label ?behavior_name . }%n ";
    		
    
    String getName2BehaviorIdQuery(String name){
    	return String.format(NAME2BEHAVIORIDBASE, name);
    }


}
