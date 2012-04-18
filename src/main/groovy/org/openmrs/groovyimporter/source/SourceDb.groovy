package org.openmrs.groovyimporter.source;

import groovy.sql.Sql;
import com.mysql.jdbc.Driver;
import java.sql.*;
import org.apache.log4j.Logger;

class SourceDb {
	def url  =  "jdbc:mysql://localhost:3306/";
	//	def login= "ahc";
	//	def pw = "angkor";
	def login= "importer";
	def pw = "Imp0rter";

	def driver = "com.mysql.jdbc.Driver"
	Logger log = Logger.getLogger("openmrs.tools.importer");


	public Sql getConnection(String dbname=""){
		def sql = Sql.newInstance( url + dbname + "?allowMultiQueries=true", login, pw, driver);
		return sql;
	}

	Connection getJDBCConnection(String dbname=""){


		Connection conn = null;

		try
		{
			Class.forName ("com.mysql.jdbc.Driver").newInstance ();
			conn = DriverManager.getConnection (url + dbname, login, pw);
			log.debug.println ("Database connection established");
		}
		catch (Exception e)
		{
			log.error("Cannot connect to database server");
		}
		finally
		{
			return conn;
		}

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
