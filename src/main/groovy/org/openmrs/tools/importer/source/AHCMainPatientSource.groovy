package org.openmrs.tools.importer.source;

import org.apache.commons.lang.StringUtils;

class AHCMainPatientSource extends CsvFileSource {


	public AHCMainPatientSource() {
		super();
	}

	public AHCMainPatientSource(String filepath) {
		super(filepath);
	}


	//defined in  OpenMRS Adminsitration > Person Attribute Management
	def personAttributeTypeIds = [
		"legacy_db_table":8,//string
		"caretaker":9,//boolean //
		"telephone":10, //string
		"distance_id":11,//string
		"legacy_age":12, //int
		"legacy_newrec":13//int
	];

	def relationshipIds = [

		"Aunt" :[ "gender":"F", "relationshipTypeId":5 ],
		"Brother In-Law" :[ "gender":"M", "relationshipTypeId":8 ],
		"Brother" :[ "gender":"M", "relationshipTypeId":7 ],
		"Daughter" :[ "gender":"F", "relationshipTypeId":11 ],
		"Father In-Law" :[ "gender":"M", "relationshipTypeId":15 ],
		"Father" :[ "gender":"M", "relationshipTypeId":13 ],
		"Friend" :[ "gender":"F", "relationshipTypeId":17 ],
		"Grandfather" :[ "gender":"M", "relationshipTypeId":18 ],
		"Grandmother" :[ "gender":"F", "relationshipTypeId":19 ],
		"Husband" :[ "gender":"M", "relationshipTypeId":22 ],
		"Mother In-Law" :[ "gender":"F", "relationshipTypeId":16 ],
		"Mother" :[ "gender":"F", "relationshipTypeId":14 ],
		"Neighbor" :[ "gender":"F", "relationshipTypeId":20 ],
		"Sister In-Law" :[ "gender":"F", "relationshipTypeId":9 ],
		"Sister" :[ "gender":"F", "relationshipTypeId":10 ],
		"Son" :[ "gender":"M", "relationshipTypeId":12 ],
		"Uncle" :[ "gender":"M", "relationshipTypeId":6 ],
		"Wife" :[ "gender":"F", "relationshipTypeId":21 ],
	];


	def getDbTable(){
		return ["personAttributeTypeId":personAttributeTypeIds["legacy_db_table"], "value":"tblPatient"];
	}
	def getTelephone(){
		return ["personAttributeTypeId":personAttributeTypeIds["telephone"], "value":readValue("Telephone")];
	}
	def getDistanceId(){
		return ["personAttributeTypeId":personAttributeTypeIds["distance_id"], "value":readValue("Distance_Disp")];
	}
	def getAge(){
		return ["personAttributeTypeId":personAttributeTypeIds["legacy_age"], "value":readValue("Age")];
	}
	def getNewRec(){
		return ["personAttributeTypeId":personAttributeTypeIds["legacy_newrec"], "value":readValue("NewRec")];
	}

	//	def get(){
	//		return ["personAttributeTypeId":personAttributeTypeIds[""], "value":readValue("")];
	//	}

	def getPrimaryIdentifier(){
		//type = 2
		//location = ahcmain
		//preferred=true
		return [ "identifier":readValue("PatientCodeNo",currentLine), "location":2, "preferred":true, "identifierType":2 ];
	};

	def getSecondaryIdentifier(){
		//type=3
		//location = 2 ahcmain
		//preferred=false
		String openmrsStyleId = StringUtils.replace(readValue("PatientCodeNo",currentLine),"-","");

		return [ "identifier":openmrsStyleId, "location":2, "preferred":false, "identifierType":3 ];
	}
	def getIdentifiers(){
		return [
			"primary": getPrimaryIdentifier(),
			"secondary":getSecondaryIdentifier()
		];
	}

	def getGender(){
		def value =  readValue("Gender");
		value = ( value != null ?  value : "U"); //"U"nknown if not set
		return [ "gender":value];
	}

	//	def getRelationship(){
	//		def key =  readValue("RelationShipType");
	//		def map = relationshipIds[key];
	//		if( map != null)
	//			map["name"] = key;
	//		return map;
	//
	//	}
	def getRelationshipType(){
		def key =  readValue("RelationShipType");
		if(StringUtils.isEmpty(key))
			return null;
		def map = relationshipIds[key];
		return [relationshipTypeId: map["relationshipTypeId"]];

	}

	def getCaretakerName(){
		def key =  readValue("RelationShipType");
		def map = relationshipIds[key];
		return [ familyName: readValue("CaretakerName_k"), preferred:true];
	}

	def getCaretakerGender(){
		def key =  readValue("RelationShipType");
		def map = relationshipIds[key];
		return [ gender: map.gender];
	}

	def getBirthdate(){
		return[ birthdate:parseDate("DateOfBirth")];
	}

	def getPersonDateCreated(){
		return[ personDateCreated:parseDate("CreationDate")];
	}

	def getDateCreated(){
		return[ dateCreated:parseDate("CreationDate")];
	}

	def getPatientNames(){
		return [ "kh":["givenName":readValue("FirstName_k"), "familyName":readValue("FamilyName_k"), "preferred":true],
			"en":["givenName":readValue("FirstName_e"), "familyName":readValue("FamilyName_e"), "preferred":false]
		];
	}


	def getAddress(){
		return[
			preferred:true, //there's only 1 - if there's a different value on a more recent import, it's preferred
			"address1":readValue("Address"),
			"cityVillage":readValue("Vi_Village_e"),//village
			"neighborhoodCell":readValue("Cn_Commune_e"), //commune
			"countyDistrict":readValue("Di_District_e"),//district
			"stateProvince":readValue("Pv_Province_e"),//province
			"country":"CAMBODIA" //country
		];
	}


}
