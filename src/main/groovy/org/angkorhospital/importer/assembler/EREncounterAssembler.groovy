package org.angkorhospital.importer.assembler

import groovy.util.FactoryBuilderSupport;
import org.openmrs.dsl.EncounterFactoryBuilder;
import org.openmrs.tools.importer.assembler.BaseEncounterAssembler;
import org.angkorhospital.importer.source.EREncounterSource;
import org.apache.commons.lang.StringUtils;

import org.openmrs.tools.importer.source.*;
import org.openmrs.api.context.Context;
import org.openmrs.Concept;
import org.openmrs.BaseCustomizableData;
import org.openmrs.attribute.*;
import org.openmrs.attribute.AttributeType.*;


class EREncounterAssembler extends BaseEncounterAssembler {

    //globals - reuse these in buildEncounter
    def loc = builder.location([id:2]);
    def frm = builder.form([id:12]){};
    def encType = new org.openmrs.EncounterType(8); //ER Visit
    def prov =  new org.openmrs.Person(id:430647);//ER STaff



    org.openmrs.Encounter buildEncounter(){

	def pat = buildPatient("PatientID");
	org.openmrs.Encounter encounter = builder.encounter(
		[ encounterDatetime:source.get("Date"),
		    dateCreated:source.get("Date"),
		    encounterType:encType ,
		    location:loc,
		    form:frm,
		    // provider:prov,
		    patient:pat]){

		    //all are encounter type ER Visit, id 8

		    obs([ valueText: source.get("legacyTable"),
				concept:getConcept(915,true)]){
				//IMPORTED FROM TABLE
			    };

		    obs([ valueNumeric: source.get("legacyEncounterId"),
				concept:getConcept(916,true)]){
				//IMPORTED FROM TABLE ID
			    };


		    obs([ concept:getConcept(46, true), //ADMIT FROM WARD
				valueCoded: getConcept(source.get("From"))] //
			    ){
			    };


		    if(! StringUtils.isBlank(source.get("VisitType"))){
			obs([ concept:getConcept(930, true), //ER VISIT TYPE
				    valueText:source.get("VisitType")] //
				){
				};
		    }

		    //diagnosis: should contain dx code
		    if(! StringUtils.isBlank(source.get("DxID1"))){
			obs([ concept: getConcept(70, true), //DX1
				    valueCoded: getConcept( source.get("DxID1"))]){
				}; //primary dx
		    }

		    if( ! StringUtils.isBlank(source.get("DxID2"))){
			obs( [  concept: getConcept(71, true), //DX2
				    valueCoded: getConcept( source.get("DxID2"))]){
				    //addl dx
				};
		    }
		    if( ! StringUtils.isBlank(source.get("DxID3"))){

			obs( [concept: getConcept(923, true), //DX3
				    valueCoded: getConcept( source.get("DxID3"))]){
				};
		    }
		    obs([ concept:getConcept(45, true), //ER TREATMENT
				valueCoded: getConcept(source.get("Treatment"))]
			    ){};



		    obs([ valueBoolean: source.get("Ketamine"), //KETAMINE ADMINISTERED?
				concept:getConcept(31, true)]){
			    };
		    obs([ valueBoolean: source.get("Observation"), //KEPT FOR OBSERVATION?
				concept:getConcept(32, true)]){};

		    if(! StringUtils.isBlank(source.get("Comment"))){
			obs([ valueText: source.get("Comment"),
				    concept:getConcept(48,true)]){
				    //COMMENT
				};
		    };


		    if(! StringUtils.isBlank(source.get("TimeInER"))){
			obs([ concept:getConcept(38, true), //TIME IN  ER
				    valueText:source.get("TimeInER")]
				){};
		    }

		    obs([ valueCoded: getConcept(source.get("DischargeTo")),//discharged to home
				concept:getConcept(931,true)]){
				//"ER DISCHARGE DISPOSITION"
			    };


		    obs([ valueCoded: getDischargeDispositionConcept(),//discharged to home
				concept:getConcept(30,true)]){
				//"DISCHARGE DISPOSITION"
			    };

		    //if discharged (admitted) to a ward, set the ward
		    if( getAdmitToWard() != null  ){ //discharged to a ward
			obs([ valueCoded: getAdmitToWard(),
				    concept:getConcept(47, true) //DISCHARGE/ADMIT TO WARD
				]){};
		    }


		} //end builder
	//	encounter.setPatient(buildPatient());//pat
	encounter.setProvider(prov);
	return encounter;
    };

    def buildVisit(){//need a visit type, indicator,
	org.openmrs.Encounter enc = buildEncounter();

	//only create visits when there was direct admission
	def val = enc.allObs.find(){ it -> it.concept?.id == 46};//ADMITTED FROM
	if(val == null ||
	    val.getValueCoded()?.getConceptId() != 49 ){//DIRECT ADMISSION
	    return enc; //don't make a visit

	    }

	org.openmrs.Visit visit = builder.visit(visitType:1){};
	if( visit.getEncounters() == null )
	    visit.setEncounters(new HashSet<org.openmrs.Encounter>());
	visit.getEncounters().add(enc);
	enc.setVisit(visit);
	visit.startDatetime=enc.encounterDatetime;
	visit.location=enc.location;
	visit.patient=enc.patient;

	//set visit attributes
	//If the patient was discharged, set end time and record where they went
	setVisitDischargeDispositionAttribute(visit,enc);
	if( visit.stopDatetime != null ) //patient was discharged
	    setVisitPrimaryDx(visit,enc);
	return visit;
    };




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

    Concept getAdmitToWard(){
	switch(source.get("DischargeTo")){
	    case "ICU":
	    case "LAU":
	    case "IPD":
	    case "ER":
	    case "Surgery":
	    case "OT":
		return getConcept(source.get("DischargeTo"), true); //ADMITTED TO WARD
		break;

	    default:
		return null;

	}
    }


    Concept getDischargeDispositionConcept(){

	if( getAdmitToWard() != null)
	    return getConcept(47,true);//DISCHARGE TO WARD

	def dd = source.get("DischargeTo");

	if( StringUtils.isBlank(dd))
	    return null;

	switch(dd){
	    case "DISCHARGE TO HOME":
	    case "Death":
	    case "LAMA":
		return getConcept(dd, true); //these are discharge dispositions
		break;
	    default:
		return null;
	}



    }//end get disposition concept




}
