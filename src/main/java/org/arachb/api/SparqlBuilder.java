package org.arachb.api;

import org.apache.log4j.Logger;

public class SparqlBuilder {
	
	final static String OBOPREFIX = "prefix obo:<http://purl.obolibrary.org/obo/> ";


	private final StringBuilder b = new StringBuilder(500);

	private static final Logger log = Logger.getLogger(SparqlBuilder.class);
	private SparqlBuilder(){

	}

	public static SparqlBuilder startSparql(){
		return new SparqlBuilder();
	}

	public static SparqlBuilder startSparqlWithOBO(){
		SparqlBuilder b = new SparqlBuilder();
		b.addText(OBOPREFIX, true);
		b.addText("\n",true);
		return b;
	}
	
	public void addSelectLine(String[] varArray) {
		b.append("SELECT ");
		for (String var : varArray){
			b.append("?").append(var).append(" ");
		}
		b.append(System.lineSeparator());
	}


	public void addText(String text, boolean engage){
		if (engage){
			b.append(text);
		}
	}

	public void addText(String text){
		b.append(text);
	}


	final static String CLAUSEINDENT = "       ";
	final static String CLAUSE_END = ". " + System.lineSeparator();
	public void addClause(String clauseBase, boolean engage){
		if (engage){
			b.append(CLAUSEINDENT);
			b.append(clauseBase);
			b.append(CLAUSE_END);
		}
	}

	public void addClause(String clauseBase){
		b.append(CLAUSEINDENT);
		b.append(clauseBase);
		b.append(CLAUSE_END);
	}


	public void debug(){
		log.info("SparqlBuilder buffer contains " + b.toString());
	}


	public String finish(){
		return b.toString();
	}
}
