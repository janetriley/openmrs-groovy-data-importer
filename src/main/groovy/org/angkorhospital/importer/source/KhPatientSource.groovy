package org.angkorhospital.importer.source;

import org.apache.commons.lang.StringUtils;
import org.openmrs.groovyimporter.source.CsvFileSource;

/**
* MS Access stored all text as Limon-S
* New data dump - exported only the KH fields plus identifier from Access to CSV
* ran the CSV through a text converter, now it's unicode KH
* Text converter utility converts arabic numbers to Khmer, this file converts them back
* Cut and pasted the english header back into the data file, so field names are the same
*/

class KhPatientSource extends CsvFileSource {

    //map Khmer numbers to Arabic numbers
    //These are all the legit characters in a patient identifier
    static def numberMap = [
	"០":"0",
	"១":"1",
	"២":"2",
	"៣":"3",
	"៤":"4",
	"៥":"5",
	"៦":"6",
	"៧":"7",
	"៨":"8",
	"៩":"9",
	"-":"-",  //identifiers contain dashes
    ];


    public KhPatientSource() {
	super();
    }
    public KhPatientSource(String filepath) {
	super(filepath);
    }
    public KhPatientSource(java.io.Reader newReader){
	super(newReader);
    }


    //for new-style identifiers, remove the dash from the  old one
    def getSecondaryIdentifier(){
	return StringUtils.replace(get("patientId"),"-","");
    }


    def get(String key){
	switch(key){
	    case [
		"patientId",
		"PatientID",
		"PatientCodeNo"
	    ]:
		def newString = new StringBuffer(11);
		(StringUtils.strip(readValue("PatientCodeNo"))).each(){it->
		    def val = numberMap[it];
		    newString.append(val == null ? it : val);
		}
		return newString.toString();
		break;
	    default:
		return StringUtils.strip(readValue(key));
	}
    }



}
