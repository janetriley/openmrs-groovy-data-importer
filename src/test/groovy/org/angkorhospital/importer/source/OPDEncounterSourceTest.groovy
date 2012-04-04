package org.angkorhospital.importer.source;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

class OPDEncounterSourceTest {

	OPDEncounterSource mySource = null;
	def filepath="/Volumes/ETUI/prod_exports/sept_exports/encounters/tblOutPatientsInfo.txt";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		mySource = new OPDEncounterSource(filepath);
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


	static def getSampleValues(){
		return	["RegtID":999,
			"Reg_PatientID":"1900-012345",
			"Reg_DateOfVisit":"07/07/2007 1:23:45",
			"Registered":1,
			"FeePaid":0,
			"GeneralImpression":"sick",
			"Reg_DiseaseID":"I02",
			"Reg_DiseaseID2":"I05.1",
			"Reg_DiseaseID3":"I11",
			"ChronicUrlDxID":"n/a",
			"DidNotWait":1,
			"Return":1,
			"FollowUp":1,
			"Referal":1,
			"Referred From":"Inside OPD",
			"DateCreated":"7/7/2007 0:00:00",
			"Wt/Ht":"2",
			"Admitted To":"ICU",
			"CaseDx1":"New",
			"CaseDx2":"Old",
			"CaseDx3":""];
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
		mySource = new OPDEncounterSource(new StringReader(sample));
		assertNotNull(mySource);
		assertTrue(mySource.hasNext());
	}

	@Test
	public void testGet() {
		def sample = getSampleValuesString();
		def reader = new StringReader(sample);
		mySource = new OPDEncounterSource(reader);
		mySource.next();
		def values = getSampleValues();

		assertEquals( mySource.get("legacyTable"), "tblOutPatientsInfo");
		assertEquals( mySource.get("legacyEncounterId"), values["RegtID"]);


		[
			"RegtID",
			"Admitted To",
			"CaseDx1",
			"CaseDx2",
			"CaseDx3",
			"ChronicUrlDxID",
			"GeneralImpression",
			"Referred From",
			"Reg_DiseaseID",
			"Reg_DiseaseID2",
			"Reg_DiseaseID3",
			"Wt/Ht"
		].each(){ it->
			assertEquals(mySource.get(it), values[it]);
		}
		[
			"DateCreated",
			"Reg_DateOfVisit"
		].each(){ it->
			assertTrue(mySource.get(it) instanceof java.util.Date);
		}

		[
			"DidNotWait",
			"FeePaid",
			"FollowUp",
			"Referal",
			"Registered",
			"Return"
		].each(){ it->
			assertEquals("Checking ${it}",mySource.get(it), (values[it] == 1 || values[it] == "1"));
		}
	}
}
