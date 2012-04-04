package org.angkorhospital.importer.source;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.tools.importer.source.LookupDatabase;

import groovy.sql.*;

class LookupDatabaseTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}


	@Test
	public void testGetConn() {
		LookupDatabase imp = new LookupDatabase();
		assertNotNull(imp.getConn());
	}

	@Ignore
	@Test
	public void testReadPatientRowStructure() {
		LookupDatabase imp = new LookupDatabase();
		String desc = imp.readRowStructure();
		assertNotNull(desc);
	}



	@Test
	public void readProvince(){
		LookupDatabase imp = new LookupDatabase();

		String tableName = "tblProvices"; //yes, sp
		String idName = "Pv_ProvinceID";
		String colName = "Pv_Province_k";
		String idValue = "01";
		//	 String queryDBParameterized(String table, String idcol, String idVal, String selectCol){
		def value = imp.queryDBParameterized(tableName,idName,idValue,colName);
		assertNotNull(value);
		println(value);
	}
	@Ignore
	@Test
	public void testReadPatients() {
		LookupDatabase imp = new LookupDatabase();
		def result = imp.readPatients();
		assertNotNull(result);
		assertNotNull(result.getGender());
		assertNotNull(result.getIdentifiers());
		assertEquals(2,result.getIdentifiers()?.size());

		//		assertNotNull(result.getBirthdate());
//		assertNotNull(result.getPersonDateCreated());

		println "Readpatients Result is " + result + ": " + result.getGender() +
			" and " + result.getBirthdate() +
			" and " + result.getPersonDateCreated() +
			" and ID number is " + result.getIdentifiers();

		imp.closeAll();
	}





}



