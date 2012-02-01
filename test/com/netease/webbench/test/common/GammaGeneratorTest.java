package com.netease.webbench.test.common;

import junit.framework.TestCase;

import com.netease.webbench.random.GammaGenerator;

public class GammaGeneratorTest extends TestCase {

	public GammaGeneratorTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGetGammaRandomNum() {
		int per = 2000;
		int max = 20000;
		GammaGenerator gamaGen = new GammaGenerator(per * 0.5, per, max, true);
		
		long total = 0;
		int curMin = max;
		int curMax = 0;
		int testTimes = 10000000;
		for (int i = 0; i < testTimes; i++) {
			int n = gamaGen.getGammaRandomNum();
			if (n > curMax)
				curMax = n;
			if (n < curMin)
				curMin = n;
			total += n;
		}
		
		int testPer = (int)(total / testTimes);
		
		System.out.println("perspect: " + testPer + ", curMin: " + curMin + ", curMax: " + curMax);
		
		assertTrue(testPer >= per * 0.99 && testPer <= per * 1.01);
		
		assertTrue(0 <= curMin && curMin <= 1);
		
		assertTrue(curMax >= max * 0.90 && curMax <= max * 1.1);		
	}
}
