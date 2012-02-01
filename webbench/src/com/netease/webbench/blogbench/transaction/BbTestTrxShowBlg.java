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

import java.io.Externalizable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.netease.webbench.blogbench.blog.Blog;
import com.netease.webbench.blogbench.blog.BlogContent;
import com.netease.webbench.blogbench.blog.BlogIdPair;
import com.netease.webbench.blogbench.blog.BlogInfoWithPub;
import com.netease.webbench.blogbench.blog.LightBlog;
import com.netease.webbench.blogbench.memcached.MemcachedClientIF;
import com.netease.webbench.blogbench.memcached.MemcachedManager;
import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.blogbench.misc.ParameterGenerator;
import com.netease.webbench.blogbench.misc.Portable;
import com.netease.webbench.blogbench.sql.SQLConfigure;
import com.netease.webbench.blogbench.statis.BlogbenchCounters;
import com.netease.webbench.blogbench.statis.BlogbenchTrxCounter;
import com.netease.webbench.blogbench.statis.MemcachedOperCounter.MemOperType;
import com.netease.webbench.common.DbSession;

/**
 * show blog transaction
 * @author LI WEIZHAO
 */
public class BbTestTrxShowBlg extends BbTestTransaction 
implements BlogDBFetcher {
	protected PreparedStatement prepareStatement;
	protected PreparedStatement lightPrpStmt;
	protected PreparedStatement fetchCntPrpStmt;

	protected long timeWaste;
	protected BlogbenchTrxCounter trxCounter;

	/**
	 * constructor
	 * @param dbSession
	 * @param pct
	 * @param totalTrxCounter
	 * @param trxCounter
	 */
	public BbTestTrxShowBlg(DbSession dbSession, BbTestOptions bbTestOpt, BlogbenchCounters counters) 
	throws Exception {
		super(dbSession, bbTestOpt, bbTestOpt.getPctShowBlg(), counters.getTotalTrxCounter());
		this.trxType = BbTestTrxType.SHOW_BLG;
		this.trxCounter = counters.getSingleTrxCounter(trxType);
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
		
		SQLConfigure sqlConfig = SQLConfigure.getInstance(dbSession.getDbOpt().getDbType());
		
		String lightSql = sqlConfig.getShowLightBlogSql(bbTestOpt.getTbName());		
		lightPrpStmt = dbSession.createPreparedStatement(lightSql);
		
		String contentSql = sqlConfig.getBlogContentSql(bbTestOpt.getUseTwoTable() ? Portable.getBlogContentTableName(bbTestOpt.getTbName())
				: bbTestOpt.getTbName());
		fetchCntPrpStmt = dbSession.createPreparedStatement(contentSql);
	
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
		/* get a random blog for query */
		BlogInfoWithPub blogInfo = paraGen.getZipfRandomBlog();
		
		long startTime = System.currentTimeMillis();
		if (bbTestOpt.isUsedMemcached()) {
			/* first query the record in memcached server, if it doesn't  exist, fetch it from database */
			MemcachedClientIF mcm = MemcachedManager.getInstance().getMajorMcc();
			
			Blog blog = null;
			LightBlog lightBlog = getLightBlogFromMemcached(mcm, blogInfo);
			
			if (lightBlog != null) {
				//if blog content doesn't exist in memcached, fetch it from database
				BlogContent blogCnt = getContentFromMemcached(mcm, blogInfo);				
				if (blogCnt == null) {
					blogCnt = getContentFromDb(blogInfo.getBlogId(), blogInfo.getUId());
					if (blogCnt != null) {
						setContentToMemcached(mcm, blogInfo, blogCnt);
					} else {
						//FIXED ME: this won't be execute
						System.out.println("[Error] failed to fetch blog content from database!");
					}
				}
				blog = new Blog(lightBlog, blogCnt);
			} else {
				BlogContent blogCnt = getContentFromMemcached(mcm, blogInfo);
				if (blogCnt == null) {
					blog = getWholeBlogFromDb(blogInfo.getBlogId(), blogInfo.getUId(), true);
					if (blog != null) {
						setLightBlogToMemcached(mcm, blogInfo, blog.getLightBlog());
						setContentToMemcached(mcm, blogInfo, blog.getBlogContent());
					} else {
						throw new Exception("[Error] failed to fetch blog record from database!");
					}	
				} else {
					lightBlog = getLightBlogFromDb(blogInfo.getBlogId(), blogInfo.getUId());
					if (lightBlog != null) {
						setLightBlogToMemcached(mcm, blogInfo, lightBlog);
						blog = new Blog(lightBlog, blogCnt);
					} else {
						System.out.println("[Error] failed to fetch blog without content from database!");
					}
				}				
			}
		} else {
			Blog blog = getWholeBlogFromDb(blogInfo.getBlogId(), blogInfo.getUId(), true);
			if (blog == null)
				throw new Exception("Error: failed to fetch blog record from database(show blog transaction)!");
		}
		long stopTime = System.currentTimeMillis();
		timeWaste += (stopTime - startTime);
		totalTrxCounter.addTrx(timeWaste);
		trxCounter.addTrx(timeWaste);
		timeWaste = 0;
	}
	
	/**
	 * bind prepared statement parameter
	 * @param localPrpStmt
	 * @param blogId
	 * @param uId
	 * @throws SQLException
	 */
	protected void bindParameter(PreparedStatement localPrpStmt, long blogId, long uId) throws SQLException {
		localPrpStmt.setLong(1, blogId);
		localPrpStmt.setLong(2, uId);
	}
	
	/**
	 * fetch a whole blog record from database
	 * @param blogId
	 * @param uId
	 * @param culculateTimeWaste
	 * @return
	 * @throws SQLException
	 */
	protected Blog getWholeBlogFromDb(long blogId, long uId,
			boolean culculateTimeWaste) throws SQLException {
		if (culculateTimeWaste) {
			long startTime = System.currentTimeMillis();
			bindParameter(prepareStatement, blogId, uId);
			long stopTime = System.currentTimeMillis();
			timeWaste -= (stopTime - startTime);
		} else
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
	
	/**
	 * fetch a light blog from database
	 * @param blogId
	 * @param uId
	 * @return
	 * @throws SQLException
	 */
	protected LightBlog getLightBlogFromDb(long blogId, long uId) throws SQLException {
		bindParameter(lightPrpStmt, blogId, uId);
		
		ResultSet rs = dbSession.query(lightPrpStmt);
		LightBlog lightBlog = null;
		if (rs.next()) {
			String title = rs.getString("Title");
			String abs = rs.getString("Abstract");
			int allowView = rs.getInt("AllowView");
			long publishTime = rs.getLong("PublishTime");
			int acsCount = rs.getInt("AccessCount");
			int cmtCount = rs.getInt("CommentCount");
			lightBlog = new LightBlog(blogId, uId, title, abs, allowView,
					publishTime, acsCount, cmtCount);
		}
		rs.close();
		return lightBlog;
	}
	
	/**
	 * fetch blog content from database
	 * @param blogId
	 * @param uId
	 * @return
	 * @throws SQLException
	 */
	protected BlogContent getContentFromDb(long blogId, long uId) throws SQLException {
		bindParameter(fetchCntPrpStmt, blogId, uId);
		
		ResultSet rs = dbSession.query(fetchCntPrpStmt);
		BlogContent blogContent = null;
		while (rs.next()) {
			String content = rs.getString(1);
			blogContent = new BlogContent(content);
			break;
		}
		rs.close();
		return blogContent;
	}
	
	/**
	 * 
	 * @param mcm
	 * @param blogInfo
	 * @return
	 * @throws Exception
	 */
	protected LightBlog getLightBlogFromMemcached(MemcachedClientIF mcm, BlogIdPair blogInfo) throws Exception {
		LightBlog lightBlog = new LightBlog();
		byte[] blogSerialData = (byte[])mcm.get("lblog:" + blogInfo.getBlogId());
		
		boolean isReadBlogSuc = lightBlog.readFromBytes(blogSerialData);
		
		trxCounter.addMemOper(MemOperType.GET_BLOG, isReadBlogSuc);
		
		return isReadBlogSuc ? lightBlog : null;
	}
	
	/**
	 * 
	 * @param mcm
	 * @param blogInfo
	 * @param lightBlog
	 * @throws Exception
	 */
	protected void setLightBlogToMemcached(MemcachedClientIF mcm, BlogIdPair blogInfo, LightBlog lightBlog) throws Exception {
		boolean isSetBlogSuc =mcm.set("lblog:" + blogInfo.getBlogId(), lightBlog.writeToBytes());
		trxCounter.addMemOper(MemOperType.SET_BLOG, isSetBlogSuc);	
	}
	
	/**
	 * get blog content from memcached
	 * @param mcm
	 * @param blogInfo
	 * @return
	 * @throws Exception
	 */
	protected BlogContent getContentFromMemcached(MemcachedClientIF mcm, BlogIdPair blogInfo) throws Exception {
		BlogContent blogCnt = (BlogContent)(Externalizable)mcm.get("cnt:" + blogInfo.getBlogId());
		trxCounter.addMemOper(MemOperType.GET_CNT, blogCnt == null ? false : true);
		return blogCnt;
	}
	
	/**
	 * 
	 * @param mcm
	 * @param blogInfo
	 * @param blogContent
	 * @throws Exception
	 */
	protected void setContentToMemcached(MemcachedClientIF mcm, BlogIdPair blogInfo, BlogContent blogContent) throws Exception {
		boolean isSetCntSuc = mcm.set("cnt:" + blogInfo.getBlogId(), (Externalizable)blogContent);
		trxCounter.addMemOper(MemOperType.SET_CNT, isSetCntSuc);
	}
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.BlogFetcher#getWholeBlog()
	 */
	public Blog getWholeBlog(long blogId, long UserId) throws SQLException {
		return getWholeBlogFromDb(blogId, UserId, false);
	}
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.BlogFetcher#getLightBlog()
	 */
	public LightBlog getLightBlog(long blogId, long UserId) throws SQLException {
		return getLightBlogFromDb(blogId, UserId);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.transaction.BbTestTransaction#cleanRes()
	 */
	public void cleanRes() throws SQLException {
		if (null != prepareStatement) {
			prepareStatement.close();
		}
		if (null != lightPrpStmt) {
			lightPrpStmt.close();
		}
		if (null != fetchCntPrpStmt) {
			fetchCntPrpStmt.close();
		}
	}
}
