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
package com.netease.webbench.blogbench.transaction;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.netease.webbench.blogbench.blog.Blog;
import com.netease.webbench.blogbench.blog.BlogInfoWithPub;
import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.blogbench.misc.ParameterGenerator;
import com.netease.webbench.blogbench.misc.Portable;
import com.netease.webbench.blogbench.sql.SQLConfigure;
import com.netease.webbench.blogbench.sql.SQLConfigureFactory;
import com.netease.webbench.blogbench.statis.BlogbenchCounters;
import com.netease.webbench.common.DbSession;

/**
 * show blog transaction
 * @author LI WEIZHAO
 */
public class BbTestTrxShowBlg extends BbTestTransaction {
	protected PreparedStatement prepareStatement;

	/**
	 * constructor
	 * @param dbSession
	 * @param pct
	 * @param totalTrxCounter
	 * @param trxCounter
	 */
	public BbTestTrxShowBlg(DbSession dbSession, BbTestOptions bbTestOpt, 
			BlogbenchCounters counters) throws Exception {
		super(dbSession, bbTestOpt, bbTestOpt.getPctShowBlg(), 
				BbTestTrxType.SHOW_BLG, counters);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.transaction.BbTestTransaction#prepare()
	 */
	@Override
	public void prepare() throws Exception {		
		SQLConfigure sqlConfig = SQLConfigureFactory.getSQLConfigure(dbOpt.getDbType());
	
		String showSql = sqlConfig.getShowWeightBlogSql(bbTestOpt.getTbName(), 
				Portable.getBlogContentTableName(bbTestOpt.getTbName()),
				bbTestOpt.getUseTwoTable());
		prepareStatement = dbSession.createPreparedStatement(showSql);
	}

	/* 
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.transaction.BbTestTransaction#exeTrx(com.netease.webbench.blogbench.misc.ParameterGenerator)
	 */
	@Override
	public void doExeTrx(ParameterGenerator paraGen)
			throws Exception {
		BlogInfoWithPub blogInfo = paraGen.getZipfRandomBlog();
		
		Blog blog = getWholeBlogFromDb(blogInfo.getBlogId(), blogInfo.getUId());
		if (blog == null) {
			throw new Exception("Error: failed to fetch blog record " +
					"from database(show blog transaction)!");
		}
	}
	
	/**
	 * bind prepared statement parameter
	 * @param localPrpStmt
	 * @param blogId
	 * @param uId
	 * @throws SQLException
	 */
	protected void bindParameter(PreparedStatement localPrpStmt, 
			long blogId, long uId) throws SQLException {
		localPrpStmt.setLong(1, blogId);
		localPrpStmt.setLong(2, uId);
	}
	
	/**
	 * fetch a whole blog record from database
	 * @param blogId
	 * @param uId
	 * @return
	 * @throws SQLException
	 */
	public Blog getWholeBlogFromDb(long blogId, long uId) 
			throws SQLException {
		bindParameter(prepareStatement, blogId, uId);

		ResultSet rs = dbSession.query(prepareStatement);
		Blog blog = null;

		while (rs.next()) {
			String title = rs.getString("Title");
			String abs = rs.getString("Abstract");
			String cnt = rs.getString("Content");
			int allowView = rs.getInt("AllowView");
			long publishTime = rs.getLong("PublishTime");
			int acsCount = rs.getInt("AccessCount");
			int cmtCount = rs.getInt("CommentCount");
			blog = new Blog(blogId, uId, title, abs, cnt, allowView, publishTime, acsCount, cmtCount);
			break;
		}
		rs.close();
		return blog;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.transaction.BbTestTransaction#cleanRes()
	 */
	public void cleanRes() throws SQLException {
		if (null != prepareStatement) {
			prepareStatement.close();
		}
	}
}
