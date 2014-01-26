package org.arachb.api;

import java.net.URI;

import junit.framework.TestCase;

public class TestTaxon extends TestCase {
	
	final static String TAXONNCBIID = "http://purl.obolibrary.org/obo/NCBITaxon_336608";

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testGetName2TaxonQuery() {
		Taxon testTaxon = new Taxon();
		String testQuery = testTaxon.getName2TaxonQuery("Habronattus");
		//System.out.println(testQuery);
		assert(testQuery.contains("Habronattus"));
	}
	
	public void testGetTaxon2partQuery(){
		Taxon testTaxon = new Taxon();
		String testQuery = testTaxon.getTaxon2partQuery(TAXONNCBIID);
		assert(testQuery.contains(TAXONNCBIID));
	}

}
