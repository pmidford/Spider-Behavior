package org.arachb.api;

import org.junit.Before;
import org.junit.Test;

public class TestTaxon {
	
	final static String TAXONNCBIID = "http://purl.obolibrary.org/obo/NCBITaxon_336608";

	@Before
	public void setUp() { //throws Exception {
	}

	@Test
	public void testGetName2EthogramQuery() {
		Taxon testTaxon = new Taxon();
		String testQuery = testTaxon.getName2GeneralQuery("Habronattus");
		System.out.println(testQuery);
		assert(testQuery.contains("Habronattus"));
	}
	

	/**
	 * Tests monomial and binomial taxa are accepted (no trinomials currently).
	 * Now checks  strings that might attack via CVE-2021-44228 - log4j vulnerability
	 */
	@Test
	public void testValidateTaxonName(){
		Taxon testTaxon = new Taxon();
		assert testTaxon.validateTaxonName("Habronattus");
		assert testTaxon.validateTaxonName("Habronattus californicus");
		assert !testTaxon.validateTaxonName("jndi:{}");
		assert !testTaxon.validateTaxonName("jndi%3A%7B%7D");
	}

}
