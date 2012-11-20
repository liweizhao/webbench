package com.netease.webbench.blogbench.rdbms.sql;

import java.util.Map;

import com.netease.webbench.common.XMLParser;

public class SQLXMLConfigure implements SQLConfigure {
	public final static String BLOG_NAME_NAME = "BLOG_TABLE_NAME";
	public final static String CONTENT_TABLE_NAME = "CONTENT_TABLE_NAME";
	public final static String SQL_CONFIG_FILENAME = "config/sql.xml";

	protected String dbType;
	protected XMLParser parser;
	protected Map<SQLStatementMeta, SQLStatement> stmtMap;
		
	@SuppressWarnings("unchecked")
	public SQLXMLConfigure(String dbType) throws RuntimeException {
		try {
			this.dbType = dbType;
			this.parser = new XMLParser();
			SQLParseHandler sqlParseHandler = new SQLParseHandler(SQL_CONFIG_FILENAME);	
			parser.setParseHandler(sqlParseHandler);
			Map<String, Map<SQLStatementMeta, SQLStatement>> m =
					(Map<String, Map<SQLStatementMeta, SQLStatement>>)parser.parse();
			this.stmtMap = m.get(dbType);
			if (this.stmtMap == null)
				throw new RuntimeException("No configuration of SQL found!");
		} catch (Exception e) {
			throw new RuntimeException(e.toString());
		}
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
			boolean useTwoTable) throws IllegalArgumentException {
		SQLStatement stmt = stmtMap.get(new SQLStatementMeta(stmtName, useTwoTable));
		if (stmt != null)
			return stmt.getSqlStmt();
		throw new IllegalArgumentException("No SQL statement of operation \"" + stmtName + "\" found in SQL configure file." );
	}
	
	/**
	 * get sql statement
	 * @param dbType
	 * @param stmtName
	 * @return
	 * @throws IllegalArgumentException
	 */
	protected String getStatement(String dbType, String stmtName) throws IllegalArgumentException  {
		return getStatement(dbType, stmtName, false);
	}

	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getListBlogsSql(java.lang.String, boolean)
	 */
	@Override
	public String getListBlogsSql() throws IllegalArgumentException {
		return getStatement(dbType, "list-blogs",  false);
	}
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getMultiShowBlogSql(int, java.lang.String)
	 */
	@Override
	public String getMultiShowBlogSql(int multiCount) throws IllegalArgumentException {
		String stmt = getStatement(dbType, "multi-show-blogs",  false);
		
		StringBuilder builder = new StringBuilder();
		builder.append(stmt);	
		for (int i = 0; i < multiCount; i++) {
			if (i != 0) {
				builder.append(" OR ");
			}
			builder.append(" (ID = ? AND UserID = ?)");
		}
		return builder.toString();
	}
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getPublishBlogSql(java.lang.String, boolean)
	 */
	@Override
	public String getPublishBlogSql(boolean useTwoTable)
	throws IllegalArgumentException {
		return getStatement(dbType, "publish-blog",  useTwoTable);
	}
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getInsertContentSql(java.lang.String)
	 */
	@Override
	public String getInsertContentSql() throws IllegalArgumentException {
		return getStatement(dbType, "insert-content");
	}
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getShowWeightBlogSql(java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public String getShowWeightBlogSql(boolean useTwoTable) throws IllegalArgumentException{
		return getStatement(dbType, "show-weight-blog", useTwoTable);
	}
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getShowLightBlogSql(java.lang.String)
	 */
	@Override
	public String getShowLightBlogSql() throws IllegalArgumentException{
		return getStatement(dbType, "show-light-blog");
	}


	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getShowPreSiblingsSql(java.lang.String, boolean)
	 */
	@Override
	public String getShowPreSiblingsSql() throws IllegalArgumentException{	
		return getStatement(dbType, "show-pre-siblings",  false);
	}
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getShowNextSiblingsSql(java.lang.String)
	 */
	@Override
	public String getShowNextSiblingsSql() 
	throws IllegalArgumentException{	
		return getStatement(dbType, "show-next-siblings",  false);
	}

	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getUpdateAccessSql(java.lang.String)
	 */
	@Override
	public String getUpdateAccessSql() throws IllegalArgumentException{
		return getStatement(dbType, "increase-access");
	}

	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getUpdateCommentSql(java.lang.String)
	 */
	@Override
	public String getUpdateCommentSql() throws IllegalArgumentException {	
		return getStatement(dbType, "update-comment");
	}

	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getUpdateBlogSql(java.lang.String, boolean)
	 */
	@Override
	public String getUpdateBlogSql(boolean useTwoTable) throws IllegalArgumentException {
		return getStatement(dbType, "update-blog",  useTwoTable);
	}
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getUpdateContentSql(java.lang.String)
	 */
	@Override
	public String getUpdateContentSql() throws IllegalArgumentException {
		return getStatement(dbType, "update-content",  true);
	}
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getBlogContentSql(java.lang.String)
	 */
	@Override
	public String getBlogContentSql() throws IllegalArgumentException {
		return getStatement(dbType, "get-blog-content");
	}
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getDropTblSql(java.lang.String)
	 */
	@Override
	public String getDropBlogTblSql()  throws IllegalArgumentException {
		return getStatement(dbType, "drop-blog-table");
	}
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.rdbms.sql.SQLConfigure#getDropContentTblSql()
	 */
	public String getDropContentTblSql()  throws IllegalArgumentException {
		return getStatement(dbType, "drop-content-table");
	}
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getSetEncodingSql(java.lang.String)
	 */
	@Override
	public String getSetEncodingSql(String charSet) throws IllegalArgumentException {
		String stmt = getStatement(dbType, "set-encoding");
		return stmt.replaceAll("CHAR_SETTING", charSet);
	}
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getQueryMaxBlogIDSql(java.lang.String)
	 */
	@Override
	public String getQueryMaxBlogIDSql() throws IllegalArgumentException {
		return "SELECT MAX(id) AS maxId FROM Blog";
	}
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getQueryBlogCountSql(java.lang.String)
	 */
	@Override
	public String getQueryBlogCountSql() throws IllegalArgumentException {
		return "SELECT COUNT(*) AS total FROM Blog";
	}
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getQueryAllBlogSql(java.lang.String)
	 */
	@Override
	public String getQueryAllBlogSql() throws IllegalArgumentException {
		String s = "SELECT ID, UserID, PublishTime FROM  Blog" +
			" WHERE UserID  != -1 AND PublishTime != -1 ORDER BY ID ASC";
		return s;
	}
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getCreatePrimaryIndexSql(java.lang.String)
	 */
	@Override
	public String getCreateBlogTblPrimaryIndexSql() throws IllegalArgumentException {
		return "ALTER TABLE Blog ADD PRIMARY KEY(ID)";
	}
	
	@Override
	public String getCreateContentTblPrimaryIndexSql() throws IllegalArgumentException {
		return "ALTER TABLE BlogContent ADD PRIMARY KEY(ID)";
	}
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getCreateSecondaryIndexSql(java.lang.String)
	 */
	@Override
	public String getCreateSecondaryIndexSql() throws IllegalArgumentException {
		return "CREATE INDEX IDX_BLOG_UID_PUBTIME ON Blog(UserID, " +
				"PublishTime, AllowView)";
	}
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getBatchQueryBlogSql(java.lang.String)
	 */
	@Override
	public String getBatchQueryBlogSql() throws IllegalArgumentException {
		return "SELECT ID, UserID, PublishTime FROM Blog WHERE ID " +
				"BETWEEN ? AND ? ORDER BY ID ASC";
	}
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getCreateBlogTblSql(java.lang.String, boolean, boolean)
	 */
	@Override
	public String getCreateBlogTblSql(boolean createPrimaryIndex, 
			boolean useTwoTable) throws IllegalArgumentException {
		String stmt = getStatement(dbType, "create-blog-table", useTwoTable);
		if (!createPrimaryIndex) {
			stmt = stmt.replaceAll("PRIMARY KEY", "");
		}		
		return stmt;
	}
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getCreateContentTblSql(java.lang.String, boolean)
	 */
	@Override
	public String getCreateContentTblSql(boolean createPrimaryIndex) throws IllegalArgumentException {
		String stmt = getStatement(dbType, "create-content-table",  true);
		if (!createPrimaryIndex) {
			stmt = stmt.replaceAll("PRIMARY KEY", "");
		}
		return stmt;
	}
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.sql.SQLConfigure#getMultiInsertBlogSql(java.lang.String, long, boolean)
	 */
	@Override
	public String getMultiInsertBlogSql(long insertRows, boolean useTwoTable) throws IllegalArgumentException {
		StringBuilder b = new StringBuilder();
		String ps = getPublishBlogSql( useTwoTable);
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
	public String getMultiInsertContentSql(long insertRows) throws IllegalArgumentException {
		StringBuilder b = new StringBuilder();
		String ps = getInsertContentSql();
		b.append(ps);
		for (long j = 0; j < insertRows - 1; j++) {
				b.append(",(?, ?, ?)");
		}
		return b.toString();
	}
}
