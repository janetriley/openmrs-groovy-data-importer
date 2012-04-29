package org.angkorhospital.importer.source;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Ignore;

import org.angkorhospital.importer.source.KhPatientSource;
import org.apache.commons.lang.StringUtils;
import au.com.bytecode.opencsv.*;


class KhPatientSourceTest extends BaseSourceTest {

    KhPatientSource mySource = null;
    static ahcSampleValues = [
	"PatientCodeNo":"០១២៣-៤៥៦៧៨៩z",//0123-456789z  z to ensure it doesn't mess with ascii
	"FamilyName_k":"FamilyKh",
	"FirstName_k":"GivenKh",
	"CaretakerName_k":"Familyname Mom",
    ]; {
	sampleValues = ahcSampleValues;
    }

    @Before
    public void setUp() throws Exception {
	sampleValues = ahcSampleValues;
	super.setUp();
	mySource = new KhPatientSource(reader);
	mySource.next();
    }

    @After
    public void tearDown() throws Exception {
	super.tearDown();
    }
    @Test
    public void getNames(){
	["FamilyName_k":"FamilyKh",
	"FirstName_k":"GivenKh",
	"CaretakerName_k":"Familyname Mom"
	].each(){ name,value->
	assertEquals(mySource.get(name), value);
	}
    }

    @Test
    public void getPrimaryIdentifier(){
	def primaryId= mySource.get("patientId");
	assertNotNull(primaryId);
	assertEquals(primaryId,"0123-456789z")
    }
    @Test
    public void getSecondaryIdentifier(){
	def secondaryId= mySource.getSecondaryIdentifier();
	assertNotNull(secondaryId);
	assertEquals(secondaryId,"0123456789z");
    }
}

