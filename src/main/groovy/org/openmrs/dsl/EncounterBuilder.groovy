package org.openmrs.dsl

import groovy.util.BuilderSupport;
import groovy.util.AbstractFactory;
import groovy.util.BuilderSupport;
import groovy.util.FactoryBuilderSupport;


import org.apache.commons.lang.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.*;
import java.util.Map;


/**
 * EncounterFactoryBuilder registers all the available factories, defined below.
 * Remember to add your factory here if you create a new one.
 */
public class EncounterFactoryBuilder extends FactoryBuilderSupport {
    public EncounterFactoryBuilder(boolean init = true) {
	super(init);
    }
    def registerObjectFactories() {
	registerFactory("encounter", new EncounterFactory());
	registerFactory("form", new FormFactory());
	registerFactory("location", new LocationFactory());
	registerFactory("obs", new ObsFactory());
	registerFactory("concept", new ConceptFactory());
	registerFactory("patient", new PatientFactory());
	registerFactory("patientIdentifier", new PatientIdentifierFactory());
	registerFactory("visit", new VisitFactory());
    }
}



public class VisitFactory extends AbstractFactory {
    public boolean isLeaf() {
	return false;
    }

    /**
     * Returns an instance of org.openmrs.Visit constructed with the
     * name/value pairs in the attributes map.
     * @param visitType - if an Integer, will instantiate a VisitType object with that id
     * @return an instance of org.openmrs.Visit
     * @see org.openmrs.Visit
     * @see http://groovy.codehaus.org/Groovy+Beans for how to use name/value pairs in constructors.
     */

    public Object newInstance(FactoryBuilderSupport builder,
    Object name, Object value, Map attributes
    ) throws InstantiationException, IllegalAccessException {
	if( attributes['visitType'] instanceof Integer ){
	    attributes['visitType'] = new VisitType(attributes['visitType']);
	}
	return new Visit(attributes);
    }
}



public class EncounterFactory extends AbstractFactory {
    public boolean isLeaf() {
	return false;
    }


    /**
     * Returns a new instance of org.openmrs.Encounter, constructed with the
     * name/value pairs in the attributes map.
     * @param  encounterType - if an Integer, will instantiate an encounterType object with that id
     * @return an instance of org.openmrs.
     * @see org.openmrs.Encounter
     * @see http://groovy.codehaus.org/Groovy+Beans for how to use name/value pairs in constructors.
     */

    public Object newInstance(FactoryBuilderSupport builder,
    Object name, Object value, Map attributes
    ) throws InstantiationException, IllegalAccessException {
	if( attributes['encounterType'] instanceof Integer ){
	    attributes['encounterType'] = new EncounterType(attributes['encounterType']);
	}

	return new Encounter(attributes);
    }


    /**
     * Adds this Encounter to enclosing Visit
     * @see groovy.util.AbstractFactory#onNodeCompleted(groovy.util.FactoryBuilderSupport, java.lang.Object, java.lang.Object)
     */
    public void onNodeCompleted(FactoryBuilderSupport builder,
    Object parent, Object child) {
	if( parent instanceof Visit ){
	    Set<Encounter> s = parent.getEncounters();
	    if( s == null )
		s = new HashSet<Encounter>(1);
	    s.add(child);
	    parent.setEncounters(s);
	}
    }
}



public class FormFactory extends AbstractFactory {
    public boolean isLeaf() {
	return false;
    }

    /**
     * Returns a new instance of org.openmrs.Form , constructed with the
     * name/value pairs in the attributes map.
     * @return an instance of org.openmrs.Form
     * @see org.openmrs.Form
     * @see http://groovy.codehaus.org/Groovy+Beans for how to use name/value pairs in constructors.
     */

    public Object newInstance(FactoryBuilderSupport builder,
    Object name, Object value, Map attributes
    ) throws InstantiationException, IllegalAccessException {
	return new Form(attributes);
    }

    /**
     * Adds Form to enclosing Encounter.
     * @see groovy.util.AbstractFactory#onNodeCompleted(groovy.util.FactoryBuilderSupport, java.lang.Object, java.lang.Object)
     */
    public void onNodeCompleted(FactoryBuilderSupport builder,
    Object parent, Object child) {
	if( parent != null && parent instanceof org.openmrs.Encounter)
	    parent.setForm(child);
    }
}



public class LocationFactory extends AbstractFactory {
    public boolean isLeaf() {
	return false;
    }

    /**
     * Returns a new instance of org.openmrs.Location , constructed with the
     * name/value pairs in the attributes map.
     * @return an instance of org.openmrs.Location
     * @see org.openmrs.Location
     * @see http://groovy.codehaus.org/Groovy+Beans for how to use name/value pairs in constructors.
     */

    public Object newInstance(FactoryBuilderSupport builder,
    Object name, Object value, Map attributes
    ) throws InstantiationException, IllegalAccessException {
	return new Location(attributes);
    }

    /**
     * If the enclosing object is another Location, assigns this Location as a child location.
     * If the enclosing object has a setLocation() method, calls setLocation.
     * Assigns the new location to the enclosing object.
     * @see groovy.util.AbstractFactory#onNodeCompleted(groovy.util.FactoryBuilderSupport, java.lang.Object, java.lang.Object)
     */
    public void onNodeCompleted(FactoryBuilderSupport builder,
    Object parent, Object location) {
	if( parent == null )
	    return;
	if( parent instanceof org.openmrs.Location){
	    parent.addChildLocation(location);
	} else if( //the parent has a setLocation method that takes a Location
	parent.metaClass.respondsTo(parent, "setLocation", org.openmrs.Location))
	    parent.setLocation(location);
    }
}

/**
 *
 * Returns a new instance of org.openmrs. , constructed with the
 * name/value pairs in the attributes map.
 * @param  - if an Integer, will instantiate a  Type object with that id
 * @return an instance of org.openmrs.
 * @see org.openmrs.
 * @see http://groovy.codehaus.org/Groovy+Beans for how to use name/value pairs in constructors.
 */

public class ObsFactory extends AbstractFactory {
    public boolean isLeaf() {
	return false;
    }

    /**
     *
     * Returns a new instance of org.openmrs.Obs , constructed with the
     * name/value pairs in the attributes map.
     * @param  value - optional - A convenience method that will call the correct setter for the concept's datatype. Requires that
     * 		attributes include an entry for "concept" and its value be a Concept with a datatype.
     *         Otherwise you must specify the right attribute name for that Concept's datatype,
     *         e.g. "valueBoolean":Boolean.TRUE, "valueCoded":new Concept(12345) , etc.
     * @return an instance of org.openmrs.Obs
     * @see org.openmrs.Obs
     * @see http://groovy.codehaus.org/Groovy+Beans for how to use name/value pairs in constructors.
     */

    public Object newInstance(FactoryBuilderSupport builder,
    Object name, Object value, Map attributes
    ) throws InstantiationException, IllegalAccessException {
	if( attributes["value"] != null &&
	attributes["concept"] instanceof Concept ){
	    val = attributes["value"];
	    attributes.remove["value"]; //obs has no member named value, remove to prevent errors

	    datatype = attributes["concept"].getDatatype();
	    if( datatype.isCoded()){
		attributes["valueCoded"] = val;
	    }
	    else if( datatype.isComplex()){
		attributes["valueComplex"] = val;
	    }
	    else if( datatype.isDate() || datatype.isTime() ||
	    datatype.isDatetime()){
		attributes["valueDatetime"] = val;
	    }
	    else if( datatype.isNumeric()){
		attributes["valueNumeric"] = val;
	    }else if( datatype.isDrug()){
		attributes["valueDrug"] = val;
	    }	else if( datatype.isText()){
		attributes["valueText"] = val;
	    } else { //we didn't succeed - put it back
		attributes["value"] = val;
	    }
	}

	if( attributes["valueBoolean"] != null ){
	    //set the numeric value as well to work with pre-1.9 implementations
	    if( attributes["valueBoolean"] == Boolean.TRUE)
		attributes["valueNumeric"] = 1;
	    else
		attributes["valueNumeric"] = 0;

	    //if value is set as a boolean, Obs will  assign a coded value of the OpenMRS concept dictionary entry for True and False
	    //htmlFormEntry at this time won't print right if there's a coded(boolean) and a numeric
	    //set numeric only - see bug  TRUNK-3150 https://tickets.openmrs.org/browse/TRUNK-3150
	    attributes.remove("valueBoolean");
	}

	Obs o =  new Obs(attributes);

	return o;
    }

    /**
     * Add Obs to enclosing Encounter.
     * @see groovy.util.AbstractFactory#onNodeCompleted(groovy.util.FactoryBuilderSupport, java.lang.Object, java.lang.Object)
     */
    public void onNodeCompleted(FactoryBuilderSupport builder,
    Object parent, Object obs) {
	if(obs == null)
	    return;
	//if the concept was bogus, return null
	//quality control decision
	if(obs.concept == null )
	    return;
	if( parent != null && parent instanceof org.openmrs.Encounter){
	    parent.addObs(obs);
	    //Workaround for https://tickets.openmrs.org/browse/TRUNK-3014

	    //Obs must have  person, dateTime, and location for successful save.
	    //OpenMRS copies the values from the Encounter to the Obs
	    //if they're null in the Obs.
	    obs.getGroupMembers().each(){ it->
		if( it.person == null)
		    it.person = obs.person;
		if( it.obsDatetime == null )
		    it.obsDatetime = obs.obsDatetime;
		if (it.location == null)
		    it.location = obs.location;
	    }
	}
	else if( parent !=null && parent instanceof org.openmrs.Obs ){
	    parent.addGroupMember(obs);
	}
    }
}


public class ConceptFactory extends AbstractFactory {
    public boolean isLeaf() {
	return false;
    }


    /**
     * Returns a new instance of org.openmrs.Concept , constructed with the
     * name/value pairs in the attributes map.
     * If id attribute is provided, and
     * the Context session is open, it will retrieve the Concept from
     * the openMRS concept dictionary without processing any of the attributes.
     * @return an instance of org.openmrs.Concept
     * @see org.openmrs.Concept
     * @see http://groovy.codehaus.org/Groovy+Beans for how to use name/value pairs in constructors.
     */
    public Object newInstance(FactoryBuilderSupport builder,
    Object name, Object value, Map attributes
    ) throws InstantiationException, IllegalAccessException {
	def isTrue = true;
	def c = null;
	if( attributes['id'] != null && Context.isSessionOpen()){
	    //If an openMRS context was initted,
	    //the concept should come from there
	    //esp. in 1.9+ where concept guid is preferred to id

	    //For performance reasons,
	    //if you're using the same concept several times it's better to
	    //do the lookup yourself, cache the results, and
	    //use that in the enclosing object's attribute map --
	    // e.g.  prefer   obs(concept:myCachedConceptObject){} to
	    //  obs(){ concept(id:123) }
	    c = Context.getConceptService().getConcept(attributes['id']);

	    if( c !=  null ) return c;

	}

	return new Concept(attributes);
    }

    /**
     * Add Concept to enclosing Obs or Visit.
     * @see groovy.util.AbstractFactory#onNodeCompleted(groovy.util.FactoryBuilderSupport, java.lang.Object, java.lang.Object)
     */
    public void onNodeCompleted(FactoryBuilderSupport builder,
    Object parent, Object concept) {
	if( parent == null )
	    return;
	if( parent instanceof org.openmrs.Obs)
	    parent.setConcept(concept);
	if( parent instanceof org.openmrs.Visit)
	    parent.setIndication(concept);
    }
}