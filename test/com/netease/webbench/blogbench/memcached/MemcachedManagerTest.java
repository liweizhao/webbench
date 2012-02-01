package com.netease.webbench.blogbench.memcached;

import java.util.List;

import junit.framework.TestCase;

import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.blogbench.misc.UnitTestHelper;

public class MemcachedManagerTest extends TestCase {
	private BbTestOptions bbTestOpt;
	private MemcachedManager mm;
	
	public MemcachedManagerTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();

		bbTestOpt = UnitTestHelper.createDflBbTestOpt();
		mm = MemcachedManager.getInstance();
		if (!mm.isInitialized()) {
			mm.init(bbTestOpt);
		}
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		
		mm.shutdownAll();
	}

	public void testFlushAllServerCache() {
		try {
			mm.getMajorMcc().set("testFlushAllServerCache", "testFlushAllServerCache");
			assertEquals("testFlushAllServerCache", mm.getMajorMcc().get("testFlushAllServerCache"));
			
			mm.getMinorMcc().set("testFlushAllServerCache", "testFlushAllServerCache");
			assertEquals("testFlushAllServerCache", mm.getMinorMcc().get("testFlushAllServerCache"));
			
			mm.flushAllServerCache();
			assertEquals(null, mm.getMajorMcc().get("testFlushAllServerCache"));
			assertEquals(null, mm.getMinorMcc().get("testFlushAllServerCache"));			
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void testGetInstance() {
		assertTrue(mm != null);
	}

	public void testGetMajorMcc() {
		try {
			assertTrue(null != mm.getMajorMcc());
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void testGetMinorMcc() {
		try {
			assertTrue(null != mm.getMinorMcc());
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@SuppressWarnings("static-access")
	public void testIsUseSpy() {
		try {
			assertFalse(mm.isUseSpy());
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void testGetMemcachedClientImplName() {
		try {
			assertEquals(MemcachedManager.getMemcachedClientImplName(), "MemcachedClientWhlImpl");
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void testGetMajorServerList() {
		List<String> list = mm.getMajorServerList();
		assertEquals(list.size(), 1);
		assertEquals(list.get(0), bbTestOpt.getMainMemcachedAddr());
	}

	public void testGetAcsCntServerList() {
		List<String> list = mm.getAcsCntServerList();
		assertEquals(list.size(), 1);
		assertEquals(list.get(0), bbTestOpt.getMinorMemcachedAddr());
	}

	public void testGetMcImplType() {
		try {
			assertEquals(MemcachedManager.getMcImplType(), MemcachedManager.MemcachedClientImplType.WHL_IMPL);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	public void testShutdownAll() {
		mm.shutdownAll();
		assertTrue(!mm.isInitialized());
		assertTrue(mm.getMinorMcc() == null);
		assertTrue(mm.getMinorMcc() == null);
	}
}
