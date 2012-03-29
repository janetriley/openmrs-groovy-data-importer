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


class LabCrossmatchAssembler extends BaseEncounterAssembler {

    //globals - reuse these in buildEncounter
    def loc = builder.location([id:2]);
    def prov =  new org.openmrs.Person(id:16); //lab staff
    def frm = builder.form([id:1]){}; //lab crossmatch form
    def encType = new org.openmrs.EncounterType(6);//lab test - crossmatch

    def buildVisit(){
	//doensn't create visits.
	//EncounterService.saveEncounter() will take care of finding one if appropriate
	return buildEncounter();
    };


    org.openmrs.Encounter buildEncounter(){
	def pat = buildPatient("CrossPatID");
	org.openmrs.Encounter encounter = builder.encounter(
		[   encounterDatetime:source.get("Date"),
		    dateCreated:source.get("Date"),
		    encounterType:encType ,
		    location:loc,
		    form:frm,
		    // provider:prov,
		    patient:pat]){

		    //"Date","CrossPatID","CrossMatchID","Ward","Doctor","Reason","Group","Bag No","Group of Bag","Volume ml","Product","Comment","Transfused"
		    //REQUESTING DOCTOR - 744
		    if(! StringUtils.isBlank(source.get("Doctor"))){
			obs([concept:getConcept(744,true), valueText:source.get("Doctor")]){};
		    }
		    //QUESTION: PATIENT Blood group 772  "Group" //add a lookup
		    obs([concept:getConcept(772,true),
				valueCoded:getConcept( source.get("Group"),true)]){};

		    //BAG BLOOD GROUP 773
		    obs([concept:getConcept(773,true),
				valueCoded:getConcept( source.get("Group of Bag"),true)]){};

		    //BAG ID NUMBER: 771
		    if(! StringUtils.isBlank(source.get("Bag No"))){
			obs([concept:getConcept(771,true),valueText:source.get("Bag No")]){};
		    }
		    //QUESTION: REquesting Ward  745
		    if(! StringUtils.isBlank(source.get("Ward"))){
			obs([concept:getConcept(745,true),
				    valueCoded:getConcept(source.get("Ward"),true)]){};
		    }
		    // REQUESTED BLOOD PRODUCT
		    obs([concept:getConcept(766,true), valueCoded: getConcept(source.get("Product"),true)]){};

		    //"Volume ml"  767  REQUESTED BLOOD PRODUCT VOLUME M/L
		    obs([concept:getConcept(767,true), valueNumeric: source.get("Volume ml")]){};

		    //REASON FOR TRANSFUSION 768  text
		    if(! StringUtils.isBlank(source.get("Reason"))){
			obs([concept:getConcept(768,true), valueText: source.get("Reason")]){};
		    }

		    //COMMENT 48
		    if(! StringUtils.isBlank(source.get("Comment"))){
			obs([concept:getConcept(48,true), valueText: source.get("Comment")]){};
		    }

		    //WAS PRODUCT TRANSFUSED?  Y/N/PENDING
		    obs([concept:getConcept(776,true), valueCoded: getConcept(source.get("Transfused"),true)]){};

		    //Legacy info
		    obs([ valueText: "LabDCrossmatch",
				concept:getConcept(915,true)]){
				//IMPORTED FROM TABLE
			    };

		    obs([ valueNumeric: source.get("CrossMatchID"),
				concept:getConcept(916,true)]){
				//IMPORTED FROM TABLE ID
			    };

		} //end builder
	encounter.setProvider(prov);
	return encounter;
    }//end  build encounter

}
