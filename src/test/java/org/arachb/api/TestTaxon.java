package org.arachb.api;

import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;

//import static org.hamcrest.CoreMatchers.equalTo;
//import static org.junit.Assert.assertThat;



public class TestTaxon extends TestCase {
	
	final static String TAXONNCBIID = "http://purl.obolibrary.org/obo/NCBITaxon_336608";

	@Before
	protected void setUp() throws Exception {
	}

	@Test
	public void testGetName2EthogramQuery() {
		Taxon testTaxon = new Taxon();
		String testQuery = testTaxon.getName2GeneralQuery("Habronattus");
		System.out.println(testQuery);
		assert(testQuery.contains("Habronattus"));
	}
	
	@Test
	public void testGetName2TaxonIdQuery(){
		Taxon testTaxon = new Taxon();
		String testQuery = testTaxon.getName2TaxonIdQuery("Habronattus");
		assert(testQuery.contains("Habronattus"));
	}

}
