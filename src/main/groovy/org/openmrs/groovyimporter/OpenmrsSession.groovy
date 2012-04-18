package org.openmrs.groovyimporter;

import org.openmrs.api.context.*;
import org.openmrs.*;
import org.openmrs.util.*;


class OpenmrsSession {

	Properties props = null;

	void finalize(){
		closeOpenMrsSession();
	}
	
	void initProps(){
		if(props == null ){
			File propsFile = new File(OpenmrsUtil.getApplicationDataDirectory(), "openmrs-runtime.properties");			
			this.props = new Properties();
			OpenmrsUtil.loadProperties(props, propsFile);
		}
	}	
	void startOpenmrs(){
		initProps();
		Context.startup("jdbc:mysql://localhost:3306/openmrs?autoReconnect=true", 
			"openmrs", "openmrs", props);
		openOpenmrsSession();
	
	}
	
	/*
	 * Will need to open and close the sesion during imports - break out to a separate method
	 */
	void openOpenmrsSession(){
		try {
		  Context.openSession();
		  Context.authenticate("admin", "Passw0rd");
		//  List<Patient> patients  Context.getPatientService().getPatients("John");
		//  for (Patient patient : patients) {
		//	System.out.println("Found patient with name " + patient.getPersonName() + " and uuid: " + patient.getUuid());
		 // }
			println("finished opening and authenticating");
		  }
		finally {
			println("Done starting session");
		}
	}
	
	void closeOpenmrsSession(){
		Context.closeSession();
		println("Closing the session");
		
	}	
}
