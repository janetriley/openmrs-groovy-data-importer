package org.openmrs.groovyimporter;

public interface Importable {

    //Pass in any resources, filepaths, etc. that you need to configure the importer
    //Resources can be a map as well as a single object
    public void initComponents( resources );

    //Run the import
    public void importRecords();

}