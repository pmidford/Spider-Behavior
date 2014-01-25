package org.arachb.api;

import junit.framework.TestCase;

public class TestTaxon extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testGetStringLiteralQuery() {
		Taxon testTaxon = new Taxon();
		String testQuery = testTaxon.getStringLiteralQuery("Habronattus");
		//System.out.println(testQuery);
		assert(testQuery.contains("Habronattus"));
	}

}
