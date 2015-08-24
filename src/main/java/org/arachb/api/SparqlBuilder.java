package org.arachb.api;

public class SparqlBuilder {

	private final StringBuilder b = new StringBuilder(500);

	private SparqlBuilder(){

	}

	public static SparqlBuilder startSparql(){
		return new SparqlBuilder();
	}

	public static SparqlBuilder startSparqlWithOBO(){
		SparqlBuilder b = new SparqlBuilder();
		b.addText(Util.OBOPREFIX, true);
		b.addText("%n",true);
		return b;
	}

	public void addText(String text, boolean engage){
		if (engage){
			b.append(String.format(text));
		}
	}

	public void addText(String text){
		b.append(String.format(text));
	}


	final static String CLAUSEINDENT = "       ";
	public void addClause(String clauseBase, boolean engage){
		if (engage){
			b.append(CLAUSEINDENT);
			b.append(clauseBase);
			b.append(String.format(". %n"));
		}
	}

	public void addClause(String clauseBase){
		b.append(CLAUSEINDENT);
		b.append(clauseBase);
		b.append(String.format(". %n"));
	}


	public void debug(){
		System.out.println("SparqlBuilder buffer contains " + b.toString());
	}


	public String finish(){
		return b.toString();
	}
}
