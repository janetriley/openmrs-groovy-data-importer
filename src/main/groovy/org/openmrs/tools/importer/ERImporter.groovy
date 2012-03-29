package org.openmrs.tools.importer;



import org.openmrs.tools.importer.builder.PatientFactoryBuilder;
import org.openmrs.tools.importer.assembler.EREncounterAssembler;
import org.openmrs.tools.importer.source.EREncounterSource;


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