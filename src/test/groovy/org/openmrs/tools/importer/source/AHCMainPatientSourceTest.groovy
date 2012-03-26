
package org.openmrs.tools.importer.source;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Ignore;
import org.openmrs.tools.importer.source.AHCMainPatientSource;

import org.apache.commons.lang.StringUtils;
import au.com.bytecode.opencsv.*;

class AHCMainPatientSourceTest {

	AHCMainPatientSource mySource = null;
	def filepath="/Volumes/ETUI/prod_exports/sept_exports/patient_sample.txt";

	@Before
	public void setUp() throws Exception {
		mySource = new AHCMainPatientSource(filepath);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void canReadIdentifier(){
		assertEquals(1, mySource.currentLineNum); //constructor consumes header line
		def line =  mySource.next();
		assertNotNull(mySource.readValue("PatientCodeNo"));
		System.out.println(mySource.readValue("PatientCodeNo"));
	}

	@Test
	public void getPrimaryIdentifier(){
		def line =  mySource.next();
		def primaryId= mySource.getPrimaryIdentifier();
		assertNotNull(primaryId);
		assertNotNull(primaryId.identifier);
		assertEquals(primaryId.location,2);
		assertEquals(primaryId.identifierType,2);
		assertEquals(primaryId.preferred,true);
		assertEquals(StringUtils.length(primaryId.identifier),11);
	}
	@Test
	public void getSecondaryIdentifier(){
		def line =  mySource.next();
		def secondaryId= mySource.getSecondaryIdentifier();
		assertNotNull(secondaryId);
		assertNotNull(secondaryId.identifier);
		assertEquals(secondaryId.location,2);
		assertEquals(secondaryId.identifierType,3);
		assertEquals(secondaryId.preferred,false);
	}

	@Test
	public void getIdentifiers(){
		def line =  mySource.next();
		def ids= mySource.getIdentifiers();
		assertNotNull(ids);
		assertTrue(ids.containsKey('primary'));
		assertTrue(ids.containsKey('secondary'));
		assertNotNull(ids.secondary);
		assertNotNull(ids.primary);
	}


	@Test
	public void getGender(){
		def line =  mySource.next();
		def attrs= mySource.getGender();
		assertNotNull(attrs);
		assertNotNull(attrs.gender);
	}

	@Test
	public void getBirthdate(){
		def line =  mySource.next();
		def attrs= mySource.getBirthdate();
		assertNotNull(attrs);
		assertNotNull(attrs.birthdate);
		assertTrue(attrs.birthdate instanceof java.util.Date);
	}
	@Test
	public void getPersonDateCreated(){
		def line =  mySource.next();
		def attrs= mySource.getPersonDateCreated();
		assertNotNull(attrs);
		assertNotNull(attrs.personDateCreated);
		assertNotNull(attrs.personDateCreated instanceof java.util.Date);
	}


	@Test
	public void getAddress(){
		def line =  mySource.next();
		def attrs= mySource.getAddress();
		assertNotNull(attrs);

			//check fields are set
		assertTrue(attrs.containsKey("address1"));
		assertTrue(attrs.containsKey("cityVillage"));
		assertTrue(attrs.containsKey("neighborhoodCell"));
		assertTrue(attrs.containsKey("countyDistrict"));
		assertTrue(attrs.containsKey("stateProvince"));
		//the only two we know will be ! null
		assertEquals(attrs.preferred,true);
		assertEquals(attrs.country,"CAMBODIA");

	}

}

