package org.angkorhospital.importer;

import static org.junit.Assert.*;

import org.openmrs.groovyimporter.BasePatientImporter;
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

class PatientUpdate2Test extends BaseContextSensitiveTest {
    static Logger thisLog4j = Logger.getLogger("openmrs.tools.importer");

    String TEST_IMPLEMENTATION_DATA = "resources/MyImplementationDataSet.xml";
    String TEST_DATA_FILE = "/Volumes/ETUI/prod_exports/sept_exports/patients/kh_sample_patient_info.txt"
    BasePatientImporter imp = null;

    @Before
    public void setUp() throws Exception {
	imp = new PatientUpdate2();
    }

    @After
    public void tearDown() throws Exception {
    }


    @Test
    public void logConfigured() {
	assertNotNull(imp.log);
	imp.log.debug("Check one two check check");
    }


    @Test
    public void confirmContextWorking(){
	assertTrue(Context.isSessionOpen());
	println( Context.getAuthenticatedUser());
    }

    @Test
    @Ignore
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



    //test drive a particular file

    @Test
    public void testImport(){
	executeDataSet(TEST_IMPLEMENTATION_DATA);
	def numBefore = Context.getPatientService().getAllPatients().size();

	//import the first time
	//exercises the part that calls super().importPatients
	imp.importPatients(TEST_DATA_FILE);
	def numAfter = Context.getPatientService().getAllPatients().size();
	System.out.println("First pass: Started with " + numBefore + " ended with " + numAfter);
	numBefore = numAfter;

	//reimport - as most patients in this file should be
	//exercises the new part
	imp.importPatients(TEST_DATA_FILE);
	System.out.println("Started with " + numBefore + " ended with " +
		Context.getPatientService().getAllPatients().size());


    }


}



