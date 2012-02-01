package com.netease.webbench.blogbench.memcached;

import junit.framework.TestCase;

import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.blogbench.misc.UnitTestHelper;
import com.netease.webbench.blogbench.statis.AcsCntFlushTaskStatis;
import com.netease.webbench.common.DbOptions;
import com.netease.webbench.blogbench.blog.*;
import com.netease.webbench.blogbench.operation.BlogbenchOperationType;

public class AcsCntFlushTaskTest extends TestCase {
	
	private  AcsCntFlushTask task; 
	private BbTestOptions bbTestOpt;
	private DbOptions dbOpt;
	private AcsCntFlushTaskStatis acsCntFlushTaskStatis;
	private GlobalAcsCntCache globalAcsCache;

	public AcsCntFlushTaskTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		
		bbTestOpt = UnitTestHelper.createDflBbTestOpt();
		bbTestOpt.setOperType(BlogbenchOperationType.RUN);
		bbTestOpt.setUsedMemcached(true);
		dbOpt = UnitTestHelper.createDflDbOpt();

		acsCntFlushTaskStatis = new AcsCntFlushTaskStatis();
		globalAcsCache = new GlobalAcsCntCache(bbTestOpt.getAcsCountTrxCacheSize());
		task = new AcsCntFlushTask(acsCntFlushTaskStatis, globalAcsCache, dbOpt, bbTestOpt);
		
		MemcachedManager mm = MemcachedManager.getInstance();
		mm.init(bbTestOpt);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		//MemcachedManager.getInstance().shutdownAll();
	}

	public void testRun() {
		try {
			assertEquals(task.getState(), Thread.State.NEW);
			task.start();
			Thread.sleep(1000);
			assertEquals(task.getState(), Thread.State.TIMED_WAITING);		
			
			for (int i = 0; i < 10000; i++) {
				BlogInfoWithAcs acsInfo = new BlogInfoWithAcs(i + 1, i + 1, i);
				globalAcsCache.cacheUpdate(acsInfo);
			}
			
			task.myWakeUp();
			Thread.sleep(100);
			assertEquals(task.getState(), Thread.State.RUNNABLE);	
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void testCancel() {
		try {
			assertEquals(task.getState(), Thread.State.NEW);
		
			for (int i = 0; i < 10000; i++) {
				BlogInfoWithAcs acsInfo = new BlogInfoWithAcs(i + 1, i + 1, i);
				globalAcsCache.cacheUpdate(acsInfo);
			}

			task.start();
			Thread.sleep(1000);
			assertEquals(task.getState(), Thread.State.RUNNABLE);	
			
			task.cancel();
			Thread.sleep(1000);
			assertEquals(task.getState(), Thread.State.TERMINATED);	
			
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
}
