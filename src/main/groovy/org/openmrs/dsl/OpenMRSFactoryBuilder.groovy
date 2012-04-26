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
 * OpenMRSFactoryBuilder registers all the available factories, defined below.
 * Remember to register your factory here if you create a new one.
 *
 * To register a factory that handles a tag, , call
 *    	registerFactory("your_tag_here", new YourFactoryConstructor);
 *
 * You'll invoke the builder like so:
 * def result = yourBuilderInstance.your_tag_here(<attribute map>){ ... }
 *
 */
public class OpenMRSFactoryBuilder extends FactoryBuilderSupport {
    public OpenMRSFactoryBuilder(boolean init = true) {
	super(init);
    }
    def registerObjectFactories() {
	registerFactory("concept", new ConceptFactory());
	registerFactory("encounter", new EncounterFactory());
	registerFactory("form", new FormFactory());
	registerFactory("location", new LocationFactory());
	registerFactory("obs", new ObsFactory());
	registerFactory("patient", new PatientFactory());
	registerFactory("patient", new PatientFactory());
	registerFactory("patientIdentifier", new PatientIdentifierFactory());
	registerFactory("person", new PersonFactory())
	registerFactory("personAddress", new PersonAddressFactory());
	registerFactory("personAttribute", new PersonAttributeFactory());
	registerFactory("personName", new PersonNameFactory());
	registerFactory("relationship", new RelationshipFactory())
	registerFactory("visit", new VisitFactory());    }
}

