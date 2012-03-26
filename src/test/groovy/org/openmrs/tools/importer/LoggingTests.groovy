package org.openmrs.tools.importer;

import static org.junit.Assert.*;
import org.apache.log4j.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

class LoggingTests {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testLogs(){

		Logger log = Logger.getLogger("openmrs.tools.importer");
		log.debug("This is a warning");
		Logger mistakes = Logger.getLogger("reimport");
		assertNotNull(mistakes);
		mistakes.info("# boo hoo hoo!");
		mistakes.error("This is a bad line");
	}
}
