package org.angkorhospital.importer.assembler;

import org.openmrs.tools.importer.source.*;
import org.angkorhospital.importer.source.*;

import static org.junit.Assert.*;

import org.angkorhospital.importer.PatientImporter;
import org.angkorhospital.importer.assembler.PatientAssembler;
import org.angkorhospital.importer.source.KhPatientSource;
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

class PatientUpdate2AssemblerTest  extends BaseContextSensitiveTest {
    static Logger thisLog4j = Logger.getLogger("openmrs.tools.importer");

    String TEST_IMPLEMENTATION_DATA = "resources/MyImplementationDataSet.xml";
    // String TEST_DATA_FILE = "/Volumes/ETUI/prod_exports/sept_exports/patient_sample.txt"
    PatientAssembler factory = null;
    PatientAssembler updateFactory = null;

    @Before
    public void setUp() throws Exception {
	executeDataSet(TEST_IMPLEMENTATION_DATA);

	//factory is to create a patient the first time
	def source = new AHCPatientSource(
		new StringReader(BaseSourceTest.getSampleValuesString(
		AHCPatientSourceTest.ahcSampleValues))
		);

	source.next();
	//test that an updated patient will be created if missing
	factory = new PatientAssembler();
	factory.setSource(source);

	//updateFactory is to run the new Kh sources
	//use this for updates and comparisons
	def values = 	KhPatientSourceTest.ahcSampleValues;
	def source2 = new AHCPatientSource(
		new StringReader(BaseSourceTest.getSampleValuesString(values)));
	source2.next();
	updateFactory = new PatientUpdate2Assembler();
	updateFactory.setSource(source2);
    }

    @After
    public void tearDown() throws Exception {
    }


    @Test
    public void checkUpdate(){
	//create a patient
	def patient = factory.buildPatient();
	assertNotNull(Context.getPatientService().savePatient(patient));
	assertNotNull(patient.getId());
	assertTrue(patient.getId() > 0);

	//now update it
	def updatedPatient = updateFactory.buildPatient();
	assertEquals(patient.id, updatedPatient.id);

	def names = updatedPatient.getNames();
	assertEquals(names.size(), 2);
	names.each(){ name->
	    assertNotNull(name.givenName);
	    assertNotNull(name.familyName);
	    if( name.preferred == Boolean.TRUE){
		assertEquals(name.givenName,KhPatientSourceTest.ahcSampleValues["FirstName_k"]);
		assertEquals(name.familyName,KhPatientSourceTest.ahcSampleValues["FamilyName_k"]);
	    } else {
		assertEquals(name.givenName,AHCPatientSourceTest.ahcSampleValues["FirstName_k"]);
		assertEquals(name.familyName,AHCPatientSourceTest.ahcSampleValues["FamilyName_k"]);
	    }
	};

	//caretaker info is being handled as an attribute rather than a Person and Relationship
	def attr = updatedPatient.getAttribute(factory.personAttributeTypeIds["CaretakerName_k"]);
	assertNotNull( "Checking attr " + name, attr);
	def value =KhPatientSourceTest.ahcSampleValues["CaretakerName_k"];
	assertEquals(attr.value, value);

	assertNotNull(Context.getPatientService().savePatient(updatedPatient));
    }


}



