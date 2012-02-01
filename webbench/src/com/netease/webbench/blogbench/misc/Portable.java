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
package com.netease.webbench.blogbench.misc;

/**
 *  about portability of different type of databases
 * 
 * currently only support MySQL, PostgreSQL and Oracle
 * 
 * @author LI WEIZHAO
 */
public class Portable {
	/* MySQL default character set  */
	public static final String DFL_CHAR_SET_MYSQL = "UTF8";
	
	/* Oracle default character set */
	public static final String DFL_CHAR_SET_ORACLE = "UTF8";
	
	/* PostgreSQL default character set */
	public static final String DFL_CHAR_SET_POSTGRESQL= "UTF8";
	
	/* character set */
	public static String characterSet = "UTF8";

	public static String CONTENT_TABLE_SUFFIX = "Content";

	private Portable() {}
	
	/**
	 * set character set of blogbench test
	 * @param charSet
	 */
	public static void setCharacterSet(String charSet) {
		characterSet = charSet;
	}
	
	/**
	 * get default character set of blogbench test
	 * @return
	 */
	public static String getCharacterSet() {
		return characterSet;
	}
	
	/**
	 * get default JDBC driver name
	 * @param dbType
	 * @return
	 */
	public static String getDflJdbcDrvName(String dbType) {
		if (dbType.equalsIgnoreCase("mysql")) {
			return "com.mysql.jdbc.Driver";
		} else if (dbType.equalsIgnoreCase("oracle")) {
			return "oracle.jdbc.driver.OracleDriver";
		} else if (dbType.equalsIgnoreCase("postgresql")) {
			return "org.postgresql.Driver";
		} else {
			throw new IllegalArgumentException("Unsurported or invalid dabase type specified!");
		}		
	}
	
	/**
	 * get default JDBC URL
	 * @param dbType
	 * @param host
	 * @param port
	 * @param database
	 * @return
	 */
	public static String getDflJdbcUrl(String dbType, String host, int port, String database) {
		if (dbType.equalsIgnoreCase("mysql")) {
			return String.format("jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=%s&autoReconnect=true", host, port, 
					database, DFL_CHAR_SET_MYSQL);
		} else if (dbType.equalsIgnoreCase("oracle")) {
			//return String.format("jdbc:oracle:thin:@(DESCRIPTION =(ADDRESS_LIST =(ADDRESS = (PROTOCOL = TCP)" +
					//"(HOST =%s)(PORT = %d)) )(CONNECT_DATA =(SID =%s)))&autoReconnect=true", host, port, database);
			return String.format("jdbc:oracle:thin:@%s:%d:%s", host, port, database);
		} else if (dbType.equalsIgnoreCase("postgresql")) {
			return String.format("jdbc:postgresql://%s:%d/%s?useUnicode=true&characterEncoding=%s&autoReconnect=true", host, port, 
					database, DFL_CHAR_SET_POSTGRESQL);
		} else {
			throw new IllegalArgumentException("Unsurported or invalid dabase type specified!");
		}	
	}
	
	/**
	 * if database support multi-insert
	 * @param dbType
	 * @return
	 * @throws Exception
	 */
	public static boolean surportMultiInsert(String dbType) throws Exception {
		if (dbType.equalsIgnoreCase("mysql")) {
			return true;
		} else if (dbType.equalsIgnoreCase("oracle") || dbType.equalsIgnoreCase("postgresql")) {
			return false;
		} else {
			throw new IllegalArgumentException("Unsurported or invalid dabase type specified!");
		}	
	}
	
	/**
	 * if database is transactional
	 * @param dbType
	 * @param engine
	 * @return
	 * @throws Exception
	 */
	public static boolean isTransactional(String dbType, String engine) throws Exception {
		if (dbType.equalsIgnoreCase("mysql")) {
			if ("innodb".equalsIgnoreCase(engine)) {
				return true;
			} else {
				return false;
			}
		} else if (dbType.equalsIgnoreCase("oracle") || dbType.equalsIgnoreCase("postgresql")) {
			return true;
		} else {
			throw new IllegalArgumentException("Unsurported or invalid dabase type specified!");
		}	
	}

	/**
	 * get name of blog content table
	 * just add suffix to blog table name
	 * @param tableName blog table name
	 * @return
	 */
	public static String getBlogContentTableName(String tableName) {
		return tableName + CONTENT_TABLE_SUFFIX;
	}
}
