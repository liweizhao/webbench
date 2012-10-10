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
package com.netease.webbench.blogbench.transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.netease.webbench.blogbench.memcached.AccessCountCache;
import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.blogbench.statis.BlogbenchCounters;
import com.netease.webbench.common.DbSession;

/**
 * blogbench transaction pool per thread
 * @author LI WEIZHAO
 */
public class BbTestTrxPool {
	protected List<BbTestTransaction> trxList;
	protected double[] probabilities;
	protected Random randomGenerator;
	protected int trxTypeNum;
	
	public BbTestTrxPool(DbSession dbSession, BbTestOptions bbTestOpt, BlogbenchCounters trxCounters,
			AccessCountCache accessCountCache, int trxTypeNum) 	throws Exception {
		this.trxTypeNum = trxTypeNum;
		this.trxList = new ArrayList<BbTestTransaction>(trxTypeNum);
		
		initialiseTrxs(dbSession, bbTestOpt, trxCounters, accessCountCache);		
		initialiseProb();
	}
	
	private void initialiseTrxs(DbSession dbSession, BbTestOptions bbTestOpt, 
			BlogbenchCounters trxCounters, AccessCountCache accessCountCache) throws Exception {		
		
		trxList.add(new BbTestTrxListBlg(dbSession, bbTestOpt, trxCounters));
		
		BbTestTrxShowBlg showTrx= new BbTestTrxShowBlg(dbSession, bbTestOpt, trxCounters);
		trxList.add(showTrx);
					
		BbTestTrxUpdateAcs updateAcsTrx = new BbTestTrxUpdateAcs(dbSession, bbTestOpt,  trxCounters);
		updateAcsTrx.setBlogDBFetcher(showTrx);
		updateAcsTrx.setAcsCntUpdateCache(accessCountCache);
		trxList.add(updateAcsTrx);
		
		BbTestTrxUpdateCmt updateCmtTrx = new BbTestTrxUpdateCmt(dbSession, bbTestOpt, trxCounters);
		updateCmtTrx.setBlogDBFetcher(showTrx);
		trxList.add(updateCmtTrx);
		
		BbTestTrxShowSiblings showSiblingsTrx = new BbTestTrxShowSiblings(dbSession, bbTestOpt, trxCounters);
		showSiblingsTrx.setBlogDBFetcher(showTrx);
		trxList.add(showSiblingsTrx);		
		
		trxList.add(new BbTestTrxPublishBlg(dbSession, bbTestOpt, trxCounters));
		
		trxList.add(new BbTestTrxUpdateBlg(dbSession, bbTestOpt, trxCounters));
		
		prepare();
	}
	
	private void initialiseProb() throws Exception {
		probabilities = new double[trxTypeNum];
		
		long totalTrx = 0;
		for (int i = 0; i < trxTypeNum; i++) {
			totalTrx += trxList.get(i).getPct();
		}
		for (int i = 0; i < trxTypeNum; i++) {
			probabilities[i] = (double) trxList.get(i).getPct() / totalTrx;
		}
        
        double sum = 0;
        for (int i = 0; i < trxTypeNum; i++) {
        	sum += probabilities[i];
        	probabilities[i] = sum;
        }
		randomGenerator = new Random();
	}
	
	private void prepare() throws Exception {
		for (int i = 0; i < trxTypeNum; i++)
			trxList.get(i).prepare();
	}
	
	/**
	 * get a random transaction
	 * @return
	 */
	public BbTestTransaction getRandomTrx() {
		double p = randomGenerator.nextDouble();
		
		int low = 0;
		int high = trxTypeNum - 1;
		while(low <= high) {
			int n = (high + low) / 2;
			if (probabilities[n] < p){
				low = n + 1;
			} else if (probabilities[n] > p) {
				high = n - 1;
			} else {
				return trxList.get(n);
			}
		}
		if (high < 0) {
			assert(high == -1);
			assert(p <= probabilities[0]);
			return trxList.get(0);
		} else {
			assert(p <= probabilities[high + 1]);
			assert(p > probabilities[high]);
			return trxList.get(high + 1);
		}
	}
	
	public List<BbTestTransaction> getAllTrxList() {
		return trxList;
	}
}
