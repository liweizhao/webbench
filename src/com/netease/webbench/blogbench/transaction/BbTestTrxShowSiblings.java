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
 * show siblings transaction
 * @author LI WEIZHAO
 */
public class BbTestTrxShowSiblings extends BbTestTransaction {
	protected PreparedStatement prepareStatementPre;
	protected PreparedStatement prepareStatementNxt;
	
	private long timeWaste = 0;
	private BlogbenchTrxCounter trxCounter;
	private BlogDBFetcher blogFetcher;
	private long userId;
	private int idPre = -1;
	private int idNext = -1;
	
	public BbTestTrxShowSiblings(DbSession dbSession, BbTestOptions bbTestOpt, BlogbenchCounters counters) 
	throws Exception {
		super(dbSession, bbTestOpt, bbTestOpt.getPctShowSibs(), counters.getTotalTrxCounter());
		this.trxType = BbTestTrxType.SHOW_SIBS;
		this.trxCounter = counters.getSingleTrxCounter(trxType);
	}
	
	public void setBlogDBFetcher(BlogDBFetcher bf) {
		blogFetcher = bf;
	}

	private void bindParameter(long userId, long publishTime) throws SQLException {
		prepareStatementPre.setLong(1, publishTime);
		prepareStatementPre.setLong(2, userId);
		prepareStatementNxt.setLong(1, publishTime);
		prepareStatementNxt.setLong(2, userId);
	}

	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.transaction.BbTestTransaction#exeTrx(com.netease.webbench.blogbench.misc.ParameterGenerator)
	 */
	@Override
	public void doExeTrx(ParameterGenerator paraGen) 
	throws Exception {
		
		getSiblingsFromDb(paraGen);
		
		if (bbTestOpt.isUsedMemcached()) {
			long startTime = System.currentTimeMillis();
			MemcachedClientIF mcm = MemcachedManager.getInstance().getMajorMcc();
			if (idPre > 0) {
				LightBlog lightBlogPre = new LightBlog();
				boolean readSuc = lightBlogPre.readFromBytes((byte[])mcm.get("lblog:" + idPre));

				trxCounter.addMemOper(MemOperType.GET_BLOG, readSuc);
				
				if (!readSuc) {
					lightBlogPre = blogFetcher.getLightBlog(idPre, userId);
					if (lightBlogPre != null) {
						boolean hit = mcm.set("lblog:" + idPre, lightBlogPre.writeToBytes());
						trxCounter.addMemOper(MemOperType.SET_BLOG, hit);
					} else {
						System.out.println("Error: failed to fetch previous blog record from database(show siblings transaction)!");
					}
				}
			}
			if (idNext > 0) {
				LightBlog lightBlogNext = new LightBlog();
				boolean readSuc = lightBlogNext.readFromBytes((byte[])mcm.get("lblog:" + idNext));
				
				trxCounter.addMemOper(MemOperType.GET_BLOG, readSuc);
				
				if (!readSuc) {
					lightBlogNext = blogFetcher.getLightBlog(idNext, userId);
					if (lightBlogNext != null) {
						boolean hit = mcm.set("lblog:" + idNext, lightBlogNext.writeToBytes());
						trxCounter.addMemOper(MemOperType.SET_BLOG, hit);
					} else {
						throw new Exception("Error: failed to fetch blog record from database(show siblings transaction)!");
					}
				}
			}
			long stopTime = System.currentTimeMillis();
			timeWaste += stopTime - startTime;
		}
		totalTrxCounter.addTrx(timeWaste);
		trxCounter.addTrx(timeWaste);
		
		timeWaste = 0;		
		idPre = -1;
		idNext = -1;
	}
	
	public void getSiblingsFromDb(ParameterGenerator paraGen) throws SQLException {
		try {
			BlogInfoWithPub blogInfo = paraGen.getZipfRandomBlog();
			userId = blogInfo.getUId();			
			long publishTime = blogInfo.getPublishTime();
			
			bindParameter(userId, publishTime);

			long startTime = System.currentTimeMillis();
			ResultSet rsPre = dbSession.query(prepareStatementPre);

			if (rsPre.next()) {
				idPre = rsPre.getInt("ID");
			}
			rsPre.close();

			ResultSet rsNext = dbSession.query(prepareStatementNxt);
			if (rsNext.next()) {
				idNext = rsNext.getInt("ID");
			}
			rsNext.close();
			long stopTime = System.currentTimeMillis();
			timeWaste += (stopTime - startTime);
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
		
		SQLConfigure sqlConfig = SQLConfigure.getInstance(dbSession.getDbOpt().getDbType());
		String preSql = sqlConfig.getShowPreSiblingsSql(bbTestOpt.getTbName(), bbTestOpt.isUsedMemcached());
		prepareStatementPre = dbSession.createPreparedStatement(preSql);
		
		String nextSql = sqlConfig.getShowNextSiblingsSql(bbTestOpt.getTbName(), bbTestOpt.isUsedMemcached());
		prepareStatementNxt = dbSession.createPreparedStatement(nextSql);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.transaction.BbTestTransaction#cleanRes()
	 */
	public void cleanRes() throws SQLException {
		if (null != prepareStatementPre) {
			prepareStatementPre.close();
		}
		if (null != prepareStatementNxt) {
			prepareStatementNxt.close();
		}
	}
}

