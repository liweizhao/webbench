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
package com.netease.webbench.blogbench.memcached;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.netease.webbench.blogbench.blog.BlogInfoWithAcs;
import com.netease.webbench.blogbench.blog.LightBlog;
import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.blogbench.sql.SQLConfigure;
import com.netease.webbench.blogbench.sql.SQLConfigureFactory;
import com.netease.webbench.blogbench.statis.AcsCntFlushTaskStatis;
import com.netease.webbench.common.DbOptions;
import com.netease.webbench.common.DbSession;

/**
 * 
 * Task for flushing access count data
 * @author LI WEIZHAO
 */
public class AcsCntFlushTask extends Thread {
	public static final long DEFAULT_FLUSH_INTERVAL = 300000;
	
	/* database connection */
	private DbSession dbSession;
	
	/* prepared statement used to flush access count data to database */
	private PreparedStatement prepareStatementSet;
		
	/* times of dead lock happens */
	private int faildTimes = 0;

	/* if task has been cancelled */
	private boolean cancel = false;	
	/* if is doing flushing task */
	private boolean isFlushing = false;
	
	/* interval for each task period */
	private long interval;

	private ReentrantLock shouldWorkLock = new ReentrantLock();
	private Condition shouldWork = shouldWorkLock.newCondition();
	
	private GlobalAcsCntCache globalAcsCache;
	private AcsCntFlushTaskStatis flushTaskStatis;
	
	/**
	 * constructor
	 * @param dbOpt
	 * @param bbTestOpt
	 * @throws Exception
	 */
	public AcsCntFlushTask(AcsCntFlushTaskStatis flushTaskStatis, GlobalAcsCntCache globalAcsCache, 
			DbOptions dbOpt, BbTestOptions bbTestOpt) throws Exception {
		this(flushTaskStatis, globalAcsCache, dbOpt, bbTestOpt, DEFAULT_FLUSH_INTERVAL);
	}
	
	/**
	 * 
	 * @param dbOpt
	 * @param bbTestOpt
	 * @param interval
	 * @param updateAcsCacheSize
	 * @throws Exception
	 */
	public AcsCntFlushTask(AcsCntFlushTaskStatis flushTaskStatis, GlobalAcsCntCache globalAcsCache, 
			DbOptions dbOpt, BbTestOptions bbTestOpt, long interval) throws Exception {
		this.dbSession = new DbSession(dbOpt);
		this.globalAcsCache = globalAcsCache;
		this.flushTaskStatis = flushTaskStatis;
		
		SQLConfigure sqlConfig = SQLConfigureFactory.getSQLConfigure();
		String sql = sqlConfig.getUpdateAccessSql(bbTestOpt.getTbName(),  bbTestOpt.isUsedMemcached());
		prepareStatementSet = this.dbSession.createPreparedStatement(sql);
			
		this.interval = interval;
	}
	
	/**
	 * sleep some time
	 * @param sleepTime  sleep time (milliseconds)
	 * @throws InterruptedException
	 */
	private void mySleepSomeTime(long sleepTime) throws InterruptedException {
		shouldWorkLock.lock();
		try {
			shouldWork.await(sleepTime, TimeUnit.MILLISECONDS);
		} finally {
			shouldWorkLock.unlock();
		}
	}
	
	/**
	 * wake up task thread if it's sleeping
	 */
	public void myWakeUp() {
		shouldWorkLock.lock();
		try {
			shouldWork.signal();
		} finally {
			shouldWorkLock.unlock();
		}
	}
	
	/**
	 * @SEE {@link Thread#run()}
	 */
	public void run() {
		try {
			while (!cancel) {
				/* if all of the maps are empty, then sleep for some time */
				if (globalAcsCache.getTotalMapSize() == 0) {
					mySleepSomeTime(interval);
				}
				flush();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * cancel task
	 * @throws InterruptedException
	 */
	public void cancel() throws InterruptedException {
		cancel = true;
		while(isFlushing) {/* if flushing task is being done, then wait for it to finish */
			Thread.sleep(100);
		}
	}
	
	/**
	 * find a map to flush
	 */
	private void flush() throws Exception {
		isFlushing = true;
		
		Map<Long, BlogInfoWithAcs> currentAcsCountMap = globalAcsCache.getNextMapToFlush();
		if (currentAcsCountMap != null) {
			flushTaskStatis.addToTotalFlush(currentAcsCountMap.size());
			doFlushMap(currentAcsCountMap);
		}

		isFlushing = false;
	}
	
	/**
	 * do flushing specified maps
	 * @param readOnlyMap map to be flushed
	 * @return how many records have been flushed
	 */
	private int doFlushMap(Map<Long, BlogInfoWithAcs> readOnlyMap) {
		int flushCnt = 0;

		try {
			/* sorting first */
			Set<Long> keySet = readOnlyMap.keySet();
			Long[] keyArr = keySet.toArray(new Long[keySet.size()]);
			Arrays.sort(keyArr);

			for (int i = 0; i < keyArr.length && !cancel; i++) {
				Long key = keyArr[i];
				BlogInfoWithAcs blogInfo = readOnlyMap.get(key);
				flushCnt++;
				updateToDb(blogInfo);
				globalAcsCache.decrTotalMapSize();

				/* update access count information in memcached server */
				MemcachedClientIF mc = MemcachedManager.getInstance().getMajorMcc();
				LightBlog lightBlog = new LightBlog();
				boolean readSuc = lightBlog.readFromBytes((byte[]) mc.get("lblog:"
						+ blogInfo.getBlogId()));
				flushTaskStatis.addMemGetBlogOper(readSuc);

				if (readSuc && blogInfo.getBlogAcs() > lightBlog.getAccessCount()) {
					lightBlog.setAccessCount(blogInfo.getBlogAcs());
					boolean rpcSuc = mc.replace("lblog:" + blogInfo.getBlogId(), lightBlog.writeToBytes());
					flushTaskStatis.addMemReplaceBlogOper(rpcSuc);
				}

			}
		} catch (SQLException e) {
			if (e.getErrorCode() == 1205) {
				faildTimes++;// dead lock occurred
			} else {
				System.err.println("Faild to flush access count cached to database!");
				System.err.println("Error number:" + e.getErrorCode()
						+ "; Message:" + e.getMessage());
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (faildTimes != 0) {
			System.err.println("DeadLock occured when flushing access count to database, total failed times: "
					+ faildTimes);
		}

		return flushCnt;
	}
		
	/**
	 * flush data to database
	 * @param blogAcs  blog information with access count 
	 * @throws SQLException
	 */
	private void updateToDb(BlogInfoWithAcs blogAcs) throws Exception {
		if (!dbSession.isClosed()) {
			prepareStatementSet.setInt(1, blogAcs.getBlogAcs());
			prepareStatementSet.setLong(2, blogAcs.getUId());
			prepareStatementSet.setLong(3, blogAcs.getBlogId());
			prepareStatementSet.setInt(4, blogAcs.getBlogAcs());
			if (dbSession.update(prepareStatementSet) != 1) {
			} 
		}
	}
}
