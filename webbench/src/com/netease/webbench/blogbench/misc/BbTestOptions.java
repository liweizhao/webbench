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
package com.netease.webbench.blogbench.misc;

import com.netease.webbench.blogbench.operation.BlogbenchOperationType;


/**
 * blogbench test options
 * @author LI WEIZHAO
 */
public class BbTestOptions {
	public static enum ActionType { LOAD, RUN };
	
	/* name of test table */
	private String tbName;
	/* size of test table */
	private long tbSize;
	/* storage engine of test table, only for mysql */
	private String tbEngine;
	
	/* create table arguments for MySQL NTSE storage engine */
	private String ntseCreateTblArgs;
	
	/* number of insert threads to use when load data */
	private int loadThreads;
	
	/**
	 * Zipf distribution parameters when select blogs
	 */
	private int blgZipfPct;
	private int blgZipfRes;
	private int blgZipfPart;
	/**
	 * Zipf distribution parameters when organize blogs to user
	 */	
	private int userZipfPct;
	private int userZipfRes;
	private int userZipfPart;
	
	/* min length of blog title */
	private int minTtlSize;
	/* max length of blog title */
	private int maxTtlSize;	
	/* min length of blog abstract*/
	private int minAbsSize;
	/* max length of blog abstract */
	private int maxAbsSize;
	/* min length of blog content */
	private int minCntSize;
	/* max length of blog content */
	private int maxCntSize;
	/* average length of blog content */
	private int avgCntSize;
	/* number of run test threads */
	private int threads;
	/* max count of transactions */
	private long maxTran;
	/* max time of blogbench test */
	private long maxTime;
	
	/* if defer creating indexes */
	private boolean deferIndex;
	private boolean specifiedDeferIdx = false;
	
	/* if print debug information */
	private boolean debug;
	
	/* percentage of all transactions */
	private int pctListBlg;
	private int pctShowBlg;
	private int pctUpdateAccess;
	private int pctUpdateComment;
	private int pctShowSibs;
	private int pctPublishBlg;
	private int pctUpdateBlg;
	
	/* directory of test report */
	private String reportDir;
	
	/* if collect system information, only for Linux OS*/
	private boolean collectSysstat;
	
	/* if create extra large blogs */
	private boolean extraLargeBlog;
	
	/* period to print transaction throughput (unit is seconds)£¬default is 60*/
	private int printThoughputPeriod;
	
	private boolean useTwoTable;
	
	/* blogbench test operation */
	private BlogbenchOperationType operType;
	
	private boolean createTable;
	private boolean parallelDml;
	
	/**
	 * constructor
	 */
	public BbTestOptions() {
		tbName = "Blog";
		tbSize = 1000000;
		tbEngine = "innodb";
		ntseCreateTblArgs = "\"\"";
		blgZipfPct = 5;
		blgZipfRes = 95;
		blgZipfPart = 200;
		userZipfPct = 5;
		userZipfRes = 95;
		userZipfPart = 200;
		minTtlSize = 10;
		maxTtlSize = 30;
		minAbsSize = 10;
		maxAbsSize = 500;
		minCntSize = 20;
		maxCntSize = 20000;
		avgCntSize = 2000;
		threads = 100;
		maxTran = Long.MAX_VALUE;
		maxTime = Long.MAX_VALUE;
		deferIndex = false;
		
		
		pctListBlg = 30;
		pctShowBlg = 60;
		pctUpdateAccess = 60;
		pctUpdateComment = 10;
		pctShowSibs = 60;
		pctPublishBlg = 10;
		pctUpdateBlg = 2;
		
	   	reportDir = "./report/";
		
		collectSysstat=true;
		extraLargeBlog=true;
		operType = null;
		printThoughputPeriod = 60;

		loadThreads = 8;
		createTable = true;
		parallelDml = false;
	}
	
	public String getTbName() {
		return tbName;
	}
	public void setTbName(String tbName) {
		this.tbName = tbName;
	}
	public long getTbSize() {
		return tbSize;
	}
	
	public void setTbSize(long tbSize) {
		this.tbSize = tbSize;
	}	
	
	public int getAvgCntSize() {
		return avgCntSize;
	}
	public void setAvgCntSize(int avgCntSize) {
		this.avgCntSize = avgCntSize;
	}
	public int getBlgZipfPart() {
		return blgZipfPart;
	}
	public void setBlgZipfPart(int blgZipfPart) {
		this.blgZipfPart = blgZipfPart;
	}
	public int getBlgZipfPct() {
		return blgZipfPct;
	}
	public void setBlgZipfPct(int blgZipfPct) {
		this.blgZipfPct = blgZipfPct;
	}
	public int getBlgZipfRes() {
		return blgZipfRes;
	}
	public void setBlgZipfRes(int blgZipfRes) {
		this.blgZipfRes = blgZipfRes;
	}
	public boolean isDeferIndex() {
		return deferIndex;
	}
	public void setDeferIndex(boolean deferIndex) {
		specifiedDeferIdx = true;
		this.deferIndex = deferIndex;
	}
	
	/**
	 * if defer creating index is set in arguments
	 * @return
	 */
	public boolean specifiedDeferIdx() {
		return specifiedDeferIdx;
	}
	
	public int getMaxAbsSize() {
		return maxAbsSize;
	}
	public void setMaxAbsSize(int maxAbsSize) {
		this.maxAbsSize = maxAbsSize;
	}
	public int getMaxCntSize() {
		return maxCntSize;
	}
	public void setMaxCntSize(int maxCntSize) {
		this.maxCntSize = maxCntSize;
	}
	public long getMaxTime() {
		return maxTime;
	}
	public void setMaxTime(long maxTime) {
		this.maxTime = maxTime;
	}
	public long getMaxTran() {
		return maxTran;
	}
	public void setMaxTran(long maxTran) {
		this.maxTran = maxTran;
	}
	public int getMaxTtlSize() {
		return maxTtlSize;
	}
	public void setMaxTtlSize(int maxTtlSize) {
		this.maxTtlSize = maxTtlSize;
	}
	public int getMinAbsSize() {
		return minAbsSize;
	}
	public void setMinAbsSize(int minAbsSize) {
		this.minAbsSize = minAbsSize;
	}
	public int getMinCntSize() {
		return minCntSize;
	}
	public void setMinCntSize(int minCntSize) {
		this.minCntSize = minCntSize;
	}
	public int getMinTtlSize() {
		return minTtlSize;
	}
	public void setMinTtlSize(int minTtlSize) {
		this.minTtlSize = minTtlSize;
	}
	public String getNtseCreateTblArgs() {
		return ntseCreateTblArgs;
	}
	public void setNtseCreateTblArgs(String ntseCreateTblArgs) {
		this.ntseCreateTblArgs = ntseCreateTblArgs;
	}
	public String getTbEngine() {
		return tbEngine;
	}
	public void setTbEngine(String tbEngine) {
		this.tbEngine = tbEngine;
	}
	public int getThreads() {
		return threads;
	}
	public void setThreads(int threads) {
		this.threads = threads;
	}
	public int getUserZipfPart() {
		return userZipfPart;
	}
	public void setUserZipfPart(int userZipfPart) {
		this.userZipfPart = userZipfPart;
	}
	public int getUserZipfPct() {
		return userZipfPct;
	}
	public void setUserZipfPct(int userZipfPct) {
		this.userZipfPct = userZipfPct;
	}
	public int getUserZipfRes() {
		return userZipfRes;
	}
	public void setUserZipfRes(int userZipfRes) {
		this.userZipfRes = userZipfRes;
	}

	public int getPctListBlg() {
		return pctListBlg;
	}

	public void setPctListBlg(int pctListBlg) {
		this.pctListBlg = pctListBlg;
	}

	public int getPctPublishBlg() {
		return pctPublishBlg;
	}

	public void setPctPublishBlg(int pctPublishBlg) {
		this.pctPublishBlg = pctPublishBlg;
	}

	public int getPctShowBlg() {
		return pctShowBlg;
	}

	public void setPctShowBlg(int pctShowBlg) {
		this.pctShowBlg = pctShowBlg;
	}

	public int getPctShowSibs() {
		return pctShowSibs;
	}

	public void setPctShowSibs(int pctShowSibs) {
		this.pctShowSibs = pctShowSibs;
	}

	public int getPctUpdateAccess() {
		return pctUpdateAccess;
	}

	public void setPctUpdateAccess(int pctUpdateAccess) {
		this.pctUpdateAccess = pctUpdateAccess;
	}

	public int getPctUpdateBlg() {
		return pctUpdateBlg;
	}

	public void setPctUpdateBlg(int pctUpdateBlg) {
		this.pctUpdateBlg = pctUpdateBlg;
	}

	public int getPctUpdateComment() {
		return pctUpdateComment;
	}

	public void setPctUpdateComment(int pctUpdateComment) {
		this.pctUpdateComment = pctUpdateComment;
	}

	public BlogbenchOperationType getOperType() {
		return operType;
	}

	public void setOperType(BlogbenchOperationType operType) {
		this.operType = operType;
	}

	public String getReportDir() {
		return reportDir;
	}

	public void setReportDir(String outputDir) {
		this.reportDir = outputDir;
	}

	public boolean isCollectSysstat() {
		return collectSysstat;
	}

	public void setCollectSysstat(boolean collectSysstat) {
		this.collectSysstat = collectSysstat;
	}

	public boolean isExtraLargeBlog() {
		return extraLargeBlog;
	}

	public void setExtraLargeBlog(boolean extraLargeBlog) {
		this.extraLargeBlog = extraLargeBlog;
	}

	public int getPrintThoughputPeriod() {
		return printThoughputPeriod;
	}

	public void setPrintThoughputPeriod(int printThoughputPeriod) {
		this.printThoughputPeriod = printThoughputPeriod;
	}


	
	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public int getLoadThreads() {
		return loadThreads;
	}

	public void setLoadThreads(int loadThreads) {
		this.loadThreads = loadThreads;
	}

	public boolean getUseTwoTable() {
		return useTwoTable;
	}

	public void setUseTwoTable(boolean useTwoTable) {
		this.useTwoTable = useTwoTable;
	}

	public boolean isCreateTable() {
		return createTable;
	}

	public void setCreateTable(boolean createTable) {
		this.createTable = createTable;
	}

	public boolean isParallelDml() {
		return parallelDml;
	}

	public void setParallelDml(boolean parallelDml) {
		this.parallelDml = parallelDml;
	}
}
