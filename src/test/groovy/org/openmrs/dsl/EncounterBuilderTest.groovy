package org.openmrs.dsl;

import org.openmrs.api.context.Context;
import org.openmrs.dsl.OpenMRSFactoryBuilder;
import org.openmrs.test.BaseContextSensitiveTest;
import static org.junit.Assert.*;
import org.junit.*;


class EncounterBuilderTest   extends BaseContextSensitiveTest {

    OpenMRSFactoryBuilder builder = new OpenMRSFactoryBuilder();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
	initializeInMemoryDatabase();
	executeDataSet("resources/MyImplementationDataSet.xml");
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void builderReturnsEncounter(){

	def enc = builder.encounter( [encounterType:7]){
	    location([id:2])
	};
	assertNotNull(enc);
	assertTrue(enc instanceof org.openmrs.Encounter);
	assertNotNull(enc.getLocation());
	assertEquals(enc.location.id,2);

	assertNotNull(enc.getEncounterType());
	assertEquals(enc.getEncounterType().id, 7);
	assertTrue(enc.getEncounterType() instanceof org.openmrs.EncounterType);
    }

    @Test
    public void location(){
	def loc = builder.location( [id:2]){};
	assertNotNull(loc);
	assertEquals(loc.getId(), 2);
    }

    @Test
    public void childLocation(){
	def loc = builder.location( [id:2]){
	    location([id:3])
	};
	assertNotNull(loc);
	assertEquals(loc.getId(), 2);
	def children = loc.getChildLocations();
	assertEquals(children.size(), 1);
	assertEquals(children.iterator().next().id,3);
    }

    @Test
    public void locationOnNodeCompleted(){
	//These should work
	def item = builder.encounter(){
	    location([id:2])
	};
	assertNotNull(item);
	assertEquals(item.location.id,2);
	item = builder.visit(){
	    location([id:2])
	};
	assertNotNull(item);
	assertEquals(item.location.id,2);

	item = builder.obs(){
	    location([id:2])
	};
	assertNotNull(item);
	assertEquals(item.location.id,2);

	//This should fail without error
	item = builder.concept(){
	    location([id:2])
	};
    }


    @Test
    public void obs(){
	Date now = new Date();
	org.openmrs.Obs obs = builder.obs( [obsDatetime:now, location:new org.openmrs.Location(2),
		    person:new org.openmrs.Patient(), valueBoolean:true]){};
	assertNotNull(obs);
	assertTrue(obs instanceof org.openmrs.Obs);
	assertEquals(obs.location.id, 2);
	assertNotNull(obs.getPerson());
	assertEquals(obs.obsDatetime, now);
	assertEquals(obs.getValueAsBoolean(),Boolean.TRUE);
	assertEquals((Integer)obs.getValueNumeric(),1);

	obs = builder.obs( [obsDatetime:now, location:new org.openmrs.Location(2),
		    person:new org.openmrs.Patient(), valueBoolean:false]){};
	assertEquals(obs.getValueAsBoolean(),Boolean.FALSE);
	assertEquals((Integer)obs.getValueNumeric(),0);
	assertNull(obs.valueCoded); //does NOT set a coded true/false
	assertNull(obs.valueBoolean);
    }

    @Test
    public void obsGroup(){
	org.openmrs.Obs obs = builder.obs(){
	    concept(id:3);
	    obs(){
		concept(id:99);
	    }
	};
	assertNotNull(obs);
	assertTrue(obs instanceof org.openmrs.Obs);
	assertNotNull(obs.getConcept());
	assertEquals(obs.getGroupMembers().size(),1);
	obs.getGroupMembers().each(){
	    assertEquals(it.getObsGroup(),obs);
	    assertEquals(it.getConcept().conceptId,99);
	}
    }

    @Test
    public void concept(){
	org.openmrs.Concept c = builder.concept(conceptId:3){
	};
	assertNotNull(c);
	assertTrue(c instanceof org.openmrs.Concept);
	assertEquals(c.conceptId, 3);
    }

    @Test
    public void visit(){
	org.openmrs.Visit v = builder.visit(visitId:3,  visitType:1,
		startDatetime:new Date(), stopDatetime:new Date()){
		    patient(id:5){};
		    encounter(encounterId:4){};
		    location(id:1);
		};


	assertNotNull(v);
	assertTrue(v instanceof org.openmrs.Visit);

	assertNotNull(v.getVisitType());
	assertNotNull(v.getPatient());
	assertNotNull(v.startDatetime);
	assertNotNull(v.stopDatetime);
	assertNotNull(v.location);
	assertEquals(v.visitId, 3);
	Set<org.openmrs.Encounter> eSet = v.getEncounters();
	assertEquals(v.getEncounters().size(),1);

	def savedVisit = Context.getVisitService().saveVisit(v);
	assertNotNull(savedVisit);
	assertTrue(savedVisit.id > 0);
    }
}