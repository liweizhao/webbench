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
import java.text.SimpleDateFormat;
import java.util.Date;

import com.netease.webbench.blogbench.transaction.BbTestTrxType;
import com.netease.webbench.statis.PeriodSummaryTaskHandler;

/**
 * 
 * @author LI WEIZHAO
 *
 */
public class BbPeriodSummaryTaskHandler implements PeriodSummaryTaskHandler {
	/* count of total transaction at last period */
	private long lastPeriodTotalTrx = 0;
	
	/* counts of each transaction at last period */
	private long[] lastPeriodTrxCount;
	private BlogbenchCounters blogbenchCounters;	
	private long lastRunTime = 0;
	private ThroughputPeriodNodes periodNodes;
	
	public BbPeriodSummaryTaskHandler(BlogbenchCounters blogbenchCounters, 
			String reportDir) throws IOException {
		this.blogbenchCounters = blogbenchCounters;
		
		lastPeriodTotalTrx = 0;	
		lastPeriodTrxCount = new long[BbTestTrxType.TRX_TYPE_NUM];		
		for (int i = 0; i < BbTestTrxType.TRX_TYPE_NUM; i++) {
			lastPeriodTrxCount[i] = 0;
		}		
		periodNodes = new ThroughputPeriodNodes(BbTestTrxType.TRX_TYPE_NUM);		
		lastRunTime = System.currentTimeMillis();
	}

	public void exe() {
		// TODO Auto-generated method stub
		try {
			long currentTime = System.currentTimeMillis();
			int wakeUpInterval = (int) ((currentTime - lastRunTime) / 1000);
			lastRunTime = currentTime;

			BlogbenchTrxCounter totalTrxCounter = blogbenchCounters.getTotalTrxCounter();

			long totalTrx = totalTrxCounter.getTrxCount();
			long totalTpm = totalTrx - lastPeriodTotalTrx;
			long tps = totalTpm / wakeUpInterval;
			lastPeriodTotalTrx = totalTrx;

			periodNodes.addTotalTPS(tps);
			for (int i = 0; i < BbTestTrxType.TRX_TYPE_NUM; i++) {
				long tmp = blogbenchCounters.getSingleTrxCounter(i).getTrxCount();
				long singleTps = (tmp - lastPeriodTrxCount[i]) / wakeUpInterval;
				periodNodes.addSingleTPS(i, singleTps);
				lastPeriodTrxCount[i] = tmp;
			}

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			String currentFormatTime = sdf.format(new Date(System.currentTimeMillis()));
			System.out.println("\n\ttotal in period: " + totalTpm + "\t\ttps: "
					+ tps + "\t" + currentFormatTime + "\tperiod: " + periodNodes.getPeriodCount());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ThroughputPeriodNodes getPeriodNodes() {
		return periodNodes;
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub
		
	}
}
