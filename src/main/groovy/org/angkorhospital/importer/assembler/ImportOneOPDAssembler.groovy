package org.angkorhospital.importer.assembler

import groovy.util.FactoryBuilderSupport;
import org.openmrs.dsl.EncounterFactoryBuilder;
import org.apache.commons.lang.StringUtils;

import org.openmrs.tools.importer.source.*;
import org.openmrs.api.context.Context;
import org.openmrs.Concept;
import org.openmrs.BaseCustomizableData;
import org.openmrs.attribute.*;
import org.openmrs.attribute.AttributeType.*;


class ImportOneOPDAssembler extends OPDEncounterAssembler{

    org.openmrs.Patient buildPatient(){
	def myPerson = builder.patient(id:429799){};
	return myPerson;

    }


    org.openmrs.Encounter buildEncounter(){

	def pat = buildPatient();
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
			valueBoolean: source.get("Registered")]) //PATIENT REGISTERED?
			    {
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

}
