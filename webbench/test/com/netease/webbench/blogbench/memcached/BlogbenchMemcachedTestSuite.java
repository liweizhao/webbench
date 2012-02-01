package com.netease.webbench.blogbench.memcached;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class BlogbenchMemcachedTestSuite extends TestCase {

	public BlogbenchMemcachedTestSuite(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();

		suite.addTest(new MemcachedClientNioImplTest(""));
		
		return suite;
	}
}
