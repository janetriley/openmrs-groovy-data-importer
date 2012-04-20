/**
 *
 */
package org.angkorhospital.importer.source;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;

import org.openmrs.groovyimporter.source.SourceDb;

import java.sql.*;
/**
 * @author me
 *
 */
class SourceDbTest {

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    //this db is no longer used
    @Ignore
    @Test
    public void testConnection() {
	SourceDb db = new SourceDb();
	//this DB is no longer used
	//Connection conn = db.getJDBCConnection("access_reloaded");
	assertNotNull(conn);

	if (conn != null) {
	    try {
		conn.close ();
		System.out.println ("Database connection terminated");
	    }
	    catch (Exception e) {
		/* ignore close errors */
	    }
	}
    }
}
