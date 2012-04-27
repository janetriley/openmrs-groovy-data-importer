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

/**
 * BaseImporter is an abstract class
 * that implements some utilities common to every import
 * (logging, caching, etc.)
 *
 */

abstract class BaseImporter  {

    static org.apache.commons.logging.Log log = LogFactory
    .getLog("org.openmrs");
    static org.apache.commons.logging.Log reimport = LogFactory.getLog("reimport");

    ImportSource source = null;
    //BaseAssembler

    public BaseImporter(){
	;
    }

    public BaseImporter(String filepath){
	this();
	initComponents(filepath);
    }

    abstract void initComponents(String filepath);
    //TODO: refactor baseEnc and basePatient to have an import() method
    //rather than importX


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


}