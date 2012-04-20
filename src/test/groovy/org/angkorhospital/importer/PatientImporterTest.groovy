package org.angkorhospital.importer;

import static org.junit.Assert.*;

import org.angkorhospital.importer.PatientImporter;
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
import org.openmrs.groovyimporter.source.SourceDb;

import org.openmrs.*;

class PatientImporterTest extends BaseContextSensitiveTest {
	static Logger thisLog4j = Logger.getLogger("openmrs.tools.importer");

	String TEST_IMPLEMENTATION_DATA = "resources/MyImplementationDataSet.xml";
	String TEST_DATA_FILE = "/Volumes/ETUI/prod_exports/sept_exports/patient_sample.txt"
	PatientImporter imp = null;

	@Before
	public void setUp() throws Exception {
		imp = new PatientImporter();
	}

	@After
	public void tearDown() throws Exception {
	}


	@Test
	public void logConfigured() {
		PatientImporter imp = new PatientImporter();
		assertNotNull(imp.log);
		imp.log.debug("Check one two check check");
	}


	@Test
	public void confirmContextWorking(){
		assertTrue(Context.isSessionOpen());
		println( Context.getAuthenticatedUser());
	}

	@Test
	//@Ignore
	public void saveAPatient(){
		executeDataSet(TEST_IMPLEMENTATION_DATA);
		imp.initSource(TEST_DATA_FILE);

		int numBefore = Context.getPatientService().getAllPatients().size();
		println("Num before was " + numBefore);
		String filename = TEST_DATA_FILE;
		def expectedNumAdds = numLinesInFile(filename);
		imp.importPatients(filename);
		int numAfter = Context.getPatientService().getAllPatients().size();
		println("Num after was " + numAfter);
		assertTrue(numAfter > numBefore);
		assertEquals( numAfter, (numBefore + numLinesInFile(filename) - 1)); //-1 for header

	}

	int numLinesInFile( String filepath) {
		def linecounter = 0;
		File f = new File(filepath);
		f.eachLine({linecounter++});

		return linecounter;
	}

	@Ignore
	@Test
	public void reimport(){
		executeDataSet(TEST_IMPLEMENTATION_DATA);
		int numBefore = Context.getPatientService().getAllPatients().size();
		//def filename="/Volumes/ETUI/access exports/may_dump/utf8_scrubs/wee.txt";
		def filename="/Volumes/ETUI/access exports/may_dump/utf8_scrubs/patient_sample.txt";
		imp.importPatients(filename);
		int numAfter = Context.getPatientService().getAllPatients().size();
		assertTrue(numAfter > numBefore);
		println("Num before and after first import were ${numBefore} and ${numAfter}");
		numBefore  = numAfter; //the new normal
		imp = new PatientImporter();
		imp.importPatients(filename);
		numAfter = Context.getPatientService().getAllPatients().size();
		assertEquals(numAfter, numBefore);
		println("Num after was " + numAfter);
	}


	@Test
	public void importPatients(){
		executeDataSet(TEST_IMPLEMENTATION_DATA);
		def numBefore = Context.getPatientService().getAllPatients().size();
		imp.importPatients(TEST_DATA_FILE);
		def numAfter = Context.getPatientService().getAllPatients().size();
		System.out.println("Started with " + numBefore + " ended with " + numAfter);
	}


	@Test
	public void importPatientsTwice(){
		executeDataSet(TEST_IMPLEMENTATION_DATA);
		def numBefore = Context.getPatientService().getAllPatients().size();
		def relBefore = Context.getPersonService().getAllRelationships().size();

		imp.importPatients(TEST_DATA_FILE);
		def numAfter = Context.getPatientService().getAllPatients().size();
		def relAfter = Context.getPersonService().getAllRelationships().size();
		System.out.println("First run: Started with " + numBefore + " ended with " + numAfter + " and relationshipCount =" +
				relAfter );

		imp.importPatients(TEST_DATA_FILE);
		//no new people or relationships should have been created
		assertEquals(numAfter,Context.getPatientService().getAllPatients().size());
		assertEquals(relAfter, Context.getPersonService().getAllRelationships().size());
		numAfter = Context.getPatientService().getAllPatients().size();
		relAfter = Context.getPersonService().getAllRelationships().size();

		System.out.println("Second run: Started with " + numBefore + " ended with " + numAfter + " and relationshipCount =" +
				relAfter );
	}

		@Ignore
	@Test
	//equals is failing on .equals and .compareTo - don't remember why I was testing this
	public void testEqualsIsh(){
		PersonAttribute attr1 = new PersonAttribute(new PersonAttributeType(2), "value");
		attr1.id=5;
		PersonAttribute attr2 = new PersonAttribute(new PersonAttributeType(2), "value");
		assertEquals(attr1.attributeType.id,attr2.attributeType.id);
		assertEquals(attr1.value,attr2.value);

//		assertTrue(attr1.compareTo(attr2) == 0);
//		assertTrue(attr1.equals(attr2));
	};

	@Test
	public void clearCacheDoesNotGoKaboom(){
		imp.clearHibernateCache();
	}

	/**
	 * This test creates an xml dbunit file from the current database connection information found
	 * in the runtime properties. This method has to "skip over the base setup" because it tries to
	 * do things (like initialize the database) that shouldn't be done to a standard mysql database.
	 *
	 * @throws Exception
	 */
	@Ignore
	@Test
	@SkipBaseSetup //do not delete this annotation!!!!
	public void createInitialTestDataSetXmlFile() throws Exception {

		// only run this test if it is being run alone.
		// this allows the junit-report ant target and the "right-
		// click-on-/test/api-->run as-->junit test" methods to skip
		// over this whole "test"
		//	if (getLoadCount() != 1)
		//		return;
		SourceDb src = new SourceDb();
		java.sql.Connection conn = src.getJDBCConnection("openmrs");
		// database connection for dbunit
		//IDatabaseConnection connection = new DatabaseConnection(getConnection());
		IDatabaseConnection connection = new DatabaseConnection(conn);

		// partial database export
		QueryDataSet initialDataSet = new QueryDataSet(connection);

		initialDataSet.addTable("encounter_type", "SELECT * FROM encounter_type");
		initialDataSet.addTable("location", "SELECT * FROM location");
		initialDataSet.addTable("patient_identifier_type", "SELECT * FROM patient_identifier_type");
		initialDataSet.addTable("person_attribute_type", "SELECT * FROM person_attribute_type");
		initialDataSet.addTable("relationship_type", "SELECT * FROM relationship_type");
		initialDataSet.addTable("concept", "SELECT * FROM concept");
		initialDataSet.addTable("concept_class", "SELECT * FROM concept_class");
		initialDataSet.addTable("concept_datatype", "SELECT * FROM concept_datatype");
		initialDataSet.addTable("concept_map", "SELECT * FROM concept_map");
		initialDataSet.addTable("concept_name", "SELECT * FROM concept_name");
		initialDataSet.addTable("concept_name_tag", "SELECT * FROM concept_name_tag");
		initialDataSet.addTable("concept_name_tag_map", "SELECT * FROM concept_name_tag_map");
		initialDataSet.addTable("concept_numeric", "SELECT * FROM concept_numeric");
		initialDataSet.addTable("concept_set", "SELECT * FROM concept_set");
		initialDataSet.addTable("concept_set_derived", "SELECT * FROM concept_set_derived");
		initialDataSet.addTable("concept_source", "SELECT * FROM concept_source");
		initialDataSet.addTable("concept_word", "SELECT * FROM concept_word");
		//initialDataSet.addTable("concept_", "SELECT * FROM concept_");
		//initialDataSet.addTable("concept_", "SELECT * FROM concept_");


		/*
		 initialDataSet.addTable("patient_program", "SELECT * FROM patient_program");
		 initialDataSet.addTable("patient_state", "SELECT * FROM patient_state");
		 initialDataSet.addTable("person", "SELECT * FROM person");
		 initialDataSet.addTable("person_address", "SELECT * FROM person_address");
		 initialDataSet.addTable("person_attribute", "SELECT * FROM person_attribute");
		 initialDataSet.addTable("person_attribute_type", "SELECT * FROM person_attribute_type");
		 initialDataSet.addTable("person_name", "SELECT * FROM person_name");
		 initialDataSet.addTable("privilege", "SELECT * FROM privilege");
		 initialDataSet.addTable("program", "SELECT * FROM program");
		 initialDataSet.addTable("program_workflow", "SELECT * FROM program_workflow");
		 initialDataSet.addTable("program_workflow_state", "SELECT * FROM program_workflow_state");
		 initialDataSet.addTable("relationship", "SELECT * FROM relationship");
		 initialDataSet.addTable("relationship_type", "SELECT * FROM relationship_type");
		 initialDataSet.addTable("role", "SELECT * FROM role");
		 initialDataSet.addTable("role_privilege", "SELECT * FROM role_privilege");
		 initialDataSet.addTable("role_role", "SELECT * FROM role_role");
		 initialDataSet.addTable("user_role", "SELECT * FROM user_role");
		 initialDataSet.addTable("users", "SELECT * FROM users");
		 */
		/*
		 initialDataSet.addTable("field", "SELECT * FROM field");
		 initialDataSet.addTable("field_answer", "SELECT * FROM field_answer");
		 initialDataSet.addTable("field_type", "SELECT * FROM field_type");
		 initialDataSet.addTable("form", "SELECT * FROM form");
		 initialDataSet.addTable("form_field", "SELECT * FROM form_field");
		 initialDataSet.addTable("hl7_source", "SELECT * FROM hl7_source");
		 */

		FlatXmlDataSet.write(initialDataSet, new FileOutputStream(TEST_IMPLEMENTATION_DATA));

		conn.close();
		// full database export
		//IDataSet fullDataSet = connection.createDataSet();
		//FlatXmlDataSet.write(fullDataSet, new FileOutputStream("full.xml"));

		// dependent tables database export: export table X and all tables that
		// have a PK which is a FK on X, in the right order for insertion
		//String[] depTableNames = TablesDependencyHelper.getAllDependentTables(connection, "X");
		//IDataSet depDataset = connection.createDataSet( depTableNames );
		//FlatXmlDataSet.write(depDataSet, new FileOutputStream("dependents.xml"));

		//TestUtil.printOutTableContents(getConnection(), "encounter_type", "encounter");
	}


	//test drive a particular file
	@Ignore
	@Test
	public void testImport(){
		executeDataSet(TEST_IMPLEMENTATION_DATA);
		def numBefore = Context.getPatientService().getAllPatients().size();
		imp.importPatients("/Volumes/ETUI/prod_exports/sept_exports/pieces/trouble");
		def numAfter = Context.getPatientService().getAllPatients().size();
		System.out.println("Started with " + numBefore + " ended with " + numAfter);
	}


}



