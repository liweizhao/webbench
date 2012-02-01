package com.netease.webbench.test.common;

import junit.framework.TestCase;

import com.netease.webbench.common.DynamicArray;

public class DynamicArrayTest extends TestCase {
	
	private long size;
	private DynamicArray<Integer> da;

	public DynamicArrayTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		size = 1000000;
		da = new DynamicArray<Integer>(size);
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testAdd() {
		for (int i = 0; i < 10000; i++) {
			da.append(i);
		}
		for (int i = 0; i < 10000; i++)
			assertEquals(new Integer(i), da.get(i));
		assertEquals(da.size(), 10000);
	}

	public void testGet() {
		for (int i = 0; i < 10000; i++) {
			da.append(i);
		}
		for (int i = 0; i < 10000; i++)
			assertEquals(new Integer(i), da.get(i));
	}

	public void testSet() {
		for (int i = 0; i < 10000; i++) {
			da.append(i);
		}
		for (int i = 0; i < 10000; i++) {
			da.set(i, 10000 - i);
		}
		for (int i = 0; i < 10000; i++) {
			assertEquals(new Integer(10000 - i), da.get(i));
		}
	}

	public void testSize() {
		for (int i = 0; i < 10000; i++) {
			da.append(i);
		}
		for (int i = 0; i < 10000; i++) {
			da.set(i, 10000 - i);
		}
		assertEquals(da.size(), 10000);
	}

}
