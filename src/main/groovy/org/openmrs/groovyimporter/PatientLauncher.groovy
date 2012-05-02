package org.openmrs.groovyimporter;

import java.util.Properties;

import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;

import org.openmrs.*;
import org.openmrs.api.*;
import org.openmrs.api.context.*;
import org.apache.commons.cli.*;

import org.openmrs.dsl.*;


class PatientLauncher  {

    static org.apache.commons.logging.Log log = LogFactory
    .getLog("org.openmrs");
    static org.apache.commons.logging.Log reimport = LogFactory.getLog("reimport");

    static BasePatientImporter importer = null;

    public static void main(String[]args) {

	println "Importing ..."


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
	options.addOption("c", "class", true, "full importer class name e.g org.openmrs.tools.importer.YourImporterClassHere");

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

	if (commandLine.hasOption('c')) {
	    importer  = Class.forName(commandLine.getOptionValue('c')).newInstance();
	    if( importer == null ){
		System.out("ERROR: couldn't find a class for " + commandLine.getOptionValue('c'));
		System.exit(1);
	    }
	}
	// exit if required fields missing
	if (commandLine.hasOption('h') || openmrsUser == null
	|| openmrsPw == null || openmrsRuntimeProperties == null
	|| importFile == null || importer == null) {

	    HelpFormatter formatter = new HelpFormatter();
	    formatter
	    .printHelp(
	    "java importer  -u openmrsUser -p openmrsPw"
	    + "-r openmrsRuntimePropertiesFile -f importDataFile -c classname.of.importer",
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

	    log.info("Starting OpenMRS... (this may take some time)");
		    // connection init
		    Context.startup(connectionUrl, connectionUser, connectionPw, prop);
	    org.openmrs.api.context.Context.openSession();
	    org.openmrs.api.context.Context
	    .authenticate(openmrsUser, openmrsPw);// openmrs user pass

	    log.info("Starting data import at " + new Date());

	    importer.importRecords(importFile);

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

}