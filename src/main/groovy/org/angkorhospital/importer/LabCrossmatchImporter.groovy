package org.angkorhospital.importer;

import org.angkorhospital.importer.assembler.LabCrossmatchAssembler;
import org.angkorhospital.importer.source.LabCrossmatchEncounterSource;
import org.openmrs.groovyimporter.BaseEncounterImporter;


class LabCrossmatchImporter extends BaseEncounterImporter  {

    public LabCrossmatchImporter(){
	super();
    }
    public LabCrossmatchImporter(filepath){
	super(filepath);
    }


   public void initComponents( filepath){
	source = new LabCrossmatchEncounterSource(filepath);
	assembler = new LabCrossmatchAssembler( source:source);
    }

    public void importRecords(){
	super.importRecords();
    }
}