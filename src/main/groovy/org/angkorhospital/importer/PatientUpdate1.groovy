package org.angkorhospital.importer;



import org.angkorhospital.importer.assembler.PatientUpdate1Assembler;
import org.angkorhospital.importer.source.AHCPatientSource;
import org.openmrs.dsl.OpenMRSFactoryBuilder;
import org.openmrs.groovyimporter.BasePatientImporter;


class PatientUpdate1 extends BasePatientImporter  {

    public PatientUpdate1(){
	super();
    }

    public PatientUpdate1(String filepath){
	super(filepath);
//	initComponents(filepath);
    }

   void initComponents(String filepath){
	source = new AHCPatientSource(filepath);
	assembler = new PatientUpdate1Assembler( source:source);
    }
}