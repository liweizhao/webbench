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

	public abstract String getDropBlogTblSql() throws IllegalArgumentException;
	
	public abstract String getDropContentTblSql() throws IllegalArgumentException;

	public abstract String getSetEncodingSql(String charSet) throws IllegalArgumentException;

	public abstract String getQueryMaxBlogIDSql() throws IllegalArgumentException;

	public abstract String getQueryBlogCountSql() throws IllegalArgumentException;

	public abstract String getQueryAllBlogSql() throws IllegalArgumentException;

	public abstract String getCreateBlogTblPrimaryIndexSql() throws IllegalArgumentException;

	public abstract String getCreateContentTblPrimaryIndexSql() throws IllegalArgumentException;
	
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