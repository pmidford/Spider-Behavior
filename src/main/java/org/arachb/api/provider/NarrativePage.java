package org.arachb.api.provider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.arachb.api.SparqlBuilder;
import org.arachb.api.Util;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
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
		eventsTableString = queryEvents();
		updateLabel(mds);
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


	private String queryEvents(){
		String target = metadata.get("localIdentifier");
		String query = narrativeEventsQuery(target);
		List<TupleQueryResult> resultList = new ArrayList<TupleQueryResult>();

		System.out.println("narrative query = " + query);
		try {
			resultList = Util.tryAndAccumulateQueryResult(resultList, query, con);

			if (resultList.isEmpty()){
				return "";
			}
			else{
				return queryEvents1(resultList);
			}
		}
		catch(RepositoryException | MalformedQueryException | QueryEvaluationException e){
			return "";
		}
	}


	private String queryEvents1(List<TupleQueryResult> resultList){
		final String ls = System.lineSeparator();
		String result = "<table class=\"table\">" + ls;
		try{
			boolean addHeaders = true;
			for(TupleQueryResult rn : resultList){
				while (rn.hasNext()){
					final BindingSet bSet = rn.next();
					List<String> names = Arrays.asList(eventsSelectVars);
					if (addHeaders){
						StringBuilder headers = new StringBuilder();
						headers.append("   <tr>" + ls);
						for (String name : names){
							headers.append(String.format("    <th>%s</th>%n",name));
						}
						headers.append("   </tr>" + ls);
						headers.append("   <tr>" + ls);
						result += headers.toString();
						addHeaders = false;
					}
					for (String name : names){
						final Binding b = bSet.getBinding(name);
						if (b!= null){
							result += String.format("<td>%s</td>%n",
									b.getValue().stringValue());
						}
						else {
							result += "<td></td>" + ls;
							return null;
						}
					}
					result += "   </tr>";
				}
			}
		} catch (QueryEvaluationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			result += "</table>" + System.lineSeparator();
		}
		return result;
	}

	private void updateLabel(MetaDataSummary mds)throws RepositoryException, MalformedQueryException, QueryEvaluationException{
		String target = metadata.get("localIdentifier");
		String query = narrativeEventsQuery(target);
		List<TupleQueryResult> queryResults = new ArrayList<TupleQueryResult>();
    	queryResults = Util.tryAndAccumulateQueryResult(queryResults, query, con);
    	String label = Util.getOneResult(queryResults,"narrative");
    	mds.add("label", label);
	}

	private String labelSelectLine =
			"SELECT ?narrative %n";

	String narrativeLabelQuery(String target){
		SparqlBuilder b = SparqlBuilder.startSparql();
		b.addText(labelSelectLine);
		String line1 = String.format("WHERE {<%s> rdfs:label ?narrative . } %n", target);
		b.addText(line1);
		b.debug();
		return b.finish();
	}



	private final String[] eventsSelectVars = {"event","narrative","behavior","behavior_id","anatomy","anatomy_id","subject","individual"};


    String narrativeEventsQuery(String target){
    	SparqlBuilder b = SparqlBuilder.startSparqlWithOBO();
		b.addEventsSelectLine(eventsSelectVars);
		String line1 = String.format("WHERE { ?event obo:BFO_0000050  <%s> . %n",target);
		b.addText(line1);
        String line2 = String.format("<%s> rdfs:label ?narrative . %n",target);
        b.addText(line2);
        b.addClause("?event rdf:type ?i1",true);
        b.addClause("?i1 owl:intersectionOf ?i2",true);
        b.addClause("?i2 rdf:first ?behavior_id",true);
        b.addClause("?behavior_id rdfs:label ?behavior",true);
        b.addClause("?event obo:RO_0002218 ?anatomy_id",true);
        b.addClause("?anatomy_id obo:BFO_0000050 ?subject",true);
        b.addClause("?anatomy_id rdfs:label ?anatomy",true);
        b.addClause("?subject rdfs:label ?individual",true);
    	b.addText("} %n");
    	b.debug();
    	return b.finish();
    }



}
