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
import com.netease.webbench.statis.CreateTableHandler;
import com.netease.webbench.statis.PdfTable;
import com.netease.webbench.visual.PieChart;


/**
 * blogbench test transactions counters
 *  @author LI WEIZHAO
 */
public class BlogbenchCounters implements CreateChartHandler, CreateTableHandler {
	private BlogbenchTrxCounter totalTrxCounter;
	
	private int singleCounterNum = 0;
	private List<BlogbenchTrxCounter> singleTrxCounterList;
	
	public BlogbenchCounters(int singleCounterNum) {
		this.totalTrxCounter = new BlogbenchTrxCounter("Total Transaction Response Time Distribution");
		this.singleCounterNum = singleCounterNum;
		singleTrxCounterList = new ArrayList<BlogbenchTrxCounter>(singleCounterNum);
		for (int i = 0; i < singleCounterNum; i++) {
			this.singleTrxCounterList.add(new BlogbenchTrxCounter(BbTestTrxType.getTrxName(i)));
		}
	}
	
	public BlogbenchTrxCounter getTotalTrxCounter() {
		return totalTrxCounter;
	}

	public BlogbenchTrxCounter getSingleTrxCounter(int i) throws Exception {
		if (i >= singleCounterNum) {
			throw new Exception("Transaction index is out of range: " + i + ", size: " + singleCounterNum);
		}
		return singleTrxCounterList.get(i);
	}
	
	public BlogbenchTrxCounter getSingleTrxCounter(BbTestTrxType trxType) throws Exception {
		return singleTrxCounterList.get(BbTestTrxType.getTrxIndex(trxType));
	}
	
	public List<BlogbenchTrxCounter> getSingleCounterList() {
		return singleTrxCounterList;
	}
	
	public int getSingleTrxCounterNum() {
		return singleCounterNum;
	}
	public List<String> createChartFiles(String reportDir) throws Exception {
		List<String> list = new ArrayList<String>();
		list.add(createTransactionPieChart(reportDir));
		list.addAll(createResponseTimeDisChart(reportDir));
		return list;
	}
	
	/**
	 * create charts of distribution of transaction response time
	 * @param counters
	 * @param filePathArr
	 * @throws IOException
	 */
	private List<String> createResponseTimeDisChart(String reportDir) throws Exception {	
		List<String> list = new ArrayList<String>();
		if (totalTrxCounter.getTrxCount() > 0) {
			String fileName = totalTrxCounter.createResponseTimeChart(reportDir);
			if (fileName != null)
				list.add(fileName);
		}
		for (int i = 0; i < getSingleTrxCounterNum(); i++) {
			BlogbenchTrxCounter counter = singleTrxCounterList.get(i);
			if (counter.getTrxCount() > 0) {
				String fileName = counter.createResponseTimeChart(reportDir);
				if (fileName != null)
					list.add(fileName);
			}
		}
		return list;
	}
	
	/**
	 * create pie charts of transactions percentage
	 * @param counters
	 * @param filePathArr
	 * @throws IOException
	 */
	private String createTransactionPieChart(String reportDir) throws Exception {
		PieChart trxPieChart = new PieChart("All Transactions Composition");
		for (int i = 0; i < BbTestTrxType.TRX_TYPE_NUM; i++) {
			trxPieChart.addPie(BbTestTrxType.getTrxName(i), getSingleTrxCounter(i).getTrxCount());
		}		
		trxPieChart.createChart();
		String fileName = reportDir + "TransactionComposition.jpeg";
		trxPieChart.savaToFile(fileName, "JPEG");
		return fileName;
	}
	
	public List<PdfTable> createTables() throws Exception {
		List<PdfTable> list = new ArrayList<PdfTable>();
		list.add(createSummaryTable());
		list.addAll(createMemcachedStaticsTables());
		return list;
	}
	
	private PdfTable createSummaryTable() throws Exception {
		//add test result to pdf file as table
		PdfSummaryTable summaryTable = new PdfSummaryTable();
		summaryTable.addTableDataRow("Total", 100.0, totalTrxCounter);
		for(int i = 0; i < BbTestTrxType.TRX_TYPE_NUM; i++) {
			double pct = singleTrxCounterList.get(i).getTrxCount() * 100.0D / totalTrxCounter.getTrxCount();
			summaryTable.addTableDataRow(BbTestTrxType.getTrxName(i), pct, 
					singleTrxCounterList.get(i));
		}
		return summaryTable;
	}
	
	private List<PdfTable> createMemcachedStaticsTables() throws Exception {
		PdfMemHitTables memTables = new PdfMemHitTables();
		memTables.addMemOperStatistics(singleTrxCounterList);
		return memTables.getPdfMemHitTableList();
	}
}
