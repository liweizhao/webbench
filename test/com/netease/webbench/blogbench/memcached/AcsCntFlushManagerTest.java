package com.netease.webbench.blogbench.memcached;

import java.lang.reflect.Field;

import junit.framework.TestCase;

import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.blogbench.misc.UnitTestHelper;
import com.netease.webbench.common.DbOptions;
import com.netease.webbench.blogbench.blog.*;
import com.netease.webbench.blogbench.operation.BlogbenchOperationType;

public class AcsCntFlushManagerTest extends TestCase {
	
	private AcsCntFlushManager instance; 
	private BbTestOptions bbTestOpt;
	private DbOptions dbOpt;

	public AcsCntFlushManagerTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		
		instance = AcsCntFlushManager.getInstance();
		bbTestOpt = UnitTestHelper.createDflBbTestOpt();
		bbTestOpt.setOperType(BlogbenchOperationType.RUN);
		bbTestOpt.setUsedMemcached(true);
		dbOpt = UnitTestHelper.createDflDbOpt();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGetInstance() {
		assertTrue(null != AcsCntFlushManager.getInstance());
		assertEquals(AcsCntFlushManager.getInstance(), AcsCntFlushManager.getInstance());
	}

	public void testInit() {
		int flushTaskNum = 4;
		try {
			instance.init(bbTestOpt, dbOpt, flushTaskNum);
			
			Field field = instance.getClass().getDeclaredField("globalAcsCntCache");
			field.setAccessible(true);
			GlobalAcsCntCache cache = (GlobalAcsCntCache)field.get(instance);
			assertTrue(cache != null);
			field.setAccessible(false);
			
			Field field2 = instance.getClass().getDeclaredField("acsFlushTimerTasks");
			field2.setAccessible(true);
			AcsCntFlushTask []tasks = (AcsCntFlushTask[])field2.get(instance);
			assertTrue(tasks != null);
			for (int i = 0; i < tasks.length; i++) {
				assertTrue(tasks[i] != null);
				assertEquals(tasks[i].getState(), Thread.State.NEW);
			}
			field2.setAccessible(false);
			
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void testGetGlobalAcsCntCache() {
		int flushTaskNum = 4;
		try {
			instance.init(bbTestOpt, dbOpt, flushTaskNum);
			assertTrue(instance.getGlobalAcsCntCache() != null);			
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void testStartAllFlushTask() {
		int flushTaskNum = 4;
		try {
			MemcachedManager mm = MemcachedManager.getInstance();
			mm.init(bbTestOpt);
			
			instance.init(bbTestOpt, dbOpt, flushTaskNum);
			
			Field field = instance.getClass().getDeclaredField("globalAcsCntCache");
			field.setAccessible(true);
			GlobalAcsCntCache cache = (GlobalAcsCntCache)field.get(instance);
			for (int i = 0; i < 10000; i++) {
				assertTrue(cache.cacheUpdate(new BlogInfoWithAcs(i + 1, i + 1, (int)Math.random())));
			}
			field.setAccessible(false);
			
			Thread.sleep(1000);
			instance.startAllFlushTask();
			
			Field field2 = instance.getClass().getDeclaredField("acsFlushTimerTasks");
			field2.setAccessible(true);
			AcsCntFlushTask []tasks = (AcsCntFlushTask[])field2.get(instance);
			assertTrue(tasks != null);
			for (int i = 0; i < tasks.length; i++)
				assertEquals(tasks[i].getState(), Thread.State.RUNNABLE);
			
			instance.cancelAllFlushTask();
			
			for (int i = 0; i < tasks.length; i++)
				assertEquals(tasks[i].getState(), Thread.State.TERMINATED);
			
			field2.setAccessible(false);	
			
			mm.shutdownAll();
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
}
