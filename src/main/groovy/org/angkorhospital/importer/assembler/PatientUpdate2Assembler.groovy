package org.angkorhospital.importer.assembler

//import org.openmrs.groovyimporter.assembler.BasePatientAssembler;
import org.angkorhospital.importer.source.KhPatientSource;
import org.openmrs.Patient;
import org.openmrs.Location;
import org.openmrs.PatientIdentifierType;
import org.apache.commons.lang.StringUtils;
import org.openmrs.dsl.*;
import org.openmrs.api.context.Context;

/**
 * First patient update:  reload english name and address, add caretaker info as attributes
 *
 */

class PatientUpdate2Assembler  extends org.angkorhospital.importer.assembler.PatientAssembler{

    public PatientUpdate2Assembler(){
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
	    //getPatientIdentifiers returns a collection
	    protopatient = existingIds.first().getPatient();
	}

	if(existingIds?.isEmpty() || protopatient == null ) {
	    //this patient was missed on last import -
	    //not enough info to create from scratch
	    return null;
	}



	//Update three things:  english name, address, and caretaker attributes
	def patient = builder.patient( me:protopatient){


	    //	    "PatientCodeNo","FamilyName_k","FirstName_k","CaretakerName_k"

	    //Khmer name is the preferred one
	    def existingName = protopatient.getNames().find(){ it.preferred == true};
	    if(existingName != null ){
		personName(
			me:existingName,
			"familyName":source.get("FamilyName_k"),
			"givenName":source.get("FirstName_k"),
			"preferred":true);
	    }
	    else  personName(
		"familyName":source.get("FamilyName_k"),
		"givenName":source.get("FirstName_k"),
		"preferred":true);



	    //caretaker info is being handled as attributes rather than a Person and Relationship
	    def value = source.get("CaretakerName_k");
	    if( ! StringUtils.isEmpty(value)){
		personAttribute("attributeType":personAttributeTypeIds["CaretakerName_k"],
			"value":value);
	    }
	}
	return patient;

    } //end build patient


} //end class

