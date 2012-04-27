package org.angkorhospital.importer.assembler;

import org.openmrs.tools.importer.source.*;
import org.angkorhospital.importer.source.*;

import static org.junit.Assert.*;

import org.angkorhospital.importer.PatientImporter;
import org.angkorhospital.importer.assembler.PatientAssembler;
import org.angkorhospital.importer.source.AHCPatientSource;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import groovy.sql.*;
import org.openmrs.test.*;

import org.openmrs.api.context.Context;
import java.io.FileOutputStream;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.junit.Ignore;
import org.junit.Test;

import org.openmrs.*;

class PatientUpdate1AssemblerTest  extends BaseContextSensitiveTest {
    static Logger thisLog4j = Logger.getLogger("openmrs.tools.importer");

    String TEST_IMPLEMENTATION_DATA = "resources/MyImplementationDataSet.xml";
    // String TEST_DATA_FILE = "/Volumes/ETUI/prod_exports/sept_exports/patient_sample.txt"
    PatientAssembler factory = null;
    PatientAssembler updateFactory = null;

    @Before
    public void setUp() throws Exception {
	executeDataSet(TEST_IMPLEMENTATION_DATA);
	def source = new AHCPatientSource(
		new StringReader(BaseSourceTest.getSampleValuesString(
		AHCPatientSourceTest.ahcSampleValues))
		);

	source.next();
	//test that an updated patient will be created if missing
	factory = new PatientUpdate1Assembler();
	factory.setSource(source);

	//use this for updates and comparisons
	def values = AHCPatientSourceTest.ahcSampleValues;

	values["FamilyName_e"]="new";
	values["FirstName_e"]="new";
	values["RelationShipType"]="new";
	values["CaretakerName_k"]="new";
	values["Address"]="new";
	values["Pv_Province_e"]="new";
	values["Di_District_e"]="new";
	values["Cn_Commune_e"]="new";
	values["Vi_Village_e"]="new";

	def source2 = new AHCPatientSource(
		new StringReader(BaseSourceTest.getSampleValuesString(values)));
	source2.next();
	updateFactory = new PatientUpdate1Assembler();
	updateFactory.setSource(source2);
    }

    @After
    public void tearDown() throws Exception {
    }


    @Test
    public void createPatient(){

	//confirm it'll call super() buildPatient and make a real patient
	def patient = factory.buildPatient();
	assertNotNull(patient);
	assertEquals(patient.birthdate,  factory.source.get("DateOfBirth"));
	assertEquals(patient.dateCreated,factory.source.get("CreationDate"));
	assertEquals(patient.personDateCreated,factory.source.get("CreationDate"));
	assertEquals(patient.gender, factory.source.get("Gender"));
    }


    @Test
    public void checkAttributes(){

	def patient = factory.buildPatient();
	PatientAssembler.personAttributeTypeIds.each(){ columnName, attributeTypeId->
	    def attr = patient.getAttribute(attributeTypeId);
	    assertNotNull(attr);
	    assertEquals("Checking ${columnName}", attr.value,factory.source.get(columnName));
	}
    }



    @Test
    public void checkNames(){
	def patient = factory.buildPatient();
	assertNotNull(patient);
	def names = patient.getNames();
	assertEquals(names.size(), 2);
	names.each(){ name->
	    assertNotNull(name.givenName);
	    assertNotNull(name.familyName);
	    if( name.preferred == Boolean.TRUE){
		assertEquals(name.givenName,factory.source.get("FirstName_k"));
		assertEquals(name.familyName,factory.source.get("FamilyName_k"));
	    } else {
		assertEquals(name.givenName,factory.source.get("FirstName_e"));
		assertEquals(name.familyName,factory.source.get("FamilyName_e"));
	    }
	};


    }

    @Test
    public void checkAddress(){
	def patient = factory.buildPatient();
	def address = patient.getPersonAddress();
	assertNotNull(address);
	assertEquals(address.address1, factory.source.get("Address"));
	assertEquals(address.cityVillage,factory.source.get("Vi_Village_e"));
	assertEquals(address.neighborhoodCell,factory.source.get("Cn_Commune_e"));
	assertEquals(address.countyDistrict,factory.source.get("Di_District_e"));
	assertEquals(address.stateProvince,factory.source.get("Pv_Province_e"));
	assertEquals(address.country,factory.source.get("country"));

    }

    @Test
    public void checkUpdate(){
	//create a patient
	def patient = factory.buildPatient();
	assertNotNull(Context.getPatientService().savePatient(patient));
	assertNotNull(patient.getId());
	assertTrue(patient.getId() > 0);

	def updatedPatient = updateFactory.buildPatient();

	assertEquals(patient.id, updatedPatient.id);
	assertEquals("no new addrs added",updatedPatient.getAddresses().size(),1);
	updatedPatient.getAddresses().each{ address->
	    [
		address.address1,
		address.cityVillage,
		address.neighborhoodCell,
		address.countyDistrict,
		address.stateProvince
	    ].each(){ value->
		assertEquals(value, "new");
	    }
	}
	assertEquals(patient.getAddresses().first().id, updatedPatient.getAddresses().first().id);

	def names = updatedPatient.getNames();
	assertEquals(names.size(), 2);
	names.each(){ name->
	    assertNotNull(name.givenName);
	    assertNotNull(name.familyName);
	    if( name.preferred == Boolean.TRUE){
		assertEquals(name.givenName,factory.source.get("FirstName_k"));
		assertEquals(name.familyName,factory.source.get("FamilyName_k"));
	    } else {
		assertEquals(name.givenName,"new");
		assertEquals(name.familyName,"new");
	    }
	};


	//caretaker info is being handled as an attribute rather than a Person and Relationship
	[
	    "CaretakerName_k",
	    "RelationShipType"
	].each(){ name ->

	    def attr = updatedPatient.getAttribute(factory.personAttributeTypeIds[name]);
	    assertNotNull( "Checking attr " + name, attr);
	    def value = factory.source.get(name);
	    assertEquals(attr.value, value);
	}


	//These were left alone:
	assertEquals(updatedPatient.birthdate,  factory.source.get("DateOfBirth"));
	assertEquals(updatedPatient.dateCreated,factory.source.get("CreationDate"));
	assertEquals(updatedPatient.personDateCreated,factory.source.get("CreationDate"));
	assertEquals(updatedPatient.gender, factory.source.get("Gender"));



	assertNotNull(Context.getPatientService().savePatient(updatedPatient));


    }


}



