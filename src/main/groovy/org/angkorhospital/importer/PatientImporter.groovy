package org.angkorhospital.importer;

import java.io.FileInputStream;
import java.io.FileReader;
import java.util.Properties;

import org.angkorhospital.importer.assembler.PatientAssembler;
import org.angkorhospital.importer.source.AHCPatientSource;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;

import org.openmrs.*;
import org.openmrs.api.*;
import org.openmrs.api.context.*;
import org.openmrs.dsl.OpenMRSFactoryBuilder;
import org.apache.commons.cli.*;

import org.openmrs.tools.importer.factory.*;
import org.openmrs.tools.importer.assembler.*;
import org.openmrs.tools.importer.source.*;
//TODO: refactor to extend basePatientImporter, use Launcher
class PatientImporter  {

	static org.apache.commons.logging.Log log = LogFactory
	.getLog("org.openmrs");
	static org.apache.commons.logging.Log reimport = LogFactory.getLog("reimport");

	/*
	* clear the cache periodically to prevent slowdowns
	*
	*/
       def clearHibernateCache(){
	       Context.flushSession();
	       Context.clearSession();
	       log.debug("Cleared hibernate cache.");
       }

       def logRedo(msg, source){
	   reimport.error(msg);
	   reimport.error(source.writeAsCsv());
       }

	def initSource(String filepath){
		def source = new AHCPatientSource(filepath);
		//source.next();//load up the first real line
		return source;
	}



	void importPatients(String filepath){

		def source = new org.angkorhospital.importer.source.AHCPatientSource(filepath);
		def factory = new PatientAssembler( source:source);
		def importCounter = 0;


		while(source.next() != null ){
			//read in each line to be processed
			importCounter++;
			if( importCounter >= 400 ){
				clearHibernateCache();
				importCounter=0;
			}

			if( source.currentLine == null )
				continue;

			//create a patient object and save it
			Patient newPatient = factory.buildPatient();

			Patient savedPatient = null; //on successful save
			Patient existingPatient = null; //if patient already exists
			try {
				savedPatient = Context.getPatientService().savePatient(newPatient);
				log.info("Success importing line " + source.currentLineNum +
						", id "  + savedPatient?.getPatientIdentifier());
				System.out.println("Success importing id (line  " + source.getCurrentLineNum() +
						"): " + savedPatient.toString() + "/" +  savedPatient.getPatientIdentifier());
			} catch (org.openmrs.api.IdentifierNotUniqueException e ){
				//patient already exists - load existing patient, edit, and apply changes
				existingPatient = null;

				def id = newPatient.getPatientIdentifier();
				List<PatientIdentifier> existingIds = Context.getPatientService().getPatientIdentifiers(id.getIdentifier(),
						[id.getIdentifierType()], null, null, id.isPreferred());
				if( existingIds && existingIds.size() > 0){

					//there should be only one
					existingPatient = Context.getPatientService().getPatient(existingIds.first().getPatient().getId());
					def dataChanged = PatientUtils.updateAll(existingPatient, newPatient);
					if(dataChanged ){   //save the update
						log.debug("Made changes to existing patient (line " + source.currentLineNum +
								"): "  + existingPatient.getPatientIdentifier()  + "/" + existingPatient.toString());
						try {
							savedPatient = Context.getPatientService().savePatient(existingPatient);
						}catch( Exception excep){
							log.error( "Got an error trying to save "  +  + existingPatient.getPatientIdentifier()  + "/" +  existingPatient  + ": line "
									+ source.currentLineNum + ": "  + excep.getMessage());
							logRedo(excep.getMessage(), source);
						}
					} else {  //nothing changed - no work to do
						savedPatient = existingPatient;
						log.debug("Patient already existed and is unchanged  (line " + source.currentLineNum +
								") "  + existingPatient.getPatientIdentifier()  + "/" +   existingPatient.toString());
					}
				}

			}catch( Exception e){  //catch all other exceptions from first save attempt
				log.error( "Got an error trying to save "  + savedPatient?.getPatientIdentifier()  + "/" +   savedPatient +
						": line " + source.currentLineNum +
						": " + e.getMessage());
				logRedo(e.getMessage(), source);
			} //end create patient

			//save relationship, if any
			if( savedPatient != null) {
				def relationship = factory.buildRelationship(savedPatient.id);
				if( relationship != null ){
					if( existingPatient != null ){
						//check to see if we're duplicating
						List<Relationship> existingRelationships = 	Context.getPersonService().getRelationships( null, savedPatient, relationship.getRelationshipType());
						if( ! PatientUtils.isNewRelationship(relationship, existingRelationships))
							continue; //relationship already defined, do not reimport

					}

					try {
						def savedPerson = Context.getPersonService().savePerson(relationship.personA);
						def savedRelationship = Context.getPersonService().saveRelationship(relationship);
					}catch( Exception e){  //catch all other exceptions from first save attempt
						log.error( "Got an error trying to save relationship "  +
								relationship +  ": line " + source.currentLineNum +
								": " + e.getMessage());
						logRedo(e.getMessage(), source);
					}
				}
			}//end save relationship

		}//end while

	}




	public static void main(String[]args) {

		println "Importing Patients..."

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

			// import
			def importer = new PatientImporter();
			importer.importPatients(importFile);

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