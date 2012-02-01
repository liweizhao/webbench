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
	
	private long lastAcsCntFlush = 0;
		
	private boolean useMemcached = false;
	private AcsCntFlushTaskStatis flushTaskStatis = null;
	private UpdateAccessStatis updateAccessStatis = null;
	
	private BlogbenchCounters blogbenchCounters;
	
	private long lastRunTime = 0;
	
	private FileWriter memStatisticFile;
	
	private ThroughputPeriodNodes periodNodes;
	
	private FileWriter acsCntFlushStatisFw;
	
	public BbPeriodSummaryTaskHandler(BlogbenchCounters blogbenchCounters, 
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

	public void cancel() {
		// TODO Auto-generated method stub
		if (useMemcached) {
			try {
				acsCntFlushStatisFw.close();
				memStatisticFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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

			if (useMemcached && periodNodes.getPeriodCount() % 5 == 0) {
				saveMemcachedStatistic(currentFormatTime);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void saveMemcachedStatistic(String currentFormatTime) {
		/* memcached operations statistic information of  all transactions */
		StringBuilder builder = new StringBuilder();
		builder.append("---------------------------------------------------------------------------------\n");
		builder.append("               get_list set_list del_list get_blog set_blog rpc_blog get_access incease_access add_access get_content set_content rpc_content\n");
		try {
			for (int i = 0; i < BbTestTrxType.TRX_TYPE_NUM; i++) {
				String trxName = BbTestTrxType.getTrxName(i);
				StringBuilder sb = new StringBuilder();
				sb.append(trxName);
				for (int j = 0; j < (15 - trxName.length()); j++)
					sb.append(' ');
				builder.append(sb.toString());
				for (int j = 0; j < blogbenchCounters.getSingleTrxCounter(i).getMemOperCounterSize(); j++) {
					builder.append(String.format("%.1f%%\t", blogbenchCounters.getSingleTrxCounter(i).getMemHitRatio(j) * 100));
				}
				builder.append('\n');
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		builder.append("---------------------------------------------------------------------------------\n");
	
		String statisticStr = builder.toString();
		System.out.println(statisticStr);
		
		try {
			memStatisticFile.write(currentFormatTime);
			memStatisticFile.write('\n');
			memStatisticFile.write(statisticStr);
			memStatisticFile.write('\n');
			memStatisticFile.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		/* statistic information of access count flushing task */
		if (flushTaskStatis != null && updateAccessStatis != null) {
			long totalAcsFlush = flushTaskStatis.getTotalFlush();
			long currentAcsFlush = totalAcsFlush - lastAcsCntFlush;
			lastAcsCntFlush = totalAcsFlush;
			
			int totalMapSize = updateAccessStatis.getTotalMapSize();
			
			System.out.println("Access count flush: " + currentAcsFlush + 
					", current total map size: " + totalMapSize + 
					", total cached request: " + updateAccessStatis.getTotalCachedReqCount() + 
					", real cached: " + updateAccessStatis.getTotalRealcachedCount());
			
			StringBuilder tmpBuilder = new StringBuilder();
			tmpBuilder.append('[');
			tmpBuilder.append(currentFormatTime);
			tmpBuilder.append("] ");
			tmpBuilder.append(currentAcsFlush);
			tmpBuilder.append('\t');
			tmpBuilder.append(String.format("%.1f%%", flushTaskStatis.getMemGetBlogOperHit() * 100));
			tmpBuilder.append("(" + flushTaskStatis.getMemGetBlogOperCount() + ")");
			tmpBuilder.append('\t');
			tmpBuilder.append(String.format("%.1f%%", flushTaskStatis.getMemReplaceBlogOperHit() * 100));
			tmpBuilder.append("(" + flushTaskStatis.getMemReplaceBlogOperCount() + ")");
			tmpBuilder.append('\t');
			tmpBuilder.append(totalMapSize);
			tmpBuilder.append('\t');
			tmpBuilder.append(updateAccessStatis.getTotalCachedReqCount());
			tmpBuilder.append('\t');
			tmpBuilder.append(updateAccessStatis.getTotalRealcachedCount());
			tmpBuilder.append('\t');
			tmpBuilder.append(updateAccessStatis.getMergeCount());
			tmpBuilder.append('\n');
			
			try {
				acsCntFlushStatisFw.write(tmpBuilder.toString());
				acsCntFlushStatisFw.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public FileWriter createAcsCntStatisFlushFile(String fileName) throws IOException {
		FileWriter fw = new FileWriter(fileName);
		fw.write("Time\tAccessCountFlush\tMemcachedGetBlogHit\tMemcachedReplaceBlogHit\tTotalMapSize" +
				"\tTotalCachedRequest\tTotalRealCached\tTotalMergeCached\n");
		return fw;
	}
		
	public void setFlushTaskStatis(AcsCntFlushTaskStatis flushTaskStatis) {
		this.flushTaskStatis = flushTaskStatis;
	}
	
	public void setUpdateAcsStatis(UpdateAccessStatis updateAccessStatis) {
		this.updateAccessStatis = updateAccessStatis;
	}

	public ThroughputPeriodNodes getPeriodNodes() {
		return periodNodes;
	}
}
