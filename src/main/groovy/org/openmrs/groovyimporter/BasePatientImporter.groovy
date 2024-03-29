package org.openmrs.groovyimporter;

import java.io.FileInputStream;
import java.io.FileReader;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;

import org.openmrs.*;
import org.openmrs.api.*;
import org.openmrs.api.context.*;
import org.apache.commons.cli.*;

import org.openmrs.dsl.OpenMRSFactoryBuilder;
import org.openmrs.groovyimporter.assembler.*;
import org.openmrs.groovyimporter.source.*;

abstract class BasePatientImporter extends BaseImporter {

    BasePatientAssembler assembler = null;

    public BasePatientImporter(){
	;
    }

    public BasePatientImporter(String filepath){
	this();
	initComponents(filepath);
    }

    //  children to implement this:
    // abstract void initComponents(String filepath);

    void importPatients(String filepath){
	importRecords(filepath);
    }

    public void importRecords( filepath){
	//set source and assembler,
	initComponents(filepath);
	def importCounter = 0;
	def numSaved = 0;
	println("Starting to import ${filepath} at: " + new Date() + " at line "  + source.getCurrentLineNum());

	while(source.next() != null ){
	    //read in each line to be processed
	    importCounter++;
	    if( importCounter >= 400 ){
		clearHibernateCache();
		importCounter=0;
		//print something to console so we know what's going on
		println("Interim update: " + new Date() + " at line "  + source.getCurrentLineNum());
	    }

	    if( source.currentLine == null )
		continue;

	    Patient newPatient = assembler.buildPatient();
	    if( newPatient == null ){
		log.error( "Couldn't assemble a patient for line " + source.currentLineNum +
			" identifier " + source.get("patientId"));
		logRedo("assembler failed to create patient", source);
		continue;
	    }

	    Patient savedPatient = null; //on successful save
	    try {
		savedPatient = Context.getPatientService().savePatient(newPatient);
		if( savedPatient == null ){
		    logRedo("Failed to save patient " + newPatient?.getPatientIdentifier(), source);
		    continue; //can't save visits and encounters without a patient
		}

		else { //success

		    log.info("Updated patient at line " + source.currentLineNum +
			    ", id "  + savedPatient?.getPatientIdentifier());
		}
	    }catch( Exception e){  //catch all other exceptions from first save attempt
		log.error( "Got an error trying to save "  + savedPatient?.getPatientIdentifier()  + "/" +   savedPatient +
			": line " + source.currentLineNum +
			": " + e.getMessage());
		logRedo(e.getMessage(), source);
	    } //end create patient

	}//end while

	println("Ending at: " + new Date() + " at line "  + source.getCurrentLineNum());

	//println("Final cache size was " + assembler.conceptCache.size());
    }


}