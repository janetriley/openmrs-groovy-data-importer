package org.openmrs.groovyimporter.assembler;

import org.openmrs.Concept;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.angkorhospital.importer.source.*;

import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.tools.importer.source.*;
import static org.junit.Assert.*;
import org.openmrs.api.context.*;
import org.openmrs.groovyimporter.assembler.BaseEncounterAssembler;
import org.openmrs.groovyimporter.assembler.OPDEncounterAssembler;
import org.openmrs.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Ignore;

class BaseEncounterAssemberTest  extends BaseContextSensitiveTest {

    BaseEncounterAssembler assembler = new OPDEncounterAssembler(); //default to OPD
    def sample = AHCOpdEncounterSourceTest.getSampleValuesString();
    def mySource;
    static final String TEST_IMPLEMENTATION_DATA = "resources/MyImplementationDataSet.xml";

    @Before
    public void setUp() throws Exception {
	Context.getAdministrationService().saveGlobalProperty(
		new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_TRUE_CONCEPT,"1"));
	Context.getAdministrationService().saveGlobalProperty(
		new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_FALSE_CONCEPT,"2"));

	mySource = new OPDEncounterSource(new StringReader(sample));
	mySource.next();
	assembler.setSource(mySource);
	executeDataSet(TEST_IMPLEMENTATION_DATA);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testSetSource() {
	assertEquals(assembler.source, mySource);
    }


    @Test
    public void shouldConstructLegitPatient() {
	def pat = assembler.buildPatient("Reg_PatientID");
	assertNotNull(pat.patient);
	assertTrue(pat instanceof org.openmrs.Patient);
	assertEquals(pat.getIdentifiers()?.size(), 2);
	//check identifiers
	Patient savedP = Context.getPatientService().savePatient(pat);
    }





    @Test
    public void testConceptService(){
	def troo= OpenmrsConstants.GLOBAL_PROPERTY_TRUE_CONCEPT;
	def phalse =  OpenmrsConstants.GLOBAL_PROPERTY_FALSE_CONCEPT;
	def tc=Context.getAdministrationService().getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_TRUE_CONCEPT);
	def fc=Context.getAdministrationService().getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_FALSE_CONCEPT);
	assertNotNull(troo);
	assertNotNull(tc);
	assertNotNull(fc);

	/*
	 try {
	 trueConcept = new Concept(Integer.parseInt(Context.getAdministrationService().getGlobalProperty(
	 OpenmrsConstants.GLOBAL_PROPERTY_TRUE_CONCEPT)));
	 falseConcept = new Concept(Integer.parseInt(Context.getAdministrationService().getGlobalProperty(
	 OpenmrsConstants.GLOBAL_PROPERTY_FALSE_CONCEPT)));
	 }
	 catch (NumberFormatException e) {
	 log.warn("Concept ids for boolean concepts should be numbers");
	 return;
	 }*/

    }



    @Test void buildPatientShouldFetchExistingPatient(){
	def p = assembler.buildPatient("Reg_PatientID");
	assertNotNull(p);
	assertEquals(p.id,null);
	p.gender="F";
	p = Context.getPatientService().savePatient(p);
	assertTrue(p.patientId > 0);
	def p2 = assembler.buildPatient("Reg_PatientID");
	assertEquals(p.id, p2.id);
	assertEquals("it loaded the existing object",p2.getIdentifiers().size(),2);
	assertEquals("it loaded the existing object",p2.gender,p.gender);
	p = Context.getPatientService().savePatient(p);
    }



    @Test
    public void shouldSavePatient() {
	def p = assembler.buildPatient("Reg_PatientID");
	def savedP =   Context.getPatientService().savePatient(p);
	assertNotNull(savedP);
	assertTrue(savedP.id > 0);
    }


    @Test
    public void shouldGetWard() {
	def values = [
		    "-427741668": 15,//,"LAU"  ward 15
		    "1050082169": 14,//,"IPD" ward 14
		    "1050089646": 12,//,"ER"  ward 12
		    "1276675133": 19,//,"Surgery"  ward 19
		    "1952110970": 17,//,"OT" ward 17
		];


	assertTrue("checking value ",assembler.getWard("-427741668") instanceof Concept);
	values.each(){ key, value->
	    def c = assembler.getWard(key);
	    assertTrue("checking value " + key,c instanceof Concept);
	    assertEquals(c.getConceptId(), value);
	}
	assertNull(assembler.getWard("bogus"));
	assertNull(assembler.getWard(null));
	assertNull(assembler.getWard(""));


    }


    @Test
    public void testConceptCache(){
	assertNotNull(assembler.conceptCache);
	OPDEncounterAssembler.conceptCache = [:];//reset - it's static
	assertEquals(assembler.conceptCache.size(), 0);
	assertNull(assembler.getConcept(""));
	assertNull(assembler.getConcept(null));
	assertNull(assembler.getConcept(''));

	//switched to cache all
	assertNull("nothing up my sleeve",assembler.conceptCache.get("TRUE"));
//	assertNotNull("could get concept",assembler.getConcept("TRUE",false));
	//assertNull("cached",assembler.conceptCache.get("TRUE"));
	//setting cacheMe to true fo rall concepts to speed up import
	//assertEquals("not cached",assembler.conceptCache.size(), 0);
	assertNotNull("call again with cache", assembler.getConcept("TRUE",true));
	assertNotNull("cached",assembler.conceptCache.get("TRUE"));
	assertEquals("not cached",assembler.conceptCache.size(), 1);

//	assertNotNull("call with integer, use default nocache",assembler.getConcept(2));
//	assertNull("by default not cached",assembler.conceptCache.get(2));
    }



}