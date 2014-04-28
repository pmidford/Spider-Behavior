package org.arachb.api;

import junit.framework.TestCase;

public class TestTaxon extends TestCase {
	
	final static String TAXONNCBIID = "http://purl.obolibrary.org/obo/NCBITaxon_336608";

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testGetName2EthogramQuery() {
		Taxon testTaxon = new Taxon();
		String testQuery = testTaxon.getName2EthogramQuery("Habronattus");
		//System.out.println(testQuery);
		assert(testQuery.contains("Habronattus"));
	}
	
	public void testGetName2TaxonIdQuery(){
		Taxon testTaxon = new Taxon();
		String testQuery = testTaxon.getName2TaxonIdQuery("Habronattus");
		assert(testQuery.contains("Habronattus"));
	}

}
