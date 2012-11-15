package com.netease.webbench.blogbench.rdbms.sql;

public interface SQLConfigure {
	public abstract String getListBlogsSql() throws IllegalArgumentException;

	public abstract String getMultiShowBlogSql(int multiCount)
			throws IllegalArgumentException;

	public abstract String getPublishBlogSql(boolean useTwoTable)
			throws IllegalArgumentException;

	public abstract String getInsertContentSql() throws IllegalArgumentException;

	public abstract String getShowWeightBlogSql(
			boolean useTwoTable) throws IllegalArgumentException;

	public abstract String getShowLightBlogSql()
			throws IllegalArgumentException;

	public abstract String getShowPreSiblingsSql() throws IllegalArgumentException;

	public abstract String getShowNextSiblingsSql() throws IllegalArgumentException;

	public abstract String getUpdateAccessSql() throws IllegalArgumentException;

	public abstract String getUpdateCommentSql() throws IllegalArgumentException;

	public abstract String getUpdateBlogSql(boolean useTwoTable) 
			throws IllegalArgumentException;

	public abstract String getUpdateContentSql() throws IllegalArgumentException;

	public abstract String getBlogContentSql() throws IllegalArgumentException;

	public abstract String getDropTblSql() throws IllegalArgumentException;

	public abstract String getSetEncodingSql(String charSet) throws IllegalArgumentException;

	public abstract String getQueryMaxBlogIDSql() throws IllegalArgumentException;

	public abstract String getQueryBlogCountSql() throws IllegalArgumentException;

	public abstract String getQueryAllBlogSql() throws IllegalArgumentException;

	public abstract String getCreatePrimaryIndexSql() throws IllegalArgumentException;

	public abstract String getCreateSecondaryIndexSql() throws IllegalArgumentException;

	public abstract String getBatchQueryBlogSql() throws IllegalArgumentException;

	public abstract String getCreateBlogTblSql(boolean createPrimaryIndex, 
			boolean useTwoTable) throws IllegalArgumentException;

	public abstract String getCreateContentTblSql(
			boolean createPrimaryIndex) throws IllegalArgumentException;

	public abstract String getMultiInsertBlogSql(
			long insertRows, boolean useTwoTable) throws IllegalArgumentException;

	public abstract String getMultiInsertContentSql(long insertRows) 
			throws IllegalArgumentException;

}