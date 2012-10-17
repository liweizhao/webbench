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

import com.netease.webbench.statis.PdfTable;
import com.netease.webbench.statis.TrxCounter;


/**
 * summary table of result PDF file
 *  @author LI WEIZHAO
 */
public class PdfSummaryTable extends PdfTable {	
	public PdfSummaryTable() throws Exception {
		super("blogbench test result", "Transaction Summary");
		
		float[] widths = {0.18f, 0.12f, 0.12f,0.1f, 0.12f, 0.12f, 0.12f, 0.12f};
		this.table = PdfTable.createNewTable(8, widths);
		
		table.addCell(makeCell("Transaction", true, true, 4));
		table.addCell(makeCell("Response Time(ms)", true, true, 4));
		
		String header = "Name#Mix %#Total#Failed#Min#Max#Avg#90th %";
		addTableHeader(header);
	}
	
	public void addTableDataRow(String name, double pct, TrxCounter counter) {
		table.addCell(makeCell(name, false, false, 1));
		table.addCell(makeCell(String.format("%.1f",	pct), false, false, 1));
		if (pct != 0) {
			String tmp = String.format("%d", counter.getTrxCount());
			table.addCell(makeCell(tmp, false, false, 1));
			tmp = String.format("%d", counter.getFailedTimes());
			table.addCell(makeCell(tmp, false, false, 1));
			tmp = String.format("%d", counter.getMinResponseTime());
			table.addCell(makeCell(tmp, false, false, 1));
			tmp = String.format("%d", counter.getMaxResponseTime());
			table.addCell(makeCell(tmp, false, false, 1));
			tmp = String.format("%d", counter.getAvgResponseTime());
			table.addCell(makeCell(tmp, false, false, 1));
			tmp = String.format("%d", counter.getMostResponseTime());
			table.addCell(makeCell(tmp, false, false, 1));
		} else {
			table.addCell(makeCell("0", false, false, 1));
			table.addCell(makeCell("-", false, false, 1));
			table.addCell(makeCell("-", false, false, 1));
			table.addCell(makeCell("-", false, false, 1));
			table.addCell(makeCell("-", false, false, 1));
			table.addCell(makeCell("-", false, false, 1));
		}		
	}
	
	private void addTableHeader(String header) {
		String[] tmp = header.split("#");
		for (int i = 0; i < tmp.length; i++) {
			table.addCell(makeCell(tmp[i], true, false, 1));
		}
	}
}
