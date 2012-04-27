package org.angkorhospital.importer.source;

import org.apache.commons.lang.StringUtils;
import org.openmrs.groovyimporter.source.CsvFileSource;

class AHCPatientSource extends CsvFileSource {

    public AHCPatientSource() {
	super();
    }

    public AHCPatientSource(String filepath) {
	super(filepath);
    }

    public AHCPatientSource(java.io.Reader newReader){
	super(newReader);
    }


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

    //for new-style identifiers, remove the dash from the  old one
    def getSecondaryIdentifier(){
	return StringUtils.replace(get("patientId"),"-","");
    }


    /*
     * "PatientCodeNo",
     * "FamilyName_e",
     * "FirstName_e",
     * "Gender",
     * Address",
     * "Distance_Disp",

     * "RelationShipType","
     * "Pv_Province_e","Di_District_e",
     * "Cn_Commune_e","Vi_Village_e",
     * "Telephone","Age","NewRec",
     * "DateOfBirth",
     * "CreationDate"
     *
     *
     * "

     */


    def get(String key){
	switch(key){
	    case [
		"legacyTable",
		"legacy_db_table"
	    ]:
		return "tblPatient";
		break;
	    case [
		"patientId",
		"PatientID",
		"PatientCodeNo"
	    ]:
		return StringUtils.strip(readValue("PatientCodeNo"));
		break;
	    case [
		"CreationDate",
		"DateCreated",
		"Date"
	    ]:
		return parseDate("CreationDate");
		break;
	    case "Gender":
		def value =  StringUtils.strip(readValue("Gender"));
		value = ( StringUtils.isBlank(value)  ?  "U" : value ); //"U"nknown if not set- required field
		return value;

	    case "DateOfBirth"   :
		return parseDate("DateOfBirth");
		break;

	    //if in doubt, return text
	    case ["Country", "country"]:
		return "CAMBODIA";
		break;

	    case [
		"RelationShipType",
		"Address",
		"Vi_Village_e",
		"Cn_Commune_e",
		"Di_District_e",
		"Pv_Province_e",
		"Telephone",
		"Distance_Disp",
		"Age",
		"NewRec"
	    ]:
	    default:
		return StringUtils.strip(readValue(key));
	}
    }



}
