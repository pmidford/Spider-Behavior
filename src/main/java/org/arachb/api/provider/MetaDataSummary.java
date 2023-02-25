package org.arachb.api.provider;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class MetaDataSummary{

	final String raw;
	final Map<String,String>contents = new HashMap<>(); 
	
	/**
	 * Constructor for term w/o json in rdf:comment
	 */
	public MetaDataSummary(){
		raw = null;
	}
	
	/**
	 * Constructor for term with json available
	 * @param commentString contains json definition (or not)
	 * @throws IOException
	 */
	public MetaDataSummary(String commentString) throws IOException{
		raw = commentString;
		JsonFactory jsonF = new JsonFactory();
		JsonParser jp = jsonF.createParser(raw);
		if (jp.nextToken() != JsonToken.START_OBJECT) {
			throw new IOException("Expected data to start with an Object");
		}
		// Iterate over object fields:
		while (jp.nextToken() != JsonToken.END_OBJECT) {
			String fieldName = jp.getCurrentName();
			jp.nextToken();	
			contents.put(fieldName,jp.getText());
		}
		jp.close();
	}

	public String raw(){
		return raw;
	}
	
	public boolean contains(String key){
		return contents.containsKey(key);
	}
	
	public void add(String key, String val){
		contents.put(key, val);
	}
	
	public String get(String key){
		return contents.get(key);
	}
	
	final static String DEFAULTLISTSTART = "            <ul>" + System.lineSeparator();
	final static String DEFAULTLISTEND = "            </ul>" + System.lineSeparator();
	final static String DEFAULTHTMLFIELDTEMPLATE =  "               <li><strong>%s: </strong>%s</li>\n";
	
	
	/**
	 * Generates html string for metadata contents
	 * @param fields
	 * @return HTML rendering (unordered list) of field-value pairs
	 */
	public String generateResults(PageField[] fields){
		StringBuilder result = new StringBuilder(fields.length*50);	
		result.append(DEFAULTLISTSTART);
		for (PageField field : fields){
			result.append(String.format(DEFAULTHTMLFIELDTEMPLATE,
						                field.displayString(),
						                field.formatValue(get(field.keyString()))));
		}
		result.append(DEFAULTLISTEND);
		return result.toString();
	}
	

}
