package com.netease.webbench.blogbench.misc;

import com.netease.webbench.blogbench.thread.ThreadRunFlagTimer;

import junit.framework.TestCase;

public class ThreadRunFlagTimerTest extends TestCase {
	
	ThreadRunFlagTimer timer;

	public ThreadRunFlagTimerTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		
		timer = new ThreadRunFlagTimer();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGetRunFlag() {
		try {
			timer.start(2000);
			assertTrue(timer.getRunFlag());
			Thread.sleep(2010);
			assertFalse(timer.getRunFlag());
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void testSetExpired() {
		assertTrue(timer.getRunFlag());
		timer.setExpired();
		assertFalse(timer.getRunFlag());
	}

}
