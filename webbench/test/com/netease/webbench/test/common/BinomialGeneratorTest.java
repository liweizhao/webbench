package com.netease.webbench.test.common;

import junit.framework.TestCase;

import com.netease.webbench.random.BinomialGenerator;

public class BinomialGeneratorTest extends TestCase {

	public BinomialGeneratorTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGetBinomialRandomNum() {
		double prop = 2000;
		int max = 50000;
		
		try {
			long total = 0;
			long testTimes = 1000000;
			int testMax = 0;
			BinomialGenerator bg = new BinomialGenerator(prop, max, true, 0.01);
			for (int i = 0; i < testTimes; i++) {
				int n = bg.getBinomialRandomNum();
				total += n;
				if (n > testMax)
					testMax = n;
			}
			
			int testProp = (int)(total / testTimes);
			assertEquals(testProp >= prop - 10 && testProp <= prop + 10, true);
			assertEquals(testMax >= max - 50 && testMax <= testMax + 50, true);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
