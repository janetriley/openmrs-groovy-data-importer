package org.openmrs.tools.importer.source;


import org.apache.log4j.Logger;
import org.openmrs.api.context.*;
import org.openmrs.*;
import org.openmrs.util.*;

import groovy.sql.Sql;
import com.mysql.jdbc.Driver;
import org.apache.log4j.*;

class LookupDatabase {

	def schema = "access_reloaded";
	def tableName = "normalized_patients";//tblPatients";
	def jdbcConn = null;
	java.sql.ResultSet rs = null;
	java.sql.Statement state = null;
	Logger log = Logger.getLogger("openmrs.tools.importer");

	public static groovy.util.Node patientXmlTemplate = null;

	Sql getConn(){
		Sql conn = new SourceDb().getConnection(schema);
		return conn;
	}

	java.sql.Connection getJDBCConn(){
		if( jdbcConn == null ){
			jdbcConn =  new SourceDb().getJDBCConnection(schema);

		}
		return jdbcConn;

	}

	void finalize(){
		closeAll();
	}


	void closeAll(){
		rs?.close();
		rs = null;
		state?.close();
		state = null;
		jdbcConn?.close();
		jdbcConn = null;
	}





	org.openmrs.Patient getNextPatient(){

		if( rs == null || ! rs.next())
			return;

		org.openmrs.Patient p = new org.openmrs.Patient(gender:rs.getString("gender"),
				birthdate:rs.getDate("dateofbirth"),
				personDateCreated:rs.getDate("creationdate")
				);

		org.openmrs.PatientIdentifier id = 	new org.openmrs.PatientIdentifier(
				rs.getString("patientcodeno"),
				new PatientIdentifierType(1),
				new Location(0));

		id.setPreferred(Boolean.FALSE);

		org.openmrs.PatientIdentifier newid = 	new org.openmrs.PatientIdentifier(
				id.getIdentifier().replaceAll("-",""),
				new PatientIdentifierType(2),
				new Location(0));
		newid.setPreferred(Boolean.TRUE);

		p.addIdentifier(id);
		p.addIdentifier(newid);

		return p;


	}

	private java.sql.ResultSet queryDB(String query){
		if(! query )
			return;
		closeAll();
		jdbcConn = this.getJDBCConn();
		assert jdbcConn;
		state = jdbcConn.createStatement();
		state?.executeQuery(query);
		rs = state?.getResultSet();
		return rs;

	}

	String queryDBParameterized(String table, String idCol, String idVal, String selectCol){
		if(idVal == null || idVal == "")
			return null;
		closeAll();
		jdbcConn = this.getJDBCConn();

		String query = "select ${selectCol} as value from ${table}  where ${idCol} = '" + idVal + "'";
		//println(query);
		state = jdbcConn.createStatement();
		state?.executeQuery(query);
		rs = state?.getResultSet();
		String value = rs.next() ? rs.getString("value") : null;
		rs.close();
		return value;

	}


	private java.sql.ResultSet queryDBPaginated(String query, int offset, int limit=10){
		if(! query )
			return;
		closeAll();
		jdbcConn = this.getJDBCConn();
		assert jdbcConn;
		state = jdbcConn.prepareStatement(query);
		state.setInt(2, offset);
		state.setInt(1, limit);

		state.executeQuery();
		rs = state?.getResultSet();
		return rs;

	}

	public org.openmrs.Patient readPatients(){
		def limit = 10;
		def myquery="select * from " + tableName + " order by patientcodeno LIMIT ?  OFFSET ?";
		queryDBPaginated(myquery,0,limit);
		return getNextPatient();
	}


	String readRowStructure(){
		Sql conn = getConn();
		assert conn;
		def desc = "";
		def myquery="select * from " + tableName + " limit 1;";
		conn?.eachRow(myquery){row->
			def meta = row.getMetaData();
			//http://download.oracle.com/javase/6/docs/api/java/sql/ResultSetMetaData.html
			meta.each(){ desc = "Row is " +  it.columnName + "     " + it.columnTypeName;}
		} ;

		conn?.close();
		return desc;
	}

	String readRowCount(){
		Sql conn = getConn();
		assert conn;
		def desc="";
		def myquery="select count(*) from " + tableName + ";";
		conn?.eachRow(myquery){row->
			desc =  "Rowcount is " + row[0];
		};

		conn?.close();
		return desc;
	}


	/*
	 void printTableColumns(){
	 def sql = Sql.newInstance("jdbc:mysql://localhost:3306/", "root",
	 "toot", "com.mysql.jdbc.Driver");
	 //      def sql = Sql.newInstance("jdbc:mysql://localhost:3306/ahc_access", "ahc",
	 //               "angkor", "com.mysql.jdbc.Driver");
	 assert sql;
	 def myquery="select LOWER(column_name) as column_name, upper(data_type) as data_type from information_schema.columns where table_schema='ahc_access' and table_name='tblPatients' "+
	 " order by column_name;";
	 //    sql.execute(myquery);
	 sql.eachRow(myquery){row->
	 println row.column_name + " : " + row.data_type;
	 };
	 }*/






}
