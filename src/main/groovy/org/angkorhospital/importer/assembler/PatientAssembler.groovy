package org.angkorhospital.importer.assembler
import org.angkorhospital.importer.source.PatientSource;
import org.openmrs.tools.importer.assembler.BasePatientAssembler;
import org.openmrs.tools.importer.source.ImportSource;

import org.openmrs.dsl.*;


class PatientAssembler  implements org.openmrs.tools.importer.assembler.BasePatientAssembler {

	PatientSource source;
	FactoryBuilderSupport builder = new PatientFactoryBuilder();

	public AHCMainPatientAssembler(){

	}
	void setSource(ImportSource newSource){
		source = newSource;
	};

	/**
	 * Use Groovy builder magic to create a Patient object
	 *
	 *
	 * @param source
	 * @return
	 */
	org.openmrs.Patient buildPatient(){
		//merge the attributes from several function calls
		def attributes = source.getGender() + source.getBirthdate() + source.getPersonDateCreated() + source.getDateCreated();
		def patient = builder.patient(attributes){
			patientIdentifier(source.getPrimaryIdentifier());
			patientIdentifier(source.getSecondaryIdentifier());
			personName((source.getPatientNames())["kh"]);
			personName((source.getPatientNames())["en"]);
			personAddress(source.getAddress());
			personAttribute(source.getTelephone());
			personAttribute(source.getDbTable());
			personAttribute(source.getNewRec());
			personAttribute(source.getAge());
			personAttribute(source.getDistanceId());
		};
		return patient;
	}


	org.openmrs.Relationship buildRelationship( int patientId){
		//check for empty caretaker
		def relationshipTypeAttrs = source.getRelationshipType();
		if( relationshipTypeAttrs == null )
		return null; //no caretaker set for this patient
		def relationship = builder.relationship( relationshipTypeAttrs +  [patientId:patientId]){
			person(source.getCaretakerGender()){
				personName(source.getCaretakerName()); //the caretaker
			}
		};
		return relationship;
	}


}
