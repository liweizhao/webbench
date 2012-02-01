package com.netease.webbench.blogbench;

import junit.framework.TestCase;

import com.netease.webbench.blogbench.misc.BbTestOptPair;
import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.blogbench.misc.ParameterGenerator;
import com.netease.webbench.blogbench.misc.UnitTestHelper;
import com.netease.webbench.blogbench.operation.BlogbenchOperationType;
import com.netease.webbench.blogbench.statis.BlogbenchCounters;
import com.netease.webbench.blogbench.thread.BbTestRunThread;
import com.netease.webbench.blogbench.thread.ThreadBarrier;
import com.netease.webbench.blogbench.thread.ThreadRunFlagTimer;
import com.netease.webbench.blogbench.transaction.BbTestTrxType;
import com.netease.webbench.common.DbOptions;

public class BbTestRunThreadTest extends TestCase {

	public BbTestRunThreadTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testRun() {
		try {
			
		UnitTestHelper.createTestTable(UnitTestHelper.createDflDbOpt(), UnitTestHelper.createDflBbTestOpt());
		
		DbOptions dbOpt = UnitTestHelper.createDflDbOpt();
		BbTestOptions bbTestOpt = UnitTestHelper.createDflBbTestOpt();
		bbTestOpt.setMaxTime(1);
		bbTestOpt.setOperType(BlogbenchOperationType.RUN);
		ParameterGenerator pg = new ParameterGenerator();
		BlogbenchCounters counters = new BlogbenchCounters(BbTestTrxType.TRX_TYPE_NUM);
		ThreadBarrier barrier = new ThreadBarrier();
		ThreadRunFlagTimer runFlagTimer = new ThreadRunFlagTimer();
			pg.init(bbTestOpt, dbOpt);
			
			BbTestRunThread []threadGrp = new BbTestRunThread[bbTestOpt.getThreads()];
			for (int i = 0; i < bbTestOpt.getThreads(); i++) {
				threadGrp[i] = new BbTestRunThread(new BbTestOptPair(bbTestOpt, dbOpt), 
						pg, counters, runFlagTimer, barrier, null);
				threadGrp[i].start();
			}			
			Thread.sleep(3000);
			
			for (int i = 0; i < bbTestOpt.getThreads(); i++) {
				assertTrue(threadGrp[i].isWaiting());
			}
			
			barrier.removeBarrier();
			Thread.sleep(3000);
			
			for (int i = 0; i < bbTestOpt.getThreads(); i++) {
				assertFalse(threadGrp[i].isWaiting());
			}

			runFlagTimer.start(bbTestOpt.getMaxTime() * 1000);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}		
	}
}
