package org.arachb.api;

import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;

public class Util {

	// Tomcat9 doesn't seem to play nice with user.home, so hard code until a better solution.
	//final static String USERHOME = System.getProperty("user.home");
	final static String USERHOME = "/home/spider-master";
	final static String RDF4JHOME = USERHOME+"/.rdf4j/";
	final static String SPARQLMIMETYPE = "application/sparql-results+json";
	final static String BASEURI = "http://arachb.org/arachb/arachb.owl";
	final static String REPONAME = "test1";




	public static boolean askQuery(String queryString, RepositoryConnection con)
			throws RepositoryException, MalformedQueryException, QueryEvaluationException{
		final BooleanQuery bQuery = con.prepareBooleanQuery(QueryLanguage.SPARQL, queryString);
		return bQuery.evaluate();
	}





}
