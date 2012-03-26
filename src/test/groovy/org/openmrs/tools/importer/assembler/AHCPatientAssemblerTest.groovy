package org.openmrs.tools.importer.assembler;

import org.openmrs.tools.importer.assembler.AHCPatientAssembler;
import org.openmrs.tools.importer.source.*;

import static org.junit.Assert.*;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import groovy.sql.*;
import org.openmrs.test.*;
import org.openmrs.tools.importer.PatientImporter;

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
	String TEST_DATA_FILE = "/Volumes/ETUI/prod_exports/sept_exports/patient_sample.txt"
	AHCPatientAssembler factory = null;

	@Before
	public void setUp() throws Exception {
		executeDataSet(TEST_IMPLEMENTATION_DATA);
		def source = new AHCMainPatientSource(TEST_DATA_FILE);
		source.next();
		factory = new AHCPatientAssembler();
		factory.setSource(source);
	}

	@After
	public void tearDown() throws Exception {
	}



	@Test
	public void createPatient(){

		factory.source.next();
		def patient = factory.buildPatient();
		assertNotNull(patient);
		assertNotNull(patient.birthdate);
		assertNotNull(patient.personDateCreated);
		assertNotNull(patient.gender);
		def names = patient.getNames();
		assertEquals(names.size(), 2);
		names.each(){ name->
			assertNotNull(name.givenName);
			assertNotNull(name.familyName);
		};

		def ids = patient.getIdentifiers();
		assertEquals(ids.size(), 2);
		def notNulls = ids.findAll(){ it-> it != null; };
		assertEquals(notNulls.size(), 2);

		def address = patient.getPersonAddress();
		assertNotNull(address);
		assertNotNull(address.address1);
		assertEquals(address.country,"CAMBODIA");
		assertNotNull(Context.getPatientService().savePatient(patient));
		assertNotNull(patient.getId());
		assertTrue(patient.getId() > 0);
	}

	@Test
	public void createRelationship(){
		def relationship = factory.buildRelationship(123);
		assertNotNull(relationship);
		assertNotNull(relationship.personA);
		assertNotNull(relationship.personA.gender);
		assertNotNull(relationship.personA);
		assertNotNull(relationship.personA.familyName);
		assertEquals(relationship.personB.id, 123);
	}
}



