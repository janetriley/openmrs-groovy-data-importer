package org.openmrs.tools.importer;

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

import org.openmrs.tools.importer.assembler.OPDEncounterAssembler;
import org.openmrs.tools.importer.builder.PatientFactoryBuilder;
import org.openmrs.tools.importer.source.AHCOpdEncounterSource;

import org.openmrs.tools.importer.builder.*;
import org.openmrs.tools.importer.assembler.*;
import org.openmrs.tools.importer.source.*;
abstract class BaseEncounterImporter  {

    static org.apache.commons.logging.Log log = LogFactory
    .getLog("org.openmrs");
    static org.apache.commons.logging.Log reimport = LogFactory.getLog("reimport");

    ImportSource source = null;
    BaseEncounterAssembler assembler = null;

    public BaseEncounterImporter(){
	;
    }

    public BaseEncounterImporter(String filepath){
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

    void importEncounters(String filepath){
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


	    def  v = assembler.buildVisit();

	    /*
	     * save the components in this sequence:
	     * Patient
	     * Visit
	     * Encounter
	     * Each successful save will generate an ID number for the component.
	     * The ID numbers will be saved in the associated objects.
	     * (i.e. Encounter needs a legit Patient ID and Visit ID
	     *
	     */
	    Patient newPatient = v.getPatient();
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

		def savedVisit = null;
		boolean encounterError = false;
		if( v instanceof org.openmrs.Visit ){
		    savedVisit = Context.getVisitService().saveVisit(v);

		    for( Encounter enc in v.getEncounters()){
			def e  = (Context.getEncounterService().saveEncounter(enc));
			//assertTrue(enc.id > 0);
			if( e == null || enc.id == 0 )
			encounterError = true;
			else
			numSaved++;
		    }
		} else if ( v instanceof org.openmrs.Encounter){

		    def e  = (Context.getEncounterService().saveEncounter(v));
		    //assertTrue(enc.id > 0);
		    if( e == null || v.id == 0 )
		    encounterError = true;
		    else{
		    numSaved++;
		    savedVisit = Boolean.TRUE;
		    }
		}


	    if( savedVisit  == null ){
		logRedo("Failed to save visit for " + newPatient?.getPatientIdentifier(), source);
	    }

	    else if( encounterError  == null ){
		logRedo("Failed to save encounter for " + newPatient?.getPatientIdentifier(), source);
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
    println("Final cache size was " + OPDEncounterAssembler.conceptCache.size());
}



def logRedo(msg, source){
    reimport.error(msg);
    reimport.error(source.writeAsCsv());
}


}