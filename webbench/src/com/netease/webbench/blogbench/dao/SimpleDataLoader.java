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
package com.netease.webbench.blogbench.dao;

import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.blogbench.misc.ParameterGenerator;
import com.netease.webbench.blogbench.statis.LoadDataStatis;
import com.netease.webbench.blogbench.thread.BbTestInsertThread;
import com.netease.webbench.blogbench.thread.BlgRecordProducer;
import com.netease.webbench.blogbench.thread.ThreadBarrier;
import com.netease.webbench.common.DbOptions;
import com.netease.webbench.common.Util;

public class SimpleDataLoader implements DataLoader {
	/* default number of threads to execute insert */
	public static final int DEFAULT_INSERT_THREAD_CNT = 8;

	/* insert threads group */
	protected BbTestInsertThread[] insertThrdGrp = null;
	
	protected DbOptions dbOpt;
	protected BbTestOptions bbTestOpt;
	protected ParameterGenerator paraGen;
	protected final LoadDataStatis statis;
	
	protected BlgRecordProducer producer = null;
	protected boolean isLoadSuccessful = false;
	
	public SimpleDataLoader(DbOptions dbOpt, BbTestOptions bbTestOpt,
			ParameterGenerator paraGen) {
		this.dbOpt = dbOpt;
		this.bbTestOpt = bbTestOpt;
		this.statis = new LoadDataStatis();
		this.paraGen = paraGen;
	}

	@Override
	public void pre() throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void load() throws Exception {
		producer = new BlgRecordProducer(paraGen, bbTestOpt.getTbSize());
		producer.start();
		
		//create insert threads and load data
		isLoadSuccessful = createThreadsAndLoadData(producer);
	}
	
	@Override
	public void post() throws Exception {
		if (isLoadSuccessful) {
			if (producer != null)
				producer.join();
		} else {
			if (producer != null)
				producer.forceExit();
			throw new Exception("Load data failed!");
		}
	}
	
	/**
	 * create insert threads group and load data
	 * @return is loading data successful ?
	 * @throws Exception 
	 */
	protected boolean createThreadsAndLoadData(BlgRecordProducer producer) throws Exception {		
		int insertThrdCnt = bbTestOpt.getLoadThreads();
		ThreadBarrier barrier = new ThreadBarrier();
		createInsertThrdGrp(insertThrdCnt, barrier, producer);
		
		int index = 0;
		while (index < insertThrdCnt) {
			if (bbTestOpt.isDebug()) {
				if (insertThrdGrp[index] == null)
					throw new Exception("Create insert thread  " + index + " failed!");
			}
			if (insertThrdGrp[index].isWaiting()) {
				index++;
			} else {
				Thread.sleep(50);
			}
		}
		
		long start = Util.currentTimeMillis();
		
		//wake up all threads to work
		barrier.removeBarrier();
		
		//wait for all insert threads to exit
		boolean caughtErr = false;
		for (int i = 0; i < insertThrdCnt; i++) {
			insertThrdGrp[i].join();
			insertThrdGrp[i].clean();
			if (0 != insertThrdGrp[i].getErrorCode())
				caughtErr = true;
		}
		
		statis.addLoadDataTimeWaste(Util.currentTimeMillis() - start);		
		return !caughtErr;
	}
	
	/**
	 *  create insert threads group
	 *  @param thrdCnt
	 *  @param barrier
	 *  @return
	 */
	protected void createInsertThrdGrp(int thrdCnt, ThreadBarrier barrier, BlgRecordProducer producer) throws Exception {	
		if (thrdCnt < 1) {
			throw new Exception("Number of load threads can't be " + thrdCnt + "!");
		}
		insertThrdGrp = new BbTestInsertThread[thrdCnt];
		
		long rcdToInsert = bbTestOpt.getTbSize() / thrdCnt;
		
		for (int i = 0; i < thrdCnt; i++) {
			long r = rcdToInsert;
			if (i == 0) {
				r = rcdToInsert + bbTestOpt.getTbSize() % thrdCnt;
			}
			insertThrdGrp[i] = new BbTestInsertThread(dbOpt, bbTestOpt, 
					r, barrier, producer);
			insertThrdGrp[i].start();
		}
	}
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.misc.LoadProgress#getProgress()
	 */
	public double getProgress() throws Exception {
		return getRecordInserted() * 1.0 / bbTestOpt.getTbSize();
	}
	
	/**
	 * get number of blog records already inserted
	 * @return
	 * @throws Exception
	 */
	public long getRecordInserted() throws Exception {
		long recordInserted = 0;
		if (insertThrdGrp != null) {
			for (int i = 0; i <  bbTestOpt.getLoadThreads(); i++) {
				if (insertThrdGrp[i] != null) 
					recordInserted += insertThrdGrp[i].getRecordInserted();
			}
			if (bbTestOpt.isDebug() && recordInserted > bbTestOpt.getTbSize()) {
				throw new Exception("Wrong num of records inserted!" + recordInserted);
			}
		}
		return recordInserted;
	}

	@Override
	public String getLoadSummary() {
		// TODO Auto-generated method stub
		return "Load Data Summary:";
	}

	@Override
	public final LoadDataStatis getStatistics() {
		return this.statis;
	}
}
