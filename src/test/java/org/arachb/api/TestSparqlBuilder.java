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



	String getName2GeneralQuery(String name){
		SparqlBuilder b = SparqlBuilder.startSparqlWithOBO();
		b.addText("SELECT ?taxon ?child_taxon ?behavior ?behavior_id ?anatomy ?anatomy_id ?publication ?pubid \n", true);
		String line1 = String.format("WHERE { ?parent_taxon rdfs:label \"%s\"^^xsd:string . \n",name);
		b.addText(line1,true);
		b.addClause("?child_taxon rdfs:subClassOf* ?parent_taxon",true);
		b.addClause("?r1 owl:someValuesFrom ?child_taxon",true);
		b.addClause("?res1 rdf:first ?r1",true);
		b.addClause("?n3 rdf:rest ?res1",true);
		b.addClause("?n3 rdf:first ?anatomy_id",true);
		b.addClause("?anatomy_id rdfs:label ?anatomy",true);
		b.addClause("?s5 owl:intersectionOf ?n3",true);
		b.addClause("?s6 owl:someValuesFrom ?s5",true);
		b.addClause("?s7 rdf:first ?s6",true);
		b.addClause("?s8 rdf:rest ?s7",true);
		b.addClause("?s8 rdf:first ?behavior_id",true);
		b.addClause("?behavior_id rdfs:label ?behavior",true);
		b.addClause("?s10 owl:intersectionOf ?s8",true);   //should be intersectionOf?
		b.addClause("?s11 owl:someValuesFrom ?s10 ",true);
		b.addClause("?s12 rdf:first ?s11",true);
		b.addClause("?s13 rdf:rest ?s12",true);
		b.addClause("?s13 rdf:first <http://purl.obolibrary.org/obo/IAO_0000300>", true);
		b.addClause("?s15 owl:intersectionOf ?s13",true);
		b.addClause("?s16 rdf:type ?s15",true);
		b.addClause("?s16 obo:BFO_0000050 ?pubid",true);
		b.addClause("?pubid rdfs:label ?publication",true);
		b.addClause("?child_taxon rdfs:label ?taxon",true);
		b.addText("} \n", true);
		//b.debug();
		return b.finish();
	}


	@Test
	public void testBehaviorQuery() {
		String Demo = getName2GeneralQuery("Tetragnatha");
		System.out.println(Demo);
	}

}
