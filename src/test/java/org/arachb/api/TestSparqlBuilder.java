package org.arachb.api;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestSparqlBuilder {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testStartSparql() {
		SparqlBuilder b1 = SparqlBuilder.startSparql();
		assertThat(b1.finish(),equalTo(""));
	}

	@Test
	public void testStartSparqlWithOBO() {
		SparqlBuilder b1 = SparqlBuilder.startSparqlWithOBO();
		assertThat(b1.finish(),equalTo(SparqlBuilder.OBOPREFIX + System.lineSeparator()));
	}

	
	private final String[] emptyVars = {};
	private final String[] eventsSelectVars = {"event","narrative","behavior","behavior_id","anatomy","anatomy_id","subject","individual"};


	@Test
	public void testAddEventsSelectLine() {
		SparqlBuilder b1 = SparqlBuilder.startSparql();
		b1.addEventsSelectLine(emptyVars);
		assertThat(b1.finish(),equalTo("SELECT \n"));
		SparqlBuilder b2 = SparqlBuilder.startSparql();
		b2.addEventsSelectLine(eventsSelectVars);
		assertThat(b2.finish(),
				equalTo("SELECT ?event ?narrative ?behavior ?behavior_id ?anatomy ?anatomy_id ?subject ?individual \n"));
	}

	@Test
	public void testAddTextStringBoolean() {
		SparqlBuilder b1 = SparqlBuilder.startSparql();
		b1.addText("Test text",true);
		assertThat(b1.finish(),equalTo("Test text"));
		b1.addText("More text",false);
		assertThat(b1.finish(),equalTo("Test text"));
	}

	@Test
	public void testAddTextString() {
		SparqlBuilder b1 = SparqlBuilder.startSparql();
		b1.addText("Test text");
		assertThat(b1.finish(),equalTo("Test text"));
		b1.addText(" More text");
		assertThat(b1.finish(),equalTo("Test text More text"));
	}

	@Test
	public void testAddClauseStringBoolean() {
		SparqlBuilder b1 = SparqlBuilder.startSparql();
		b1.addClause("?i2 rdf:first ?behavior_id",true);
		assertThat(b1.finish(),equalTo("       ?i2 rdf:first ?behavior_id. " + System.lineSeparator()));
		b1.addClause("?i2 rdf:first ?behavior_id",false);
		assertThat(b1.finish(),equalTo("       ?i2 rdf:first ?behavior_id. " + System.lineSeparator()));
	}

	@Test
	public void testAddClauseString() {
		SparqlBuilder b1 = SparqlBuilder.startSparql();
		b1.addClause("?i2 rdf:first ?behavior_id");
		assertThat(b1.finish(),equalTo("       ?i2 rdf:first ?behavior_id. " + System.lineSeparator()));
		b1.addClause("?i2 rdf:first ?behavior_id");
		assertThat(b1.finish(),not(equalTo("       ?i2 rdf:first ?behavior_id. " + System.lineSeparator())));
	}

	@Test
	public void testFinish() {
		SparqlBuilder b1 = SparqlBuilder.startSparql();
		assertThat(b1.finish(),equalTo(""));
	}

}
