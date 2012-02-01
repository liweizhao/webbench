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

import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.blogbench.statis.AcsCntFlushTaskStatis;
import com.netease.webbench.blogbench.statis.UpdateAccessStatis;
import com.netease.webbench.common.DbOptions;

/**
 * Access count flush task manager
 * @author LI WEIZHAO
 */
public class AcsCntFlushManager {
	/** access count flushing manager instance */
	private static AcsCntFlushManager instance = new AcsCntFlushManager();
	
	/** number of flushing tasks */
	private int flushTaskNum;
	/** access count flushing tasks array */ 
	private AcsCntFlushTask [] acsFlushTimerTasks;
	/** global access count cache */
	private GlobalAcsCntCache globalAcsCntCache = null;		
	/** statistic information of access count flushing task */
	private AcsCntFlushTaskStatis acsCntFlushTaskStatis = null;
	/** blogbench test options */
	private BbTestOptions bbTestOpt;
	
	private AcsCntFlushManager() {} 
	
	/**
	 * get access count flush manager instance
	 * @return
	 */
	public static AcsCntFlushManager getInstance() {
		return instance;
	}
	
	/**
	 * initialise access count cache and flushing tasks
	 * @param bbTestOpt
	 * @param dbOpt
	 * @param flushTaskNum
	 * @throws Exception
	 */
	public void init(BbTestOptions bbTestOpt, DbOptions dbOpt, int flushTaskNum) throws Exception {
		if (!bbTestOpt.isUsedMemcached())
			throw new Exception();
		
		this.bbTestOpt = bbTestOpt;
		this.flushTaskNum = flushTaskNum;
		
		globalAcsCntCache = new GlobalAcsCntCache(bbTestOpt.getAcsCountTrxCacheSize());
		acsCntFlushTaskStatis = new AcsCntFlushTaskStatis();
		acsFlushTimerTasks = new AcsCntFlushTask[flushTaskNum];	
		
		for (int i = 0; i < flushTaskNum; i++) {
			acsFlushTimerTasks[i] = new AcsCntFlushTask(acsCntFlushTaskStatis, globalAcsCntCache, dbOpt, bbTestOpt, 
					bbTestOpt.getFlushAcsCountInterval());
		}
	}
	
	/**
	 *  get global access count cache
	 * @return
	 */
	public GlobalAcsCntCache getGlobalAcsCntCache() {
		if (bbTestOpt == null || !bbTestOpt.isUsedMemcached())
			return null;
		else 
			return globalAcsCntCache;
	}
	
	
	/**
	 * get statistic information of access count flush task
	 * @return
	 */
	public AcsCntFlushTaskStatis getAcsCntFlushTaskStatis() {
		return acsCntFlushTaskStatis;
	}
	
	/**
	 * get update access statistic information
	 * @return
	 */
	public UpdateAccessStatis getUpdateAccessStatistic() {
		if (globalAcsCntCache == null)
			return null;
		else
			return globalAcsCntCache.getUpdateAccessStatistic();
	}
	
	/**
	 * start all flush tasks
	 * @throws Exception
	 */
	public void startAllFlushTask() throws Exception {
		if (!bbTestOpt.isUsedMemcached()) {
			throw new Exception("The command line option isn't specified to use memcached!");
		} 
		for (int i = 0; i < flushTaskNum; i++) {
			if (acsFlushTimerTasks[i] == null)
				throw new Exception("Fatal error: the " + i + "th flushing thread is null!");
			acsFlushTimerTasks[i].start();
		}
	}
	
	/**
	 * cancel all flush tasks 
	 * @throws InterruptedException
	 */
	public void cancelAllFlushTask() throws Exception {
		if (!bbTestOpt.isUsedMemcached())
			throw new Exception("There isn't any access count flushing thread is running!");
		for (int i = 0; i < flushTaskNum; i++) {
			if (acsFlushTimerTasks[i] != null) {
				acsFlushTimerTasks[i].cancel();
			}	
		}
		for (int i = 0; i < flushTaskNum; i++)
			acsFlushTimerTasks[i].join();
	}
}
