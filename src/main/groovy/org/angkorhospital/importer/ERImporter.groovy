package org.angkorhospital.importer;



import org.angkorhospital.importer.assembler.EREncounterAssembler;
import org.angkorhospital.importer.source.EREncounterSource;
import org.openmrs.dsl.PatientFactoryBuilder;
import org.openmrs.tools.importer.BaseEncounterImporter;


class ERImporter extends BaseEncounterImporter  {

    public ERImporter(){
	super();
    }

    public ERImporter(String filepath){
	this();
	initComponents(filepath);
    }

   void initComponents(String filepath){
	source = new EREncounterSource(filepath);
	assembler = new EREncounterAssembler( source:source);
    }
}