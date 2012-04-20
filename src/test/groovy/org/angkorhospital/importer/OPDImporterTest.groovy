package org.angkorhospital.importer;

import static org.junit.Assert.*;

import org.angkorhospital.importer.OPDImporter;
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

class OPDImporterTest extends BaseContextSensitiveTest {
    static Logger thisLog4j = Logger.getLogger("openmrs.tools.importer");

    String TEST_IMPLEMENTATION_DATA = "resources/MyImplementationDataSet.xml";
    String TEST_DATA_FILE = "/Volumes/ETUI/prod_exports/sept_exports/encounters/teeny_opd_sample.txt";
    //String TEST_DATA_FILE = "/Volumes/ETUI/prod_exports/sept_exports/encounters/imported/opd/sample";

    //String TEST_DATA_FILE = "/Volumes/ETUI/prod_exports/sept_exports/encounters/opd_sample.txt";
    //String TEST_DATA_FILE = "/Volumes/ETUI/prod_exports/sept_exports/encounters/1K_sample";

    OPDImporter imp = null;

    @Before
    public void setUp() throws Exception {
	imp = new OPDImporter(TEST_DATA_FILE);
	//imp.initComponents;

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
    public void saveEncounter(){
	executeDataSet(TEST_IMPLEMENTATION_DATA);
	//imp.initComponents(TEST_DATA_FILE);

	int numBefore = Context.getEncounterService().getEncounters(null,
		new Location(2),null, null, null, null,null, true).size();
	println("Num before was " + numBefore);
	String filename = TEST_DATA_FILE;
	def expectedNumAdds = numLinesInFile(filename);
	imp.importEncounters(filename);
	int numAfter =  Context.getEncounterService().getEncounters(null,
		new Location(2),null, null, null, null,null, true).size();

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


    /**
     *
     * run a file
     *
     */
    @Test
    public void importEncounters(){
	executeDataSet(TEST_IMPLEMENTATION_DATA);
	int numBefore = Context.getEncounterService().getEncounters(null,
		new Location(2),null, null, null, null,null, true).size();
	int numVisitsBefore = Context.getVisitService().getAllVisits().size();



	imp.importEncounters(TEST_DATA_FILE);
	def numAfter =Context.getEncounterService().getEncounters(null,
		new Location(2),null, null, null, null,null, true).size();
	int numVisitsAfter = Context.getVisitService().getAllVisits().size();

	System.out.println("Started with " + numBefore + " ended with " + numAfter);
	System.out.println("Visits started with " + numVisitsBefore + " ended with " + numVisitsAfter);

    }

    @Test
    public void lookupConcept(){
	//executeDataSet(TEST_IMPLEMENTATION_DATA);
	//def val = Context.getConceptService().getConceptByName( source.get("Reg_DiseaseID2"));
	//def val = Context.getConceptService().getConceptByName("");

	def val = Context.getConceptService().getConcept("");
	println("Value is " +  val );

    }


    @Test
    public void clearCacheDoesNotGoKaboom(){
	imp.clearHibernateCache();
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



