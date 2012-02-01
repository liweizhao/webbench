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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.netease.webbench.blogbench.transaction.BbTestTrxType;
import com.netease.webbench.statis.CreateChartHandler;
import com.netease.webbench.statis.PeriodNodes;
import com.netease.webbench.visual.TimeLineChart;

/**
 * 
 * @author LI WEIZHAO
 *
 */
public class ThroughputPeriodNodes implements CreateChartHandler {
	private PeriodNodes totalThroughputNodes;
	private PeriodNodes[] singleThroughputNodes;
	
	public ThroughputPeriodNodes(int trxTypeNum) {
		totalThroughputNodes = new PeriodNodes();
		singleThroughputNodes = new PeriodNodes[trxTypeNum];
		for (int i = 0; i < trxTypeNum; i++)
			singleThroughputNodes[i] = new PeriodNodes();
	}
	
	public void addTotalTPS(long tps) {
		totalThroughputNodes.addNode(tps);
	}
	
	public void addSingleTPS(int index, long tps) throws Exception {
		if (index >= singleThroughputNodes.length)
			throw new Exception(String.format("Index %d is out of range, array size is %d.", 
					index, singleThroughputNodes.length));
		singleThroughputNodes[index].addNode(tps);
	}
	
	public long getPeriodCount() {
		return totalThroughputNodes.getPeriodCount();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.statis.CreateChartHandler#createChartFiles(java.lang.String)
	 */
	public List<String> createChartFiles(String reportPath) throws Exception {
		List<String> list = new ArrayList<String>(2);
		String totalChartFileName = createTotalChartFile(reportPath);
		if (null != totalChartFileName)
			list.add(totalChartFileName);
		String diffChartFileName = createDiffTrxChartFile(reportPath);
		if (null != diffChartFileName)
			list.add(diffChartFileName);
		return list;
	}
	
	/**
	 * create total transactions statistic charts and save to file
	 * @param reportDir directory of result file 
	 * @return file name
	 */
	protected String createTotalChartFile(String reportDir) throws Exception {
		long startTime = totalThroughputNodes.getFirstTime();
		long stopTime = totalThroughputNodes.getLastTime();
		if (stopTime - startTime > 0) {
			TimeLineChart chart = new TimeLineChart("Total Transactions Throughput");
			int line = chart.newLine("Total TPS");
			for (PeriodNodes.TimeNode tn : totalThroughputNodes) {
				chart.addPoint(line, tn.time, tn.value);
			}
			return createTrxChartFile(reportDir, startTime, stopTime, chart, "Total Transactions Throughput", 
					"TotalTransaction");
		} else
			return null;
	}
	
	/**
	 * create difference transaction statistic chart and save to file
	 * @param reportDir directory of result file
	 * @return file name
	 */
	protected String createDiffTrxChartFile(String reportDir) throws Exception {
		long startTime = totalThroughputNodes.getFirstTime();
		long stopTime = totalThroughputNodes.getLastTime();
		if (stopTime - startTime > 0) {
			TimeLineChart chart = new TimeLineChart("All Transactions Throughput");
			for (int i = 0; i < singleThroughputNodes.length; i++) {
				PeriodNodes pn = singleThroughputNodes[i];
				int line = chart.newLine(BbTestTrxType.getTrxName(i));
				for (PeriodNodes.TimeNode tn : pn) {
					chart.addPoint(line, tn.time, tn.value);
				}
			}
			return createTrxChartFile(reportDir, startTime, stopTime, chart, "All Kinds of Transactions Confrontation", 
					"DifferentTransaction");
		} else {
			return null;
		}
	}
	
	/**
	 * create transaction throughput chart file
	 * @param reportDir   directory of image file
	 * @param filePathArr add file path to this list
	 * @param title             chart title
	 * @param fileName    image file name(not include suffix)
 	 * @throws IOException 
	 */
	protected String createTrxChartFile(String reportDir, long startTime, long endTime, TimeLineChart chart, 
			String title, String fileName) throws IOException {	
		long duration = totalThroughputNodes.getLastTime() - totalThroughputNodes.getFirstTime();
		int distance = 0;
		if (duration > 86400000) {
			distance = (int)duration / 5000;
			chart.setStartTime(startTime - distance);
			chart.setEndTime(endTime - distance);
			chart.createChart("Test Time", "Throughput", distance, "MM/dd HH:mm:ss", 0, -1, true);
		} else {
			distance = (int)duration / 10000;
			chart.setStartTime(startTime - distance);
			chart.setEndTime(endTime - distance);
			chart.createChart("Test Time", "Throughput", 	distance, "HH:mm:ss", 0, -1, true);
		}
		chart.setSubTitle(title);
		String file = reportDir + fileName + ".png"; 
		chart.savaToFile(file, "PNG");
		return file;
	}
}
