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

abstract class BasePatientImporter  {

    static org.apache.commons.logging.Log log = LogFactory
    .getLog("org.openmrs");
    static org.apache.commons.logging.Log reimport = LogFactory.getLog("reimport");

    ImportSource source = null;
    BasePatientAssembler assembler = null;

    public BasePatientImporter(){
	;
    }

    public BasePatientImporter(String filepath){
	this();
	initComponents(filepath);
    }

    abstract void initComponents(String filepath);

    /*
     * clear the cache periodically to prevent slowdowns
     *
     */
    def clearHibernateCache(){
	Context.flushSession();
	Context.clearSession();
	log.debug("Cleared hibernate cache.");
    }

    void importPatients(String filepath){
	//global property log.level.openmrs
	//OpenmrsUtil.applyLogLevels("error");

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


	    Patient savedPatient = null; //on successful save
	    try {

		if( newPatient.id > 0 ){
		    savedPatient = newPatient; //no work to do, this import doesn't change patients
		} else{
		    //patient not found - save a stub
		    savedPatient = Context.getPatientService().savePatient(newPatient);
		    log.info("Created a new patient at line " + source.currentLineNum +
			    ", id "  + savedPatient?.getPatientIdentifier());
		}
		if( savedPatient == null ){
		    logRedo("Failed to save patient " + newPatient?.getPatientIdentifier(), source);
		    continue; //can't save visits and encounters without a patient
		}

		else { //success
		    //;
		    //   System.out.println("Success   (line  " + source.getCurrentLineNum() +
		    //	    "): " + savedVisit.toString() + ": patient " + savedPatient.toString() + "/" +  savedPatient.getPatientIdentifier());
		}
	    }catch( Exception e){  //catch all other exceptions from first save attempt
		log.error( "Got an error trying to save "  + savedPatient?.getPatientIdentifier()  + "/" +   savedPatient +
			": line " + source.currentLineNum +
			": " + e.getMessage());
		logRedo(e.getMessage(), source);
	    } //end create patient

	}//end while

	println("Ending at: " + new Date() + " at line "  + source.getCurrentLineNum());
	println("Final cache size was " + assembler.conceptCache.size());
    }


    def logRedo(msg, source){
	reimport.error(msg);
	reimport.error(source.writeAsCsv());
    }


}