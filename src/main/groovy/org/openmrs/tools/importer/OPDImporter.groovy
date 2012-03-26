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
class OPDImporter extends BaseEncounterImporter {

    void initComponents(String filepath){
	importer = new OPDImporter();
	source = new AHCOpdEncounterSource(filepath);
	assembler = new OPDEncounterAssembler( source:source);
    }

    public OPDImporter(){
	super();
    }

    public OPDImporter(String filepath){
	this();
	initComponents(filepath);
    }

}