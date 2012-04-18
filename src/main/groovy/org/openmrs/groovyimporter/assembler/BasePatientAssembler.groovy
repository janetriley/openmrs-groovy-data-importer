package org.openmrs.groovyimporter.assembler;

import org.openmrs.Patient;
import org.openmrs.Relationship;
import org.openmrs.tools.importer.source.ImportSource;

public interface BasePatientAssembler {
	void setSource(ImportSource source);
	org.openmrs.Patient buildPatient();
	org.openmrs.Relationship buildRelationship( int patientId);
}
