package org.arachb.api.provider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.arachb.api.SparqlBuilder;
import org.arachb.api.Util;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;


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

	RepositoryConnection con;
	String eventsTableString;
	
	public NarrativePage(MetaDataSummary mds, RepositoryConnection c) 
			throws RepositoryException, MalformedQueryException, QueryEvaluationException, IOException{
		super(mds);
		con = c;
		eventsTableString = fillEvents();
	}
	
	
	@Override
	public String generateHTML() throws IOException  {
		String result = "";
		result += "<!DOCTYPE html>";
		result += "<html lang=\"en\">";
		result += "<head>";
		result += generateHeader("Narrative");
		result += "</head>";
		result += "<body>";
		
		result += addNavBar();
		result += "   <div class=\"container\">";
		result += String.format("      <h3>%s</h3>",metadata.get("localIdentifier"));			//name?
		result += "   </div>";
		result += "   <div class=\"row\">";
		result += "      <div class=\"span11\">";
		result += "         <div id=\"results\">";
		result += metadata.generateResults(Field.values());
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

	
	private String fillEvents() throws RepositoryException, MalformedQueryException, QueryEvaluationException, IOException{
		String target = metadata.get("localIdentifier");
		String query = narrativeEventsQuery(target);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		List<TupleQueryResult> resultList = new ArrayList<TupleQueryResult>();

		System.out.println("narrative query = " + query);
		resultList = Util.tryAndAccumulateQueryResult(resultList, query, con);

		if (resultList.isEmpty()){
			Util.noResultsError(baos);
		}
		else{
			Util.jsonFormatResultList(resultList,baos);
		}
		String eventsTableJson = baos.toString("utf-8");
		return eventsTableJson;
	}
	
	
    private String selectLine =
    		"SELECT ?event %n";


    String narrativeEventsQuery(String target){
    	SparqlBuilder b = SparqlBuilder.startSparqlWithOBO();
		b.addText(selectLine);
		String line1 = String.format("WHERE { ?event obo:BFO_0000050  <%s> . %n",target);
		b.addText(line1);
    	b.addText("} %n");
    	b.debug();
    	return b.finish();
    }


	
}
