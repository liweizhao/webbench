package com.netease.webbench.blogbench.sql;

public interface SQLConfigure {	
	public abstract String getListBlogsSql(String tableName) throws Exception;

	public abstract String getMultiShowBlogSql(int multiCount, String tableName)
			throws Exception;

	public abstract String getPublishBlogSql(String tableName,
			boolean useTwoTable) throws Exception;

	public abstract String getInsertContentSql(String tableName)
			throws Exception;

	public abstract String getShowWeightBlogSql(String blogTblName,
			String contentTblName, boolean useTwoTable) throws Exception;

	public abstract String getShowLightBlogSql(String tableName)
			throws Exception;

	public abstract String getShowPreSiblingsSql(String tableName) throws Exception;

	public abstract String getShowNextSiblingsSql(String tableName) throws Exception;

	public abstract String getUpdateAccessSql(String tableName) throws Exception;

	public abstract String getUpdateCommentSql(String tableName) throws Exception;

	public abstract String getUpdateBlogSql(String blogTblName, boolean useTwoTable) throws Exception;

	public abstract String getUpdateContentSql(String contentTblName) 	throws Exception;

	public abstract String getBlogContentSql(String tableName) throws Exception;

	public abstract String getDropTblSql(String tableName) throws Exception;

	public abstract String getSetEncodingSql(String charSet) throws Exception;

	public abstract String getQueryMaxBlogIDSql(String tableName) throws Exception;

	public abstract String getQueryBlogCountSql(String tableName) throws Exception;

	public abstract String getQueryAllBlogSql(String tableName) throws Exception;

	public abstract String getCreatePrimaryIndexSql(String tableName) throws Exception;

	public abstract String getCreateSecondaryIndexSql(String tableName) throws Exception;

	public abstract String getBatchQueryBlogSql(String tableName) throws Exception;

	public abstract String getCreateBlogTblSql(String tableName,
			boolean createPrimaryIndex, boolean useTwoTable) throws Exception;

	public abstract String getCreateContentTblSql(String tableName,
			boolean createPrimaryIndex) throws Exception;

	public abstract String getMultiInsertBlogSql(String tableName,
			long insertRows, boolean useTwoTable) throws Exception;

	public abstract String getMultiInsertContentSql(String tableName,
			long insertRows) throws Exception;

}