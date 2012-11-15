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

import java.sql.SQLException;

import com.netease.webbench.blogbench.dao.BlogDAO;
import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.blogbench.misc.ParameterGenerator;
import com.netease.webbench.blogbench.model.Blog;
import com.netease.webbench.blogbench.statis.BlogbenchCounters;
/**
 * publish blog transaction 
 * @author LI WEIZHAO
 */
public class BbTestTrxPublishBlg extends BbTestTransaction {
	
	/**
	 * constructor
	 * @param dbSession
	 */
	public BbTestTrxPublishBlg(BlogDAO blogDao, BbTestOptions bbTestOpt) 
			throws Exception {
		super(blogDao, bbTestOpt, 1, BbTestTrxType.PUBLISH_BLG, null);
	}
	
	/**
	 * constructor
	 * @param dbSession
	 * @param pct
	 * @param totalTrxCounter
	 * @param trxCounter
	 */
	public BbTestTrxPublishBlg(BlogDAO blogDao, BbTestOptions bbTestOpt, 
			BlogbenchCounters counters) {
		super(blogDao, bbTestOpt, bbTestOpt.getPctPublishBlg(), 
				BbTestTrxType.PUBLISH_BLG, counters);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.transaction.BbTestTransaction#exeTrx(com.netease.webbench.blogbench.misc.ParameterGenerator)
	 */
	@Override
	public void doExecTrx(ParameterGenerator paraGen) throws Exception {
		try {
			Blog blog = paraGen.generateNewBlog();
			
			if (1 == blogDAO.insertBlog(blog)) {				
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
//	public void batchExec(DbSession dbSession, ParameterGenerator paraGen, 
//			Blog blogs[]) throws Exception {
//		if (!dbSession.getAutoCommit()) 
//			dbSession.setAutoCommit(false);
//		for (int i = 0; i < blogs.length; i++) {
//			bindParameter(blogs[i]);
//			ps.addBatch();
//		}
//		ps.executeBatch();
//		dbSession.commit();
//	}
}
