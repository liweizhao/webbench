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
import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.blogbench.misc.ParameterGenerator;
import com.netease.webbench.blogbench.sql.SQLConfigure;
import com.netease.webbench.blogbench.sql.SQLConfigureFactory;
import com.netease.webbench.blogbench.statis.BlogbenchCounters;
import com.netease.webbench.common.DbSession;
/**
 * update comment transaction
 * @author LI WEIZHAO
 */
public class BbTestTrxUpdateCmt extends BbTestTransaction {
	protected PreparedStatement prepareStatement;/* prepared SQL statement to execute this transaction */
	
	public BbTestTrxUpdateCmt(DbSession dbSession, BbTestOptions bbTestOpt, 
			BlogbenchCounters counters) throws Exception {
		super(dbSession, bbTestOpt, bbTestOpt.getPctUpdateComment(), 
				BbTestTrxType.UPDATE_CMT, counters);
	}
	
	private void bindParameter(long blogId, long uId) throws SQLException {
		prepareStatement.setLong(1, uId);
		prepareStatement.setLong(2, blogId);
	}


	/* 
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.transaction.BbTestTransaction#exeTrx(com.netease.webbench.blogbench.misc.ParameterGenerator)
	 */
	@Override
	public void doExeTrx(ParameterGenerator paraGen) throws Exception {
		BlogInfoWithPub blogInfo = paraGen.getZipfRandomBlog();
		long blogId = blogInfo.getBlogId();
		long uId = blogInfo.getUId();
		
		updateComment(blogId, uId);		
	}
	
	public void updateComment(long blogId, long uId) throws Exception {
		try {
			bindParameter(blogId, uId);
			if (1 != dbSession.update(prepareStatement)) {
				myTrxCounter.incrFailedTimes();
			}
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
		if (bbTestOpt.isParallelDml()) {
			dbSession.setParallelDML(true);
		}
		
		SQLConfigure sqlConfig = SQLConfigureFactory.getSQLConfigure(dbOpt.getDbType());;
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
	}
}
