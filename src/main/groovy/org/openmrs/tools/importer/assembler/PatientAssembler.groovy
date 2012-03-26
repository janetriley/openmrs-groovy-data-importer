package org.openmrs.tools.importer.assembler;

import org.openmrs.Patient;
import org.openmrs.Relationship;
import org.openmrs.tools.importer.source.ImportSource;

public interface PatientAssembler {
	void setSource(ImportSource source);
	org.openmrs.Patient buildPatient();
	org.openmrs.Relationship buildRelationship( int patientId);
}
