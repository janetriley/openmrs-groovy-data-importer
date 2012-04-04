package org.openmrs.dsl;

import static org.junit.Assert.*;

import org.junit.*;

import org.openmrs.*;
//import org.openmrs.tools.importer.builder.PatientBuilder;
import org.openmrs.dsl.PatientFactory;
import org.openmrs.dsl.PatientFactoryBuilder;
class PatientBuilderTest {

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
    public void testFactoryConstructor(){
	def factory= new PatientFactory();
	assertNotNull(factory);
	assertFalse(factory.isLeaf());
    }


    @Test
    public void testPatientBuilderFactory(){
	def builder= new PatientFactoryBuilder();
	def today = new Date();
	assertNotNull(builder);
	def patient = builder.patient( gender:"F", birthdate:today, personDateCreated:today){
	    patientIdentifier(identifier:"123456", location:2, identifierType:2);
	    personName( givenName:"given", familyName:"family",	preferred:true);
	};

	assertNotNull(patient);
	assertEquals(patient.getBirthdate(), today);
	assertEquals(patient.getPersonDateCreated(), today);
	assertEquals(patient.gender,"F");
	//identifiers
	assertNotNull(patient.getIdentifiers());
	def ids = patient.getIdentifiers();
	assertEquals(ids.size(), 1);
	ids.each{ it-> assertEquals(it.identifier,"123456")};

	//names
	def names = patient.getNames();
	assertEquals(names.size(), 1);
	names.each(){ name -> assertTrue( name.isPreferred())};

	//address

	//caretaker

    }

    @Test
    public void testPatientFactory(){
	def builder= new PatientFactoryBuilder();
	def today = new Date();
	assertNotNull(builder);
	def patient = builder.patient( gender:"F", birthdate:today, personDateCreated:today);
	assertNotNull(patient);
	assertEquals(patient.getBirthdate(), today);
	assertEquals(patient.getPersonDateCreated(), today);
	assertEquals(patient.gender,"F");
    }

    @Test
    public void testRelationshipFactory(){
	def builder= new PatientFactoryBuilder();
	assertNotNull(builder);
	def caretakerName = "Mom of Patient"

	def relationship = builder.relationship( relationshipTypeId:12, patientId:123){
	    person( gender:"F"){
		personName(familyName:caretakerName); //the caretaker
	    };
	};
	assertNotNull(relationship);
	assertNotNull(relationship.personB);
	assertEquals(relationship.personB.id, 123);
	assertNotNull(relationship.personA);
	assertEquals(relationship.personA.getFamilyName(),caretakerName );
	assertEquals(relationship.relationshipType.id, 12);
    }

    @Test
    public void testPersonAddressFactory(){
	def builder= new PatientFactoryBuilder();
	def today = new Date();
	assertNotNull(builder);
	def address = builder.personAddress( 	preferred:true,
		"address1":"address",
		"cityVillage":"village",
		"neighborhoodCell":"commune",
		"countyDistrict":"district",
		"stateProvince":"province",
		"country":"CAMBODIA"
		);
	assertNotNull(address);
	assertEquals(address.getAddress1(),"address");
	assertEquals(address.getCityVillage(),"village");
	assertEquals(address.getNeighborhoodCell(),"commune");
	assertEquals(address.getCountyDistrict(),"district");
	assertEquals(address.getStateProvince(),"province");
	assertEquals(address.getCountry(),"CAMBODIA");
	assertTrue(address.isPreferred());
    }


    @Test
    public void testPersonNameFactory(){
	def builder= new PatientFactoryBuilder();
	assertNotNull(builder);
	def name = builder.personName( givenName:"given", familyName:"family",
		preferred:true);
	assertNotNull(name);
	assertEquals(name.getGivenName(),"given");
	assertEquals(name.getFamilyName(),"family");
	assertEquals(name.getPreferred(),true);
    }


    @Test
    public void testPatientIdentifierFactory(){
	def builder= new PatientFactoryBuilder();
	assertNotNull(builder);
	def id = builder.patientIdentifier(location:2, identifierType:3,
		preferred:true);
	assertNotNull(id);
	assertNotNull(id.getLocation());
	assertEquals(id.getLocation().getLocationId(),2);
	assertNotNull(id.getIdentifierType());
	assertEquals(id.getIdentifierType().getId(),3);
    }

    @Test
    public void testPersonAttributeFactory(){
	def builder= new PatientFactoryBuilder();
	assertNotNull(builder);

	def attr = builder.personAttribute(value:"no id returns null");
	assertNull(attr);
	attr = builder.personAttribute(personAttributeTypeId:5);
	assertNull(attr);
	attr = builder.personAttribute(value:"happy path", personAttributeTypeId:5);
	assertNotNull(attr);
	assertEquals(attr.getValue(),"happy path");
	assertNotNull(attr.getAttributeType());
	assertEquals(attr.getAttributeType().id, 5);


    }

    @Test
    public void testPersonFactory(){
	def builder= new PatientFactoryBuilder();
	def today = new Date();
	assertNotNull(builder);
	def patient = builder.person( gender:"F", birthdate:today, personDateCreated:today);
	assertNotNull(patient);
	assertEquals(patient.getBirthdate(), today);
	assertEquals(patient.getPersonDateCreated(), today);
	assertEquals(patient.gender,"F");
	assertTrue( patient instanceof org.openmrs.Person);
    }


    @Test
    public void testMeShouldReturnObj(){
	Patient pat = new Patient(123);
	def builder= new PatientFactoryBuilder();
	def patient = builder.patient(["me":pat]);
	assertEquals(pat, patient);

    }



}
