package org.angkorhospital.importer.source;

import static org.junit.Assert.*;

import org.angkorhospital.importer.source.LabCrossmatchEncounterSource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

class LabCrossmatchEncounterSourceTest {

    LabCrossmatchEncounterSource mySource = null;
    def filepath="/Volumes/ETUI/prod_exports/sept_exports/labs/sampleCrossmatch.csv";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
	//mySource = new  LabCrossMatchEncounterSource(filepath);
	mySource = new  LabCrossmatchEncounterSource(new StringReader(getSampleValuesString()));
	mySource.next();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testInit() {
	assertTrue(mySource.hasNext());
	assertNotNull(mySource.headerList);
	assertTrue(mySource.headerList.size() > 0);
    }

    //"Date","CrossPatID","CrossMatchID",
    //"Ward","Doctor","Reason","Group","Bag No","Group of Bag","Volume ml","Product","Comment","Transfused"

    static def getSampleValues(){
	return	[
	    "Date":"9/8/2011 11:46:49",
	    "CrossPatID":"2011-018290",
	    "CrossMatchID":11065,
	    "Ward":"ER",
	    "Doctor":258,
	    "Reason":"A reason",
	    "Group":"B Rh Positive",
	    "Bag No":"4942",
	    "Group of Bag":"B Rh Positive",
	    "Volume ml":"30",
	    "Product":"PC",
	    "Comment":"A comment",
	    "Transfused":"Pending"];
    }

    static def getSampleValuesString(){
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


    @Test
    public void testInitWithReader() {
//	def sample = getSampleValuesString();
//	mySource = new  LabCrossMatchEncounterSource(new StringReader(sample));
	assertNotNull(mySource);
	assertTrue(mySource.hasNext());
    }

    @Test
    public void testGet() {

	def values = getSampleValues();

	//special keywords:
	assertEquals( mySource.get("legacyTable"), "LabDCrossmatch");
	assertEquals( mySource.get("legacyId"), values["CrossMatchID"]);
	assertEquals( mySource.get("patientId"), values["CrossPatID"]);

	//check all headers
	[   "CrossPatID",
	    "CrossMatchID",
	    "Ward",
	    "Doctor",
	    "Reason",
	    "Group",
	    "Bag No",
	    "Group of Bag",
	    "Volume ml",
	    "Product",
	    "Comment",
	    "Transfused"
	].each(){ it->
	    assertEquals("checking ${it}",mySource.get(it).toString(), values[it].toString());
	}

	[
	    "DateCreated",
	    "Date"
	].each(){ it->
	    assertTrue("checking ${it}",mySource.get(it) instanceof java.util.Date);
	}

    }
}
