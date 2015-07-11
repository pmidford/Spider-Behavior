package org.arachb.api;

public class SparqlBuilder {
	
	private final StringBuilder b = new StringBuilder(500);
	
	private SparqlBuilder(){
		
	}

	public static SparqlBuilder startSparql(){
		return new SparqlBuilder();
	}
	
	public void addText(String text, boolean engage){
		if (engage){
			b.append(String.format(text));
		}
	}
	
	public void addClause(String clauseBase, boolean engage){
		if (engage){
			b.append("       ");
			b.append(clauseBase);
			b.append(String.format(". %n"));
		}
	}
	
	
	public String finish(){
		return b.toString();
	}
}
