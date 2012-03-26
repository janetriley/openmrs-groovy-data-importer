package org.openmrs.tools.importer.assembler;

import org.openmrs.Concept;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;


import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.tools.importer.source.*;
import static org.junit.Assert.*;
import org.openmrs.api.context.*;
import org.openmrs.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Ignore;

class OPDEncounterAssemberTest  extends BaseContextSensitiveTest {

    OPDEncounterAssembler assembler = new OPDEncounterAssembler();
    def sample = AHCOpdEncounterSourceTest.getSampleValuesString();
    def mySource;
    static final String TEST_IMPLEMENTATION_DATA = "resources/MyImplementationDataSet.xml";

    @Before
    public void setUp() throws Exception {
	Context.getAdministrationService().saveGlobalProperty(
		new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_TRUE_CONCEPT,"1"));
	Context.getAdministrationService().saveGlobalProperty(
		new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_FALSE_CONCEPT,"2"));

	mySource = new AHCOpdEncounterSource(new StringReader(sample));
	mySource.next();
	assembler.setSource(mySource);
	executeDataSet(TEST_IMPLEMENTATION_DATA);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void shouldConstructLegitPatient() {
	//BaseEncounterAssemblerTest checks that patient is constructed
	//This checks that buildEncounter sets a patient and it's saveable
	def enc = assembler.buildEncounter();
	assertNotNull(enc.patient);
	assertTrue(enc.patient instanceof org.openmrs.Patient);
	Patient savedP = Context.getPatientService().savePatient(enc.getPatient());
    }



    @Test
    public void checkObs() {
	def enc = assembler.buildEncounter();
	//def allObs = enc.getObs();
	def allObs = enc.getAllObs();

	def tblInfo = allObs.find(){ it -> it.concept?.id == 915};
	assertNotNull(tblInfo);
	assertEquals( mySource.get("legacyTable"), tblInfo.valueText);

	def val = allObs.find(){ it -> it.concept?.id == 916};
	assertNotNull(val);
	assertEquals( mySource.get("legacyEncounterId"), (int)val.getValueNumeric());


	val = allObs.find(){ it -> it.concept?.id == 912};
	assertNotNull(val);
	assertEquals( mySource.get("GeneralImpression"), val.getValueText());

	val = allObs.find(){ it -> it.concept?.id == 55};
	assertNotNull(val);
	assertEquals( mySource.get("Registered"), val.getValueBoolean());

	val = allObs.find(){ it -> it.concept?.id == 56};
	assertNotNull(val);
	assertEquals( mySource.get("Return"), val.getValueBoolean());

	val = allObs.find(){ it -> it.concept?.id == 57};
	assertNotNull(val);
	assertEquals( mySource.get("Followup"), val.getValueBoolean());


	val = allObs.find(){ it -> it.concept?.id == 53};
	assertNotNull(val);
	assertEquals( mySource.get("FeePaid"), val.getValueBoolean());

	val = allObs.find(){ it -> it.concept?.id == 914};
	assertNotNull(val);
	assertEquals( mySource.get("Referal"), val.getValueBoolean());

	val = allObs.find(){ it -> it.concept?.id == 87};
	assertNotNull(val);
	assertEquals( assembler.getReferralSource(mySource.get("Referred From")), val.getValueCoded());


	//primary fx
	val = allObs.find(){ it -> it.concept?.id == 70};
	assertNotNull(val);
	def dxConcept = Context.getConceptService().getConceptByName( mySource.get("Reg_DiseaseID"));
	assertNotNull(mySource.get("Reg_DiseaseID"));
	assertNotNull(dxConcept);
	assertNotNull(val.getValueCoded());
	assertEquals(dxConcept, val.getValueCoded());


	//primary fx new or old?
	val = val.getGroupMembers().find(){ it -> it.concept?.id == 72};
	assertNotNull(val);
	dxConcept = assembler.getNewOrFollowupConcept("CaseDx1");
	assertEquals(dxConcept, val.getValueCoded());

	;


	//dx2
	val = allObs.find(){ it -> it.concept?.id == 71};
	assertNotNull(val);
	dxConcept = Context.getConceptService().getConceptByName( mySource.get("Reg_DiseaseID2"));
	assertNotNull(mySource.get("Reg_DiseaseID2"));
	assertNotNull(dxConcept);
	assertNotNull(val.getValueCoded());
	assertEquals(dxConcept, val.getValueCoded());

	//dx3
	val = allObs.find(){ it -> it.concept?.id == 923};
	assertNotNull(val);
	dxConcept = Context.getConceptService().getConceptByName( mySource.get("Reg_DiseaseID3"));
	assertNotNull(mySource.get("Reg_DiseaseID3"));
	assertNotNull(dxConcept);
	assertNotNull(val.getValueCoded());
	assertEquals(dxConcept, val.getValueCoded());


	val = allObs.find(){ it -> it.concept?.id == 917};
	assertNotNull(val);
	def ratio = assembler.getWtHtConcept(mySource.get("Wt/Ht"));
	assertEquals(ratio, val.getValueCoded());

	val = allObs.find(){ it -> it.concept?.id == 30}; //discharge disp
	assertNotNull(val);
	assertEquals(assembler.getDischargeDispositionConcept(), val.getValueCoded());

	assertNotNull( allObs.find(){ it -> it.concept?.id == 87} );
    }

    @Test
    public void wtHeight_shouldReturnCorrectly(){
	"1".."4".each { it ->
	    assertTrue("works for " + it,
		    assembler.getWtHtConcept(it) instanceof Concept); }
	assertNull(assembler.getWtHtConcept(5));
	assertNull("no earth-shattering kaboom",assembler.getWtHtConcept(0));

    }

    @Test
    public void visit_shouldSaveWithoutError() {
	def v = assembler.buildVisit();
	def p = v.getPatient();
	assertNotNull(p);
	p = Context.getPatientService().savePatient(p);
	assertNotNull(p.id);
	Visit savedVisit = Context.getVisitService().saveVisit(v);
	assertTrue(savedVisit.getId() > 0);
    }

    @Test
    public void visit_shouldSave() {
	def vCount = Context.getVisitService().getAllVisits().size();
	def encCount = Context.getEncounterService().getEncounters(null,null).size();
	def numSaved = 0;
	def v = assembler.buildVisit();
	def p = v.getPatient();
	assertNotNull(p);
	assertNotNull(v.getEncounters());
	assertEquals(v.getEncounters().size(),1);
	p = Context.getPatientService().savePatient(p);
	assertNotNull(p.id);
	Visit savedVisit = Context.getVisitService().saveVisit(v);
	assertTrue(savedVisit.getId() > 0);
	for( Encounter enc in v.getEncounters()){
	    assertNotNull(Context.getEncounterService().saveEncounter(enc));
	    assertTrue(enc.id > 0);
	    assertTrue(enc.getVisit().id > 0); //id was set, visit was assigned
	    numSaved++;
	}
	/*	def attrs = v.getAttributes();
	 assertEquals(attrs.size(),2);
	 */
	def savedCount = Context.getEncounterService().getEncounters(null,null).size();
	assertEquals( "one visit saved", Context.getVisitService().getAllVisits().size(), vCount + 1);
	assertEquals( "one encounter saved", savedCount, encCount + numSaved);

    }


    @Test
    public void encounter_shouldSaveWithoutError() {
	def enc = assembler.buildEncounter();
	def p = enc.getPatient();
	assertNotNull(p);
	p = Context.getPatientService().savePatient(p);
	assertNotNull(p.id);
	Encounter savedEnc = Context.getEncounterService().saveEncounter(enc);
	assertNotNull(savedEnc.getId());
	assertTrue(savedEnc.getId() > 0);
    }

    @Test
    public void encounter_shouldSave() {
	def encCount = Context.getEncounterService().getEncounters(null,null).size();
	def enc = assembler.buildEncounter();
	Context.getPatientService().savePatient(enc.getPatient());
	Encounter savedEnc = Context.getEncounterService().saveEncounter(enc);
	def savedCount = Context.getEncounterService().getEncounters(null,null).size();
	assertTrue( "one encounter saved", savedCount == encCount + 1);

    }



    @Test
    public void shouldSetForm() {
	def enc = assembler.buildEncounter();
	assertNotNull(enc);
	assertNotNull(enc.getForm());
	assertTrue(enc.form instanceof org.openmrs.Form);
	assertEquals(enc.form.id, 10);
    }

    @Test
    public void shouldSetLocation() {
	def enc = assembler.buildEncounter();
	assertNotNull(enc);
	assertNotNull(enc.location);
	assertTrue(enc.location instanceof org.openmrs.Location);
	assertEquals(enc.location.id, 2);
    }



    @Test
    public void shouldReturnVisit() {
	def v = assembler.buildVisit();
	assertNotNull(v);
	assertTrue(v instanceof Visit);
	def enc = v.getEncounters().toArray();
	enc = enc[0];
	assertEquals(v.startDatetime,enc.encounterDatetime);
	assertEquals(v.location, enc.location);
	assertNotNull(v.patient);
	assertEquals(v.patient,enc.patient);
	//def savedE = Context.getEncounterService().saveEncounter(enc);
	//assertNotNull(enc);
	//assertTrue(enc.id > 0);
	assertNotNull(Context.getPatientService().savePatient(v.getPatient()));
	def savedV = Context.getVisitService().saveVisit(v);
	assertNotNull(savedV);
	assertTrue(savedV.id > 0);
    }

    @Test
    public void shouldReturnEnc() {
	def enc = assembler.buildEncounter();
	assertNotNull(enc);
	assertEquals(mySource.get("Reg_DateOfVisit"),enc.encounterDatetime);
	assertTrue( enc.encounterDatetime instanceof java.util.Date);
	assertTrue(enc instanceof org.openmrs.Encounter);
    }

    @Test
    public void shouldSaveEnc() {
	def enc = assembler.buildEncounter();
	assertNotNull(enc);
	assertTrue(enc instanceof org.openmrs.Encounter);
	def patient = enc.patient;
	assertEquals(patient.getIdentifiers().size(),2);
	assertNotNull(Context.getPatientService().savePatient(enc.getPatient()));
	def savedE = Context.getEncounterService().saveEncounter(enc);
	assertNotNull(enc);
	assertTrue(enc.id > 0);
	//test double-save
	enc = assembler.buildEncounter();
	patient = enc.patient;
	savedE = Context.getEncounterService().saveEncounter(enc);
	assertTrue(enc.id > 0);
    }



    @Test
    public void shouldSetEncDate() {
	def enc = assembler.buildEncounter();
	assertNotNull(enc);
	assertEquals(mySource.get("Reg_DateOfVisit"),enc.encounterDatetime);
	assertTrue( enc.encounterDatetime instanceof java.util.Date);
    }
    @Test
    public void shouldSetProvider() {
	def enc = assembler.buildEncounter();
	assertNotNull(enc);
	//assertTrue(enc.provider instanceof org.openmrs.Provider);
	assertEquals(enc.provider.personId,17);
    }



    @Test
    public void dischargeDispositions() {

	def values = AHCOpdEncounterSourceTest.getSampleValues();
	values["DidNotWait"] = "1";
	values["Admitted To"] = "ER";
	mySource = new AHCOpdEncounterSource(new StringReader(
		AHCOpdEncounterSourceTest.getSampleValuesString(values)));
	mySource.next();
	assembler.setSource(mySource);

	assertEquals(assembler.getDischargeDispositionConcept().conceptId,26);// DID NOT WAIT


	values["DidNotWait"] = "";
	mySource = new AHCOpdEncounterSource(new StringReader(
		AHCOpdEncounterSourceTest.getSampleValuesString(values)));
	mySource.next();
	assembler.setSource(mySource);

	assertEquals(assembler.getDischargeDispositionConcept().conceptId,47);//ADMITTED TO WARD


	values["Admitted To"] = "";
	mySource = new AHCOpdEncounterSource(new StringReader(
		AHCOpdEncounterSourceTest.getSampleValuesString(values)));
	mySource.next();
	assembler.setSource(mySource);

	assertEquals(assembler.getDischargeDispositionConcept().conceptId,27); //DISCHARGE TO HOME

	/*
	 Concept getDischargeDispositionConcept(){
	 //source is initted from sample
	 // DID NOT WAIT
	 if( source.get("DidNotWait") == Boolean.TRUE )
	 return getConcept(26, true); //did not wait
	 //ADMITTED TO WARD
	 if( source.get("Admitted To") != null)
	 return getConcept(47, true); //ADMITTED TO WARD
	 //else HOME
	 return getConcept(27, true); //DISCHARGE TO HOME
	 }
	 */

    }


}