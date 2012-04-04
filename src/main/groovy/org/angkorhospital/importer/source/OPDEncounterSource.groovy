package org.angkorhospital.importer.source
import java.io.Reader;
import org.openmrs.tools.importer.source.CsvFileSource;

class OPDEncounterSource extends CsvFileSource {

	OPDEncounterSource() {
		super();
	}

	OPDEncounterSource(String filepath) {
		super(filepath);
	}

	OPDEncounterSource(java.io.Reader newReader){
		//use for testing - hand it a StringReader with header and test data
		super(newReader);
	}

	//"RegtID","Reg_PatientID","Reg_DateOfVisit","Registered","FeePaid","GeneralImpression",
	//"Reg_DiseaseID","Reg_DiseaseID2","Reg_DiseaseID3","ChronicUrlDxID","DidNotWait","Return",
	//"FollowUp","Referal","Referred From","DateCreated","Wt/Ht","Admitted To","CaseDx1","CaseDx2","CaseDx3"

	def get(String key){
		switch(key){
			case "legacyTable":
				return "tblOutPatientsInfo";
				break;
			case ["legacyEncounterId","RegtID"]:
				return parseInteger("RegtID");
				break;
			case ["patientId", "Reg_PatientID"]:
				return readValue("Reg_PatientID");
				break;
			case [
				"DateCreated",
				"Reg_DateOfVisit"
			]:
				parseDate(key);
				break;

			case [
				"DidNotWait",
				"FeePaid",
				"FollowUp",
				"Referal",
				"Registered",
				"Return"
			]:
				return parseBoolean(key); //booleans - test
				break;

			case [
				"Admitted To",
				"CaseDx1",
				"CaseDx2",
				"CaseDx3",
				"ChronicUrlDxID",
				"GeneralImpression",
				"Referred From",
				"Reg_DiseaseID",
				"Reg_DiseaseID2",
				"Reg_DiseaseID3",
				"Wt/Ht"
			]:
				return readValue(key);
				break;
			default:
				return readValue(key);
				return null;
		}
	}






}
