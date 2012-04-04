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
/*
 class PatientBuilder extends BuilderSupport {
 protected void setParent(Object parent, Object child){
 };
 protected Object createNode(Object name){
 }; // a node without parameter and closure
 protected Object createNode(Object name, Object value){}; //a node without parameters, but with closure
 protected Object createNode(Object name, Map attributes){}; // a Node without closure but with parameters
 protected Object createNode(Object name, Map attributes, Object value){}; //a node with closure and parameters
 protected Object getName(String methodName){};
 }
 */




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





public class PersonNameFactory extends AbstractFactory {
    public boolean isLeaf() {
	return false;
    }
    public Object newInstance(FactoryBuilderSupport builder,
    Object name, Object value, Map attributes
    ) throws InstantiationException, IllegalAccessException {

	//expects givenName, middleName, familyName, preferred
	return new PersonName(attributes);
    }
    /*	public void onNodeCompleted(FactoryBuilderSupport builder,
     Object parent, Object child) {
     ;
     }
     */

    public void setParent(FactoryBuilderSupport builder,
    Object parent, Object name) {
	if (parent != null && parent instanceof org.openmrs.Person)
	    parent.addName(name);
    }
}


public class PersonAddressFactory extends AbstractFactory {
    public boolean isLeaf() {
	return true;
    }

    public Object newInstance(FactoryBuilderSupport builder,
    Object name, Object value, Map attributes
    ) throws InstantiationException, IllegalAccessException {

	return new PersonAddress(attributes);
    }
    /*	public void onNodeCompleted(FactoryBuilderSupport builder,
     Object parent, Object child) {
     ;
     }
     */

    public void setParent(FactoryBuilderSupport builder,
    Object parent, Object address) {
	if (parent != null && (parent instanceof org.openmrs.Person || parent instanceof org.openmrs.Patient))
	    parent.addAddress(address);
    }
}

public class PersonAttributeFactory extends AbstractFactory {
    public boolean isLeaf() {
	return true;
    }
    public Object newInstance(FactoryBuilderSupport builder,
    Object name, Object value, Map attributes
    ) throws InstantiationException, IllegalAccessException {

	if( StringUtils.isEmpty(attributes.value) ||
	attributes.personAttributeTypeId == null ||
	attributes.personAttributeTypeId == 0 )
	    return null;//don't return empty attributes
	return  new PersonAttribute( value:attributes.value,
	attributeType:new PersonAttributeType(attributes.personAttributeTypeId));
    }
    public boolean onHandleNodeAttributes(FactoryBuilderSupport builder,
    Object node,
    Map attributes){
	//it's throwing errors about the personAttributeTypeId not being a prop of PersonAttribute.
	// We don't need any more attributes processed, so prevent further attribute processing.
	return false;
    }

    public void setParent(FactoryBuilderSupport builder,
    Object parent, Object attribute) {
	if (parent != null && attribute != null &&
	( parent instanceof org.openmrs.Person ||
	parent instanceof org.openmrs.Patient )
	){
	    parent.addAttribute(attribute);
	    ;
	}
    }
}

public class RelationshipFactory extends AbstractFactory {
    public boolean isLeaf() {
	return false;
    }
    public Object newInstance(FactoryBuilderSupport builder,
    Object name, Object value, Map attributes
    ) throws InstantiationException, IllegalAccessException {
	//expects: relationshipTypeId, patientId
	Relationship r = new Relationship();
	r.setRelationshipType(new RelationshipType(attributes.relationshipTypeId));
	r.setPersonB(new Person( attributes.patientId));
	return r;
    }

    public boolean onHandleNodeAttributes(FactoryBuilderSupport builder,
    Object node,
    Map attributes){
	//handle attributes here
	return false;
    }

    public void onNodeCompleted(FactoryBuilderSupport builder,
    Object parentPatient, Object relationship) {
	;
    }

    public void setParent(FactoryBuilderSupport builder,
    Object parent, Object relationship) {
	if( parent != null && parent instanceof org.openmrs.Patient)
	//all the relationships are expressed in "Caretaker is-relationship-to Patient)
	//so set patient as PersonB
	relationship.setPersonB(parent);

    }
}




/**
 *
 public class XFactory extends AbstractFactory {
 public boolean isLeaf() {
 return false;
 }
 public Object newInstance(FactoryBuilderSupport builder,
 Object name, Object value, Map attributes
 ) throws InstantiationException, IllegalAccessException {
 //expects ...
 //return new X(attributes);
 }
 //		public void onNodeCompleted(FactoryBuilderSupport builder,
 //	 Object parent, Object child) {
 //	 ;
 //	 }
 //
 public void setParent(FactoryBuilderSupport builder,
 Object parent, Object name) {
 //	if (parent != null && parent instanceof org.openmrs.X)
 ;
 }
 }
 */


