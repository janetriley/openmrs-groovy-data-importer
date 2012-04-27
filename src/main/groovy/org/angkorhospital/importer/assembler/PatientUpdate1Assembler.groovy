package org.angkorhospital.importer.assembler

//import org.angkorhospital.importer.source.AHCPatientSource;
//import org.openmrs.groovyimporter.assembler.BasePatientAssembler;
import org.openmrs.Patient;
import org.openmrs.Location;
import org.openmrs.PatientIdentifierType;
//import org.openmrs.groovyimporter.source.ImportSource;
import org.apache.commons.lang.StringUtils;
import org.openmrs.dsl.*;
//import org.openmrs.Person;
import org.openmrs.api.context.Context;

/**
 * First patient update:  reload english name and address, add caretaker info as attributes
 *
 */

class PatientUpdate1Assembler  extends org.angkorhospital.importer.assembler.PatientAssembler{

    public PatientUpdate1Assembler(){
	super();
    }


    /**
     * Use Groovy builder magic to create a Patient object
     * @return Patient, initted with the source's current line
     */
    org.openmrs.Patient buildPatient(){

	//make a prototype patient for the finder to use
	def protopatient = null;
	def ctx = Context.getPatientService();

	java.util.List existingIds = Context.getPatientService().getPatientIdentifiers(
		source.get("PatientCodeNo"), null, null, null,true);

	if( ! existingIds?.isEmpty()){
	    //getPatientByExample will return existing patient or a new patient object
	    protopatient = existingIds.first().getPatient();
	}

	if(existingIds?.isEmpty() || protopatient == null ) {
	    //this patient was missed on last import - reimport everything
	    return super.buildPatient();
	}


	//remove the old address and names before adding updated ones
	//protopatient.setAddresses(null);
	//protopatient.setNames(null);

	//Update three things:  english name, address, and caretaker attributes
	def patient = builder.patient( me:protopatient){


	    //Khmer name will be reimported separately

	    def existingEngName = protopatient.getNames().find(){ it.preferred == false};

	    //English name is secondary
	    personName(
		    me:existingEngName,
		    "familyName":source.get("FamilyName_e"),
		    "givenName":source.get("FirstName_e"),
		    "preferred":false);


	    def existingAddr = protopatient.getAddresses().find(){ it.preferred == true && it.voided == false};

	    personAddress(
		    me:existingAddr,
		    preferred:true, //there's only 1 - if there's a different value on a more recent import, it's preferred
		    "address1":source.get("Address"),
		    "cityVillage":source.get("Vi_Village_e"),//village
		    "neighborhoodCell":source.get("Cn_Commune_e"), //commune
		    "countyDistrict":source.get("Di_District_e"),//district
		    "stateProvince":source.get("Pv_Province_e"),//province
		    "country":source.get("Country") //country
		    );


	    //caretaker info is being handled as an attribute rather than a Person and Relationship
	    [
		"CaretakerName_k",
		"RelationShipType"
	    ].each(){ name ->
		def value = source.get(name);
		if( ! StringUtils.isEmpty(value)){
		    personAttribute("attributeType":personAttributeTypeIds[name],
			    "value":value);
		}
	    }
	}
	return patient;

    } //end build patient


} //end class

