package org.angkorhospital.importer.source;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Ignore;

import org.angkorhospital.importer.source.AHCPatientSource;
import org.apache.commons.lang.StringUtils;
import au.com.bytecode.opencsv.*;

class AHCPatientSourceTest extends BaseSourceTest {

    AHCPatientSource mySource = null;
    def filepath="/Volumes/ETUI/prod_exports/sept_exports/patient_sample.txt";
    static ahcSampleValues = [
	    "PatientCodeNo":"2001-100003",
	    "FamilyName_e":"FamilyEng",
	    "FamilyName_k":"FamilyKh",
	    "FirstName_e":"GivenEng",
	    "FirstName_k":"GivenKh",
	    "Gender":"F",
	    "DateOfBirth":"5/14/2004 0:00:00",
	    "RelationShipType":"Mother",
	    "CaretakerName_k":"Familyname Mom",
	    "Address":"street address",
	    "Telephone":"phone",
	    "Age":"12",
	    "NewRec":"1",
	    "CreationDate":"6/29/2001 15:09:56",
	    "Distance_Disp":">60 km",
	    "Vi_Village_e":"village",
	    "Cn_Commune_e":"commune",
	    "Di_District_e":"district",
	    "Pv_Province_e":"province"
	];
    {
	sampleValues = ahcSampleValues;
	}



    @Before
    public void setUp() throws Exception {
	sampleValues = ahcSampleValues;
	super.setUp();
	mySource = new AHCPatientSource(reader);
	mySource.next();

    }

    @After
    public void tearDown() throws Exception {
	super.tearDown();
    }

    @Test
    public void canReadIdentifier(){
	assertEquals(1, mySource.currentLineNum); //constructor consumes header line
	def line =  mySource.next();
	assertNotNull(mySource.readValue("PatientCodeNo"));

    }

    @Test
    public void getPrimaryIdentifier(){
	def primaryId= mySource.getPrimaryIdentifier();
	assertNotNull(primaryId);
	assertNotNull(primaryId.identifier);
	assertEquals(primaryId.location,2);
	assertEquals(primaryId.identifierType,2);
	assertEquals(primaryId.preferred,true);
	assertEquals(StringUtils.length(primaryId.identifier),11);
    }
    @Test
    public void getSecondaryIdentifier(){
	def secondaryId= mySource.getSecondaryIdentifier();
	assertNotNull(secondaryId);
	assertNotNull(secondaryId.identifier);
	assertEquals(secondaryId.location,2);
	assertEquals(secondaryId.identifierType,3);
	assertEquals(secondaryId.preferred,false);
    }

    @Test
    public void getIdentifiers(){
	def line =  mySource.next();
	def ids= mySource.getIdentifiers();
	assertNotNull(ids);
	assertTrue(ids.containsKey('primary'));
	assertTrue(ids.containsKey('secondary'));
	assertNotNull(ids.secondary);
	assertNotNull(ids.primary);
    }


    @Test
    public void getGender(){
	def attrs= mySource.getGender();
	assertNotNull(attrs);
	assertNotNull(attrs.gender);
    }

    @Test
    public void getBirthdate(){
	def attrs= mySource.getBirthdate();
	assertNotNull(attrs);
	assertNotNull(attrs.birthdate);
	assertTrue(attrs.birthdate instanceof java.util.Date);
    }
    @Test
    public void getPersonDateCreated(){
	def attrs= mySource.getPersonDateCreated();
	assertNotNull(attrs);
	assertNotNull(attrs.personDateCreated);
	assertNotNull(attrs.personDateCreated instanceof java.util.Date);
    }


    @Test
    public void getAddress(){
	def attrs= mySource.getAddress();
	assertNotNull(attrs);

	//check fields are set
	assertTrue(attrs.containsKey("address1"));
	assertTrue(attrs.containsKey("cityVillage"));
	assertTrue(attrs.containsKey("neighborhoodCell"));
	assertTrue(attrs.containsKey("countyDistrict"));
	assertTrue(attrs.containsKey("stateProvince"));
	//the only two we know will be ! null
	assertEquals(attrs.preferred,true);
	assertEquals(attrs.country,"CAMBODIA");

    }




    @Test
    public void testGet() {
	def sample = getSampleValuesString();
	def reader = new StringReader(sample);
	def values = getSampleValues();

	[
	    "legacyTable",
	    "legacy_db_table"
	].each(){it->
	    assertEquals("Checking ${it}",mySource.get(it), "tblPatient");
	}

	[
	    //these return actual values
	    //"Address",
	    "Age",
	    "CaretakerName_k",
	    "Cn_Commune_e",
	    "Di_District_e",
	    "Distance_Disp",
	    "FamilyName_e",
	    "FamilyName_k",
	    "FirstName_e",
	    "FirstName_k",
	    "Gender",
	    "NewRec",
	    "PatientCodeNo",
	    "Pv_Province_e",
	    "RelationShipType",
	    "Telephone",
	    "Vi_Village_e",
	].each(){ it->
	    assertEquals("Checking ${it}",values[it],mySource.get(it));
	}

	[
	    "DateOfBirth",
	    "CreationDate",
	].each(){ it->
	    assertTrue("Checking ${it}",mySource.get(it) instanceof java.util.Date);
	}

/*	["NewRec",].each(){ it->
	    def shouldBe = (values[it] == 1 || values[it] == "1" ) ? Boolean.TRUE : Boolean.FALSE;
	    assertEquals("Checking ${it}",mySource.get(it), shouldBe);
	    assertTrue("Checking ${it}",mySource.get(it) instanceof Boolean);
	}
  */  }



}

