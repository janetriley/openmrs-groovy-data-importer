package org.openmrs.tools.importer.assembler

import groovy.util.FactoryBuilderSupport;
import org.openmrs.tools.importer.builder.EncounterFactoryBuilder;
import org.openmrs.tools.importer.source.AHCOpdEncounterSource;
import org.apache.commons.lang.StringUtils;

import org.openmrs.tools.importer.source.*;
import org.openmrs.api.context.Context;
import org.openmrs.Concept;
import org.openmrs.BaseCustomizableData;
import org.openmrs.attribute.*;
import org.openmrs.attribute.AttributeType.*;


class OPDEncounterAssembler extends BaseEncounterAssembler {

    static org.openmrs.VisitAttributeType  dischargeAt = null;
    static org.openmrs.VisitAttributeType  primaryDx = null;
    static org.openmrs.VisitAttributeType  dischargeAtId = null;
    static org.openmrs.VisitAttributeType  primaryDxId = null;

    //globals - reuse these in buildEncounter
    def loc = builder.location([id:2]);
    def frm = builder.form([id:10]){};
    def encType = new org.openmrs.EncounterType(7);
    def prov =  new org.openmrs.Person(id:17);



    org.openmrs.Encounter buildEncounter(){

	def pat = buildPatient("Reg_PatientID");
	org.openmrs.Encounter encounter = builder.encounter(
		[ encounterDatetime:source.get("Reg_DateOfVisit"),
		    dateCreated:source.get("DateCreated"),
		    encounterType:encType ,
		    location:loc,
		    form:frm,
		    // provider:prov,
		    patient:pat]){

		    //all are encounter type OPD Visit, id 7

		    obs([ valueText: "tblOutPatientsInfo",
				concept:getConcept(915,true)]){
				//IMPORTED FROM TABLE
			    };

		    obs([ valueNumeric: source.get("RegtID"),
				concept:getConcept(916,true)]){
				//IMPORTED FROM TABLE ID
			    };


		    if(! StringUtils.isBlank(source.get("GeneralImpression"))){
			obs([ valueText: source.get("GeneralImpression"),
				    concept:getConcept(912,true)]){
				};
		    };

		    obs([ concept:getConcept(55, true),
				valueCoded: getTrueOrFalseConcept(source.get("Registered"))] //PATIENT REGISTERED?
			    ){
			    };

		    obs([ valueBoolean: source.get("Return") ,	//RETURNING PATIENT?
				concept:getConcept(56, true)]){
			    };
		    obs([ valueBoolean: source.get("Followup"),
				concept:getConcept(57, true)]){
				//IS VISIT A FOLLOWUP?
			    };

		    obs([ valueBoolean: source.get("FeePaid"),
				concept:getConcept(53, true)]){
				//"FEE PAID?"
			    };

		    obs([ valueBoolean:  source.get("Referal"),
				concept:getConcept(914, true)]){
				//IS VISIT A REFERRAL?
			    };


		    if( ! StringUtils.isBlank(source.get("Referred From")) || source.get("Referal") == true){
			obs([ valueCoded: getReferralSource(source.get("Referred From")),
				    concept:getConcept(87, true)]){};   //REFERRED IN BY
		    }; //end referral source check

		    //diagnosis: should contain dx code, new or old, primary or not
		    if(! StringUtils.isBlank(source.get("Reg_DiseaseID"))){
			obs([ concept: getConcept(70, true), //DX1
				    valueCoded: getConcept( source.get("Reg_DiseaseID"))]){
				    //diagnosis code
				    obs([ valueCoded: getNewOrFollowupConcept("CaseDx1"),  //case - new or old?
						concept:getConcept(72, true)]){}; //new or old complaint
				}; //primary dx
		    }

		    if( ! StringUtils.isBlank(source.get("Reg_DiseaseID2"))){
			obs( [  concept: getConcept(71, true), //DX2
				    valueCoded: getConcept( source.get("Reg_DiseaseID2"))]){
				    //addl dx
				    obs([ valueCoded: getNewOrFollowupConcept("CaseDx2"),  //case - new or old?
						concept:getConcept(72, true)]){}; //new or old complaint
				};
		    }
		    if( ! StringUtils.isBlank(source.get("Reg_DiseaseID3"))){

			obs( [concept: getConcept(923, true), //DX3
				    valueCoded: getConcept( source.get("Reg_DiseaseID3"))]){
				    //addl dx
				    obs([ valueCoded: getNewOrFollowupConcept("CaseDx3"),  //case - new or old?
						concept:getConcept(72, true)]){}; //new or old complaint
				};
		    }
		    obs([ valueCoded: getDischargeDispositionConcept(),//discharged to home
				concept:getConcept(30,true)]){
				//"DISCHARGE DISPOSITION"
			    };


		    def admitToWard = getWard(source.get("Admitted To"));
		    if( admitToWard != null ){
			obs([ valueCoded: admitToWard,
				    concept:getConcept(47, true) //DISCHARGE TO WARD
				]){};
		    }

		    if( ! StringUtils.isBlank(source.get("Wt/Ht"))){
			obs([ valueCoded: getWtHtConcept(source.get("Wt/Ht")),
				    concept:getConcept(917, true)]){
				    //"WT HEIGHT  RATIO"
				};
		    }
		} //end builder
	//	encounter.setPatient(buildPatient());//pat
	encounter.setProvider(prov);
	return encounter;
    };

    org.openmrs.Visit buildVisit(){//need a visit type, indicator,
	org.openmrs.Encounter enc = buildEncounter();
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


    Concept getDischargeDispositionConcept(){
	// DID NOT WAIT
	if( source.get("DidNotWait") == Boolean.TRUE )
	    return getConcept(26, true); //did not wait
	//ADMITTED TO WARD
	if( ! StringUtils.isBlank(source.get("Admitted To")))
	    return getConcept(47, true); //ADMITTED TO WARD
	//else HOME
	return getConcept(27, true); //DISCHARGE TO HOME
    }


    static Concept getWtHtConcept(key){
	def tblDisposition = [
		    "1": 918,//">100%"
		    "2":919,//"86-100%"
		    "3":920,//"75-85%"
		    "4":921,//"<75%"
		];

	def conceptId = tblDisposition[key];
	if( conceptId )
	    return getConcept(conceptId, true);

	return null; //value not found
    }



    Concept getNewOrFollowupConcept(String header){
	String newOrOld = source.get(header);
	if( StringUtils.equals(newOrOld, "New"))
	    return getConcept(60, true); //NEW COMPLAINT
	else if( StringUtils.equals(newOrOld, "Old"))
	    return getConcept(73, true); //FOLLOWUP / CONTINUED COMPLAINT

	return null;

    }

    Concept getReferralSource(String sourceConcept){
	if( StringUtils.isBlank(sourceConcept)) //not set or couldn't find it
	    return  getConcept(919, true);//unknown referral source

	return getConcept(sourceConcept, true);
    }


}
