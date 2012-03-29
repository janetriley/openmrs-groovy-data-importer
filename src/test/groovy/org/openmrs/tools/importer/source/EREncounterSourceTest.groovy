package org.openmrs.tools.importer.source;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

class EREncounterSourceTest {

    EREncounterSource mySource = null;
    def filepath="/Volumes/ETUI/prod_exports/sept_exports/encounters/teeny_er_sample.txt";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
	mySource = new EREncounterSource(filepath);
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


    //"Date","PatientID","ERID","From","VisitType","DxID1","DxID2","DxID3","Treatment","Ketamine","Observation","TimeInER","DischargeTo","Comment"

    static def getSampleValues(){
	return	[
	    "Date": " 6/25/2407 5:25:13",
	    "PatientID": "2007-012018",
	    "ERID": 53711,
	    "From": -1383732646,
	    "VisitType": 3,
	    "DxID1": "A91",
	    "DxID2": "G40",
	    "DxID3": "T30",
	    "Treatment":-2091716494 ,
	    "Ketamine":0,
	    "Observation": 1,
	    "TimeInER": 6,
	    "DischargeTo": 1276675133,
	    "Comment":"A comment"];
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
	def sample = getSampleValuesString();
	mySource = new EREncounterSource(new StringReader(sample));
	assertNotNull(mySource);
	assertTrue(mySource.hasNext());
    }

    @Test
    public void testGet() {
	def sample = getSampleValuesString();
	def reader = new StringReader(sample);
	mySource = new EREncounterSource(reader);
	mySource.next();
	def values = getSampleValues();

	assertEquals( mySource.get("legacyTable"), "tblERPatientsInfo");
	assertEquals( mySource.get("legacyEncounterId"), values["ERID"]);

	[
	    //these return actual values
	    "DxID1",
	    "DxID2",
	    "DxID3",
	    "Comment"
	].each(){ it->
	    assertEquals(mySource.get(it), values[it]);
	}

	[
	    //these are translated from numbers to codes - will check actual result in separate tests
	    "From",
	    "VisitType",
	    "Treatment",
	    "TimeInER",
	    "DischargeTo",
	].each(){ it->
	    assertNotNull(mySource.get(it));
	}
	[
	    "DateCreated",
	    "Date"
	].each(){ it->
	    assertTrue(mySource.get(it) instanceof java.util.Date);
	}

	[
	    "Ketamine",
	    "Observation",
	].each(){ it->
	    def shouldBe = (values[it] == 1 || values[it] == "1" ) ? Boolean.TRUE : Boolean.FALSE;
	    assertEquals("Checking ${it}",mySource.get(it), shouldBe);
	    assertTrue(mySource.get(it) instanceof Boolean);
	}
    }


    @Test
    public void testFrom(){
	def values = [
		    1:"ICU",
		    2:"IPD",
		    3:"OPD",
		    4:"LAU",
		    5:"ER",
		    1052195837:"OT",
		    1129191084:"Surgery",
		    1233386422:"Minor Procedure",
		    1296207625:"Homecare",
		    "-1383732646" : "Direct Admission",
		]
	checkField("From", values);

    }
    @Test
    public void testVisitType(){
	def values = [
		    1:"EM",
		    2:"Non-EM",
		    3:"Urgent",
		]
	checkField("VisitType", values);

    }

    @Test
    public void testTreatment(){
	def values = [
		    "-2091716494": "Minor Procedure",
		    "-65359861": "Cast/Cast Removal",
		    9913350: "Emergency-Non Trauma",
		    183968164: "Dressing Change",
		    715801045: "Minor Surgery",
		    1697929487: "Consult",
		    2140836921: "Emergency-Trauma",
		]
	checkField("Treatment", values);

    }


    @Test
    public void testTimeInER(){
	def values = [
		    1: "15 Minutes",
		    2: "30 Minutes",
		    3: "60 Minutes",
		    4: "90 Minutes",
		    5: "120 Minutes",
		    6: ">120 Minutes",
		    999999999:null
		]
	checkField("TimeInER", values);

    }

    @Test
    public void testDischargeTo(){
	def values = [
		    "-1893580051":"ICU",
		    "-711104359":"DISCHARGE TO HOME",
		    "-505095962":"Death",
		    "-427741668":"LAU",
		    1049893479:"LAMA",
		    1050082169:"IPD",
		    1050089646:"ER",
		    1276675133:"Surgery",
		    1952110970:"OT",
		    999999999:null,
		]
	checkField("DischargeTo", values);

    }

/*
    @Test
    public void t(){
	def values = [
		    "bogus":null,
		]
	checkField("", values);

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
