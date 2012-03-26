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
class OPDEncounterAssemberAttributesTest  extends BaseContextSensitiveTest {

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
    public void checkDischargeDisposition() {
	def visit = assembler.buildVisit();

	def allAttrs = visit.getAttributes();
	assertEquals(allAttrs.size(),2);
	def dd = allAttrs.find(){ it -> it.attributeType.id == 3};
	assertNotNull(dd);
	assertNotNull(dd.value);
	assertTrue(dd.value instanceof String);

	dd = allAttrs.find(){ it -> it.attributeType.id == 4};

	assertNotNull(dd);
	assertNotNull(dd.value);
	//assertTrue(dd.value instanceof String);
	assertTrue(dd.value instanceof String);

	    }
}