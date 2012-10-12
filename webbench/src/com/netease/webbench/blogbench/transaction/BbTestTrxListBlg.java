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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.netease.webbench.blogbench.blog.BlogIdPair;
import com.netease.webbench.blogbench.blog.LightBlog;
import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.blogbench.misc.ParameterGenerator;
import com.netease.webbench.blogbench.sql.SQLConfigure;
import com.netease.webbench.blogbench.sql.SQLConfigureFactory;
import com.netease.webbench.blogbench.statis.BlogbenchCounters;
import com.netease.webbench.common.DbSession;

/**
 * list blogs transaction
 * @author LI WEIZHAO
 */
public class BbTestTrxListBlg extends BbTestTransaction {
	public static int QUERY_LIMIT_SIZE = 10;
	
	protected PreparedStatement prepareStatement;
	protected PreparedStatement []multiGetBlogPs;

	public BbTestTrxListBlg(DbSession dbSession, BbTestOptions bbTestOpt, 
			BlogbenchCounters counters) throws Exception {
		super(dbSession, bbTestOpt, bbTestOpt.getPctListBlg(), 
				BbTestTrxType.LIST_BLGS, counters);
		multiGetBlogPs = new PreparedStatement[QUERY_LIMIT_SIZE];
	}

	private void bindParameter(long uid) throws SQLException{
		prepareStatement.setLong(1, uid);
	}

	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.transaction.BbTestTransaction#exeTrx(com.netease.webbench.blogbench.misc.ParameterGenerator)
	 */
	@Override
	public void doExeTrx(ParameterGenerator paraGen) 	throws Exception {
		long uId = paraGen.getZipfUserId();
		getListFromDb(dbSession, uId);
	}
	
	public ArrayList<Long> getListFromDb(DbSession dbSession, long uId) throws SQLException {
		try {
			bindParameter(uId);
			
			ResultSet rs = dbSession.query(prepareStatement);
						
			ArrayList<Long> resultList = new ArrayList<Long>();
			while (rs.next()) {
				resultList.add(rs.getLong("ID"));
			}
			
			rs.close();
			return resultList;
		} catch (SQLException e) {
			myTrxCounter.incrFailedTimes();
			throw e;
		}
	}
	
	/**
	 * fetch all light blogs of specified ids from database
	 * @param blogIdPairList the blog id list of blogs to fetch
	 * @return light blogs list
	 * @throws Exception
	 */
	public List<LightBlog> multiGetLightBlogFromDb(List<BlogIdPair> blogIdPairList) throws SQLException {
		List<LightBlog> blogsList = new LinkedList<LightBlog>();
		if (blogIdPairList == null)
			return null;
		if (blogIdPairList.size() == 0)
			return blogsList;

		int idx = 1;
		int psIndex = blogIdPairList.size() - 1;
		for (int i = 0; i < blogIdPairList.size(); i++) {
			multiGetBlogPs[psIndex].setLong(idx++, blogIdPairList.get(i).getBlogId());
			multiGetBlogPs[psIndex].setLong(idx++, blogIdPairList.get(i).getUId());
		}
		
		ResultSet rs = dbSession.query(multiGetBlogPs[psIndex]);
		
		if (rs != null) {
			while(rs.next()) {
				long id = rs.getLong("ID");
				long uId = rs.getLong("UserId");
				String title = rs.getString("Title");
				String abs = rs.getString("Abstract");
				int allowView = rs.getInt("AllowView");
				long publishTime = rs.getLong("PublishTime");
				int AcsCnt = rs.getInt("AccessCount");
				int cmnCnt = rs.getInt("CommentCount");
				blogsList.add(new LightBlog(id, uId, title, abs, allowView, publishTime, AcsCnt, cmnCnt));
			}
		} else {
			return null;
		}
		return blogsList;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.transaction.BbTestTransaction#prepare()
	 */
	@Override
	public void prepare() throws Exception {
		if (dbSession == null) {
			throw new Exception("Database connection doesn't exit!");
		}
		SQLConfigure sqlConfig = SQLConfigureFactory.getSQLConfigure();
		String listSql = sqlConfig.getListBlogsSql(bbTestOpt.getTbName(), bbTestOpt.isUsedMemcached());
		prepareStatement = dbSession.createPreparedStatement(listSql);
		
		if (bbTestOpt.isUsedMemcached()) {
			for (int i = 0; i < QUERY_LIMIT_SIZE; i++) {
				String sql = sqlConfig.getMultiShowBlogSql(i + 1, bbTestOpt.getTbName());
				multiGetBlogPs[i] = dbSession.createPreparedStatement(sql);
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.transaction.BbTestTransaction#cleanRes()
	 */
	public void cleanRes() throws SQLException {
		if (null != prepareStatement) {
			prepareStatement.close();
		}
		if (null != multiGetBlogPs) {
			for (PreparedStatement it : multiGetBlogPs) {
				if (null != it) {
					it.close();
				}
			}
		}
	}
}
