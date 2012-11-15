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
package com.netease.webbench.blogbench.operation;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import sun.misc.Signal;

import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.blogbench.misc.ParameterGenerator;
import com.netease.webbench.blogbench.statis.BbPeriodSummaryTaskHandler;
import com.netease.webbench.blogbench.statis.BlogbenchCounters;
import com.netease.webbench.blogbench.thread.BbTestRunThread;
import com.netease.webbench.blogbench.thread.ThreadBarrier;
import com.netease.webbench.blogbench.thread.ThreadRunFlagTimer;
import com.netease.webbench.blogbench.transaction.BbTestTrxType;
import com.netease.webbench.common.DbOptions;
import com.netease.webbench.common.Util;
import com.netease.webbench.statis.CreateChartHandler;
import com.netease.webbench.statis.CreateTableHandler;
import com.netease.webbench.statis.PeriodSummaryTask;
import com.netease.webbench.statis.RunTimeInfoCollector;
import com.netease.webbench.statis.TestResultExporter;
import com.netease.webbench.statis.TrxCounter;

/**
 * blogbench run operation
 * @author LI WEIZHAO
 *
 */
@SuppressWarnings("restriction")
public class BlogbenchRunOperation extends BlogbenchOperation {
	private BlogbenchCounters blogbenchCounters;
	
	private BbPeriodSummaryTaskHandler periodTaskHandler;
	private PeriodSummaryTask periodSummaryTask;
	
	private ThreadRunFlagTimer runFlagTimer = null;
	
	private BbTestRunThread[] trdArr;
	private RunTimeInfoCollector runTimeInfoCollector = null;
	private ParameterGenerator paraGen;
	
	/* duration of current test */
	private long testStartTime;
	private long testStopTime;
	
	/**
	 * constructor
	 * @param dbOpt
	 * @param bbTestOpt
	 * @throws Exception
	 */
	public BlogbenchRunOperation(DbOptions dbOpt, BbTestOptions bbTestOpt,
			ParameterGenerator paraGen) throws Exception {
		super(BlogbenchOperType.RUN, dbOpt, bbTestOpt);
		this.blogbenchCounters = new BlogbenchCounters(BbTestTrxType.TRX_TYPE_NUM);
		this.paraGen = paraGen;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.BlogbenchOperation#executeOper()
	 */
	public void execute()  throws Exception {		
			
		testStartTime = Util.currentTimeMillis();
		/* print current blogbench test options */
		System.out.println("------------------------");
		System.out.println("blogbench test options---------------------");
		System.out.println("------------------------");
		System.out.println(getRunSummary(false));
		System.out.println("-------------------------------------------");
				
		/* create run flag timer thread */
		runFlagTimer = new ThreadRunFlagTimer();
		ThreadBarrier barrier = new ThreadBarrier();
		
		/* create test threads */
		trdArr = new BbTestRunThread[bbTestOpt.getThreads()];
		for (int i = 0; i < bbTestOpt.getThreads(); i++) {
			trdArr[i] = new BbTestRunThread(dbOpt, bbTestOpt,  
					paraGen, blogbenchCounters, runFlagTimer, barrier);
			trdArr[i].start();
		}
		
		/* wait for all test threads to be suspended*/
		int index = 0;
		while (index < bbTestOpt.getThreads()) {
			if (trdArr[index].isWaiting()) {
				index++;
			} else {
				Thread.sleep(100);
			}
		}
				
		if (bbTestOpt.getMaxTime() != Long.MAX_VALUE) {
			runFlagTimer.start(bbTestOpt.getMaxTime() * 1000);
		}
		
		if (bbTestOpt.isCollectSysstat()) {
			/* begin collecting system information */
			runTimeInfoCollector = RunTimeInfoCollector.getInstance();
			runTimeInfoCollector.setReportDir(bbTestOpt.getReportDir());
			runTimeInfoCollector.setDuration(bbTestOpt.getMaxTime());
			runTimeInfoCollector.setTableEngine(bbTestOpt.getTbEngine());
			runTimeInfoCollector.setDbOpt(dbOpt);
			runTimeInfoCollector.beginCollectInfo();
		}
		
		periodTaskHandler = new BbPeriodSummaryTaskHandler(blogbenchCounters, 
				bbTestOpt.getReportDir()); 
		long msInterval = bbTestOpt.getPrintThoughputPeriod() * 1000;
		periodSummaryTask = new PeriodSummaryTask(msInterval, periodTaskHandler);
							
		/* wake up all test threads to work */
		barrier.removeBarrier();
		
		System.out.println("Creating all " + bbTestOpt.getThreads() + 
				" test threads successful, blogbench test begin...");
		
		/* wait for test threads to exit*/
		for (int i = 0; i < bbTestOpt.getThreads(); i++) {
				trdArr[i].join();
		}
		
		boolean hasError = false;
		for (int i = 0; i < bbTestOpt.getThreads(); i++) {
			if (trdArr[i].getErrorCode() != 0) {
				hasError = true;
				break;
			}
		}
		
		finish(hasError);
	}
	
	/**
	 * get run operation summary
	 * @param showActualTime
	 * @return
	 * @throws IOException
	 */
	private String getRunSummary(boolean showActualTime) throws IOException {
		StringBuilder buf = new StringBuilder(1024);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date(testStartTime);
		buf.append("Test begin Time: " + sdf.format(date) + "\n");
		buf.append("Database type: " + dbOpt.getDbType() + "\n");
		if (dbOpt.getDbType().equalsIgnoreCase("mysql")) {
			buf.append("Table engine: " + bbTestOpt.getTbEngine() + "\n");
		} 
		buf.append("JDBC URL: " + dbOpt.getJdbcUrl() + "\n");
		if (bbTestOpt.getMaxTran() != Long.MAX_VALUE) 
			buf.append("Max transactions: " + bbTestOpt.getMaxTran() + "\n");
		buf.append("Test time setting: " + bbTestOpt.getMaxTime() + " seconds\n");
		if (showActualTime)
			buf.append("Actual test time: " + getActualTestTime() / 1000 + " seconds\n");
		buf.append("Total thread: " + bbTestOpt.getThreads() + "\n");
		buf.append("Hottest blog partition frequency: " + String.format("%.2f", 
				paraGen.getBlogHottestPartionFreq() * 100) + "%\n");
		buf.append("Top " + String.format("%d", 
				bbTestOpt.getBlgZipfPct())+ "% hottest blog record frequency: " + 
				String.format("%.2f", paraGen.getBlogHottestPctFreq() * 100.0) + "%\n");
		buf.append("Hottest user partition frequency: " + String.format("%.2f", 
				paraGen.getUserHottestPartionFreq()* 100) + "%\n");
		buf.append("Top " + String.format("%d", 
				bbTestOpt.getUserZipfPct()) + "% hottest user id frequency:" +
				String.format("%.2f", paraGen.getUserHottestPctFreq() * 100.0) + "%\n");
		buf.append("Test result output directory: " + bbTestOpt.getReportDir() + "\n");
		buf.append("use two tables:" + bbTestOpt.getUseTwoTable() + "\n");		
		return buf.toString();
	}
	
	private List<CreateChartHandler> getCreateChartHandlerList() {
		List<CreateChartHandler> list = new ArrayList<CreateChartHandler>(16);
		list.add(paraGen.getParaDistribution());

		list.add(periodTaskHandler.getPeriodNodes());
		list.add(blogbenchCounters);
		return list;
	}
	
	private List<CreateTableHandler> getCreateTableHandlerList() {
		List<CreateTableHandler> list = new ArrayList<CreateTableHandler>(16);
		list.add(blogbenchCounters);
		return list;
	}
	
	/**
	 *  finish run test
	 * @param hasError     if error occurred when run test
	 * @throws Exception
	 */
	private void finish(boolean hasError) throws Exception {
		if (!hasError)
			System.out.println("All run threads finished!");
		else
			System.out.println("Errors occured during blogbench test! " +
					"Please check the error message!");
		
		periodSummaryTask.exit();
		
		testStopTime = Util.currentTimeMillis();
		
		if (bbTestOpt.isCollectSysstat() && runTimeInfoCollector.isRunning()) {
			/* stop collecting system information */
			System.out.print("Waiting for system information collecting process to exit...");
			runTimeInfoCollector.stopCollectInfo(runFlagTimer.getIsTimeout());
			System.out.println("done!");
		}
		
		if (!hasError) {
			printResult();			
			exportResult();
			System.out.println("\nBlogbench test finished.");
		}
	}
	
	/**
	 * export blogbench test result to files
	 * @throws Exception
	 */
	private void exportResult() throws Exception {
		//export to result file		
		List<CreateChartHandler> createCharthandlerList = getCreateChartHandlerList();
		List<CreateTableHandler> createTableHandlerList = getCreateTableHandlerList();
		TestResultExporter resultExporter = new TestResultExporter("blogbench", 
				createCharthandlerList, createTableHandlerList);
		resultExporter.setRunSummary(getRunSummary(true));
		resultExporter.export(bbTestOpt.getReportDir());
	}
		
	public long getActualTestTime() {
		return testStopTime - testStartTime;
	}
	
	/**
	 * print blogbench test result and save to file
	 * @throws IOException
	 */
	private void printResult() throws Exception {
		StringBuilder buf = new StringBuilder(2048);
		buf.append("--------------\n");
		buf.append("Blogbench Test Report----------------------------------------\n");
		buf.append("--------------\n");
		buf.append("Test Duration:" + getActualTestTime() + " milliseconds\n" );
		buf.append("--------------------------------" +
				"Total Transactions Statistics" +
				"---------------------------------\n");
		buf.append(getTrxResult(blogbenchCounters.getTotalTrxCounter()));
		
		for (int i = 0; i < BbTestTrxType.TRX_TYPE_NUM; i++) {
			buf.append("----------------------------");
			buf.append(BbTestTrxType.getTrxName(i));
			buf.append("----------------------------\n");
			if (blogbenchCounters.getSingleTrxCounter(i).getTrxCount() > 0) {
				buf.append(getTrxResult(blogbenchCounters.getSingleTrxCounter(i)));
			}
		}
		
		String result = buf.toString();
		System.out.println(result);
		
		/* save oper result to file */
		FileWriter fw = new FileWriter(super.bbTestOpt.getReportDir() + 
				"blogbench-report.txt");
		fw.write(result);
		fw.close();
	}	
	
	private String getTrxResult(TrxCounter counter) {
		StringBuilder buf = new StringBuilder(512);
		buf.append("Total transactions: " + counter.getTrxCount() + "\n");
		buf.append("Average TPS: " + String.format("%.0f\n", 
				(double)counter.getTrxCount() * 1000/ getActualTestTime()));
		buf.append("Transaction Failed Times: " + counter.getFailedTimes() + "\n");
		buf.append("Response Time:\n\tMin:" + counter.getMinResponseTime() 
				+ " milliseconds\n");
		buf.append("\tMax:" + counter.getMaxResponseTime() + " milliseconds\n");
		buf.append("\tAverage:" + counter.getAvgResponseTime() + " milliseconds\n");
		buf.append("\t90%:" + counter.getMostResponseTime() + " milliseconds\n");	
		return buf.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.BlogbenchOperation#signalAction(sun.misc.Signal)
	 */
	public void signalAction(Signal signal) {
		//TODO: add signal action here
		if (runFlagTimer != null)
			runFlagTimer.setExpired();
	}
}
