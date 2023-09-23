package org.arachb.api.provider;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.arachb.api.ResultTable;
import org.arachb.api.SparqlBuilder;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;


public class NarrativePage extends AbstractPage {

	enum Field implements PageField{
		LOCALID("localIdentifier","local identifier","");

		private final String key;
		private final String displayStr;
		private final String separator;
		static final Map<String,Field> keylookup = new HashMap<>();

		Field(String k,String ds,String s){
			this.key = k;
			this.displayStr = ds;
			this.separator = s;
		}

		@Override
		public String displayString(){
			return displayStr;
		}

		@Override
		public String formatValue(String fieldValue){
			if ("".equals(separator)){
				return fieldValue;
			}
			else {
				return fieldValue;
			}
		}

		@Override
		public String keyString() {
			return key;
		}

		@Override
		public PageField lookupKey(String key) {
			return Field.keylookup.get(key);
		}
	}

	final RepositoryConnection con;
	String eventsTableString;
	private static final Logger log = Logger.getLogger(NarrativePage.class);

	public NarrativePage(MetaDataSummary mds, RepositoryConnection c, HttpServletResponse response)
			throws RepositoryException, MalformedQueryException, QueryEvaluationException, IOException{
		super(mds);
		log.info("Creating Narrative Page");
		con = c;
		eventsTableString = queryEvents(response);
		log.info("Narrative page checkpoint 1");
		updateLabel(mds, response);
		log.info("Finished creating narrative page");
	}


	@Override
	public String generateHTML() {
		log.info("Starting to generate narrative page");
		String result = "";
		result += "<!DOCTYPE html>";
		result += "<html lang=\"en\">";
		result += "<head>";
		result += generateHeader("Narrative");
		result += "</head>";
		result += "<body>";

		result += addNavBar();
		result += "   <div class=\"container\">";
		result += String.format("      <h3>%s</h3>",metadata.get("label"));
		result += "   </div>";
		result += "   <div class=\"row\">";
		result += "      <div class=\"col-md-11\">";
		result += "         <div id=\"results\">";
		result += metadata.generateResults(Field.values());
		result += "		    </div>";
		result += "      </div>";
		result += "   </div>";
		result += "   <div class=\"row\">";
		result += "      <div class=\"col-md-11\">";
		result += "         <div id=\"events\">";
		result += eventsTableString;
		result += "		    </div>";
		result += "      </div>";
		result += "   </div>";
		result += "   <hr/>";
		result += addFooter();
		result += addScript(JQUERYSTRING);
		result += addScript(BOOTSTRAPSTRING);
		result += addScript(ENVSTRING);
		result += "</body>";
		result += "</html>";
		return result;
	}

	private final String[] eventsSelectVars = //{"narrative","event","behavior","behavior_id","anatomy","anatomy_id","individual","subject"};
	                                          {"narrative_id","narrative","behavior_id","behavior","anatomy_id","anatomy","subject_id","subject"};

	private String queryEvents(HttpServletResponse response) throws IOException{
		String target = metadata.get("localIdentifier");
		String query = narrativeEventsQuery(target);
		ResultTable resultTable = new ResultTable(response);

		System.out.println("narrative query = " + query);
		try {
			resultTable.tryAndAccumulateQueryResult(query, con);
			log.info("Return from tryAndAccumulateQuery result" + resultTable.getContents());
			if (resultTable.isEmpty()){
				return "";
			}
			else{
				log.info("Trying to format columns");
				String foo = resultTable.htmlFormatPairedColumns(eventsSelectVars);
				log.info("Have formatted columns " + foo);
				return foo;
			}
		}
		catch(RepositoryException e){
			response.setStatus(500);
			PrintStream ps = new PrintStream(response.getOutputStream());
			e.printStackTrace(ps);
			log.error("Repository Exception: ",e);
		}
		catch(MalformedQueryException e){
			response.setStatus(500);
			PrintStream ps = new PrintStream(response.getOutputStream());
			e.printStackTrace(ps);
			log.error(String.format("Malformed query: %s ",query),e);
			
		}
		catch(QueryEvaluationException e){
			response.setStatus(500);
			PrintStream ps = new PrintStream(response.getOutputStream());
			e.printStackTrace(ps);
			log.error(String.format("Query evaluation problem: %s ",query),e);
		}

		return "";
	}



	private void updateLabel(MetaDataSummary mds, HttpServletResponse response)
			throws RepositoryException, MalformedQueryException, QueryEvaluationException{
		String target = metadata.get("localIdentifier");
		String query = narrativeEventsQuery(target);
		ResultTable resultTable = new ResultTable(response);
    	resultTable.tryAndAccumulateQueryResult(query, con);
    	String label = resultTable.getOneResult("narrative");
    	mds.add("label", label);
	}


	String narrativeLabelQuery(String target){
		final String labelSelectLine ="SELECT ?narrative \n";
		SparqlBuilder b = SparqlBuilder.startSparql();
		b.addText(labelSelectLine);
		String line1 = String.format("WHERE {<%s> rdfs:label ?narrative . } \n", target);
		b.addText(line1);
		//b.debug();
		return b.finish();
	}




    String narrativeEventsQuery(String target){
    	SparqlBuilder b = SparqlBuilder.startSparqlWithOBO();
		b.addSelectLine(eventsSelectVars);
		String line1 = String.format("WHERE { ?narrative_id obo:BFO_0000050  <%s> . \n",target);
		b.addText(line1);
        String line2 = String.format("<%s> rdfs:label ?narrative . \n",target);
        b.addText(line2);
        b.addClause("?event rdf:type ?i1",true);
        b.addClause("?i1 owl:intersectionOf ?i2",true);
        b.addClause("?i2 rdf:first ?behavior_id",true);
        b.addClause("?behavior_id rdfs:label ?behavior",true);
        b.addClause("?event obo:RO_0002218 ?anatomy_id",true);
        b.addClause("?anatomy_id obo:BFO_0000050 ?subject_id",true);
        b.addClause("?anatomy_id rdfs:label ?anatomy",true);
        b.addClause("?subject_id rdfs:label ?subject",true);
    	b.addText("} \n");
    	//b.debug();
    	return b.finish();
    }



}
