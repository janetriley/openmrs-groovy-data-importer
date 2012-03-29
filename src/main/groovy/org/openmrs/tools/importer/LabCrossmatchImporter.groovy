package org.openmrs.tools.importer;

import org.openmrs.tools.importer.assembler.LabCrossmatchAssembler;
import org.openmrs.tools.importer.source.LabCrossmatchEncounterSource;


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