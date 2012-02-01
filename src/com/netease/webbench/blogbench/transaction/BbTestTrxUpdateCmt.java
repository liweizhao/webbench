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

import com.netease.webbench.blogbench.blog.BlogInfoWithPub;
import com.netease.webbench.blogbench.blog.LightBlog;
import com.netease.webbench.blogbench.memcached.MemcachedClientIF;
import com.netease.webbench.blogbench.memcached.MemcachedManager;
import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.blogbench.misc.ParameterGenerator;
import com.netease.webbench.blogbench.sql.SQLConfigure;
import com.netease.webbench.blogbench.statis.BlogbenchCounters;
import com.netease.webbench.blogbench.statis.BlogbenchTrxCounter;
import com.netease.webbench.blogbench.statis.MemcachedOperCounter.MemOperType;
import com.netease.webbench.common.DbSession;
/**
 * update comment transaction
 * @author LI WEIZHAO
 */
public class BbTestTrxUpdateCmt extends BbTestTransaction {
	protected PreparedStatement prepareStatement;/* prepared SQL statement to execute this transaction */
	protected BlogbenchTrxCounter trxCounter;
	protected BlogDBFetcher blogFetcher;
	
	public BbTestTrxUpdateCmt(DbSession dbSession, BbTestOptions bbTestOpt, BlogbenchCounters counters) 
	throws Exception {
		super(dbSession, bbTestOpt, bbTestOpt.getPctUpdateComment(), counters.getTotalTrxCounter());
		this.trxType = BbTestTrxType.UPDATE_CMT;
		this.trxCounter = counters.getSingleTrxCounter(trxType);
	}
	
	private void bindParameter(long blogId, long uId) throws SQLException {
		prepareStatement.setLong(1, uId);
		prepareStatement.setLong(2, blogId);
	}
	
	public void setBlogDBFetcher(BlogDBFetcher bf) {
		blogFetcher = bf;
	}

	/* 
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.transaction.BbTestTransaction#exeTrx(com.netease.webbench.blogbench.misc.ParameterGenerator)
	 */
	@Override
	public void doExeTrx(ParameterGenerator paraGen)
			throws Exception {
		BlogInfoWithPub blogInfo = paraGen.getZipfRandomBlog();
		long blogId = blogInfo.getBlogId();
		long uId = blogInfo.getUId();
		long startTime = 0;
		try {
			bindParameter(blogId, uId);
			startTime = System.currentTimeMillis();
			if (1 != dbSession.update(prepareStatement)) {
				trxCounter.incrFailedTimes();
			}
		} catch (SQLException e) {
			trxCounter.incrFailedTimes();
			throw e;
		}
		if (bbTestOpt.isUsedMemcached()) {
			MemcachedClientIF mcm = MemcachedManager.getInstance().getMajorMcc();
			LightBlog lightBlog = new LightBlog();
			boolean readSuc = lightBlog.readFromBytes((byte[])mcm.get("lblog:" + blogId));
			
			trxCounter.addMemOper(MemOperType.GET_BLOG, readSuc);
			
			if (readSuc) {
				lightBlog.increaseCmtCnt();
				boolean hit = mcm.set("lblog:" + blogId, lightBlog.writeToBytes());
				trxCounter.addMemOper(MemOperType.SET_BLOG, hit);
			} else {
				lightBlog = blogFetcher.getLightBlog(blogId, uId);
				boolean hit = mcm.set("lblog:" + blogId, lightBlog.writeToBytes());
				trxCounter.addMemOper(MemOperType.SET_BLOG, hit);
			}
		}//end useMemcached
		long stopTime = System.currentTimeMillis();
		totalTrxCounter.addTrx(stopTime - startTime);
		trxCounter.addTrx(stopTime - startTime);
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
		
		SQLConfigure sqlConfig = SQLConfigure.getInstance(dbSession.getDbOpt().getDbType());
		String sql = sqlConfig.getUpdateCommentSql(bbTestOpt.getTbName());
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
