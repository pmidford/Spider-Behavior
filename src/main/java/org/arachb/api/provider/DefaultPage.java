package org.arachb.api.provider;

import java.util.HashMap;
import java.util.Map;

public class DefaultPage extends AbstractPage {

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

	
	public DefaultPage(MetaDataSummary mds){
		super(mds);
	}

	@Override
	public String generateHTML(){
		String result = "";
		result += "<!DOCTYPE html>";
		result += "<html lang=\"en\">";
		result += "<head>";
		result += generateHeader("unknown");
		result += "</head>";
		result += "<body>";

		result += addNavBar();
		result += "   <div class=\"container\">";
		if (metadata.contains("title")){
			result += String.format("      <h3>%s</h3>",metadata.get("title"));
		}
		else {
			result += String.format("      <h3>%s</h3>",metadata.get("localIdentifier"));			
		}
		result += "   </div>";
		result += "   <div class=\"row\">";
		result += "      <div class=\"col-md-11\">";
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




}
