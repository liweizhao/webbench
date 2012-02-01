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


/**
 * Common JDBC database connection options
 * @author LI WEIZHAO
 */
public class DbOptions {
	/* type of database */
	private String dbType;
	/* user of database */
	private String user;
	/* password of database */
	private String password;
	/* host of database */
	private String host;
	/* port of database */
	private int port;
	/* name of database schema */
	private String database;
	/* driver name of JDBC */
	private String driverName;
	/* URL of JDBC */
	private String jdbcUrl;
	
	public DbOptions() {
		this.dbType = "mysql";
		this.host = "127.0.0.1";
		this.port = 3306;
		this.user = "root";
		this.password = "";
		this.database = "test";
		this.driverName = "";
		this.jdbcUrl = null;
	}
	
	public DbOptions(DbOptions dbOpt) {
		this.dbType = dbOpt.getDbType();
		this.host = dbOpt.getHost();
		this.port = dbOpt.getPort();
		this.user = dbOpt.getUser();
		this.password = dbOpt.getPassword();
		this.database = dbOpt.getDatabase();
		this.driverName = dbOpt.getDriverName();
		this.jdbcUrl = dbOpt.getJdbcUrl();
	}
	
	public DbOptions(String driverName, String jdbcUrl, String user, String password, String host, int port, String database) {
		this.user = user;
		this.password = password;
		this.host = host;
		this.port = port;
		this.database = database;
		
		if (driverName.equals("")) {
			this.driverName = "com.mysql.jdbc.Driver";
		} else {
			this.driverName = driverName;
		}
		
		this.jdbcUrl = jdbcUrl;
	}
	
	public String getDriverName() {
		return driverName;
	}
	public void setDriverName(String driverName) {
		this.driverName = driverName;
	}
	public String getDatabase() {
		return database;
	}
	public void setDatabase(String database) {
		this.database = database;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getJdbcUrl() {
		return jdbcUrl;
	}

	public String getDbType() {
		return dbType;
	}

	public void setDbType(String dbType) {
		this.dbType = dbType;
	}

	public void setJdbcUrl(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
	}
}
