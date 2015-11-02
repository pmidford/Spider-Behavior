package org.arachb.api.provider;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;


public class TestMetaDataSummary {

	
	//ugly, but apparently necessary
	enum Field implements PageField{
		FIELD1("field1","field1",""),
		FIELD2("field2","field2","");
		
		private final String key;
		private final String displayStr;
		private final String separator;
		static final Map<String,Field> keylookup = new HashMap<>();
		
		Field(String k,String ds,String s){
			this.key = k;
			this.displayStr = ds;
			this.separator = s;
		}
		
		@Override
		public String displayString(){
			return displayStr;
		}
		
		@Override
		public String formatValue(String fieldValue){
			return fieldValue;
		}

		@Override
		public String keyString() {
			return key;
		}

		@Override
		public PageField lookupKey(String key) {
			return Field.keylookup.get(key);
		}
	}

	
	
	final static String dummyJson = "{\"field1\": \"Habronattus\" , \"field2\": 2}";
	
	static private MetaDataSummary mds1; 
	private MetaDataSummary mds2;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		mds1 = new MetaDataSummary(dummyJson);
	}

	@Before
	public void setUp() throws Exception {
		mds2 = new MetaDataSummary();
	}

	@After
	public void tearDown() throws Exception {
		mds2 = null;
	}

	@Test
	public void testMetaDataSummary() {
		MetaDataSummary mds = new MetaDataSummary();
		assertThat(mds.raw(),equalTo(null));
	}

	@Test
	public void testMetaDataSummaryString() throws IOException {
		assertThat(mds1.raw(),equalTo(dummyJson));
	}

	@Test
	public void testRaw() throws IOException {
		assertThat(mds1.raw(),equalTo(dummyJson));
	}

	@Test
	public void testContains() {
		assert(mds1.contains("field1"));
		assert(mds1.contains("field2"));
		assertFalse(mds1.contains("bad field"));
	}

	@Test
	public void testAdd() {
		mds2.add("testfield", "testvalue");
		assert(mds2.contains("testfield"));
		assertThat(mds2.get("testfield"),equalTo("testvalue"));
		assertFalse(mds2.contains("testfield2"));
	}

	@Test
	public void testGet() {
		assertThat(mds1.get("field1"),equalTo("Habronattus"));
		assertThat(mds1.get("field2"),equalTo("2"));
		assertThat(mds1.get("field3"),equalTo(null));
	}

	@Test
	public void testGenerateResults() {
		String fieldList = mds1.generateResults(Field.values()).trim();
		String startStr = fieldList.substring(0, 4);
		String endStr = fieldList.substring(fieldList.length()-5);
		assertThat(startStr,equalTo("<ul>"));
		assertThat(endStr,equalTo("</ul>"));
	}

}
