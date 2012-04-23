package org.angkorhospital.dsl

import groovy.util.BuilderSupport;
import groovy.util.AbstractFactory;
import groovy.util.BuilderSupport;
import groovy.util.FactoryBuilderSupport;

import org.openmrs.api.context.Context;
import org.openmrs.*;
import org.apache.commons.lang.StringUtils;
import java.util.Map;
import org.openmrs.dsl.*;

/**
 * EncounterFactoryBuilder registers all the available factories.
 */
public class AHCEncounterFactoryBuilder extends org.openmrs.dsl.EncounterFactoryBuilder  {

    public AHCEncounterFactoryBuilder(boolean init = true) {
	super(init);
    }
    def registerObjectFactories() {
	super.registerObjectFactories();
	registerFactory("diagnosis", new DiagnosisFactory());
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

