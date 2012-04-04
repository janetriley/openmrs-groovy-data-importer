package org.angkorhospital.importer.source
import java.io.Reader;

import org.openmrs.tools.importer.source.CsvFileSource;

class EREncounterSource extends CsvFileSource {

    EREncounterSource() {
	super();
    }

    EREncounterSource(String filepath) {
	super(filepath);
    }

    EREncounterSource(java.io.Reader newReader){
	//use for testing - hand it a StringReader with header and test data
	super(newReader);
    }

    //"Date","PatientID","ERID","From","VisitType","DxID1","DxID2","DxID3",
    //"Treatment","Ketamine","Observation","TimeInER","DischargeTo","Comment"



    def get(String key){
	switch(key){
	    case "legacyTable":
		return "tblERPatientsInfo";
		break;
	    case ["legacyEncounterId", "ERID"]:
		return parseInteger("ERID");
		break;
	    case ["patientId", "PatientID"]:
		return readValue("PatientID");
		break;
	    case [
		"DateCreated",
		"Date"
	    ]:
		parseDate("Date");
		break;
	    case "From":
	    /*from AcuityPWard.txt*/
	    /*"WardID","Ward"
	     -1383732646,"Direct"
	     1,"ICU"
	     2,"IPD"
	     3,"OPD"
	     4,"LAU"
	     5,"ER"
	     1052195837,"OT"
	     1129191084,"Surgery"
	     1233386422,"Minor Procedure"
	     1296207625,"Homecare"
	     */
		def ward = parseInteger("From");
		switch(ward){
		    case -1383732646: return "Direct Admission"; //need concept DIRECT ADMISSION return "Direct";
		    case 1: return "ICU";
		    case 2: return "IPD";
		    case 3: return "OPD";
		    case 4: return "LAU";
		    case 5: return "ER";
		    case 1052195837: return "OT";
		    case 1129191084: return "Surgery";
		    case 1233386422: return "Minor Procedure";
		    case 1296207625: return "Homecare";
		    default: return null;
		}
		break;

	    case "VisitType":
	    /*Minerva:constants me$ cat tblPERVisitType.txt
	     "VisitTypeID","VisitType"
	     1,"EM"
	     2,"Non-EM"
	     3,"Urgent"
	     */
		def visit = parseInteger("VisitType");
		switch(visit){
		    case 1: return "EM";
		    case 2: return "Non-EM";
		    case 3: return "Urgent";
		    default: return null;
		}
		break;

	    case "Treatment":
	    /*from ERPTreatment
	     * -2091716494	Minor Procedure
	     -65359861	Cast/Cast Removal
	     9913350	Emergency-Non Trauma
	     183968164	Dressing Change
	     715801045	Minor Surgery
	     1697929487	Consult
	     2140836921	Emergency-Trauma
	     */
		def treatment = parseInteger("Treatment");
		switch(treatment){
		    case -2091716494: return "Minor Procedure";
		    case  -65359861: return "Cast/Cast Removal";
		    case 9913350: return "Emergency-Non Trauma";
		    case  183968164: return "Dressing Change";
		    case  715801045: return "Minor Surgery";
		    case  1697929487: return "Consult";
		    case  2140836921: return "Emergency-Trauma";
		    default:
			return null;
		}
		break;
	    case "TimeInER":
	    /*source: "ERPTimeInER"
	     *
	     * Minerva:constants me$ cat ERPTimeInER.txt
	     1	15 Minutes
	     2	30 Minutes
	     3	60 Minutes
	     4	90 Minutes
	     5	120 Minutes
	     6	>120 Minutes
	     *
	     *
	     * */
		def time = parseInteger("TimeInER");
		switch(time){
		    case 1: return "15 Minutes";
		    case 2: return "30 Minutes";
		    case 3: return "60 Minutes";
		    case 4: return "90 Minutes";
		    case 5: return "120 Minutes";
		    case 6: return ">120 Minutes";
		    default: return null;
		}
		break;
	    case [
		"Ketamine",
		"Observation",
	    ]:
		return parseBoolean(key); //booleans - test
		break;
	    case "DischargeTo":
	    /*	    Minerva:constants me$ cat tblDispositions.txt
	     "DispositionID","disp_Disposition"
	     -1893580051,"ICU"
	     -711104359,"Home"
	     -505095962,"Death"
	     -427741668,"LAU"
	     1049893479,"LAMA"
	     1050082169,"IPD"
	     1050089646,"ER"
	     1276675133,"Surgery"
	     1952110970,"OT"
	     */		def disp = parseInteger("DischargeTo");
		switch(disp){
		    case -1893580051: return "ICU";
		    case -711104359: return "DISCHARGE TO HOME";// return "Home";
		    case -505095962: return "Death";
		    case -427741668: return "LAU";
		    case 1049893479: return "LAMA";
		    case 1050082169: return "IPD";
		    case 1050089646: return "ER";
		    case 1276675133: return "Surgery";
		    case 1952110970: return "OT";
		    default: return null;
		}
	    //tblDispositions
		break;
	    //if in doubt, return text
	    case "Comment":
	    case "DxID1":
	    case "DxID2":
	    case "DxID3":
	    default:
		return readValue(key);
	}
    }






}
