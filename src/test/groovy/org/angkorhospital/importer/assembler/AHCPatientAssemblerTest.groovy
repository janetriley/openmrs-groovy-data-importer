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

class AHCPatientAssemblerTest  extends BaseContextSensitiveTest {
    static Logger thisLog4j = Logger.getLogger("openmrs.tools.importer");

    String TEST_IMPLEMENTATION_DATA = "resources/MyImplementationDataSet.xml";
    // String TEST_DATA_FILE = "/Volumes/ETUI/prod_exports/sept_exports/patient_sample.txt"
    PatientAssembler factory = null;

    @Before
    public void setUp() throws Exception {
	executeDataSet(TEST_IMPLEMENTATION_DATA);
	def source = new AHCPatientSource(
		new StringReader(BaseSourceTest.getSampleValuesString(
		AHCPatientSourceTest.ahcSampleValues))
		);

	source.next();
	factory = new PatientAssembler();
	factory.setSource(source);
    }

    @After
    public void tearDown() throws Exception {
    }


    @Test
    public void createPatient(){
	def patient = factory.buildPatient();
	assertNotNull(patient);
	assertEquals(patient.birthdate,  factory.source.get("DateOfBirth"));
	assertEquals(patient.dateCreated,factory.source.get("CreationDate"));
	assertEquals(patient.personDateCreated,factory.source.get("CreationDate"));
	assertEquals(patient.gender, factory.source.get("gender"));
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
    public void checkIdentifiers(){
	def patient = factory.buildPatient();
	def ids = patient.getIdentifiers();
	assertEquals(ids.size(), 2);
	def notNulls = ids.findAll(){ it-> it == null; };
	assertEquals(notNulls.size(), 0);
	ids.each(){ it ->
	    if( it.preferred == Boolean.TRUE){//original legacy identifier
		assertEquals(2, it.location.id);
		assertEquals(2, it.identifierType.id);
		assertEquals(it.identifier, factory.source.get("patientId"));
	    } else {
		assertEquals(2, it.location.id);//new style
		assertEquals(3, it.identifierType.id);
		assertEquals(it.identifier, factory.source.getSecondaryIdentifier());
	    }
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
    public void patientIsSaveable(){

	def patient = factory.buildPatient();
	assertNotNull(Context.getPatientService().savePatient(patient));
	assertNotNull(patient.getId());
	assertTrue(patient.getId() > 0);
    }

}



