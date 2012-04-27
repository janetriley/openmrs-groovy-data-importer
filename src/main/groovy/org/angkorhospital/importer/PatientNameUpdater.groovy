package org.angkorhospital.importer;



import org.angkorhospital.importer.assembler.PatientUpdate1Assembler;
import org.angkorhospital.importer.source.AHCPatientSource;
import org.openmrs.dsl.OpenMRSFactoryBuilder;
import org.openmrs.groovyimporter.BasePatientImporter;


class PatientNameUpdater extends BasePatientImporter  {

    public PatientNameUpdater(){
	super();
    }

    public PatientNameUpdater(String filepath){
	this();
	initComponents(filepath);
    }

   void initComponents(String filepath){
	source = new AHCPatientSource(filepath);
	assembler = new PatientUpdate1Assembler( source:source);
    }
}