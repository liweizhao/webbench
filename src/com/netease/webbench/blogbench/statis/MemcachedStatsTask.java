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
package com.netease.webbench.blogbench.statis;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.netease.webbench.blogbench.memcached.MemcachedStatisHelper;
import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.statis.CreateChartHandler;
import com.netease.webbench.statis.PeriodNodes;
import com.netease.webbench.visual.TimeLineChart;

/**
 * Task to query memcached servers statistic information every period
 *  @author LI WEIZHAO
 */
public class MemcachedStatsTask extends Thread implements CreateChartHandler {
	private long testStartTime;
	private long testStopTime;
	
	private AtomicBoolean runFlag = new AtomicBoolean(true);
	private long period;
	private long runTimeLen;
	
	private BbTestOptions bbTestOpt;
	private FileWriter fw;
	
	private int runTimes = 0;
	
	private int periodNodeNum;
	private List<PeriodNodes> periodNodes;
	
	/**
	 * constructor
	 * @param period
	 * @param runTimeLen
	 * @param bbTestOpt
	 */
	public MemcachedStatsTask( long period, long runTimeLen, BbTestOptions bbTestOpt) {
		this.bbTestOpt = bbTestOpt;
		this.runTimeLen = runTimeLen;
		this.period = period;
		this.periodNodeNum = 2;
		periodNodes = new ArrayList<PeriodNodes>(this.periodNodeNum);
		for (int i = 0; i < this.periodNodeNum; i++) {
			periodNodes.add(new PeriodNodes());
		}
		
		try {
			createStatisFile(bbTestOpt.getReportDir() + "/memcached-statistic/memcached-statistic.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
		testStartTime = System.currentTimeMillis();
	}
	
	/* (non-Javadoc)
	 * @see java.util.TimerTask#run()
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		runTimes++;
		try {
			while (runFlag.get()) {				
				Map<String, String> majorStatisticMap = MemcachedStatisHelper.getMemcachedStatistic(
						bbTestOpt.getMainMemcachedHost(), bbTestOpt.getMainMemcachedPort(), true);
				double hit1 = MemcachedStatisHelper.getDftMmcGetHitRate(majorStatisticMap);
				periodNodes.get(0).addNode(new Double(hit1 * 100.0));
								
				Map<String, String> minorStatisticMap = MemcachedStatisHelper.getMemcachedStatistic(
						bbTestOpt.getMinorMemcachedHost(), bbTestOpt.getMinorMemcachedPort(), false);
				double hit2 = MemcachedStatisHelper.getDftMmcGetHitRate(minorStatisticMap);				
				periodNodes.get(1).addNode(new Double(hit2 * 100.0));

				try {
					writeToFile(majorStatisticMap, minorStatisticMap);
				} catch (IOException e) {
					e.printStackTrace();
				}

				long currentTime = System.currentTimeMillis();
				if (currentTime - testStartTime > runTimeLen - period) {
					cancel();
					break;
				}				
				Thread.sleep(period);
			}
		} catch (InterruptedException e) {
			System.err.println("Memcached stats collecting thread is interruped!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void cancel() throws Exception {
		if (isRunning()) {
			testStopTime = System.currentTimeMillis();
			runFlag.set(false);
			this.interrupt();
			closeMemcachedStatisFile();
		}
	}
	
	public boolean isRunning() {
		return runFlag.get();
	}
	
	private void createStatisFile(String fileFullPath) throws IOException {
		fw = new FileWriter(fileFullPath);
	}
	
	private void writeToFile(Map<String, String> majorMap, Map<String, String> minorMap) throws IOException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		String currentFormatTime = sdf.format(new Date(System.currentTimeMillis()));
		
		fw.write(currentFormatTime);
		fw.write("\n--------------------------------------------------------\n");
		Set<String> keySet = majorMap.keySet();
		for (String key : keySet) {
			fw.write(key);
			fw.write('\t');
			if (majorMap != null)
				fw.write(majorMap.get(key).trim());
			fw.write('\t');
			if (minorMap != null)
				fw.write(minorMap.get(key).trim());
			fw.write('\n');
		}
		fw.flush();
	}
	
	private void closeMemcachedStatisFile() throws IOException {
		fw.close();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.statis.CreateChartHandler#createChartFiles(java.lang.String)
	 */
	public List<String> createChartFiles(String reportPath) throws Exception {
		long duration = this.testStopTime - this.testStartTime;
		if (duration < period)
			return null;
		
		System.out.print("Create memcached get hits chart...");
		
		TimeLineChart memGetHitsChart = new TimeLineChart("Memcached Server Get Hits");		
		int majorMemLine = memGetHitsChart.newLine("Normal Records Server");
		for (PeriodNodes.TimeNode tn : periodNodes.get(0)) {
			memGetHitsChart.addPoint(majorMemLine, tn.time, tn.value);
		}
		int minorMemLine = memGetHitsChart.newLine("Access Count Server");
		for (PeriodNodes.TimeNode tn : periodNodes.get(1)) {
			memGetHitsChart.addPoint(minorMemLine, tn.time, tn.value);
		}
				
		int distan = 0;		
		if (duration > 86400000) {
			distan = (int)duration / 5000;
			memGetHitsChart.setStartTime(testStartTime - distan);
			memGetHitsChart.setEndTime(testStopTime - distan);
			memGetHitsChart.createChart("Test Time", "Throughput", distan, "MM/dd HH:mm:ss", 0.0D, 100.0D, false);
		} else {
			distan = (int)duration / 10000;
			memGetHitsChart.createChart("Test Time", "Throughput", distan, "HH:mm:ss", 0.0D, 100.0D, false);
		}
		
		List<String> filePathList = new ArrayList<String>(1);
		String file = reportPath + "/memcachedGetHits.png";
		memGetHitsChart.savaToFile(file, "PNG");
		filePathList.add(file);
		
		System.out.println("done.");
		
		return filePathList;
	}
}
