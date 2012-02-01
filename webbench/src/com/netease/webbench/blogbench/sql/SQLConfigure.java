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
package com.netease.webbench.blogbench.sql;

import java.util.Map;

import com.netease.webbench.common.XMLParser;

/**
 * SQL configures
 * @author LI WEIZHAO
 *
 */
public class SQLConfigure  {
	public final static String BLOG_NAME_NAME = "BLOG_TABLE_NAME";
	public final static String CONTENT_TABLE_NAME = "CONTENT_TABLE_NAME";
	public final static String SQL_CONFIG_FILENAME = "config/sql.xml";
	
	private static SQLConfigure instance;
	protected String dbType;
	protected XMLParser parser;
	protected Map<String, Map<SQLStatementMeta, SQLStatement>> stmtMaps;
	
	public static SQLConfigure getInstance(String dbType) throws Exception  {
		if (null == instance)
			instance = new SQLConfigure(dbType);
		return instance;
	}
		
	@SuppressWarnings("unchecked")
	private SQLConfigure(String dbType) throws Exception {
		this.dbType = dbType;
		
		this.parser = new XMLParser();
		SQLParseHandler sqlParseHandler = new SQLParseHandler(SQL_CONFIG_FILENAME);	
		parser.setParseHandler(sqlParseHandler);
		stmtMaps = (Map<String, Map<SQLStatementMeta, SQLStatement>>)parser.parse();
	}
	
	protected String filledBlogTableName(String s, String tableName) {
		return s.replaceAll(BLOG_NAME_NAME, tableName);
	}
	
	protected String filledContentTableName(String s, String tableName) {
		return s.replaceAll(CONTENT_TABLE_NAME, tableName);
	}
	
	/**
	 * get sql statement
	 * @param dbType
	 * @param stmtName
	 * @param useMemcached
	 * @param useTwoTable
	 * @return
	 */
	protected String getStatement(String dbType, String stmtName, 
			boolean useMemcached, boolean useTwoTable) throws Exception {
		if (stmtMaps != null) {
			Map<SQLStatementMeta, SQLStatement> map = stmtMaps.get(dbType);
			if (map != null) {
				SQLStatement stmt = map.get(new SQLStatementMeta(stmtName, useMemcached, useTwoTable));
				if (stmt != null)
					return stmt.getSqlStmt();
			}
		}
		throw new Exception("No SQL statement of operation \"" + stmtName + "\" found in SQL configure file." );
	}
	
	/**
	 * get sql statement
	 * @param dbType
	 * @param stmtName
	 * @return
	 * @throws Exception
	 */
	protected String getStatement(String dbType, String stmtName) throws Exception  {
		return getStatement(dbType, stmtName, false, false);
	}

	public String getListBlogsSql(String tableName, boolean useMemcached) 
	throws Exception {
		String stmt = getStatement(dbType, "list-blogs",  useMemcached, false);
		return filledBlogTableName(stmt, tableName);
	}
	
	public String getMultiShowBlogSql(int multiCount, String tableName) throws Exception {
		String stmt = getStatement(dbType, "multi-show-blogs",  false, false);
		
		StringBuilder builder = new StringBuilder();
		builder.append(stmt);		
		for (int i = 0; i < multiCount; i++) {
			if (i != 0) {
				builder.append(" OR ");
			}
			builder.append(" (ID = ? AND UserID = ?)");
		}
		stmt = builder.toString();
		return filledBlogTableName(stmt, tableName);
	}
	
	public String getPublishBlogSql(String tableName,boolean useTwoTable)
	throws Exception {
		String stmt = getStatement(dbType, "publish-blog",  false, useTwoTable);
		return filledBlogTableName(stmt, tableName);
	}
	
	public String getInsertContentSql(String tableName) throws Exception {
		String stmt = getStatement(dbType, "insert-content");
		return filledContentTableName(stmt, tableName);
	}
	
	public String getShowWeightBlogSql(String blogTblName, String contentTblName, 
			boolean useTwoTable) throws Exception{
		String stmt = getStatement(dbType, "show-weight-blog", false, useTwoTable);
		stmt = filledBlogTableName(stmt, blogTblName);
		if (useTwoTable)
			stmt = filledContentTableName(stmt, contentTblName);
		return stmt;
	}
	
	public String getShowLightBlogSql(String tableName) 
	throws Exception{
		String stmt = getStatement(dbType, "show-light-blog");
		return filledBlogTableName(stmt, tableName);
	}


	public String getShowPreSiblingsSql(String tableName,boolean useMemcached) 
	throws Exception{	
		String stmt = getStatement(dbType, "show-pre-siblings",  useMemcached, false);
		return filledBlogTableName(stmt, tableName);
	}
	
	public String getShowNextSiblingsSql(String tableName, boolean useMemcached) 
	throws Exception{	
		String stmt = getStatement(dbType, "show-next-siblings",  useMemcached, false);
		return filledBlogTableName(stmt, tableName);
	}

	public String getUpdateAccessSql(String tableName,boolean useMemcached) 
	throws Exception{
		if (useMemcached) {
			String stmt = getStatement(dbType, "update-access");
			return filledBlogTableName(stmt, tableName);
		} else {
			String stmt = getStatement(dbType, "increase-access");
			return filledBlogTableName(stmt, tableName);
		}		
	}

	public String getUpdateCommentSql(String tableName) 
	throws Exception {	
		String stmt = getStatement(dbType, "update-comment");
		return filledBlogTableName(stmt, tableName);
	}

	public String getUpdateBlogSql(String blogTblName,boolean useTwoTable) throws Exception {
		String stmt = getStatement(dbType, "update-blog",  false, useTwoTable);
		stmt = filledBlogTableName(stmt, blogTblName);
		return stmt;
	}
	
	public String getUpdateContentSql(String contentTblName) throws Exception {
		String stmt = getStatement(dbType, "update-content",  false, true);
		stmt = filledContentTableName(stmt, contentTblName);
		return stmt;
	}
	
	public String getBlogContentSql(String tableName) 
	throws Exception {
		String stmt = getStatement(dbType, "get-blog-content");
		return filledBlogTableName(stmt, tableName);
	}
	
	public String getDropTblSql(String tableName)  throws Exception {
		String stmt = getStatement(dbType, "drop-table");
		return filledBlogTableName(stmt, tableName);
	}
	
	public String getSetEncodingSql(String charSet) throws Exception {
		String stmt = getStatement(dbType, "set-encoding");
		return stmt.replaceAll("CHAR_SETTING", charSet);
	}
	
	public String getQueryMaxBlogIDSql(String tableName) throws Exception {
		String s = "SELECT MAX(id) AS maxId FROM " + tableName;
		return s;
	}
	
	public String getQueryBlogCountSql(String tableName) throws Exception {
		String s = "SELECT COUNT(*) AS total FROM " + tableName;
		return s;
	}
	
	public String getQueryAllBlogSql(String tableName) throws Exception {
		String s = "SELECT ID, UserID, PublishTime FROM  " + tableName +
			" WHERE UserID  != -1 AND PublishTime != -1 ORDER BY ID ASC";
		return s;
	}
	
	public String getCreatePrimaryIndexSql(String tableName) throws Exception {
		return "ALTER TABLE " + tableName +" ADD PRIMARY KEY(ID)";
	}
	
	public String getCreateSecondaryIndexSql(String tableName) throws Exception {
		return "CREATE INDEX IDX_BLOG_UID_PUBTIME ON " + tableName + "(UserID, PublishTime, AllowView)";
	}
	
	public String getBatchQueryBlogSql(String tableName) throws Exception {
		return "SELECT ID, UserID, PublishTime FROM " + tableName 
		+	" WHERE ID BETWEEN ? AND ? ORDER BY ID ASC";
	}
	
	public String getCreateBlogTblSql(String tableName, boolean createPrimaryIndex, 
			boolean useTwoTable) throws Exception {
		String stmt = getStatement(dbType, "create-blog-table", false, useTwoTable);
		stmt = filledBlogTableName(stmt, tableName);		
		if (!createPrimaryIndex) {
			stmt = stmt.replaceAll("PRIMARY KEY", "");
		}		
		return stmt;
	}
	
	public String getCreateContentTblSql(String tableName, boolean createPrimaryIndex) throws Exception {
		String stmt = getStatement(dbType, "create-content-table", false, true);
		stmt = filledContentTableName(stmt, tableName);
		if (!createPrimaryIndex) {
			stmt = stmt.replaceAll("PRIMARY KEY", "");
		}
		return stmt;
	}
	
	public String getMultiInsertBlogSql(String tableName, long insertRows, boolean useTwoTable) throws Exception {
		StringBuilder b = new StringBuilder();
		String ps = getPublishBlogSql(tableName, useTwoTable);
		ps = filledBlogTableName(ps, tableName);
		b.append(ps);
		for (long j = 0; j < insertRows - 1; j++) {
			if (useTwoTable)
				b.append(",(?, ?, ?, ?, ?, ?, 0, 0)");
			else
				b.append(",(?, ?, ?, ?, ?, ?, ?, 0, 0)");
		}
		return b.toString();
	}
	
	public String getMultiInsertContentSql(String tableName, long insertRows) throws Exception {
		StringBuilder b = new StringBuilder();
		String ps = getInsertContentSql(tableName);
		ps = filledContentTableName(ps, tableName);
		b.append(ps);
		for (long j = 0; j < insertRows - 1; j++) {
				b.append(",(?, ?, ?)");
		}
		return b.toString();
	}
}
