package org.angkorhospital.importer.assembler
import org.angkorhospital.importer.source.AHCPatientSource;
import org.openmrs.tools.importer.assembler.BasePatientAssembler;
import org.openmrs.tools.importer.source.ImportSource;
import org.apache.commons.lang.StringUtils;
import org.openmrs.dsl.*;


class PatientAssembler  implements org.openmrs.tools.importer.assembler.BasePatientAssembler {

    AHCPatientSource source;
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

	def patient = builder.patient(
		gender: source.get("Gender"),
		birthdate: source.get("DateOfBirth"),
		dateCreated: source.get("CreationDate"),
		personDateCreated: source.get("CreationDate")){

		    //primary identifier is legacy ID, XXXX-XXXXXX
		    patientIdentifier(	 [ "identifier":source.get("PatientCodeNo"),
				"location":2, "preferred":true, "identifierType":2 ]);

		    //secondary identifier is all numbers - take out the dash
		    patientIdentifier([ "identifier":source.getSecondaryIdentifier(),
				"location":2, "preferred":false, "identifierType":3 ]);

		    //Khmer name is preferred
		    personName("givenName":source.get("FirstName_k"),
			    "familyName":source.get("FamilyName_k"),
			    "preferred":true);

		    //English name is secondary
		    personName("givenName":source.get("FirstName_e"),
			    "familyName":source.get("FamilyName_e"),
			    "preferred":false);

		    personAddress(
			    preferred:true, //there's only 1 - if there's a different value on a more recent import, it's preferred
			    "address1":source.get("Address"),
			    "cityVillage":source.get("Vi_Village_e"),//village
			    "neighborhoodCell":source.get("Cn_Commune_e"), //commune
			    "countyDistrict":source.get("Di_District_e"),//district
			    "stateProvince":source.get("Pv_Province_e"),//province
			    "country":source.get("Country") //country
			    );


			personAttributeTypeIds.each(){ name, value ->
			    personAttribute("attributeType":value,
				"value":source.get(name));
			}




		};
	return patient;
    }
    //defined in  OpenMRS Adminsitration > Person Attribute Management
    static def personAttributeTypeIds = [
	"legacyTable":8,//string Legacy
	"CaretakerName_k":9,//boolean //Caretaker Name
	"Telephone":10, //string
	"Distance_Disp":11,//string Distance Id
	"Age":12, //int Legacy Age
	"NewRec":13,//Legacy newRec int
	"RelationShipType":14//Caretaker Relatiopnship - string
    ];

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
