package org.angkorhospital.importer;



import org.angkorhospital.importer.assembler.EREncounterAssembler;
import org.angkorhospital.importer.source.EREncounterSource;
import org.openmrs.dsl.OpenMRSFactoryBuilder;
import org.openmrs.groovyimporter.BaseEncounterImporter;


class ERImporter extends BaseEncounterImporter  {

    public ERImporter(){
	super();
    }

    public ERImporter(String filepath){
	this();
	initComponents(filepath);
    }

   public void initComponents( filepath ){
	source = new EREncounterSource(filepath);
	assembler = new EREncounterAssembler( source:source);
    }
   public void importRecords(){ super.importRecords();}
}