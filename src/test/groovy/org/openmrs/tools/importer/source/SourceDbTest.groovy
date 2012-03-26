/**
 *
 */
package org.openmrs.tools.importer.source;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.tools.importer.source.SourceDb;

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

	/**
	 * Test method for {@link kata.SourceDb#getConnection()}.
	 */
	@Test
	public void testGetConnectionString() {
		SourceDb db = new SourceDb();
		Connection conn = db.getJDBCConnection("access_reloaded");
		assertNotNull(conn);

		if (conn != null)
		{
			try
			{
				conn.close ();
				System.out.println ("Database connection terminated");
			}
			catch (Exception e) { /* ignore close errors */ }
		}


		}


}
