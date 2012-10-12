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

import com.netease.webbench.blogbench.blog.Blog;
import com.netease.webbench.blogbench.memcached.MemcachedClientIF;
import com.netease.webbench.blogbench.memcached.MemcachedManager;
import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.blogbench.misc.ParameterGenerator;
import com.netease.webbench.blogbench.misc.Portable;
import com.netease.webbench.blogbench.sql.SQLConfigure;
import com.netease.webbench.blogbench.sql.SQLConfigureFactory;
import com.netease.webbench.blogbench.statis.BlogbenchCounters;
import com.netease.webbench.blogbench.statis.MemcachedOperCounter.MemOperType;
import com.netease.webbench.common.DbSession;
/**
 * publish blog transaction 
 * @author LI WEIZHAO
 */
public class BbTestTrxPublishBlg2 extends BbTestTransaction {
	protected PreparedStatement ps;
	protected PreparedStatement contentPs = null;
	
	/**
	 * constructor
	 * @param dbSession
	 */
	public BbTestTrxPublishBlg2(DbSession dbSession, BbTestOptions bbTestOpt) 
			throws Exception {
		super(dbSession, bbTestOpt, 1, BbTestTrxType.PUBLISH_BLG, null);
	}
	
	/**
	 * constructor
	 * @param dbSession
	 * @param pct
	 * @param totalTrxCounter
	 * @param trxCounter
	 */
	public BbTestTrxPublishBlg2(DbSession dbSession, BbTestOptions bbTestOpt, 
			BlogbenchCounters counters) throws Exception {
		super(dbSession, bbTestOpt, bbTestOpt.getPctPublishBlg(), 
				BbTestTrxType.PUBLISH_BLG, counters);
	}
	
	/**
	 * bind prepared statement parameters
	 * @param blog
	 * @throws Exception
	 */
	private void bindParameter(Blog blog) throws Exception {		
		ps.setLong(1, blog.getId());
		ps.setLong(2, blog.getUid());
		ps.setString(3, blog.getTitle());
		ps.setString(4, blog.getAbs());
		ps.setInt(5, blog.getAllowView());
		ps.setLong(6, blog.getPublishTime());
		
		contentPs.setLong(1, blog.getId());
		contentPs.setLong(2, blog.getUid());
		contentPs.setString(3, blog.getCnt());
	}

	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.transaction.BbTestTransaction#exeTrx(com.netease.webbench.blogbench.misc.ParameterGenerator)
	 */
	@Override
	public void doExeTrx(ParameterGenerator paraGen) throws Exception {
		try {
			Blog blog = paraGen.generateNewBlog();
			bindParameter(blog);

			if ((1 == dbSession.update(ps)) 
					&&  1 == dbSession.update(contentPs)) {
				if (bbTestOpt.isUsedMemcached()) {
					MemcachedClientIF mcc = 
							MemcachedManager.getInstance().getMajorMcc();
					boolean hit = mcc.delete("blog:ids:" + blog.getUid());					
					myTrxCounter.addMemOper(MemOperType.DEL_LIST, hit);
				}
				
				/* if insert blog record successfully, 
				 * update the blog id and blog user id map array */
				paraGen.updateBlgMapArr(blog.getId(), blog.getUid(), 
						blog.getPublishTime());
			} else {
				myTrxCounter.incrFailedTimes();
			}
		} catch (SQLException e) {
			myTrxCounter.incrFailedTimes();
			throw e;
		}
	}
	
	/**
	 * batch execute publish blog transaction
	 * @param dbSession
	 * @param paraGen
	 * @param blogs
	 * @throws Exception
	 */
	public void batchExec(DbSession dbSession, ParameterGenerator paraGen, 
			Blog blogs[]) throws Exception {
		if (!dbSession.getAutoCommit()) 
			dbSession.setAutoCommit(false);
		
		for (int i = 0; i < blogs.length; i++) {
			bindParameter(blogs[i]);
			ps.addBatch();
			if (bbTestOpt.getUseTwoTable()) {
				contentPs.addBatch();
			}
		}
		ps.executeBatch();
		contentPs.executeBatch();
		
		dbSession.commit();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.transaction.BbTestTransaction#prepare()
	 */
	@Override
	public void prepare() throws Exception {
		// TODO Auto-generated method stub
		if (bbTestOpt.isParallelDml()) {
			dbSession.setParallelDML(true);
		}
		
		SQLConfigure sqlConfig = SQLConfigureFactory.getSQLConfigure();
		String sql = sqlConfig.getPublishBlogSql(bbTestOpt.getTbName(), 
				bbTestOpt.getUseTwoTable());
		ps = dbSession.createPreparedStatement(sql);
		
		sql = sqlConfig.getInsertContentSql(Portable.getBlogContentTableName(bbTestOpt.getTbName()));
		contentPs = dbSession.createPreparedStatement(sql);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.transaction.BbTestTransaction#cleanRes()
	 */
	public void cleanRes() throws Exception {
		if (null != ps) {
			ps.close();
			ps = null;
		}
		if (null != contentPs) {
			contentPs.close();
			contentPs = null;
		}
		
		if (bbTestOpt.isParallelDml()) {
			dbSession.setParallelDML(false);
		}
	}
}
