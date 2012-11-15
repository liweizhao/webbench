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
package com.netease.webbench.blogbench.thread;

import java.sql.SQLException;

import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.blogbench.misc.ParameterGenerator;
import com.netease.webbench.blogbench.statis.BlogbenchCounters;
import com.netease.webbench.blogbench.transaction.BbTestTransaction;
import com.netease.webbench.blogbench.transaction.BbTestTrxPool;
import com.netease.webbench.common.DbOptions;

/**
 * blogbench run test thread
 * @author LI WEIZHAO
 */
public class BbTestRunThread extends BbTestThread {
	public static final int PRINT_STAR_PERIOD_MASK = 1023;
	
	private BbTestTrxPool trxPool;
	
	private BlogbenchCounters trxCounter;

	private ThreadRunFlagTimer runFlagTimer;
	
	private ParameterGenerator paraGen;
	
	public BbTestRunThread(DbOptions dbOpt, BbTestOptions bbTestOpt, 
			ParameterGenerator paraGen,	BlogbenchCounters trxCounter, 
			ThreadRunFlagTimer runFlagTimer) throws Exception {
		this(dbOpt, bbTestOpt, paraGen, trxCounter, runFlagTimer, null);
	}
	
	public BbTestRunThread(DbOptions dbOpt, BbTestOptions bbTestOpt, 
			ParameterGenerator paraGen,
			BlogbenchCounters trxCounters, 
			ThreadRunFlagTimer runFlagTimer,
			ThreadBarrier barrier
			) throws Exception {
		super(barrier, dbOpt, bbTestOpt);
		this.trxCounter = trxCounters;
		this.runFlagTimer = runFlagTimer;
		this.trxPool = new BbTestTrxPool(blogDao, bbTestOpt, trxCounters);
		this.paraGen = paraGen;
	}

	public void run() {
		try {
			if (barrier != null) {
				myWait();
			}
			/* loop and execute transaction */
			while (true) {
				BbTestTransaction trx = trxPool.getRandomTrx();
				trx.execTrx(paraGen);

				long totalTrx = trxCounter.getTotalTrxCounter().getTrxCount();
				
				barrier.getSyncLock().lock();
				try {
					if (totalTrx != 0 && (totalTrx & PRINT_STAR_PERIOD_MASK) == 0) {
						System.out.print("*");
					}
				} finally {
					barrier.getSyncLock().unlock();
				}
				
				if (totalTrx >= bbTestOpt.getMaxTran()
						|| !runFlagTimer.getRunFlag()) {
					break;
				}
			}
			exitErrorCode = 0;
		} catch (SQLException e) {
			if (e.getErrorCode() == 1205) {
				if (bbTestOpt.isDebug()) {
					System.err.println("DeadLock occured in Thread(ID:" + this.getId() + 
							") when excute transaction!");
				}
			} else {
				exitErrorCode = 1;
				System.err.println("Error occured in thread(ID:" + this.getId() + 
						") when excute transaction!");
				System.err.println("SQL ERROR CODE:" + e.getErrorCode());
				System.err.println(e.getMessage());
				runFlagTimer.setExpired();
				e.printStackTrace();
			}
		} catch (Exception e) {
			exitErrorCode = 1;
			runFlagTimer.setExpired();
			e.printStackTrace();
		}
	}
}
