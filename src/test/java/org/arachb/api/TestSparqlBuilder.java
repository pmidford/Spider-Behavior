package org.arachb.api;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestSparqlBuilder {

	@BeforeClass
	public static void setUpBeforeClass() { //throws Exception {
		
	}

	@Before
	public void setUp() {  //throws Exception {
	}

	@After
	public void tearDown() {  //throws Exception {
	}

	@Test
	public void testStartSparql() {
		SparqlBuilder b1 = SparqlBuilder.startSparql();
		assertEquals("",b1.finish());
	}

	@Test
	public void testStartSparqlWithOBO() {
		SparqlBuilder b1 = SparqlBuilder.startSparqlWithOBO();
		assertEquals(SparqlBuilder.OBOPREFIX + System.lineSeparator(), b1.finish());
	}

	
	private final String[] emptyVars = {};
	private final String[] eventsSelectVars = {"event","narrative","behavior","behavior_id","anatomy","anatomy_id","subject","individual"};


	@Test
	public void testAddEventsSelectLine() {
		SparqlBuilder b1 = SparqlBuilder.startSparql();
		b1.addSelectLine(emptyVars);
		assertEquals("SELECT \n", b1.finish());
		SparqlBuilder b2 = SparqlBuilder.startSparql();
		b2.addSelectLine(eventsSelectVars);
		assertEquals("SELECT ?event ?narrative ?behavior ?behavior_id ?anatomy ?anatomy_id ?subject ?individual \n",
				b2.finish());
	}

	@Test
	public void testAddTextStringBoolean() {
		SparqlBuilder b1 = SparqlBuilder.startSparql();
		b1.addText("Test text",true);
		assertEquals("Test text", b1.finish());
		b1.addText("More text",false);
		assertEquals("Test text", b1.finish());
	}

	@Test
	public void testAddTextString() {
		SparqlBuilder b1 = SparqlBuilder.startSparql();
		final String baseStr = "Test text";
		b1.addText(baseStr);
		assertEquals(baseStr, b1.finish());
		b1.addText(" More text");
		assertEquals("Test text More text", b1.finish());
	}

	@Test
	public void testAddClauseStringBoolean() {
		SparqlBuilder b1 = SparqlBuilder.startSparql();
		final String baseStr = "?i2 rdf:first ?behavior_id";
		b1.addClause(baseStr,true);
		assertEquals("       ?i2 rdf:first ?behavior_id. " + System.lineSeparator(),b1.finish());
		b1.addClause(baseStr,false);
		assertEquals("       ?i2 rdf:first ?behavior_id. " + System.lineSeparator(), b1.finish());
	}

	@Test
	public void testAddClauseString() {
		SparqlBuilder b1 = SparqlBuilder.startSparql();
		b1.addClause("?i2 rdf:first ?behavior_id");
		assertEquals("       ?i2 rdf:first ?behavior_id. " + System.lineSeparator(),b1.finish());
		b1.addClause("?i2 rdf:first ?behavior_id");
		assertNotEquals("       ?i2 rdf:first ?behavior_id. " + System.lineSeparator(),b1.finish());
	}

	@Test
	public void testFinish() {
		SparqlBuilder b1 = SparqlBuilder.startSparql();
		assertEquals("", b1.finish());
	}

}
