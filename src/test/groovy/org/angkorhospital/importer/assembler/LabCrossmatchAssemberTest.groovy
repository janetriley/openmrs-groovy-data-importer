package org.angkorhospital.importer.assembler;

import org.openmrs.Concept;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;


import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.tools.importer.source.*;
import static org.junit.Assert.*;
import org.openmrs.api.context.*;
import org.openmrs.*;

import org.angkorhospital.importer.assembler.LabCrossmatchAssembler;
import org.angkorhospital.importer.source.LabCrossmatchEncounterSource;
import org.angkorhospital.importer.source.LabCrossmatchEncounterSourceTest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Ignore;

class LabCrossmatchAssemberTest  extends BaseContextSensitiveTest {

    LabCrossmatchAssembler assembler = new LabCrossmatchAssembler();
    def sample = LabCrossmatchEncounterSourceTest.getSampleValuesString();
    def mySource;
    static final String TEST_IMPLEMENTATION_DATA = "resources/MyImplementationDataSet.xml";

    @Before
    public void setUp() throws Exception {
	Context.getAdministrationService().saveGlobalProperty(
		new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_TRUE_CONCEPT,"1"));
	Context.getAdministrationService().saveGlobalProperty(
		new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_FALSE_CONCEPT,"2"));

	mySource = new LabCrossmatchEncounterSource(new StringReader(sample));
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
    public void buildVisitReturnsEncounter(){
	def v = assembler.buildVisit();
	assertNotNull(v);
	assertTrue(v instanceof Encounter);
    }


    @Test
    public void checkObs() {
	def enc = assembler.buildEncounter();
	//def allObs = enc.getObs();
	def allObs = enc.getAllObs();
	def tblInfo = allObs.find(){ it -> it.concept?.id == 915};
	assertNotNull(tblInfo);
	assertEquals( mySource.get("legacyTable"), tblInfo.valueText);

	/**
	 *
	 *  org.openmrs.Encounter buildEncounter(){
	 def pat = buildPatient("CrossPatID");
	 org.openmrs.Encounter encounter = builder.encounter(
	 [   encounterDatetime:source.get("Date"),
	 dateCreated:source.get("Date"),
	 encounterType:encType ,
	 location:loc,
	 form:frm,
	 // provider:prov,
	 patient:pat]){
	 */
	def val = allObs.find(){ it -> it.concept?.id == 916};
	assertNotNull(val);
	//	assertEquals( mySource.get("CrossMatchID"), val.getValueNumeric());

	val = allObs.find(){ it -> it.concept?.id == 916};
	assertNotNull(val);
	assertEquals( mySource.get("legacyId"),(Integer) val.getValueNumeric());

	//REQUESTING DOCTOR - 744
	val = allObs.find(){ it -> it.concept?.id == 744};
	assertNotNull(val);
	assertEquals( mySource.get("Doctor"), val.getValueText());

	//QUESTION: PATIENT Blood group 772  "Group" //add a lookup
	val = allObs.find(){ it -> it.concept?.id == 772};
	assertNotNull(val);
	assertNotNull(val.valueCoded);

	//BAG BLOOD GROUP 773  source.get("Group of Bag")

	val = allObs.find(){ it -> it.concept?.id == 773};
	assertNotNull(val);
	assertNotNull(val.valueCoded);


	//BAG ID NUMBER: 771
	val = allObs.find(){ it -> it.concept?.id == 771 };
	assertNotNull(val);
	assertEquals( mySource.get("Bag No"), val.getValueText());

	//QUESTION: REquesting Ward  745
	val = allObs.find(){ it -> it.concept?.id == 745};
	assertNotNull(val);
	assertNotNull(val.valueCoded);

	// REQUESTED BLOOD PRODUCT
	//obs([concept:getConcept(766,true), valueCoded: getConcept(source.get("Product"),true)]){};

	val = allObs.find(){ it -> it.concept?.id == 766};
	assertNotNull(val);
	assertNotNull(val.valueCoded);

	//"Volume ml"  767  REQUESTED BLOOD PRODUCT VOLUME M/L
	val = allObs.find(){ it -> it.concept?.id == 767 };
	assertEquals( mySource.get("Volume ml"), (Integer)val.getValueNumeric());

	//REASON FOR TRANSFUSION 768  text
	val = allObs.find(){ it -> it.concept?.id == 768};
	assertNotNull(val);
	assertEquals( mySource.get("Reason"), val.getValueText());

	//COMMENT 48
	val = allObs.find(){ it -> it.concept?.id == 48 };
	assertNotNull(val);
	assertEquals( mySource.get("Comment"), val.getValueText());

	//WAS PRODUCT TRANSFUSED?  Y/N/PENDING
	val = allObs.find(){ it -> it.concept?.id == 776 };
	assertNotNull(val);
	assertNotNull(val.valueCoded);
    }





    @Test
    public void encounter_shouldSave() {
	def encCount = Context.getEncounterService().getEncounters(null,null).size();
	def enc = assembler.buildEncounter();
	def p = enc.getPatient();
	assertNotNull(p);
	p = Context.getPatientService().savePatient(p);
	assertNotNull(p.id);
	Encounter savedEnc = Context.getEncounterService().saveEncounter(enc);
	def savedCount = Context.getEncounterService().getEncounters(null,null).size();
	assertTrue( "one encounter saved", savedCount == encCount + 1);
	assertNotNull(savedEnc.getId());
	assertTrue(savedEnc.getId() > 0);
    }



    @Test
    public void shouldSetForm() {
	def enc = assembler.buildEncounter();
	assertNotNull(enc);
	assertNotNull(enc.getForm());
	assertTrue(enc.form instanceof org.openmrs.Form);
	assertEquals(enc.form.id, 1);
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
    public void shouldReturnEnc() {
	def enc = assembler.buildEncounter();
	assertNotNull(enc);
	assertEquals(mySource.get("Date"),enc.encounterDatetime);
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
	assertEquals(mySource.get("Date"),enc.encounterDatetime);
	assertTrue( enc.encounterDatetime instanceof java.util.Date);
    }
    @Test
    public void shouldSetProvider() {
	def enc = assembler.buildEncounter();
	assertNotNull(enc);
	assertEquals(enc.provider.personId,16);
    }





}