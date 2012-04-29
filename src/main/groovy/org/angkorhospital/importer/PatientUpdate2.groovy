package org.angkorhospital.importer;



import org.angkorhospital.importer.assembler.PatientUpdate2Assembler;
import org.angkorhospital.importer.source.KhPatientSource;
import org.openmrs.dsl.OpenMRSFactoryBuilder;
import org.openmrs.groovyimporter.BasePatientImporter;
/*
 * PatientUpdate2 imports the Khmer first and family name, and caretaker name
 *
 */

class PatientUpdate2 extends BasePatientImporter  {

    public PatientUpdate2(){
	super();
    }

    public PatientUpdate2(String filepath){
	super(filepath);
//	initComponents(filepath);
    }

   void initComponents(String filepath){
	source = new KhPatientSource(filepath);
	assembler = new PatientUpdate2Assembler( source:source);
    }
}