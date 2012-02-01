package com.netease.webbench.blogbench.memcached;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import junit.framework.TestCase;

public class MemcachedClientNioImplTest extends TestCase {
	private MemcachedClientIF mc;
	private boolean hasConnected = false;

	public MemcachedClientNioImplTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();

		if (!hasConnected) {
			hasConnected = true;
			try {
				List<String> mccServerList = new ArrayList<String>();
				mccServerList.add("127.0.0.1:8609");
				mc = new MemcachedClientNioImpl(mccServerList, 1, 1024, 128);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		mc.flushAll();
	}

	public void testAddStringObjectLong() {
		try {
			assertEquals(null, mc.get("testAddStringObjectLong"));
			mc.add("testAddStringObjectLong", "testAddStringObjectLong", 2000);
			assertEquals("testAddStringObjectLong", mc.get("testAddStringObjectLong"));
			Thread.sleep(3000);
			mc.get("testAddStringObjectLong");
			assertEquals(null, mc.get("testAddStringObjectLong"));
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void testAddStringObject() {
		try {
			assertEquals(null, mc.get("testAddStringObject"));
			mc.add("testAddStringObject", "testAddStringObject");
			assertEquals("testAddStringObject", mc.get("testAddStringObject"));
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void testDelete() {
		try {
			assertEquals(null, mc.get("testDelete"));
			mc.add("testDelete", "testDelete");
			assertEquals("testDelete", mc.get("testDelete"));
			mc.delete("testDelete");
			assertEquals(null, mc.get("testDelete"));
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void testGetCounter() {
		try {
			assertTrue(mc.getCounter("testGetCounter") < 0);
			long count = mc.addOrIncr("testGetCounter");
			assertEquals(count, mc.getCounter("testGetCounter"));
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void testFlushAll() {
		try {
			assertEquals(null, mc.get("testFlushAll"));
			mc.add("testFlushAll", "testFlushAll");
			assertEquals("testFlushAll", mc.get("testFlushAll"));
			mc.flushAll();
			assertEquals(null, mc.get("testFlushAll"));
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void testIncrString() {
		try {
			assertTrue(mc.incr("testIncrString") < 0);
			long count1 = mc.addOrIncr("testIncrString");
			long count2 = -1;
			for (int i  = 1; i <= 100; i++) {
				count2 = mc.incr("testIncrString");
				assertTrue(count1 + i == count2);
				assertEquals(count2, mc.getCounter("testIncrString"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void testIncrStringInt() {
		try {
			assertTrue(mc.incr("testIncrStringInt") < 0);
			long count1 = mc.addOrIncr("testIncrStringInt");
			int step = 10;
			long count2 = mc.incr("testIncrStringInt", step);
			assertTrue(count1 + step == count2);
			assertEquals(count2, mc.getCounter("testIncrStringInt"));
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void testGetMultiStringArray() {
		try {
			int testTime = 100;
			String []keys = new String[testTime];
			for (int i = 0; i < testTime; i++) {
				keys[i] = "testGetMultiStringArray" + i;
				mc.set(keys[i], keys[i]);
			}
			Map<String, Object> result = mc.getMulti(keys);
			for (int i = 0; i < testTime; i++) {
				assertEquals(keys[i], result.get(keys[i]));
			}
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void testGetMultiCollectionOfString() {
		try {
			int testTime = 100;
			Collection<String> keys = new Vector<String>(testTime);
			for (int i = 0; i < testTime; i++) {
				String key = "testGetMultiStringArray" + i;
				keys.add(key);
				mc.set(key, key);
			}
			Map<String, Object> result = mc.getMulti(keys);
			
			for (int i = 0; i < testTime; i++) {
				assertEquals("testGetMultiStringArray" + i, result.get("testGetMultiStringArray" + i));
			}
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@SuppressWarnings("unchecked")
	public void testGetStats() {
		try {
			Map<String, Map<String, String>> stats = mc.getStats();
			assertTrue(stats != null);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void testReplaceStringObjectLong() {
		try {
			boolean hit = mc.replace("testReplaceStringObjectLong", "testReplaceStringObjectLong");
			assertFalse(hit);
			mc.add("testReplaceStringObjectLong", "testReplaceStringObjectLong");
			hit = mc.replace("testReplaceStringObjectLong", "new", 2000);
			assertTrue(hit);
			assertEquals(mc.get("testReplaceStringObjectLong"), "new");
			Thread.sleep(3000);
			assertEquals(mc.get("testReplaceStringObjectLong"), null);	
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void testReplaceStringObject() {
		try {
			boolean hit = mc.replace("testReplaceStringObject", "testReplaceStringObject");
			assertFalse(hit);
			mc.add("testReplaceStringObject", "testReplaceStringObject");
			hit = mc.replace("testReplaceStringObject", "new");
			assertTrue(hit);
			assertEquals(mc.get("testReplaceStringObject"), "new");
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void testSetStringObjectLong() {
		try {
			boolean hit = mc.set("testSetStringObject", "testSetStringObject", 2000);
			assertTrue(hit);
			assertEquals("testSetStringObject", mc.get("testSetStringObject"));
			Thread.sleep(3000);
			assertEquals(null, mc.get("testSetStringObject"));
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void testSetStringObject() {
		try {
			boolean hit = mc.set("testSetStringObject", "testSetStringObject");
			assertTrue(hit);
			assertEquals("testSetStringObject", mc.get("testSetStringObject"));
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
}
