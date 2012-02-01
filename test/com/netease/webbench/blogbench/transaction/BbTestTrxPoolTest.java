package com.netease.webbench.blogbench.transaction;

import junit.framework.TestCase;

import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.blogbench.misc.UnitTestHelper;
import com.netease.webbench.blogbench.statis.BlogbenchCounters;
import com.netease.webbench.common.DbOptions;
import com.netease.webbench.common.DbSession;

public class BbTestTrxPoolTest extends TestCase {
	DbSession dbSession;
	DbOptions dbOpt;
	BbTestOptions bbTestOpt;
	BlogbenchCounters counters;
	BbTestTrxPool trxPool;
	int trxNum;

	public BbTestTrxPoolTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		dbOpt = UnitTestHelper.createDflDbOpt();
		
		bbTestOpt = UnitTestHelper.createDflBbTestOpt();
		dbSession = new DbSession(dbOpt);
		
		trxNum = BbTestTrxType.TRX_TYPE_NUM;
		counters = new BlogbenchCounters(trxNum);
		trxPool = new BbTestTrxPool(dbSession, bbTestOpt, counters, null, trxNum);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGetRandomTrx() {
		int totalPct = 0;
		long []count = new long[trxNum];
		for (int i = 0; i < trxNum; i++) {
			totalPct += trxPool.getAllTrxList().get(i).getPct();
			count[i] = 0;
		}
		for (int i = 0; i < 1000000; i++) {
			BbTestTransaction trx = trxPool.getRandomTrx();
			int idx = BbTestTrxType.getTrxIndex(trx.getTrxType());
			count[idx]++;
		}
		long total = 0; 
		for (int i = 0; i < trxNum; i++) {
			System.out.println("count[" + i + "]: " + count[i]);
			total += count[i];
		} 
		for (int i = 0; i < trxNum; i++) {
			double pct = count[i] * 1.0 / total;
			double pct2 = trxPool.getAllTrxList().get(i).getPct() * 1.0 / totalPct;
			System.out.println(trxPool.getAllTrxList().get(i).getTrxName() + ", pct:" + pct + ", pct2: " + pct2);
			assertTrue(pct >= pct2 * 0.95 && pct <= pct2 * 1.05);
		}
	}
}
