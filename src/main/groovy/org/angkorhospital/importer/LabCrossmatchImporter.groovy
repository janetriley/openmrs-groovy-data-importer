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


       void initComponents(String filepath){
	source = new LabCrossmatchEncounterSource(filepath);
	assembler = new LabCrossmatchAssembler( source:source);
    }
}