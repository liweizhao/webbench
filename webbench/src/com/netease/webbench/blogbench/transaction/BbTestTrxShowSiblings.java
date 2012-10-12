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
import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.blogbench.misc.ParameterGenerator;
import com.netease.webbench.blogbench.sql.SQLConfigure;
import com.netease.webbench.blogbench.sql.SQLConfigureFactory;
import com.netease.webbench.blogbench.statis.BlogbenchCounters;
import com.netease.webbench.common.DbSession;

/**
 * show siblings transaction
 * @author LI WEIZHAO
 */
public class BbTestTrxShowSiblings extends BbTestTransaction {
	protected PreparedStatement prepareStatementPre;
	protected PreparedStatement prepareStatementNxt;
	
	public BbTestTrxShowSiblings(DbSession dbSession, BbTestOptions bbTestOpt, BlogbenchCounters counters) 
	throws Exception {
		super(dbSession, bbTestOpt, bbTestOpt.getPctShowSibs(), 
				BbTestTrxType.SHOW_SIBS, counters);
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
		BlogInfoWithPub blogInfo = paraGen.getZipfRandomBlog();
		getSiblingsFromDb(blogInfo, paraGen);
	}	
		
	public int[] getSiblingsFromDb(BlogInfoWithPub blogInfo, 
			ParameterGenerator paraGen) throws SQLException {
		try {			
			long userId = blogInfo.getUId();			
			long publishTime = blogInfo.getPublishTime();
			
			bindParameter(userId, publishTime);

			ResultSet rsPre = dbSession.query(prepareStatementPre);

			int [] sibings = new int[2];
			
			if (rsPre.next()) {
				sibings[0] = rsPre.getInt("ID");
			}
			rsPre.close();

			ResultSet rsNext = dbSession.query(prepareStatementNxt);
			if (rsNext.next()) {
				sibings[1] = rsNext.getInt("ID");
			}
			rsNext.close();
			
			return sibings;
		} catch (SQLException e) {
			myTrxCounter.incrFailedTimes();
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
		
		SQLConfigure sqlConfig = SQLConfigureFactory.getSQLConfigure();
		String preSql = sqlConfig.getShowPreSiblingsSql(bbTestOpt.getTbName());
		prepareStatementPre = dbSession.createPreparedStatement(preSql);
		
		String nextSql = sqlConfig.getShowNextSiblingsSql(bbTestOpt.getTbName());
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

