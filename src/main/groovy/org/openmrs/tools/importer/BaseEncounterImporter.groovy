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


    static BaseEncounterImporter importer = null;
    static ImportSource source = null;
    static BaseEncounterAssembler assembler = null;
    abstract void initComponents(String filepath);

/*
    public BaseEncounterImporter(String filepath){
	this();
	initComponents(filepath);

    }
  */  /*
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
	println("Starting at: " + new Date() + " at line "  + source.getCurrentLineNum());


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


	    Visit  v = assembler.buildVisit();
	    Patient newPatient = v.getPatient();

	    Patient savedPatient = null; //on successful save
	    Patient existingPatient = null; //if patient already exists
	    try {
		if( newPatient.id > 0 ){
		    savedPatient = newPatient; //no work to do, this import doesn't change patients
		} else{
		    //patient not found - save a stub
		    savedPatient = Context.getPatientService().savePatient(newPatient);
		    log.info("Created a new patient at line " + source.currentLineNum +
			    ", id "  + savedPatient?.getPatientIdentifier());
		}
		Visit savedVisit = Context.getVisitService().saveVisit(v);
		boolean encounterError = false;
		for( Encounter enc in v.getEncounters()){
		    def e  = (Context.getEncounterService().saveEncounter(enc));
		    //assertTrue(enc.id > 0);
		    if( e == null || enc.id == 0 )
			encounterError = true;
		    else
			numSaved++;
		}

		if( savedPatient == null ){
		    logRedo("Failed to save patient " + newPatient?.getPatientIdentifier(), source);
		}

		else if( savedVisit  == null ){
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




    public static void main(String[]args) {

	println "Importing Encounters..."

	// set params
	String importFile = null;
	String openmrsRuntimeProperties = null;
	String openmrsUser = null;
	String openmrsPw = null;

	CommandLineParser parser = new BasicParser();
	Options options = new Options();
	options.addOption("u", "user", true, "openMRS username (required)");
	options.addOption("p", "password", true,
		"openMRS password (required)");
	options.addOption("r", "runtime", true,
		"openMRS runtime properties (required)");
	options.addOption("f", "file", true, "file to import (required)");
	options.addOption("d", "delim", true,
		"field delmiter (optional, defaults to semicolon (;))");
	options.addOption("i", "inner", true,
		"delimiter inside a field (optional, defaults to pipe (|) )");
	options.addOption("h", "help", false, "show this help");

	// Parse the program arguments
	CommandLine commandLine = parser.parse(options, args);

	if (commandLine.hasOption('u')) {
	    openmrsUser = commandLine.getOptionValue('u');
	}
	if (commandLine.hasOption('p')) {
	    openmrsPw = commandLine.getOptionValue('p');
	}
	if (commandLine.hasOption('r')) {
	    openmrsRuntimeProperties = commandLine.getOptionValue('r');
	}
	if (commandLine.hasOption('f')) {
	    importFile = commandLine.getOptionValue('f');
	}

	// exit if required fields missing
	if (commandLine.hasOption('h') || openmrsUser == null
	|| openmrsPw == null || openmrsRuntimeProperties == null
	|| importFile == null) {

	    HelpFormatter formatter = new HelpFormatter();
	    formatter
		    .printHelp(
		    "java importer  -u openmrsUser -p openmrsPw"
		    + "-r openmrsRuntimePropertiesFile -f importDataFile",
		    options);
	    System.exit(1);
	}

	try {

	    Properties prop = new Properties();
	    prop.load(new FileInputStream(openmrsRuntimeProperties));
	    String connectionUser = prop.getProperty("connection.username");
	    String connectionPw = prop.getProperty("connection.password");
	    String connectionUrl = prop.getProperty("connection.url");

	    log.info("Importing file " + importFile + " to database "
		    + connectionUrl + " as db user " + connectionUser
		    + " and openMRS user " + openmrsUser);
	    // connection init
	    Context.startup(connectionUrl, connectionUser, connectionPw, prop);
	    org.openmrs.api.context.Context.openSession();
	    org.openmrs.api.context.Context
		    .authenticate(openmrsUser, openmrsPw);// openmrs user pass
	    importer.importEncounters(importFile);

	} catch (java.io.FileNotFoundException e) {
	    log.error("Couldn't find file, can't import anything."
		    + e.getMessage());

	} catch (RuntimeException e) {
	    log.error("Runtime exception: " + e.toString() + "\n");
	    e.printStackTrace();
	} finally {
	    Context.closeSession();
	}
	log.info("Finished importing.");
	System.out.println("Finished importing.");
    }

    def logRedo(msg, source){
	reimport.error(msg);
	reimport.error(source.writeAsCsv());
    }


}