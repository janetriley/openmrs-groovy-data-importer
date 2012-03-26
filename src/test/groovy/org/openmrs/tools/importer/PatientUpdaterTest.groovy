package org.openmrs.tools.importer;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openmrs.*;
import org.openmrs.tools.importer.PatientUpdater;
class PatientUpdaterTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}


	@Test
	public void diffNamesAreSame() {
		Patient pat1 = new Patient();
		PersonName name1 = new PersonName("given", "middle", "family");
		pat1.addName(name1);

		Patient pat2 = new Patient();
		PersonName name2 = new PersonName("given", "middle", "family");
		pat2.addName(name2);

		def diffs = PatientUpdater.aNotInB(pat1.getNames(), pat2.getNames(), PatientUpdater.compareNames);
		assertNotNull(diffs);
		assertEquals(diffs.size(),0);
	}






	@Test
	public void nameComparison() {
		PersonName name1 = new PersonName("given", "middle", "family");
		PersonName name2 = new PersonName("given", "middle", "family");
		PersonName name3 = new PersonName("different", "middle", "family");
		PersonName name4  = new PersonName(); //test for nulls

		assertTrue(PatientUpdater.compareNames.call(name1, name2));
		assertFalse(PatientUpdater.compareNames.call(name1, name3));
		assertFalse(PatientUpdater.compareNames.call(name1, name4));

	}

	/**
	 *  pat1 and pat2 have totally different names
	 *
	 * Auto generated method comment
	 *
	 */

	@Test
	public void diffNamesAreDifferent() {
		Patient pat1 = new Patient();
		PersonName name1 = new PersonName("given", "middle", "family");
		pat1.addName(name1);

		Patient pat2 = new Patient();
		PersonName name2 = new PersonName("different", "middle", "family");
		pat2.addName(name2);
		def diffs = PatientUpdater.aNotInB(pat1.getNames(), pat2.getNames(), PatientUpdater.compareNames);
		assertTrue(diffs.contains(name1));
		assertFalse(diffs.contains(name2));

		diffs = PatientUpdater.aNotInB(pat2.getNames(), pat1.getNames(),  PatientUpdater.compareNames);
		assertTrue(diffs.contains(name2));
		assertFalse(diffs.contains(name1));
	}



	@Test
	public void updateExtraNames() {
		Patient pat1 = new Patient();
		Patient pat2 = new Patient();

		PersonName name1 = new PersonName("given", "middle", "family");
		PersonName name2 = new PersonName("different", "middle", "family");
		PersonName name3 = new PersonName("third", "middle", "family");

		pat1.addName(name1);
		pat1.addName(name3);

		pat2.addName(name2);
		pat2.addName(name3)

		def changed = PatientUpdater.updateNames(pat1, pat2);
		assertTrue(changed);
		def names = pat1.getNames();
		assertEquals(names.size(), 2);
		names.each() { n ->
			assertTrue ( n == name2 || n == name3 );
		}
	}

	@Test
	public void updateNoNames() {
		Patient pat1 = new Patient();
		Patient pat2 = new Patient();

		PersonName name1 = new PersonName("given", "middle", "family");
		PersonName name2 = new PersonName("different", "middle", "family");
		PersonName name3 = new PersonName("third", "middle", "family");

		pat1.addName(name1);
		pat1.addName(name3);

		def changed = PatientUpdater.updateNames(pat1, pat2);
		assertTrue(changed);
		def names = pat1.getNames();
		assertEquals(names.size(), 0);
	}

	@Test
	public void updateAddresses() {
		Patient pat1 = new Patient();
		Patient pat2 = new Patient();

		assertFalse( PatientUpdater.updateAddresses(pat1, pat2)); //no addresses


		PersonAddress addr1 = new PersonAddress( address1:"address1", address2:"address2", cityVillage:"cityVillage",
				country:"country", countyDistrict:"countyDistrict", neighborhoodCell:"neighborhoodCell");

		PersonAddress addr2 = new PersonAddress( address1:"address2", address2:"address2", cityVillage:"cityVillage",
				country:"country", countyDistrict:"countyDistrict", neighborhoodCell:"neighborhoodCell");

		PersonAddress addr3 = new PersonAddress( address1:"address2", address2:"address2", cityVillage:"cityVillage",
				country:"country", countyDistrict:"countyDistrict", neighborhoodCell:"neighborhoodCell");


		pat1.addAddress(addr1);
		assertTrue( PatientUpdater.updateAddresses(pat1, pat2));
		assertEquals(pat1.getAddresses().size(),0);

		pat2.addAddress(addr1);
		assertTrue( PatientUpdater.updateAddresses(pat1, pat2));
		assertEquals(pat1.getAddresses().size(),1);

		pat2.addAddress(addr2);
		assertTrue( PatientUpdater.updateAddresses(pat1, pat2));
		assertEquals(pat1.getAddresses().size(),2);


	}

	@Test
	public void compareAttributes(){

		PersonAttribute attr1 = new PersonAttribute(new PersonAttributeType(1), "first");
		PersonAttribute attr2 = new PersonAttribute(new PersonAttributeType(1), "third");
		PersonAttribute attr3 = new PersonAttribute(new PersonAttributeType(2), "third");

		assertTrue(PatientUpdater.compareContents.call(attr1, attr1));
		assertFalse(PatientUpdater.compareContents.call(attr1, attr2));
		assertFalse(PatientUpdater.compareContents.call(attr3, attr2));
	}

	@Test
	public void updateAttributes(){

		PersonAttribute attr1 = new PersonAttribute(new PersonAttributeType(1), "first");
		PersonAttribute attr2 = new PersonAttribute(new PersonAttributeType(1), "third");
		PersonAttribute attr3 = new PersonAttribute(new PersonAttributeType(2), "third");

		Patient pat1 = new Patient();
		Patient pat2 = new Patient();

		assertFalse( PatientUpdater.updateAttributes(pat1, pat2)); //no addresses
		pat1.addAttribute(attr1);
		assertTrue(PatientUpdater.updateAttributes(pat1, pat2));
		assertEquals(pat1.getAttributes().size(),0);
		pat2.addAttribute(attr1);
		assertTrue(PatientUpdater.updateAttributes(pat1, pat2));
		assertEquals(pat1.getAttributes().size(),1);
		pat2.addAttribute(attr3);
		assertTrue(PatientUpdater.updateAttributes(pat1, pat2));
		assertEquals(pat1.getAttributes().size(),2);



	}

	@Test
	public void updateIdentifiers(){

		PatientIdentifier id1 = new PatientIdentifier("value", new PatientIdentifierType(1), new Location(1));
		PatientIdentifier id2 = new PatientIdentifier("value2", new PatientIdentifierType(1), new Location(1));
		PatientIdentifier id3 = new PatientIdentifier("value", new PatientIdentifierType(2), new Location(1));


		Patient pat1 = new Patient();
		Patient pat2 = new Patient();

		assertFalse( PatientUpdater.updateIdentifiers(pat1, pat2)); //no ids
		pat1.addIdentifier(id1);
		assertTrue( PatientUpdater.updateIdentifiers(pat1, pat2)); //no ids
		assertEquals(pat1.getIdentifiers().size(),0);

		pat2.addIdentifier(id1);
		assertTrue( PatientUpdater.updateIdentifiers(pat1, pat2)); //no ids
		assertEquals(pat1.getIdentifiers().size(),1);

		pat2.addIdentifier(id2);
		assertTrue( PatientUpdater.updateIdentifiers(pat1, pat2)); //no ids
		assertEquals(pat1.getIdentifiers().size(),2);

	}
	@Test
	public void newRelationship(){
		Person patient = new Person();
		patient.addName( new PersonName("last","middle","first"));
		Person caretaker1 = new Person();
		caretaker1.addName( new PersonName("last","middle","mom"));


		//exact match
		Relationship rel1 = new Relationship(caretaker1, patient, new RelationshipType(1));
		assertFalse( PatientUpdater.isNewRelationship(rel1, [rel1]));

		//exact match, one saved one not
		Relationship rel4 = new Relationship(caretaker1, patient, new RelationshipType(1));
		rel4.id = 1234;
		assertFalse( PatientUpdater.isNewRelationship(rel1, [rel4]));

		//same name new relationship
		Person caretaker2 = new Person();
		caretaker2.addName( new PersonName("last","middle","mom"));
		Relationship rel2 = new Relationship(caretaker1, patient, new RelationshipType(2));
		assertTrue( PatientUpdater.isNewRelationship(rel1, [rel2]));


		//new name same relationship
		Person caretaker3 = new Person();
		caretaker3.addName( new PersonName("last","middle","new mom"));
		Relationship rel3 = new Relationship(caretaker3, patient, new RelationshipType(1));
		assertTrue( PatientUpdater.isNewRelationship(rel1, [rel3]));


	}


}
