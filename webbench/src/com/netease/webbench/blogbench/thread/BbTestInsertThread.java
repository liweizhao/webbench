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
package com.netease.webbench.blogbench.thread;

import java.util.ArrayList;
import java.util.List;

import com.netease.webbench.blogbench.dao.BlogDAO;
import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.blogbench.model.Blog;
import com.netease.webbench.common.DbOptions;

/**
 * blogbench insert thread
 * @author LI WEIZHAO
 */
public class BbTestInsertThread extends BbTestThread {
	/* number of rows in a multi-insert statement,  to set this you should check the package size that database server can receive */
	public static final int MULTI_INSERT_ROWS = 500;
	
	/* asynchronous blog record producer */
	private BlgRecordProducer blogRcdProducer;
	
	/* number of rows per insert thread need to insert*/
	private final long rcdCntToInsert;
	
	/* number of rows that has been inserted */
	private long recordInserted;
	
	public BbTestInsertThread(DbOptions dbOpt, BbTestOptions bbTestOpt, 
			long rcdCntToInsert, ThreadBarrier barrier, 
			BlgRecordProducer producer, BlogDAO blogDao) throws Exception {
		super(barrier, dbOpt, bbTestOpt, blogDao);
		this.rcdCntToInsert = rcdCntToInsert;
		this.recordInserted = 0;
		this.blogRcdProducer = producer;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		try {
			if (barrier != null) {
				myWait();
			}
			doLoadData();
		} catch (Exception e) {
			exitErrorCode = 1;
			e.printStackTrace();
		}
	}
	
	/**
	 * insert records into test table
	 *  
	 * @throws Exception 
	 */
	protected void doLoadData() throws Exception {	
		long mutiInsertTimes = rcdCntToInsert / MULTI_INSERT_ROWS;
		long lastInsertRows = rcdCntToInsert % MULTI_INSERT_ROWS;
		
		List<Blog> blogList = new ArrayList<Blog>(MULTI_INSERT_ROWS);
		for  (int i = 0; i < mutiInsertTimes; i++) {
			for (int j = 0; j < MULTI_INSERT_ROWS; j++) {
				blogList.add(blogRcdProducer.getBlog());
				recordInserted++;
			}
			blogDao.batchInsert(blogList);
			blogList.clear();
		}
		
		if (lastInsertRows != 0) {
			blogList = new ArrayList<Blog>((int)lastInsertRows);
			for (int j = 0; j < lastInsertRows; j++) {
				blogList.add(blogRcdProducer.getBlog());
				recordInserted++;
			}
			blogDao.batchInsert(blogList);
			blogList.clear();
		}
	}
	
	protected void doMultiInsert() throws Exception {		
//		if (bbTestOpt.getUseTwoTable()) 
//			doMultiInsertTwoTable();
//		else 
//			doMultiInsertOneTable();
	}
		
	/**
	 * do multi-insert
	 * 
	 * @pre database server must support multi-insert
	 * @param dbSession database connection
	 * @param paraGen query parameter generator
	 */
//	private void doMultiInsertOneTable() throws Exception {		
//		long mutiInsertTimes = rcdCntToInsert / MULTI_INSERT_ROWS;
//		long lastInsertRows = rcdCntToInsert % MULTI_INSERT_ROWS;
//		
//		long insertParamPair[][] = { 
//				{ mutiInsertTimes,  MULTI_INSERT_ROWS}, 
//				{ 1, lastInsertRows}
//		};
//		
//		SQLConfigure sqlConfig = SQLConfigureFactory.getSQLConfigure(dbOpt.getDbType());
//		for (int i = 0; i < 2; i++) {
//			PreparedStatement ps;
//			long insertTime = insertParamPair[i][0];
//			long insertRows = insertParamPair[i][1];
//			
//			if (insertTime > 0 && insertRows > 0) {
//				//prepare the multi-insert SQL statement
//				String sql = sqlConfig.getMultiInsertBlogSql(bbTestOpt.getTbName(), insertRows, false);
//				ps = dbSession.createPreparedStatement(sql);
//				
//				for (int j = 0; j < insertTime; j++) {
//					doInsertOneTable(insertRows, ps);
//				}
//				ps.close();
//			}
//		}
//	}
//	
//	private void doMultiInsertTwoTable() throws Exception {
//		long mutiInsertTimes = rcdCntToInsert / MULTI_INSERT_ROWS;
//		long lastInsertRows = rcdCntToInsert % MULTI_INSERT_ROWS;
//		
//		long insertParamPair[][] = { 
//				{ mutiInsertTimes,  MULTI_INSERT_ROWS}, 
//				{ 1, lastInsertRows}
//		};
//		
//		String testTableName = bbTestOpt.getTbName();
//		String testContentTableName = Portable.getBlogContentTableName(testTableName);
//		
//		SQLConfigure sqlConfig = SQLConfigureFactory.getSQLConfigure(dbOpt.getDbType());
//		for (int i = 0; i < 2; i++) {			
//			PreparedStatement prepareStatement;	
//			PreparedStatement prepareStatement2;
//			
//			long insertTime = insertParamPair[i][0];
//			long insertRows = insertParamPair[i][1];
//			
//			if (insertTime > 0 && insertRows > 0) {
//				//prepare the multi-insert SQL statement
//				String sql = sqlConfig.getMultiInsertBlogSql(testTableName, insertRows, true);
//				prepareStatement = dbSession.createPreparedStatement(sql);
//				
//				String sql2 = sqlConfig.getMultiInsertContentSql(testContentTableName, insertRows);
//				prepareStatement2 = dbSession.createPreparedStatement(sql2);
//				
//				for (int j = 0; j < insertTime; j++) {
//					doInsertTwoTable(insertRows, prepareStatement, prepareStatement2);
//				}
//				prepareStatement.close();
//				prepareStatement2.close();
//			}			
//		}
//	}
//	
//	private void doInsertOneTable(long insertRows, PreparedStatement prepareStatement) throws Exception {
//		final int trxNum = BbTestTrxType.TRX_TYPE_NUM;
//		for (int j = 0; j < insertRows; j++) {
//			Blog blog = blogRcdProducer.getBlog();
//			prepareStatement.setLong(1 + j * trxNum, blog.getId());
//			prepareStatement.setLong(2 + j * trxNum, blog.getUid());
//			prepareStatement.setString(3 + j * trxNum, blog.getTitle());
//			prepareStatement.setString(4 + j * trxNum, blog.getAbs());		
//			prepareStatement.setString(5 + j * trxNum, blog.getCnt());
//			prepareStatement.setInt(6 + j * trxNum, blog.getAllowView());
//			prepareStatement.setLong(7 + j * trxNum, blog.getPublishTime());	
//		}			
//		
//		if (dbSession.update(prepareStatement) != insertRows) {
//			prepareStatement.close();
//			dbSession.close();
//			throw new Exception("Error occured when insert records!");
//		}				
//		recordInserted += insertRows;
//	}
//	
//	private void doInsertTwoTable(long insertRows, PreparedStatement prepareStatement, 
//			PreparedStatement prepareStatement2) throws Exception {
//		for (int j = 0; j < insertRows; j++) {
//			Blog blog = blogRcdProducer.getBlog();
//			prepareStatement.setLong(1 + j * 6, blog.getId());
//			prepareStatement.setLong(2 + j * 6, blog.getUid());
//			prepareStatement.setString(3 + j * 6, blog.getTitle());
//			prepareStatement.setString(4 + j * 6, blog.getAbs());			
//			prepareStatement.setInt(5 + j * 6, blog.getAllowView());
//			prepareStatement.setLong(6 + j * 6, blog.getPublishTime());
//			
//			prepareStatement2.setLong(1 + j * 3, blog.getId());
//			prepareStatement2.setLong(2 + j * 3, blog.getUid());
//			prepareStatement2.setString(3 + j * 3, blog.getCnt());
//		}			
//		try {
//		if (!(dbSession.update(prepareStatement) == insertRows  && 
//				dbSession.update(prepareStatement2) == insertRows)) {
//			prepareStatement.close();
//			prepareStatement2.close();
//			dbSession.close();
//			throw new Exception("Error occured when insert records!");
//		}			
//		} catch (Exception e) {
//			throw e;
//		}
//		recordInserted += insertRows;
//	}
	
	public long getRecordInserted() {
		return recordInserted;
	}
}
