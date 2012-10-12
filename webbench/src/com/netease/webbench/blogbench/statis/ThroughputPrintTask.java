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
import java.util.Date;
import java.util.TimerTask;

import com.netease.webbench.blogbench.transaction.BbTestTrxType;

/**
 * transaction throughput printing task
 * @author LI WEIZHAO
 */
public class ThroughputPrintTask extends TimerTask {	
	/* count of total transaction at last period */
	private long lastPeriodTotalTrx = 0;
	
	/* counts of each transaction at last period */
	private long[] lastPeriodTrxCount;

		
	private boolean useMemcached = false;

	private BlogbenchCounters blogbenchCounters;
	
	private long lastRunTime = 0;
	
	private FileWriter memStatisticFile;
	
	private ThroughputPeriodNodes periodNodes;
	
	private FileWriter acsCntFlushStatisFw;
	
	public ThroughputPrintTask (BlogbenchCounters blogbenchCounters, 
			boolean useMemcached, String reportDir) throws IOException {
		this.blogbenchCounters = blogbenchCounters;
		this.useMemcached = useMemcached;
		
		if (useMemcached) {
			memStatisticFile = new FileWriter(reportDir + "/memcached-statistic/memcached-operation-statistic.txt");
			acsCntFlushStatisFw = createAcsCntStatisFlushFile(reportDir + 
					"/memcached-statistic/access-count-flush-statistic.txt");
		}
		
		lastPeriodTotalTrx = 0;	
		lastPeriodTrxCount = new long[BbTestTrxType.TRX_TYPE_NUM];
		
		for (int i = 0; i < BbTestTrxType.TRX_TYPE_NUM; i++) {
			lastPeriodTrxCount[i] = 0;
		}
		
		periodNodes = new ThroughputPeriodNodes(BbTestTrxType.TRX_TYPE_NUM);
		
		lastRunTime = System.currentTimeMillis();
	}
	
	/* (non-Javadoc)
	 * @see java.util.TimerTask#run()
	 */
	@Override
	public void run() {
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
	
	/* (non-Javadoc)
	 * @see java.util.TimerTask#cancel()
	 */
	@Override
	public boolean cancel() {
		if (useMemcached) {
			try {
				acsCntFlushStatisFw.close();
				memStatisticFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return super.cancel();
	}
	
	public FileWriter createAcsCntStatisFlushFile(String fileName) throws IOException {
		FileWriter fw = new FileWriter(fileName);
		fw.write("Time\tAccessCountFlush\tMemcachedGetBlogHit\tMemcachedReplaceBlogHit\tTotalMapSize" +
				"\tTotalCachedRequest\tTotalRealCached\tTotalMergeCached\n");
		return fw;
	}

	public ThroughputPeriodNodes getPeriodNodes() {
		return periodNodes;
	}
}
