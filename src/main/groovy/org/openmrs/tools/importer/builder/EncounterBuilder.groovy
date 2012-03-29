package org.openmrs.tools.importer.builder

import groovy.util.BuilderSupport;

import groovy.util.AbstractFactory;
import groovy.util.BuilderSupport;
import groovy.util.FactoryBuilderSupport;

import org.openmrs.api.context.Context;
import org.openmrs.*;
import org.apache.commons.lang.StringUtils;
import java.util.Map;



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
	registerFactory("diagnosis", new DiagnosisFactory());
	registerFactory("visit", new VisitFactory());
    }
}

public class VisitFactory extends AbstractFactory {
    public boolean isLeaf() {
	return false;
    }

    public Object newInstance(FactoryBuilderSupport builder,
    Object name, Object value, Map attributes
    ) throws InstantiationException, IllegalAccessException {
	if( attributes['visitType'] instanceof Integer ){
	    attributes['visitType'] = new VisitType(attributes['visitType']);
	}
	return new Visit(attributes);
    }
    public void onNodeCompleted(FactoryBuilderSupport builder,
    Object parent, Object child) {
	;
    }
}



public class EncounterFactory extends AbstractFactory {
    public boolean isLeaf() {
	return false;
    }
    public Object newInstance(FactoryBuilderSupport builder,
    Object name, Object value, Map attributes
    ) throws InstantiationException, IllegalAccessException {
	if( attributes['encounterType'] instanceof Integer ){
	    attributes['encounterType'] = new EncounterType(attributes['encounterType']);
	}

	return new Encounter(attributes);
    }
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
    public Object newInstance(FactoryBuilderSupport builder,
    Object name, Object value, Map attributes
    ) throws InstantiationException, IllegalAccessException {
	return new Form(attributes);
    }
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
    public Object newInstance(FactoryBuilderSupport builder,
    Object name, Object value, Map attributes
    ) throws InstantiationException, IllegalAccessException {
	return new Location(attributes);
    }
    public void onNodeCompleted(FactoryBuilderSupport builder,
    Object parent, Object location) {
	if( parent != null) // &&  method exists for set Locationencounter instanceof org.openmrs.Encounter)
	    parent.setLocation(location);
    }
}


public class ObsFactory extends AbstractFactory {
    public boolean isLeaf() {
	return false;
    }
    public Object newInstance(FactoryBuilderSupport builder,
    Object name, Object value, Map attributes
    ) throws InstantiationException, IllegalAccessException {
	Obs o =  new Obs(attributes);
	if( attributes["value"] != null &&
	    attributes["concept"] instanceof Concept ){

	    val = attributes["value"];
	    attributes.remove["value"]; //obs has no member named value, constructor will fail

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
	//if it's set as a boolean, it'll assign a coded value
		//htmlFormEntry at this time won't print right if there's a coded(boolean) and a numeric
		//set numeric only - see bug  TRUNK-3150 https://tickets.openmrs.org/browse/TRUNK-3150
	   attributes.remove("valueBoolean");
		// o.setValueBoolean(attributes["valueBoolean"]);
	}
	return o;
    }
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
	    //adding obs to enc sets some values
	    //hibernate will give errors about null or transient values
	    //for child obs, so update them if not set
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


public class ObsFactory2 extends AbstractFactory {
    public boolean isLeaf() {
	return false;
    }
    public Object newInstance(FactoryBuilderSupport builder,
    Object name, Object value, Map attributes
    ) throws InstantiationException, IllegalAccessException {
	return new Obs(attributes);
    }
    public void onNodeCompleted(FactoryBuilderSupport builder,
    Object parent, Object obs) {
	if( parent != null && parent instanceof org.openmrs.Encounter)
	    parent.addObs(obs);
	else if( parent !=null && parent instanceof org.openmrs.Obs )
	    parent.addGroupMember(obs);
    }
}


public class DiagnosisFactory extends AbstractFactory {
    public boolean isLeaf() {
	return false;
    }
    public boolean onHandleNodeAttributes(FactoryBuilderSupport builder,
    Object node,
    Map attributes){
	//it's throwing errors about the personAttributeTypeId not being a prop of PersonAttribute.
	// We don't need any more attributes processed, so prevent further attribute processing.
	return false;
    }

    public Object newInstance(FactoryBuilderSupport builder,
    Object name, Object value, Map attributes
    ) throws InstantiationException, IllegalAccessException {
	//expects attributes: primary/additional,  caseForDiagnosis, code, is New Complaint
	Obs dx = new Obs();
	//
	if( attributes["isPrimary"] == Boolean.TRUE){
	    dx.setConcept(new Concept(70));//Context.getConceptSer<ce().getConcept(70)); //primary MOH diagnosis
	} else {
	    dx.setConcept(new Concept(71));//Context.getConceptService().getConcept(71)); //additional diagnosis
	}
	//set type: primary
	return dx;
    }
    public void onNodeCompleted(FactoryBuilderSupport builder,
    Object parent, Object obs) {
	if( parent != null && parent instanceof org.openmrs.Encounter)
	    parent.addObs(obs);
	else if( parent !=null && parent instanceof org.openmrs.Obs )
	    parent.addGroupMember(obs);
    }
}



public class ConceptFactory extends AbstractFactory {
    public boolean isLeaf() {
	return false;
    }

    public Object newInstance(FactoryBuilderSupport builder,
    Object name, Object value, Map attributes
    ) throws InstantiationException, IllegalAccessException {
	def isTrue = true;
	def c = null;
	if( attributes['id'] != null ){
	    //def
	    c = Context.getConceptService().getConcept(attributes['id']);
	    if( c !=  null ) return c;
	}

	return new Concept(attributes);
    }

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