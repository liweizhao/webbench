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
import java.sql.SQLException;

import com.netease.webbench.blogbench.blog.BlogInfoWithAcs;
import com.netease.webbench.blogbench.blog.BlogInfoWithPub;
import com.netease.webbench.blogbench.blog.LightBlog;
import com.netease.webbench.blogbench.memcached.AccessCountCache;
import com.netease.webbench.blogbench.memcached.MemcachedClientIF;
import com.netease.webbench.blogbench.memcached.MemcachedManager;
import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.blogbench.misc.ParameterGenerator;
import com.netease.webbench.blogbench.sql.SQLConfigure;
import com.netease.webbench.blogbench.sql.SQLConfigureFactory;
import com.netease.webbench.blogbench.statis.BlogbenchCounters;
import com.netease.webbench.blogbench.statis.BlogbenchTrxCounter;
import com.netease.webbench.blogbench.statis.MemcachedOperCounter.MemOperType;
import com.netease.webbench.common.DbSession;
/**
 * update access count transaction
 * @author LI WEIZHAO
 */
public class BbTestTrxUpdateAcs extends BbTestTransaction {	
	protected PreparedStatement prepareStatement;
	protected BlogbenchTrxCounter trxCounter;
	protected BlogDBFetcher blogFetcher;
	protected long timeWaste = 0;
	protected AccessCountCache accessCountCache;
	
	public BbTestTrxUpdateAcs(DbSession dbSession, BbTestOptions bbTestOpt, BlogbenchCounters counters) 
	throws Exception {
		super(dbSession, bbTestOpt, bbTestOpt.getPctUpdateAccess(),
				BbTestTrxType.UPDATE_ACS, counters);
	}
	
	public void setBlogDBFetcher(BlogDBFetcher bf) {
		blogFetcher = bf;
	}
	
	public void setAcsCntUpdateCache(AccessCountCache cache) {
		this.accessCountCache = cache;
	}
	
	protected void bindParameter(long blogId, long uId) throws SQLException {
		prepareStatement.setLong(1, uId);
		prepareStatement.setLong(2, blogId);
	}
	
	protected void bindParameter(BlogInfoWithAcs blogInfo) throws SQLException {
		prepareStatement.setInt(1, blogInfo.getBlogAcs());
		prepareStatement.setLong(2, blogInfo.getUId());
		prepareStatement.setLong(3, blogInfo.getBlogId());
		prepareStatement.setInt(4, blogInfo.getBlogAcs());
	}

	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.transaction.BbTestTransaction#exeTrx(com.netease.webbench.blogbench.misc.ParameterGenerator)
	 */
	@Override
	public void doExeTrx(ParameterGenerator paraGen) 
	throws Exception {
		
		BlogInfoWithPub blogInfo = paraGen.getZipfRandomBlog();
		
		long timeStart = System.currentTimeMillis();
		if (bbTestOpt.isUsedMemcached() && accessCountCache != null) {
			MemcachedClientIF mcm = MemcachedManager.getInstance().getMajorMcc();
			MemcachedClientIF counterMcm = MemcachedManager.getInstance().getMinorMcc();
			
			/* get newest access count of a blog in memcached server */
			long newestAcsCnt = counterMcm.getCounter("blog:" + blogInfo.getBlogId());
			
			trxCounter.addMemOper(MemOperType.GET_ACS, newestAcsCnt >= 0 ? true : false);//统计memcached中取浏览计数操作的成功次数
			
			if (newestAcsCnt < 0) {
				
				/* fetch blog record in memcached server */
				LightBlog lightBlog = new LightBlog();
				boolean readSuc = lightBlog.readFromBytes((byte[])mcm.get("lblog:" + blogInfo.getBlogId()));
				
				trxCounter.addMemOper(MemOperType.GET_BLOG, readSuc);
				
				if (!readSuc) {
					/* if we can't get blog, fetch it from database */
					lightBlog = blogFetcher.getLightBlog(blogInfo.getBlogId(), blogInfo.getUId());
					
					if (lightBlog != null) {
						boolean hit = mcm.set("lblog:" + lightBlog.getId(), lightBlog.writeToBytes());
						trxCounter.addMemOper(MemOperType.SET_BLOG, hit);
					} else {
						throw new Exception("Error: failed to fetch blog record from database(update blog access transaction)!");
					}
				}
				newestAcsCnt = lightBlog.getAccessCount();
			} 
			
			/* update access count information in memcached */
			BlogInfoWithAcs blogInfoWithAcs = new BlogInfoWithAcs(blogInfo.getBlogId(),
					blogInfo.getUId(), (int)newestAcsCnt + 1);
			
			long incrRtn = counterMcm.incr("blog:" + blogInfo.getBlogId());
			if (incrRtn < 0) {
				trxCounter.addMemOper(MemOperType.INC_ACS, false);
				
				boolean isAddSuc = counterMcm.addOrIncr("blog:" + blogInfo.getBlogId()) < 0 ? false : true;
				trxCounter.addMemOper(MemOperType.ADD_ACS, isAddSuc);
			} else {
				trxCounter.addMemOper(MemOperType.INC_ACS, true);
			}
			
			/**
			 * update access count in local cache
			 * if cache of access count is full, will update in database directly
			 */
			if (!accessCountCache.cacheUpdate(blogInfoWithAcs))
				updateAcsToDb(blogInfoWithAcs);
			
		} else {
			//update access count in database
			updateAcsToDb(blogInfo.getBlogId(), blogInfo.getUId());
		}
		long timeStop = System.currentTimeMillis();
		timeWaste += (timeStop - timeStart);
		totalTrxCounter.addTrx(timeWaste);
		trxCounter.addTrx(timeWaste);
		timeWaste  = 0;
	}
	
	public void updateAcsToDb(long blogId, long uId) throws SQLException {
		try {
			long startTime = System.currentTimeMillis();
			bindParameter(blogId, uId);
			long stopTime = System.currentTimeMillis();
			timeWaste -= (stopTime - startTime);
			if (1 != dbSession.update(prepareStatement)) {
				System.out.println("Update access count failed");
				trxCounter.incrFailedTimes();
			}
		} catch (SQLException e) {
			trxCounter.incrFailedTimes();
			throw e;
		}		
	}
	
	public void updateAcsToDb(BlogInfoWithAcs blogInfo) throws SQLException {
		try {
			long startTime = System.currentTimeMillis();
			bindParameter(blogInfo);
			long stopTime = System.currentTimeMillis();
			timeWaste -= (stopTime - startTime);
			if (1 != dbSession.update(prepareStatement)) {
				//System.out.println("Update access count failed");
				//failedTimes++;
			}
		} catch (SQLException e) {
			trxCounter.incrFailedTimes();
			throw e;
		}
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
		
		if (bbTestOpt.isParallelDml()) {
			dbSession.setParallelDML(true);
		}

		SQLConfigure sqlConfig = SQLConfigureFactory.getSQLConfigure();
		String sql = sqlConfig.getUpdateAccessSql(bbTestOpt.getTbName(), bbTestOpt.isUsedMemcached());
		prepareStatement = dbSession.createPreparedStatement(sql);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.transaction.BbTestTransaction#cleanRes()
	 */
	public void cleanRes() throws Exception {
		if (null != prepareStatement) {
			prepareStatement.close();
		}
		
		if (bbTestOpt.isParallelDml()) {
			dbSession.setParallelDML(false);
		}
	}
}
