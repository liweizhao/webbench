/**
  * Copyright (c) <2011>, <NetEase Corporation>
  * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *    3. Neither the name of the <ORGANIZATION> nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.netease.webbench.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import com.netease.webbench.blogbench.misc.Portable;
import com.netease.webbench.blogbench.sql.SQLConfigure;
import com.netease.webbench.blogbench.sql.SQLConfigureFactory;

/**
 * database connection
 * @author LI WEIZHAO
 */
public class DbSession {

	private Connection con;
	private DbOptions dbOpt;
	
	/**
	 * constructor
	 * @param driverName
	 * @param url
	 * @param user
	 * @param psw
	 */
	public DbSession(DbOptions dbOpt) throws Exception {
		this.dbOpt = dbOpt;
		createConnection();
	}
	
	public DbSession(DbSession dbSession) throws Exception {
		this.dbOpt = dbSession.getDbOpt();
		createConnection();		
	}

	/**
	 * create database connection
	 */
	private void createConnection() throws Exception {
		Class.forName(dbOpt.getDriverName());
				
		con = DriverManager.getConnection(dbOpt.getJdbcUrl(), 
				dbOpt.getUser(), dbOpt.getPassword());
		if (con == null) 
			throw new Exception("Can't create database connection!");
		
		/* set autocommit to true*/
		con.setAutoCommit(true);
	}
	
	public boolean getAutoCommit() throws SQLException {
		return con.getAutoCommit();
	}
	
	public void setAutoCommit(boolean autoCommit) throws Exception {
		con.setAutoCommit(autoCommit);
	}

	/**
	 * create prepared SQL statement
	 * @param sql  SQL statement
	 * @return 
	 */
	public PreparedStatement createPreparedStatement(String sql) throws SQLException {
		PreparedStatement pstm = con.prepareStatement(sql);
		return pstm;
	}

	/**
	 * execute query in prepared statement
	 * @param pstm
	 */
	public ResultSet query(PreparedStatement pstm) throws SQLException {
		return  pstm.executeQuery();
	}
	
	/**
	 * execute update in prepared statement
	 */
	public int update(PreparedStatement pstm) throws SQLException {
		return pstm.executeUpdate(); 
	}
	
	/**
	 * execute query
	 * 
	 * @param SQL statement
	 * @return result set
	 */
	public ResultSet query(String sql) throws SQLException {
		Statement st = con.createStatement();
		return st.executeQuery(sql);
	}

	/**
	 * execute update
	 * 
	 * @param SQL statement
	 * @return rows affected by this statement
	 */
	public int update(String sql) throws SQLException {
		Statement st = con.createStatement();
		return st.executeUpdate(sql);
	}
	
	/**
	 * commit transaction
	 * @throws SQLException
	 */
	public void commit() throws SQLException {
		if (con.getAutoCommit()) {
			con.commit();
		}
	}
	
	/**
	 * simulate store result set
	 * @param rs result set
	 * @throws SQLException
	 */
	public void simulateTouchResultSetData(ResultSet rs) throws SQLException {
		if (rs == null) {
			System.out.println("Fail to store result set, result set is null");
			return;
		}
		ResultSetMetaData rsmd = rs.getMetaData();
		int colCount = rsmd.getColumnCount();
		
		while (rs.next()) {
			for(int i = 1; i <= colCount; i++) {
				rs.getString(i);
			}
		}
	}
	
	/**
	 * close database connection
	 * @throws SQLException
	 */
	public void close() throws SQLException {
		if (con != null) {
     		con.close(); 
		}
	}

	public DbOptions getDbOpt() {
		return dbOpt;
	}

	public void setDbOpt(DbOptions dbOpt) {
		this.dbOpt = dbOpt;
	}
	
	public boolean isClosed() throws SQLException {
		return con.isClosed();
	}
	
	/**
	 * check if character set of database server is UTF8
	 *
	 * @throws Exception
	 */
	public void checkServerCharaSet() throws Exception {
	/*	if (dbOpt.getDbType().equalsIgnoreCase("mysql")) {
			
			String queryCharSetSql = "show variables like \"character_set_server\"";
			ResultSet rs = query(queryCharSetSql);
			if (rs != null && rs.next()) {
				String serverCharSet = rs.getString("Value");
				if (!serverCharSet.equalsIgnoreCase(Portable.getCharacterSet())) {
					throw new Exception("Database type is " + dbOpt.getDbType() 
							+ ", its character set should be " + Portable.getCharacterSet()
							+ ", but we find it's " + serverCharSet + "!");
				} 
			} else {
				throw new Exception("Query \"" + queryCharSetSql + "\" returns null");
			}
			rs.close();			
		} else if (dbOpt.getDbType().equalsIgnoreCase("oracle")) {
			String queryCharSetSql = "select * from v$nls_parameters where PARAMETER='NLS_CHARACTERSET'";
			ResultSet rs = query(queryCharSetSql);
			if (rs != null && rs.next()) {
				String serverCharSet = rs.getString("value");
				if (serverCharSet == null)
					throw new Exception("Failed to excute query \'" + serverCharSet + "\'");
				if (!serverCharSet.contains(Portable.getCharacterSet())) {
					throw new Exception("Database type is " + dbOpt.getDbType() 
							+ ", its character set should be " + Portable.getCharacterSet()
							+ ", but we find it's " + serverCharSet + "!");
				}
			} else {
				throw new Exception("Query \"" + queryCharSetSql + "\" returns null");
			}
		} else if (dbOpt.getDbType().equalsIgnoreCase("postgresql")) {
			
			String queryCharSetSql = "select encoding from pg_database where datname like \'" + dbOpt.getDbType() + "\'";
			ResultSet rs = query(queryCharSetSql);
			if (rs != null && rs.next()) {
				int serverCharSet = rs.getInt("encoding");
				if (serverCharSet != 6) {
					throw new Exception("Database type is " + dbOpt.getDbType() 
							+ ", its character set should be " + Portable.getCharacterSet());
				}
			}
			rs.close();
		} else {
			throw new IllegalArgumentException("Unsurported or invalid dabase type specified!");
		}*/
	}
	
	/**
	 * set character set of this client connection
	 * @throws Exception
	 */
	public void setClientCharaSet() throws Exception {
		if (dbOpt.getDbType().equalsIgnoreCase("mysql") ||
				dbOpt.getDbType().equalsIgnoreCase("postgresql")) {
			SQLConfigure sqlConfig = SQLConfigureFactory.getSQLConfigure(dbOpt.getDbType());
			update(sqlConfig.getSetEncodingSql(Portable.getCharacterSet()));
		}
	}
	
	public void setParallelDML(boolean enable) throws Exception {
		if (!"ORACLE".equalsIgnoreCase(dbOpt.getDbType())) 
				throw new Exception("Parallel DML only supports Oracle database. ");
		if (enable) {
			update("alter session enable parallel dml");
		} else {
			update("alter session disable parallel dml");
		}
	}
}
