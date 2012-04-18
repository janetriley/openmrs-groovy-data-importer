package org.openmrs.groovyimporter.assembler


import groovy.util.FactoryBuilderSupport;
import org.openmrs.dsl.EncounterFactoryBuilder;
import org.apache.commons.lang.StringUtils;

import org.openmrs.tools.importer.source.*;
import org.openmrs.api.context.Context;
import org.openmrs.Concept;
import org.openmrs.BaseCustomizableData;
import org.openmrs.attribute.*;
import org.openmrs.attribute.AttributeType.*;



abstract class BaseEncounterAssembler {


    static org.openmrs.VisitAttributeType  dischargeAt = null;
    static org.openmrs.VisitAttributeType  primaryDx = null;
    static org.openmrs.VisitAttributeType  dischargeAtId = null;
    static org.openmrs.VisitAttributeType  primaryDxId = null;

    org.openmrs.tools.importer.source.ImportSource source;
    org.openmrs.dsl.EncounterFactoryBuilder builder = new org.openmrs.dsl.EncounterFactoryBuilder();
    static def conceptCache = [:];


    abstract  buildVisit();

    static org.openmrs.Concept getConcept( name ){
	return getConcept(name, false);
    }

    static org.openmrs.Concept getConcept( name, cacheMe){
	if( name == null ||
	( name instanceof String && StringUtils.isBlank(name)))
	    return null;
	cacheMe = true; //let's see what happens...
	if( name instanceof String)
	    name = StringUtils.trim(name);

	def concept = conceptCache[name];
	if( concept == null ){
	    concept =  Context.getConceptService().getConcept(name); //gets by id or name
	    if( concept != null && (cacheMe == true)){
		conceptCache[name] =  concept;
	    }
	}
	return concept;
    }


    static Concept getTrueOrFalseConcept( boolean boolValue ){
	def c= null;
	if (boolValue == true )
	    c = getConcept(1, true);
	else
	    c = getConcept(2, true);
	/*
	 * 	if (boolValue == true )
	 c = Context.getConceptService().getTrueConcept();
	 else
	 c = Context.getConceptService().getFalseConcept();
	 */

	return c;
    };

    void setSource(org.openmrs.tools.importer.source.ImportSource newSource){
	source = newSource;
    };

    org.openmrs.Patient buildPatient(patientIdColumn){
	def identifier = source.get(patientIdColumn);
	if( StringUtils.isBlank(identifier)){
	    //need to add logging
	 //   log.error("Patient identifier ${identifier} is blank, can't create a patient.");
	    return null;

	}
	//def patientId = source.get("Reg_PatientID"); //look up here
	def myPerson = builder.patient( gender:"U"){
	    patientIdentifier( [ "identifier":source.get(patientIdColumn), "location":2,
			"preferred":true, "identifierType":2 ]);
	    patientIdentifier( [ "identifier":StringUtils.replace(source.get(patientIdColumn),"-",""),
			"location":2, "preferred":false, "identifierType":3 ]){};
	};
	def typeList = new ArrayList<org.openmrs.PatientIdentifierType>(1);
	typeList.add(new org.openmrs.PatientIdentifierType(2));
	def foundIds = Context.getPatientService().getPatientIdentifiers(
		identifier,new org.openmrs.PatientIdentifierType(2));
	if(  ! foundIds.isEmpty() ){//use existing patient
	    def existingPatient =  foundIds.get(0).getPatient();
	    if(existingPatient != null)
		return existingPatient;
	}
	return myPerson;

    }


    static Concept getWard(key){
	def values = [
		    "-427741668": 15,//,"LAU"  ward 15
		    "1050082169": 14,//,"IPD" ward 14
		    "1050089646": 12,//,"ER"  ward 12
		    "1276675133": 19,//,"Surgery"  ward 19
		    "1952110970": 17,//,"OT" ward 17
		];
	def conceptId = values[key];
	if( conceptId == null )
	    return null;
	return getConcept(conceptId, true);
    }

    /*
    * set the visit discharge disposition
    *
    */

   String setVisitDischargeDispositionAttribute(visit, enc){
       def dischargeObs = enc.getAllObs().find(){ it -> it.concept?.id == 30}; //the discharge disposition concept

       if( dischargeObs == null )
	   return;//record may not have been updated from chart
       String phrase = "";
       boolean ended = false;
       switch( dischargeObs.getValueCoded()?.conceptId){
	   case 26: //did not wait
	   case 27://"Discharged to home";
	       ended = true;
	       break;
	   case 47://admitted to ward
	   default:
	       ended = false; //if it isn't yes, it's no
       }//end switch
       if( ended == false )
	   return; //they weren't discharged

       //mark visit ended
       visit.stopDatetime =enc.encounterDatetime;

       //set discharged concept ID
       if( dischargeAtId == null )
	   dischargeAtId = Context.getVisitService().getVisitAttributeType(4);

       org.openmrs.VisitAttribute attr = new org.openmrs.VisitAttribute();
       attr.setAttributeType(dischargeAtId);
       attr.setValue(dischargeObs?.valueCoded?.conceptId.toString());
       visit.setAttribute(attr);
   }

   String setVisitPrimaryDx(visit, enc){
       def dxObs = enc.getAllObs().find(){ it -> it.concept?.id == 70}; //primary MOH dx
       if( dxObs == null) //record may not have been updated from chart
	   return;

       if( primaryDxId == null )
	   primaryDxId = Context.getVisitService().getVisitAttributeType(3);
       org.openmrs.VisitAttribute attr = new org.openmrs.VisitAttribute();
       attr.setAttributeType(primaryDxId);
       attr.setValue(dxObs.valueCoded?.conceptId.toString());
       visit.setAttribute(attr);

   }

}
