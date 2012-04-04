package org.angkorhospital.importer.source
import java.io.Reader;

import org.openmrs.tools.importer.source.CsvFileSource;

class LabCrossmatchEncounterSource extends CsvFileSource {

    LabCrossmatchEncounterSource() {
	super();
    }

    LabCrossmatchEncounterSource(String filepath) {
	super(filepath);
    }

    LabCrossmatchEncounterSource(java.io.Reader newReader){
	//use for testing - hand it a StringReader with header and test data
	super(newReader);
    }


    //"Date","CrossPatID","CrossMatchID",
    //"Ward","Doctor","Reason","Group","Bag No","Group of Bag","Volume ml","Product","Comment","Transfused"

    def get(String key){
	switch(key){
	    case "legacyTable":
		return  "LabDCrossmatch";
		break;
	    case ["legacyId", "CrossMatchID"]:
		return parseInteger("CrossMatchID");
		break;
	    case "patientId":
		return readValue("CrossPatID");
		break;
	    case [
		"DateCreated",
		"Date"
	    ]:
		parseDate("Date");
		break;
	    case [
		"Volume ml"
	    ]:
		return parseInteger(key); //booleans - test
		break;
	    //all others return text
	    case [
		"CrossPatID",
		"Bag No",
		"Ward",
		"Doctor",
		"Reason",
		"Group",
		"Group of Bag",
		"Product",
		"Comment",
		"Transfused" //Yes,No,Pending
	    ]:
	    default:
		return readValue(key);
		return null;
	}
    }






}
