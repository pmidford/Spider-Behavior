package org.arachb.api;

import java.io.IOException;
import java.io.OutputStream;

import org.openrdf.query.BooleanQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

public class Util {

	final static String USERHOME = System.getProperty("user.home");
	final static String ADUNAHOME = USERHOME+"/.aduna/";
	final static String SPARQLMIMETYPE = "application/sparql-results+json";
	final static String BASEURI = "http://arachb.org/arachb/arachb.owl";
	final static String REPONAME = "test1";




	public static boolean askQuery(String queryString, RepositoryConnection con)
			throws RepositoryException, MalformedQueryException, QueryEvaluationException{
		final BooleanQuery bQuery = con.prepareBooleanQuery(QueryLanguage.SPARQL, queryString);
		return bQuery.evaluate();
	}





}
