package org.openmrs.groovyimporter;
/*
 import static org.junit.Assert.*;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 */
import org.apache.log4j.Logger;

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

public class populateData  extends BaseContextSensitiveTest {
    static Logger thisLog4j = Logger.getLogger("openmrs.tools.importer");

    //String TEST_IMPLEMENTATION_DATA = "resources/temp_MyImplementationDataSet.xml";
    String TEST_IMPLEMENTATION_DATA = "temp_MyImplementationDataSet.xml";



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
	System.out.println("Starting export");

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
	initialDataSet.addTable("relationship_type", "SELECT * FROM relationship_type");
	initialDataSet.addTable("concept", "SELECT * FROM concept");
	initialDataSet.addTable("concept_class", "SELECT * FROM concept_class");
	initialDataSet.addTable("concept_datatype", "SELECT * FROM concept_datatype");
	initialDataSet.addTable("concept_map_type", "SELECT * FROM concept_map_type");
	//bye bye in 1.9
	//initialDataSet.addTable("concept_map", "SELECT * FROM concept_map");
	//initialDataSet.addTable("concept_source", "SELECT * FROM concept_source");

	initialDataSet.addTable("concept_name", "SELECT * FROM concept_name");
	initialDataSet.addTable("concept_name_tag", "SELECT * FROM concept_name_tag");
	initialDataSet.addTable("concept_name_tag_map", "SELECT * FROM concept_name_tag_map");
	initialDataSet.addTable("concept_numeric", "SELECT * FROM concept_numeric");
	initialDataSet.addTable("concept_set", "SELECT * FROM concept_set");
	initialDataSet.addTable("concept_set_derived", "SELECT * FROM concept_set_derived");
	initialDataSet.addTable("concept_word", "SELECT * FROM concept_word");
	//initialDataSet.addTable("concept_", "SELECT * FROM concept_");
	//initialDataSet.addTable("concept_", "SELECT * FROM concept_");
	initialDataSet.addTable("form", "SELECT * FROM form");
	/*
	 initialDataSet.addTable("field", "SELECT * FROM field");
	 initialDataSet.addTable("field_answer", "SELECT * FROM field_answer");
	 initialDataSet.addTable("field_type", "SELECT * FROM field_type");
	 // initialDataSet.addTable("form", "SELECT * FROM form");
	 initialDataSet.addTable("form_field", "SELECT * FROM form_field");
	 initialDataSet.addTable("hl7_source", "SELECT * FROM hl7_source");
	 */

	/*
	 initialDataSet.addTable("patient_program", "SELECT * FROM patient_program");
	 initialDataSet.addTable("patient_state", "SELECT * FROM patient_state");
	 */initialDataSet.addTable("person", "SELECT * FROM person where person_id < 20");
	initialDataSet.addTable("person_address", "SELECT * FROM person_address  where person_id < 20");
	initialDataSet.addTable("person_attribute", "SELECT * FROM person_attribute  where person_id < 20");
	initialDataSet.addTable("person_attribute_type", "SELECT * FROM person_attribute_type");
	initialDataSet.addTable("person_name", "SELECT * FROM person_name  where person_id < 20");
	initialDataSet.addTable("provider", "SELECT * FROM provider");
	initialDataSet.addTable("provider_attribute", "SELECT * FROM provider_attribute");
	initialDataSet.addTable("provider_attribute_type", "SELECT * FROM provider_attribute_type");
	/*		 initialDataSet.addTable("privilege", "SELECT * FROM privilege");
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
	initialDataSet.addTable("visit_attribute_type", "SELECT * FROM visit_attribute_type");
	initialDataSet.addTable("visit_type", "SELECT * FROM visit_type");


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
	System.out.println("Done exporting to " + TEST_IMPLEMENTATION_DATA );
    }

}



