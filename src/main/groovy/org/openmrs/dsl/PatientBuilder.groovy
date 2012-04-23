package org.openmrs.dsl

import groovy.util.BuilderSupport;
import org.openmrs.*;
import org.apache.commons.lang.StringUtils;

/**
 * PatientFactoryBuilder registers all the available factories, defined below.
 * Remember to add your factory here if you create a new one.
 */
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

    /**
     * Returns an instance of org.openmrs.Patient constructed with the
     * name/value pairs in the attributes map.
     *
     * If gender is unspecified, gender will default to 'U' for unknown.
     * Gender is required to save the object.
     * @return an instance of org.openmrs.Patient
     * @see org.openmrs.Patient
     * @see http://groovy.codehaus.org/Groovy+Beans for how to use name/value pairs in constructors.
     */
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
	//call parent.setPatient if parent has the method
	if(parent != null &&
	parent.metaClass.respondsTo(parent, "setPatient", org.openmrs.Patient))
	    parent.setPatient(child);
    }
}




public class PatientIdentifierFactory extends AbstractFactory {

    /**
     * Returns an instance of org.openmrs. constructed with the
     * name/value pairs in the attributes map.
     * @param location - if an Integer, will instantiate an identifierType object with that id
     * @param identifierType - if an Integer, will instantiate an identifierType object with that id
     * @return an instance of org.openmrs.PatientIdentifier
     * @see org.openmrs.PatientIdentifier
     * @see http://groovy.codehaus.org/Groovy+Beans for how to use name/value pairs in constructors.
     */
    public Object newInstance(FactoryBuilderSupport builder,
    Object name, Object value, Map attributes
    ) throws InstantiationException, IllegalAccessException {
	if( attributes.location instanceof Integer)
	    attributes.location  = new Location(attributes.location);
	if( attributes.identifierType instanceof Integer)
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

    /**
     * Returns an instance of org.openmrs.Person constructed with the
     * name/value pairs in the attributes map.
     * @return an instance of org.openmrs.Person
     * @see org.openmrs.Person
     * @see http://groovy.codehaus.org/Groovy+Beans for how to use name/value pairs in constructors.
     */
    public Object newInstance(FactoryBuilderSupport builder,
    Object name, Object value, Map attributes
    ) throws InstantiationException, IllegalAccessException {
	return new Person(attributes);
    }


    public void setParent(FactoryBuilderSupport builder,
    Object parent, Object person) {
    }
}



public class PersonNameFactory extends AbstractFactory {

    /**
     * Returns an instance of org.openmrs.PersonName constructed with the
     * name/value pairs in the attributes map.
     * @return an instance of org.openmrs.PersonName
     * @see org.openmrs.PersonName
     * @see http://groovy.codehaus.org/Groovy+Beans for how to use name/value pairs in constructors.
     */
    public Object newInstance(FactoryBuilderSupport builder,
    Object name, Object value, Map attributes
    ) throws InstantiationException, IllegalAccessException {
	//PersonName expects fields givenName, middleName, familyName, preferred
	return new PersonName(attributes);
    }

    /**
     * Assign name to enclosing person.
     * @see groovy.util.AbstractFactory#setParent(groovy.util.FactoryBuilderSupport, java.lang.Object, java.lang.Object)
     */
    public void setParent(FactoryBuilderSupport builder,
    Object parent, Object name) {
	//add the name to the person
	if (parent != null && parent instanceof org.openmrs.Person)
	    parent.addName(name);
    }
}





public class PersonAddressFactory extends AbstractFactory {

    /**
     * Returns an instance of org.openmrs.PersonAddress constructed with the
     * name/value pairs in the attributes map.
     * @param Type - if an Integer, will instantiate a VisitType object with that id
     * @return an instance of org.openmrs.PersonAddress
     * @see org.openmrs.PersonAddress
     * @see http://groovy.codehaus.org/Groovy+Beans for how to use name/value pairs in constructors.
     */

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

    /**
    * Returns an instance of org.openmrs.PersonAttribute constructed with the
    * name/value pairs in the attributes map.
    *
    * @param attributeType - if an Integer, will instantiate a personAttributeType object with that id
    * @return null if value is empty, else an instance of org.openmrs.PersonAttribute
    * @see org.openmrs.PersonAttribute
    * @see http://groovy.codehaus.org/Groovy+Beans for how to use name/value pairs in constructors.
    */
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
    	if(parent != null &&
	parent.metaClass.respondsTo(parent, "addAttribute", org.openmrs.PersonAttribute)){
	    /* addAttribute adds if the person doesn't already have the attribute */
	    parent.addAttribute(personAttribute);
	}
    }
}





public class RelationshipFactory extends AbstractFactory {
    public boolean isLeaf() {
	return false;
    }

    /**
    * Returns an instance of org.openmrs. constructed with the
    * name/value pairs in the attributes map.
    * @param personA - personA in the relationship - an instance of org.openmrs.Person or Integer. If an Integer, will create a Person object with that id. The Person with that id
    *    must already exist in the database. An id isn't sufficient to create a valid Person.
    * @param personB - personB in the relationship - an instance of org.openmrs.Person or Integer. If an Integer, will create a Person object with that id. The Person with that id
    *    must already exist in the database. An id isn't sufficient to create a valid Person.
    * @return an instance of org.openmrs.Relationship, with PersonA and PersonB assigned.
    * @see org.openmrs.Relationship
    * @see http://groovy.codehaus.org/Groovy+Beans for how to use name/value pairs in constructors.
    */

    public Object newInstance(FactoryBuilderSupport builder,
    Object name, Object value, Map attributes
    ) throws InstantiationException, IllegalAccessException {
	if( attributes['relationshipType'] instanceof Integer ){
	    attributes['relationshipType'] = new RelationshipType( id:attributes['relationshipType']);
	}

	//A Person object needs gender and possibly birthdate to be created.
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




