package com.netease.webbench.blogbench.sql;

import java.util.Map;

import com.netease.webbench.common.XMLParser;

public class SQLXMLConfigure implements SQLConfigure {
	public final static String BLOG_NAME_NAME = "BLOG_TABLE_NAME";
	public final static String CONTENT_TABLE_NAME = "CONTENT_TABLE_NAME";
	public final static String SQL_CONFIG_FILENAME = "config/sql.xml";

	protected String dbType;
	protected XMLParser parser;
	protected Map<String, Map<SQLStatementMeta, SQLStatement>> stmtMaps;
		
	@SuppressWarnings("unchecked")
	public SQLXMLConfigure(String dbType) throws Exception {
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

	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getListBlogsSql(java.lang.String, boolean)
	 */
	@Override
	public String getListBlogsSql(String tableName, boolean useMemcached) 
	throws Exception {
		String stmt = getStatement(dbType, "list-blogs",  useMemcached, false);
		return filledBlogTableName(stmt, tableName);
	}
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getMultiShowBlogSql(int, java.lang.String)
	 */
	@Override
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
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getPublishBlogSql(java.lang.String, boolean)
	 */
	@Override
	public String getPublishBlogSql(String tableName,boolean useTwoTable)
	throws Exception {
		String stmt = getStatement(dbType, "publish-blog",  false, useTwoTable);
		return filledBlogTableName(stmt, tableName);
	}
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getInsertContentSql(java.lang.String)
	 */
	@Override
	public String getInsertContentSql(String tableName) throws Exception {
		String stmt = getStatement(dbType, "insert-content");
		return filledContentTableName(stmt, tableName);
	}
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getShowWeightBlogSql(java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public String getShowWeightBlogSql(String blogTblName, String contentTblName, 
			boolean useTwoTable) throws Exception{
		String stmt = getStatement(dbType, "show-weight-blog", false, useTwoTable);
		stmt = filledBlogTableName(stmt, blogTblName);
		if (useTwoTable)
			stmt = filledContentTableName(stmt, contentTblName);
		return stmt;
	}
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getShowLightBlogSql(java.lang.String)
	 */
	@Override
	public String getShowLightBlogSql(String tableName) 
	throws Exception{
		String stmt = getStatement(dbType, "show-light-blog");
		return filledBlogTableName(stmt, tableName);
	}


	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getShowPreSiblingsSql(java.lang.String, boolean)
	 */
	@Override
	public String getShowPreSiblingsSql(String tableName,boolean useMemcached) 
	throws Exception{	
		String stmt = getStatement(dbType, "show-pre-siblings",  useMemcached, false);
		return filledBlogTableName(stmt, tableName);
	}
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getShowNextSiblingsSql(java.lang.String, boolean)
	 */
	@Override
	public String getShowNextSiblingsSql(String tableName, boolean useMemcached) 
	throws Exception{	
		String stmt = getStatement(dbType, "show-next-siblings",  useMemcached, false);
		return filledBlogTableName(stmt, tableName);
	}

	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getUpdateAccessSql(java.lang.String, boolean)
	 */
	@Override
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

	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getUpdateCommentSql(java.lang.String)
	 */
	@Override
	public String getUpdateCommentSql(String tableName) 
	throws Exception {	
		String stmt = getStatement(dbType, "update-comment");
		return filledBlogTableName(stmt, tableName);
	}

	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getUpdateBlogSql(java.lang.String, boolean)
	 */
	@Override
	public String getUpdateBlogSql(String blogTblName,boolean useTwoTable) throws Exception {
		String stmt = getStatement(dbType, "update-blog",  false, useTwoTable);
		stmt = filledBlogTableName(stmt, blogTblName);
		return stmt;
	}
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getUpdateContentSql(java.lang.String)
	 */
	@Override
	public String getUpdateContentSql(String contentTblName) throws Exception {
		String stmt = getStatement(dbType, "update-content",  false, true);
		stmt = filledContentTableName(stmt, contentTblName);
		return stmt;
	}
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getBlogContentSql(java.lang.String)
	 */
	@Override
	public String getBlogContentSql(String tableName) 
	throws Exception {
		String stmt = getStatement(dbType, "get-blog-content");
		return filledBlogTableName(stmt, tableName);
	}
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getDropTblSql(java.lang.String)
	 */
	@Override
	public String getDropTblSql(String tableName)  throws Exception {
		String stmt = getStatement(dbType, "drop-table");
		return filledBlogTableName(stmt, tableName);
	}
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getSetEncodingSql(java.lang.String)
	 */
	@Override
	public String getSetEncodingSql(String charSet) throws Exception {
		String stmt = getStatement(dbType, "set-encoding");
		return stmt.replaceAll("CHAR_SETTING", charSet);
	}
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getQueryMaxBlogIDSql(java.lang.String)
	 */
	@Override
	public String getQueryMaxBlogIDSql(String tableName) throws Exception {
		String s = "SELECT MAX(id) AS maxId FROM " + tableName;
		return s;
	}
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getQueryBlogCountSql(java.lang.String)
	 */
	@Override
	public String getQueryBlogCountSql(String tableName) throws Exception {
		String s = "SELECT COUNT(*) AS total FROM " + tableName;
		return s;
	}
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getQueryAllBlogSql(java.lang.String)
	 */
	@Override
	public String getQueryAllBlogSql(String tableName) throws Exception {
		String s = "SELECT ID, UserID, PublishTime FROM  " + tableName +
			" WHERE UserID  != -1 AND PublishTime != -1 ORDER BY ID ASC";
		return s;
	}
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getCreatePrimaryIndexSql(java.lang.String)
	 */
	@Override
	public String getCreatePrimaryIndexSql(String tableName) throws Exception {
		return "ALTER TABLE " + tableName +" ADD PRIMARY KEY(ID)";
	}
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getCreateSecondaryIndexSql(java.lang.String)
	 */
	@Override
	public String getCreateSecondaryIndexSql(String tableName) throws Exception {
		return "CREATE INDEX IDX_BLOG_UID_PUBTIME ON " + tableName + "(UserID, PublishTime, AllowView)";
	}
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getBatchQueryBlogSql(java.lang.String)
	 */
	@Override
	public String getBatchQueryBlogSql(String tableName) throws Exception {
		return "SELECT ID, UserID, PublishTime FROM " + tableName 
		+	" WHERE ID BETWEEN ? AND ? ORDER BY ID ASC";
	}
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getCreateBlogTblSql(java.lang.String, boolean, boolean)
	 */
	@Override
	public String getCreateBlogTblSql(String tableName, boolean createPrimaryIndex, 
			boolean useTwoTable) throws Exception {
		String stmt = getStatement(dbType, "create-blog-table", false, useTwoTable);
		stmt = filledBlogTableName(stmt, tableName);		
		if (!createPrimaryIndex) {
			stmt = stmt.replaceAll("PRIMARY KEY", "");
		}		
		return stmt;
	}
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getCreateContentTblSql(java.lang.String, boolean)
	 */
	@Override
	public String getCreateContentTblSql(String tableName, boolean createPrimaryIndex) throws Exception {
		String stmt = getStatement(dbType, "create-content-table", false, true);
		stmt = filledContentTableName(stmt, tableName);
		if (!createPrimaryIndex) {
			stmt = stmt.replaceAll("PRIMARY KEY", "");
		}
		return stmt;
	}
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getMultiInsertBlogSql(java.lang.String, long, boolean)
	 */
	@Override
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
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getMultiInsertContentSql(java.lang.String, long)
	 */
	@Override
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
