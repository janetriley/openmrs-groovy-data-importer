package org.openmrs.dsl

import groovy.util.BuilderSupport;
import org.openmrs.*;
import org.apache.commons.lang.StringUtils;


public class PatientFactoryBuilder extends FactoryBuilderSupport {
    public PatientFactoryBuilder(boolean init = true) {
	super(init);
    }
    def registerObjectFactories() {
	registerFactory("patient", new PatientFactory());
	registerFactory("patientIdentifier", new PatientIdentifierFactory());
	registerFactory("personName", new PersonNameFactory());
	registerFactory("personAttribute", new PersonAttributeFactory());
	registerFactory("personAddress", new PersonAddressFactory());
	registerFactory("person", new PersonFactory())
	registerFactory("relationship", new RelationshipFactory())
    }
}



public class PatientFactory extends AbstractFactory {
    public boolean isLeaf() {
	return false;
    }
    public Object newInstance(FactoryBuilderSupport builder,
    Object name, Object value, Map attributes
    ) throws InstantiationException, IllegalAccessException {

	Patient patient = null;

	if( attributes.containsKey("me") &&
	attributes["me"] instanceof org.openmrs.Patient ){
	    patient = attributes["me"];
	    attributes.remove("me");
	} else if( attributes != null){
	    if( attributes.gender == null )
		attributes.gender="U"; //there must be a gender, set U for Unknown
	    patient = new Patient(attributes); //encounterDatetime
	} else {
	    patient = new Patient();
	}
	return patient;
    }
    public void onNodeCompleted(FactoryBuilderSupport builder,
    Object parent, Object child) {
	if(parent == null)
	    return;
	if( parent instanceof org.openmrs.Encounter ||
	parent instanceof org.openmrs.Visit ||
	parent instanceof org.openmrs.Obs )
	    parent.setPatient(child);
	;
    }
}



public class PatientIdentifierFactory extends AbstractFactory {
    public boolean isLeaf() {
	return false;
    }
    public Object newInstance(FactoryBuilderSupport builder,
    Object name, Object value, Map attributes
    ) throws InstantiationException, IllegalAccessException {
	attributes.location  = new Location(attributes.location);
	attributes.identifierType = new PatientIdentifierType(attributes.identifierType);

	return new PatientIdentifier(attributes);
    }

    public void setParent(FactoryBuilderSupport builder,
    Object parent, Object identifier) {
	if (parent != null && parent instanceof org.openmrs.Patient)
	    parent.addIdentifier(identifier);
    }
}



public class PersonFactory extends AbstractFactory {
    public boolean isLeaf() {
	return false
    }

    public Object newInstance(FactoryBuilderSupport builder,
    Object name, Object value, Map attributes
    ) throws InstantiationException, IllegalAccessException {
	Person person = null;
	if( attributes != null){
	    person = new Person(attributes); //encounterDatetime
	}		else
	    person = new Person();
	return person;
    }


    public void setParent(FactoryBuilderSupport builder,
    Object parent, Object person) {
	if( parent != null && parent instanceof org.openmrs.Relationship)
	//all the relationships are expressed in "Caretaker is-relationship-to Patient)
	//this is the caretaker person so set them as
	parent.setPersonA(person);
    }
}



/**
 * PersonName creates an instance of org.openmrs.PersonName
 * and associates it with the person.
 */

public class PersonNameFactory extends AbstractFactory {
    public boolean isLeaf() {
	return false;
    }
    public Object newInstance(FactoryBuilderSupport builder,
    Object name, Object value, Map attributes
    ) throws InstantiationException, IllegalAccessException {
	//PersonName expects fields givenName, middleName, familyName, preferred
	return new PersonName(attributes);
    }


    public void setParent(FactoryBuilderSupport builder,
    Object parent, Object name) {
	//add the name to the person
	if (parent != null && parent instanceof org.openmrs.Person)
	    parent.addName(name);
    }
}


/**
 * PersonAddressFactory creates an instance of org.openmrs.PersonAddress
 * and associates it with a person.
 */

public class PersonAddressFactory extends AbstractFactory {
    public boolean isLeaf() {
	return true;
    }

    public Object newInstance(FactoryBuilderSupport builder,
    Object name, Object value, Map attributes
    ) throws InstantiationException, IllegalAccessException {

	return new PersonAddress(attributes);
    }


    public void setParent(FactoryBuilderSupport builder,
    Object parent, Object address) {
	//add the address to the person
	if (parent != null && (parent instanceof org.openmrs.Person || parent instanceof org.openmrs.Patient))
	    parent.addAddress(address);
    }
}

/**
 * PersonAttributeFactory creates an instance of org.openmrs.PersonAttribute
 * and associates it with a person.
 *
 * @should return null for null attributes
 * @should return null if attributeType null
 * @should add attribute to Person if created in a <patient> or <person> block, and Person doesn't already have the attribute
 *
 * @see org.openmrs.Person#addAttribute
 *
 */

public class PersonAttributeFactory extends AbstractFactory {

     public boolean isLeaf() {
	 //don't process any deeper
	 return true;
    }
    public Object newInstance(FactoryBuilderSupport builder,
    Object name, Object value, Map attributes
    ) throws InstantiationException, IllegalAccessException {

	if( StringUtils.isEmpty(attributes.value) ||
	attributes.attributeType == null )
	    return null;//don't return empty attributes
	    if( attributes['attributeType'] instanceof Integer ){
		attributes['attributeType'] = new PersonAttributeType( id:attributes['attributeType']);
	    }
	return  new PersonAttribute( attributes );
    }

    public void setParent(FactoryBuilderSupport builder,
    Object parent, Object personAttribute) {
	if (parent != null && personAttribute != null &&
	( parent instanceof org.openmrs.Person ||
	parent instanceof org.openmrs.Patient )
	){
	    /* adds if the person doesn't already have the attribute */
	    parent.addAttribute(personAttribute);
	}
    }
}

/**
* RelationshipFactory creates an instance of org.openmrs.Reationship
* and assigns it to
*
* @should return null for null attributes
* @should return null if attributeType null
* @should add attribute to Person if created in a <patient> or <person> block, and Person doesn't already have the attribute
*
* @see org.openmrs.Person#addAttribute
*
*/
public class RelationshipFactory extends AbstractFactory {
    public boolean isLeaf() {
	return false;
    }
    public Object newInstance(FactoryBuilderSupport builder,
    Object name, Object value, Map attributes
    ) throws InstantiationException, IllegalAccessException {
    if( attributes['relationshipType'] instanceof Integer ){
	attributes['relationshipType'] = new RelationshipType( id:attributes['relationshipType']);
    }

    //Make sure these People already exist
    //These aren't enough attributes to create a new person on save (need gender, poss. birthdate)
    if( attributes['personA'] instanceof Integer ){
	attributes['personA'] = new Person( id:attributes['personA']);
    }
    if( attributes['personB'] instanceof Integer ){
	attributes['personB'] = new Person( id:attributes['personB']);
    }
	Relationship r = new Relationship(attributes);
	return r;
    }

}




