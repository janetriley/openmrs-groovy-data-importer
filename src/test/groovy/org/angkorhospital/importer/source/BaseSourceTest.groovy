package org.angkorhospital.importer.source;

import org.openmrs.tools.importer.source.*;


import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

abstract class BaseSourceTest {

    static sampleValues;

    org.openmrs.tools.importer.source.CsvFileSource mySource = null;
    def StringReader reader = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
	 reader = new StringReader(getSampleValuesString());
    }

    @After
    public void tearDown() throws Exception {
    }

    def getSampleValues(){return sampleValues;}

    def getSampleValuesString(){
	return getSampleValuesString(getSampleValues());
    }


    static def getSampleValuesString( values){
	StringBuilder header = null;
	StringBuilder data = null;

	values.each(){ key,value->
	    if( header == null ){
		header = new StringBuilder(key);
	    } else
		header.append("," + "${key}");
	    if( data == null ){
		data = new StringBuilder("${value}");
	    } else
		data.append("," + value);
	}

	return header.toString() + "\n" + data.toString();
    }
/*
    @Test
    public void testInit() {
	assertTrue(mySource.hasNext());
	assertNotNull(mySource.headerList);
	assertTrue(mySource.headerList.size() > 0);
    }

    @Test
    public void testInitWithReader() {
	assertNotNull(mySource);
	assertTrue(mySource.hasNext());
    }


    @Test
    public void testGetValueEquals( keys ) {
	def sample = getSampleValuesString();
	def reader = new StringReader(sample);
	mySource = new EREncounterSource(reader);
	mySource.next();
	def values = getSampleValues();

	keys.each(){ it->
	    assertEquals(mySource.get(it), values[it]);
	}
    }

    @Test
    public void testGetValueIsDate( keys ) {
	def sample = getSampleValuesString();
	def reader = new StringReader(sample);
	mySource = new EREncounterSource(reader);
	mySource.next();
	def values = getSampleValues();

	keys.each(){ it->
	    assertTrue(mySource.get(it) instanceof java.util.Date);
	}
    }

    @Test
    public void testGetNotNull( keys ) {
	def sample = getSampleValuesString();
	def reader = new StringReader(sample);
	mySource = new EREncounterSource(reader);
	mySource.next();
	def values = getSampleValues();

	assertEquals( mySource.get("legacyTable"), "tblERPatientsInfo");
	assertEquals( mySource.get("legacyEncounterId"), values["ERID"]);

	keys.each(){ it->
	    assertNotNull(mySource.get(it));
	}
    }
*/

    public void checkField(fieldName, values){
	def sample = getSampleValues();
	values.each(){ key, value ->
	    sample[fieldName] = key;
	    def sampleString = getSampleValuesString(sample);
	    mySource = new EREncounterSource(new StringReader(getSampleValuesString(sample)));
	    mySource.next();
	    assertEquals("Checking ${key} in field ${fieldName}",mySource.get(fieldName), value);
	}
    }


}
