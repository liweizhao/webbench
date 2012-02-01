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

import java.util.ArrayList;
import java.util.List;

import com.netease.webbench.blogbench.statis.MemcachedOperCounter.MemOperType;
import com.netease.webbench.blogbench.transaction.BbTestTrxType;
import com.netease.webbench.statis.PdfTable;

/**
 * statistic table of memcached server hit ratiod of all transactions
 * 
 * memcached operation types:
 * get list, set list, delete list, get blog, set blog, get access, increase access
 * 
 * @author LI WEIZHAO
 */
public class PdfMemHitTables {
	private PdfMemHitTable table1;
	private PdfMemHitTable table2;
	
	public PdfMemHitTables() throws Exception {		
		this.table1 = new PdfMemHitTable("Memcached Operation Statistics", "lists/blogs");
		this.table2 = new PdfMemHitTable("Memcached Operation Statistics", "blog contents/access counts");
		
		table1.addTextRow("", true, true, 1);
		table1.addTextRow("Lists", true, true, 3);
		table1.addTextRow("Blogs(without content)", true, true, 3);
		
		table1.addTextRow("", true, false, 1);
		table1.addTextRow("GET", true, false, 1);
		table1.addTextRow("SET", true, false, 1);
		table1.addTextRow("DELETE", true, false, 1);
		table1.addTextRow("GET", true, false, 1);
		table1.addTextRow("SET", true, false, 1);
		table1.addTextRow("REPLACE", true, false, 1);
		
		table2.addTextRow("", true, true, 1);
		table2.addTextRow("Blog Contents", true, true, 3);
		table2.addTextRow("Access Counts", true, true, 3);
		
		table2.addTextRow("", true, false, 1);
		table2.addTextRow("GET", true, false, 1);
		table2.addTextRow("SET", true, false, 1);
		table2.addTextRow("REPLACE", true, false, 1);
		table2.addTextRow("GET", true, false, 1);
		table2.addTextRow("INCREASE", true, false, 1);
		table2.addTextRow("ADD", true, false, 1);
	}
	
	public void addMemOperStatistics(List<BlogbenchTrxCounter> singleTrxCountersList)
			throws Exception {
		for (int i = 0; i < singleTrxCountersList.size(); i++) {
			List<MemcachedOperCounter> counterList1 = new ArrayList<MemcachedOperCounter>();	
			counterList1.add(singleTrxCountersList.get(i).getMemOperCounter(MemOperType.GET_LIST));
			counterList1.add(singleTrxCountersList.get(i).getMemOperCounter(MemOperType.SET_LIST));	
			counterList1.add(singleTrxCountersList.get(i).getMemOperCounter(MemOperType.DEL_LIST));	
			counterList1.add(singleTrxCountersList.get(i).getMemOperCounter(MemOperType.GET_BLOG));	
			counterList1.add(singleTrxCountersList.get(i).getMemOperCounter(MemOperType.SET_BLOG));			
			counterList1.add(singleTrxCountersList.get(i).getMemOperCounter(MemOperType.RPC_BLOG));	
			table1.addDataRow(BbTestTrxType.getTrxName(i), getStringList(counterList1));
			
			List<MemcachedOperCounter> counterList2 = new ArrayList<MemcachedOperCounter>();
			counterList2.add(singleTrxCountersList.get(i).getMemOperCounter(MemOperType.GET_CNT));		
			counterList2.add(singleTrxCountersList.get(i).getMemOperCounter(MemOperType.SET_CNT));	
			counterList2.add(singleTrxCountersList.get(i).getMemOperCounter(MemOperType.RPC_CNT));		
			counterList2.add(singleTrxCountersList.get(i).getMemOperCounter(MemOperType.GET_ACS));
			counterList2.add(singleTrxCountersList.get(i).getMemOperCounter(MemOperType.INC_ACS));		
			counterList2.add(singleTrxCountersList.get(i).getMemOperCounter(MemOperType.ADD_ACS));			
			table2.addDataRow(BbTestTrxType.getTrxName(i), getStringList(counterList2));
		}
	}
	private List<String> getStringList(List<MemcachedOperCounter> counterList) {
		List<String> strList = new ArrayList<String>(counterList.size());
		for (int i = 0; i < counterList.size(); i++) {
			double hitRatio = counterList.get(i).getHitRatio();
			long totalOper = counterList.get(i).getTotalOper();
			long sucOper = counterList.get(i).getSuccessOper();
			if (totalOper == 0)
				strList.add("-");
			else
				strList.add(String.format("%.1f%%\n(%d/%d)", hitRatio * 100, sucOper, totalOper));	
		}
		return strList;
	}
	
	public List<PdfTable> getPdfMemHitTableList() {
		List<PdfTable> list = new ArrayList<PdfTable>();
		list.add(table1);
		list.add(table2);
		return list;
	}
}
