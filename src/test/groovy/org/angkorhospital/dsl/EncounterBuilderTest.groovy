package org.angkorhospital.dsl;

import org.openmrs.api.context.Context;
import org.openmrs.dsl.EncounterFactoryBuilder;
import org.openmrs.test.BaseContextSensitiveTest;
import static org.junit.Assert.*;
import org.junit.*;


class AHCEncounterBuilderTest   extends org.openmrs.test.BaseContextSensitiveTest {

    AHCEncounterFactoryBuilder builder = new AHCEncounterFactoryBuilder();

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


    //check if this case is still valid
    @Ignore
    @Test
    public void diagnosis(){
	assertNotNull(Context.getConceptService());
	def obs = builder.diagnosis( "isPrimary":true, code:"C96", caseForDx:"spots", ){};
	assertNotNull(obs);
	assertTrue(obs instanceof org.openmrs.Obs);
	assertNotNull(obs.getConcept());

	assertEquals(obs.getGroupMembers()?.size(),1);
	obs.getGroupMembers().each(){
	    assertEquals(it.getObsGroup(),obs);
	    assertEquals(it.getConcept().conceptId,99);
	}
    }
}