package com.netease.webbench.blogbench;

import java.sql.ResultSet;

import junit.framework.TestCase;

import com.netease.webbench.blogbench.misc.BbTestOptPair;
import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.blogbench.misc.ParameterGenerator;
import com.netease.webbench.blogbench.misc.UnitTestHelper;
import com.netease.webbench.blogbench.operation.BlogbenchOperationType;
import com.netease.webbench.blogbench.thread.BbTestInsertThread;
import com.netease.webbench.blogbench.thread.BlgRecordProducer;
import com.netease.webbench.blogbench.thread.ThreadBarrier;
import com.netease.webbench.common.DbOptions;
import com.netease.webbench.common.DbSession;

public class BbTestInsertThreadTest extends TestCase {
	
	private BbTestOptions bbTestOpt;
	private DbOptions dbOpt;
	private ParameterGenerator pg;

	public BbTestInsertThreadTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		
		bbTestOpt = UnitTestHelper.createDflBbTestOpt();
		bbTestOpt.setOperType(BlogbenchOperationType.LOAD);
		dbOpt = UnitTestHelper.createDflDbOpt();
		
		pg = new ParameterGenerator();
		pg.init(bbTestOpt, dbOpt);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testRun() {
		try {
			truncateTbl();
			assertEquals(0, queryTblSize());
			
			int insertRows = 10000;
			BlgRecordProducer producer = new BlgRecordProducer(pg, insertRows);
			producer.start();
			
			ThreadBarrier barrier = new ThreadBarrier();
			
			BbTestInsertThread []thrds = new BbTestInsertThread[bbTestOpt.getLoadThreads()];
			for (int i = 0; i < bbTestOpt.getLoadThreads(); i++) {
				thrds[i] = new BbTestInsertThread(new BbTestOptPair(bbTestOpt, dbOpt), pg, insertRows / bbTestOpt.getLoadThreads(), barrier, producer);
				assertFalse(thrds[i].isWaiting());
			}
			for (int i = 0; i < bbTestOpt.getLoadThreads(); i++) {
				thrds[i].start();
				Thread.sleep(10);
				assertTrue(thrds[i].isWaiting());
			}
			barrier.removeBarrier();
			Thread.sleep(100);
			for (int i = 0; i < bbTestOpt.getLoadThreads(); i++) {
				assertFalse(thrds[i].isWaiting());
			}
			
			for (int i = 0; i < bbTestOpt.getLoadThreads(); i++) {
				thrds[i].join();
				assertEquals(insertRows / bbTestOpt.getLoadThreads(), thrds[i].getRecordInserted());
			}
			producer.join();
			
			assertEquals(insertRows, queryTblSize());
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	private long queryTblSize() throws Exception {
		DbSession dbSession = new DbSession(dbOpt);
		try {
			ResultSet rs = dbSession.query("select count(*) as total from "
					+ bbTestOpt.getTbName());
			if (rs != null && rs.next()) {
				long tblSize = rs.getLong("total");
				return tblSize;
			} else {
				return 0;
			}
		} finally {
			dbSession.close();
		}
	}
	
	private void truncateTbl() throws Exception  {
		DbSession dbSession = new DbSession(dbOpt);
		try {
			dbSession.query("truncate table " + bbTestOpt.getTbName());
		} finally {
			dbSession.close();
		}
	}
}
