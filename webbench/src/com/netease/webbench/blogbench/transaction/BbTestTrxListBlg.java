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

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.netease.webbench.blogbench.blog.BlogIdPair;
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
 * list blogs transaction
 * @author LI WEIZHAO
 */
public class BbTestTrxListBlg extends BbTestTransaction {
	public static int QUERY_LIMIT_SIZE = 10;
	
	protected PreparedStatement prepareStatement;/* prepared SQL statement to execute this transaction */
	protected PreparedStatement []multiGetBlogPs;
	protected long timeWaste = 0;
	protected BlogbenchTrxCounter trxCounter;

	public BbTestTrxListBlg(DbSession dbSession, BbTestOptions bbTestOpt, 
			BlogbenchCounters counters) throws Exception {
		super(dbSession, bbTestOpt, bbTestOpt.getPctListBlg(), counters.getTotalTrxCounter());
		this.trxType = BbTestTrxType.LIST_BLGS;
		this.trxCounter = counters.getSingleTrxCounter(trxType);
		multiGetBlogPs = new PreparedStatement[QUERY_LIMIT_SIZE];
	}

	private void bindParameter(long uid) throws SQLException{
		prepareStatement.setLong(1, uid);
	}

	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.transaction.BbTestTransaction#exeTrx(com.netease.webbench.blogbench.misc.ParameterGenerator)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void doExeTrx(ParameterGenerator paraGen) 	throws Exception {
		long uId = paraGen.getZipfUserId();
		
		long startTime = System.currentTimeMillis();
		if (bbTestOpt.isUsedMemcached()) {
			MemcachedClientIF mcc = MemcachedManager.getInstance().getMajorMcc();
			
			ArrayList<Long> blogsList = (ArrayList<Long>)(Serializable)mcc.get("blog:ids:" + uId);

			if (blogsList == null) {
				trxCounter.addMemOper(MemOperType.GET_LIST, false);
				blogsList = getListFromDb(dbSession, uId);
				boolean isSetSuc = mcc.set("blog:ids:" + uId, (Serializable)blogsList);
				trxCounter.addMemOper(MemOperType.SET_LIST, isSetSuc);
			} else {
				trxCounter.addMemOper(MemOperType.GET_LIST, true);
			}

			//may be some users don't have any blogs
			if (blogsList != null && blogsList.size() > 0) {

				//now we have got blog id list, will query memcached first
				ArrayList<String> keyList = new ArrayList<String>(blogsList.size());
				for (Long blogId : blogsList) {
					keyList.add("lblog:" + blogId);
				}

				//execute multi-get in memcached
				Map<String, Object> memBlogItems = mcc.getMulti(keyList);

				if (memBlogItems != null) {
					List<BlogIdPair> blogIdPairList = new ArrayList<BlogIdPair>();
					for (Long blogId : blogsList) {

						byte[] blogSerialData = (byte[]) memBlogItems.get("lblog:"
								+ blogId);

						trxCounter.addMemOper(MemOperType.GET_BLOG, blogSerialData == null ? false
								: true);
						
						if (blogSerialData == null)
							blogIdPairList.add(new BlogIdPair(blogId, uId));
					}

					//fetch blog records from database
					List<LightBlog> lightBlogList = multiGetLightBlogFromDb(blogIdPairList);

					//put records to memcached
					if (lightBlogList != null) {
						if (lightBlogList.size() > 0) {
							for (LightBlog blogFromDb : lightBlogList) {
								boolean setSuc = mcc.set("lblog:" + blogFromDb.getId(), blogFromDb.writeToBytes());
								trxCounter.addMemOper(MemOperType.SET_BLOG, setSuc);
							}
						}
					} else {
						throw new Exception("BlogIdPairList is null");
					}
				} else {
					throw new Exception("Fatal error occured when do multi get from memcached!");
				}
			} 
		} else {
			getListFromDb(dbSession, uId);
 		}
		long stopTime = System.currentTimeMillis();
		timeWaste += stopTime - startTime;
		totalTrxCounter.addTrx(timeWaste);
		trxCounter.addTrx(timeWaste);
		timeWaste = 0;
	}
	
	public ArrayList<Long> getListFromDb(DbSession dbSession, long uId) throws SQLException {
		try {
			long start = System.currentTimeMillis();
			bindParameter(uId);
			long stop = System.currentTimeMillis();
			timeWaste -= (stop - start);
			
			ResultSet rs = dbSession.query(prepareStatement);
						
			ArrayList<Long> resultList = new ArrayList<Long>();
			while (rs.next()) {
				resultList.add(rs.getLong("ID"));
			}
			
			rs.close();
			return resultList;
		} catch (SQLException e) {
			trxCounter.incrFailedTimes();
			throw e;
		}
	}
	
	/**
	 * fetch all light blogs of specified ids from database
	 * @param blogIdPairList the blog id list of blogs to fetch
	 * @return light blogs list
	 * @throws Exception
	 */
	private List<LightBlog> multiGetLightBlogFromDb(List<BlogIdPair> blogIdPairList) throws SQLException {
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
		SQLConfigure sqlConfig = SQLConfigure.getInstance(dbSession.getDbOpt().getDbType());
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
