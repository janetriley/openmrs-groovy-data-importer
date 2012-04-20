package org.angkorhospital.importer;

import static org.junit.Assert.*;

import org.angkorhospital.importer.LabCrossmatchImporter;
import org.angkorhospital.importer.assembler.LabCrossmatchAssembler;
import org.angkorhospital.importer.source.LabCrossmatchEncounterSource;
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

class LabCrossmatchImporterTest extends BaseContextSensitiveTest {
    static Logger thisLog4j = Logger.getLogger("openmrs.tools.importer");

    String TEST_IMPLEMENTATION_DATA = "resources/MyImplementationDataSet.xml";
    String TEST_DATA_FILE = "/Volumes/ETUI/prod_exports/sept_exports/labs/1k_labcrossmatch_sample.csv";
    //String TEST_DATA_FILE = "/Volumes/ETUI/prod_exports/sept_exports/labs/teeny_labcrossmatch_sample.csv";

    LabCrossmatchImporter imp = null;

    @Before
    public void setUp() throws Exception {
	imp = new LabCrossmatchImporter(TEST_DATA_FILE);
    }

    @After
    public void tearDown() throws Exception {
    }


    @Test
    public void logConfigured() {
	//LabCrossmatchImporter imp = new LabCrossmatchImporter();
	assertNotNull(imp.log);
	imp.log.debug("Check one two check check");
    }


    @Test
    public void confirmContextWorking(){
	assertTrue(Context.isSessionOpen());
	println( Context.getAuthenticatedUser());
    }

    @Test
    public void confirminitComponents(){
	imp.initComponents(TEST_DATA_FILE);
	assertTrue( imp instanceof LabCrossmatchImporter);
	assertTrue( imp.source instanceof LabCrossmatchEncounterSource);
	assertTrue( imp.assembler instanceof LabCrossmatchAssembler);

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
	assertEquals(numVisitsAfter, numVisitsBefore); //labs should not make visits
	System.out.println("Started with " + numBefore + " ended with " + numAfter);
	System.out.println("Visits started with " + numVisitsBefore + " ended with " + numVisitsAfter);

    }

    @Ignore
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
    @Ignore //until ready to test drive
    @Test
    public void testImport(){
	executeDataSet(TEST_IMPLEMENTATION_DATA);
	def numBefore = Context.getPatientService().getAllPatients().size();
	imp.importPatients("/Volumes/ETUI/prod_exports/sept_exports/pieces/trouble");
	def numAfter = Context.getPatientService().getAllPatients().size();
	System.out.println("Started with " + numBefore + " ended with " + numAfter);
    }




}



