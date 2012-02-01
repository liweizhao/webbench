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
import java.sql.SQLException;

import com.netease.webbench.blogbench.blog.Blog;
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
 * update blog transaction
 * @author LI WEIZHAO
 */
public class BbTestTrxUpdateBlg extends BbTestTransaction {
	protected PreparedStatement psUpdtBlog;
	protected PreparedStatement psUpdtContent;
	protected BlogbenchTrxCounter trxCounter;
	
	public BbTestTrxUpdateBlg(DbSession dbSession, BbTestOptions bbTestOpt, BlogbenchCounters counters) 
	throws Exception {
		super(dbSession, bbTestOpt, bbTestOpt.getPctUpdateBlg(), counters.getTotalTrxCounter());
		this.trxType = BbTestTrxType.UPDATE_BLOG;
		this.trxCounter = counters.getSingleTrxCounter(trxType);
	}
	
	private void bindParameter(Blog blog) throws Exception {
		if (bbTestOpt.getUseTwoTable()) {
			psUpdtBlog.setLong(1, blog.getPublishTime());
			psUpdtBlog.setString(2, blog.getTitle());
			psUpdtBlog.setString(3, blog.getAbs());
			psUpdtBlog.setLong(4, blog.getId());
			psUpdtBlog.setLong(5, blog.getUid());
			
			psUpdtContent.setString(1, blog.getCnt());
			psUpdtContent.setLong(2, blog.getId());
			psUpdtContent.setLong(3, blog.getUid());
		} else {
			psUpdtBlog.setLong(1, blog.getPublishTime());
			psUpdtBlog.setString(2, blog.getTitle());
			psUpdtBlog.setString(3, blog.getAbs());
			psUpdtBlog.setString(4, blog.getCnt());

			psUpdtBlog.setLong(5, blog.getId());
			psUpdtBlog.setLong(6, blog.getUid());
		}
	}

	/* 
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.transaction.BbTestTransaction#exeTrx(com.netease.webbench.blogbench.misc.ParameterGenerator)
	 */
	@Override
	public void doExeTrx(ParameterGenerator paraGen)
			throws Exception {
		Blog blog = paraGen.generateZipfDistrBlog();
		
		long startTime = 0;
		try {
			bindParameter(blog);
			startTime = System.currentTimeMillis();
			if (bbTestOpt.getUseTwoTable()) {
				if (!(1 == dbSession.update(psUpdtBlog) && 
						1 ==dbSession.update(psUpdtContent))) {
					trxCounter.incrFailedTimes();
				}
			} else {
				if (1 != dbSession.update(psUpdtBlog)) {
					trxCounter.incrFailedTimes();
				}
			}
		} catch (SQLException e) {
			trxCounter.incrFailedTimes();
			throw e;
		}
		if (bbTestOpt.isUsedMemcached()) {
			MemcachedClientIF mcm = MemcachedManager.getInstance().getMajorMcc();
			boolean isReplaceBlgSuc = mcm.replace("lblog:" + blog.getId(), blog.getLightBlog().writeToBytes());
			trxCounter.addMemOper(MemOperType.RPC_BLOG, isReplaceBlgSuc);
			
			boolean isReplaceCntSuc = mcm.replace("cnt:" + blog.getId(), (Externalizable)blog.getBlogContent());
			trxCounter.addMemOper(MemOperType.RPC_CNT, isReplaceCntSuc);
		}
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
		String sql = sqlConfig.getUpdateBlogSql(bbTestOpt.getTbName(), 
				bbTestOpt.getUseTwoTable());
		psUpdtBlog = dbSession.createPreparedStatement(sql);
		
		if (bbTestOpt.getUseTwoTable()) {
			String sql2 = sqlConfig.getUpdateContentSql(Portable.getBlogContentTableName(bbTestOpt.getTbName()));
			psUpdtContent = dbSession.createPreparedStatement(sql2);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.transaction.BbTestTransaction#cleanRes()
	 */
	public void cleanRes() throws Exception {
		if (null != psUpdtBlog) {
			psUpdtBlog.close();
		}
		if (null != psUpdtContent) {
			psUpdtContent.close();
		}
		
		if (bbTestOpt.isParallelDml()) {
			dbSession.setParallelDML(false);
		}
	}
}
