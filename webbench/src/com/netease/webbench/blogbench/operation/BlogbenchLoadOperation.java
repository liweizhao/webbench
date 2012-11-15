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
import java.util.Timer;

import com.netease.webbench.blogbench.dao.DataLoader;
import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.blogbench.misc.LoadProgressTask;
import com.netease.webbench.blogbench.statis.LoadDataStatis;
import com.netease.webbench.common.DbOptions;

/**
 * blogbench load operation
 * @author LI WEIZHAO
 *
 */
public class BlogbenchLoadOperation extends BlogbenchOperation  {	
	/* progress bar task */
	private Timer loadProgressTimer;
	private LoadProgressTask loadProgressTask;

	private DataLoader dataLoader;

	/**
	 * @param dbOpt
	 * @param bbTestOpt
	 * @param dataLoader
	 * @throws Exception
	 */
	public BlogbenchLoadOperation(DbOptions dbOpt, 
			BbTestOptions bbTestOpt, DataLoader dataLoader) 
			throws Exception {
		super(BlogbenchOperType.LOAD, dbOpt, bbTestOpt);
		this.dataLoader = dataLoader;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.BlogbenchOperation#executeOper()
	 */
	public void execute() throws Exception {
		try {		
			dataLoader.pre();
			
			System.out.println("Is loading data, please wait...");
			System.out.println("Cocurrent load threads num: " + bbTestOpt.getLoadThreads());
			
			createProgressBar();
			dataLoader.load();			
			removeProgressBar(true);
			
			dataLoader.post();			
			printStatistics();
		} catch (Exception e) {
			removeProgressBar(false);
			System.err.println("Failed to load data!");
			throw e;
		}		
		
		writePerformanceToFile();
		
		System.out.println("Load data finished!");	
	}
	
	private void writePerformanceToFile() throws IOException {
		FileWriter fw = new FileWriter(bbTestOpt.getReportDir()
				+ "/loaddata_performance.txt");
		fw.write(dataLoader.getLoadSummary());
		fw.close();
	}
	
	/**
	 * create progress bar
	 */
	private void createProgressBar() {
		loadProgressTimer = new Timer("Load progress print task thread");
		loadProgressTask = new LoadProgressTask(dataLoader);
		loadProgressTimer.schedule(loadProgressTask, 2000, 1000);
	}
	
	/**
	 * remove progress bar
	 * @throws Exception
	 */
	private void removeProgressBar(boolean waitForFinish) throws Exception {
		while (waitForFinish && !loadProgressTask.isFinish()) {
			Thread.sleep(10);
		}
		loadProgressTimer.cancel();
	}
	
	/**
	 *  print statistics
	 */
	private void printStatistics() {
		LoadDataStatis statis = dataLoader.getStatistics();
		System.out.println("Total time waste:  " 
				+ statis.getTotalTimeWaste() + "  milliseconds");
		System.out.println("Create table waste: " 
				+ statis.getCreateTableTimeWaste() + "  milliseconds");
		System.out.println("Load data waste: " + statis.getLoadDataTimeWaste() 
				+ "  milliseconds");
		System.out.println("Create index waste: " + statis.getCreateIndexTimeWaste()
				+ "  milliseconds");
	}
}
