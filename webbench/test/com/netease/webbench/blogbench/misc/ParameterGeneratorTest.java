package com.netease.webbench.blogbench.misc;

import java.util.Arrays;

import junit.framework.TestCase;

import com.netease.webbench.blogbench.blog.Blog;
import com.netease.webbench.blogbench.blog.BlogInfoWithPub;
import com.netease.webbench.blogbench.operation.BlogbenchOperationType;
import com.netease.webbench.common.DbOptions;

public class ParameterGeneratorTest extends TestCase {
	
	private ParameterGenerator pg;
	private BbTestOptions bbTestOpt;
	private DbOptions dbOpt;
	
	public ParameterGeneratorTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {	
		super.setUp();	
		if (super.getName().equals("testGetZipfRandomBlog")
				|| super.getName().equals("testUpdateBlgMapArr")) {
			initRun();
		} else {
			initLoad();
		}
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	private void initLoad() throws Exception {
		dbOpt = UnitTestHelper.createDflDbOpt();
		bbTestOpt = UnitTestHelper.createDflBbTestOpt();
		bbTestOpt.setOperType(BlogbenchOperationType.LOAD);
		bbTestOpt.setTbSize(1000000);
	
		pg = new ParameterGenerator();
		pg.init(bbTestOpt, dbOpt);
	}
	
	private void initRun() throws Exception {
		dbOpt = UnitTestHelper.createDflDbOpt();
		bbTestOpt = UnitTestHelper.createDflBbTestOpt();
		bbTestOpt.setTbSize(10000);
	
		UnitTestHelper.createTestTable(UnitTestHelper.createDflDbOpt(), bbTestOpt);
		bbTestOpt.setOperType(BlogbenchOperationType.RUN);
		pg = new ParameterGenerator();
		pg.init(bbTestOpt, dbOpt);
	}
	
	public void testGetZipfUserId() {
		int testTimes = 2000000;
		int userSize = (int)(bbTestOpt.getTbSize() * 0.2);
		int []dist = new int[userSize];
		for (int i = 0; i < userSize; i++) {
			dist[i] = 0;
		}
		for (int i = 0; i < testTimes; i++) {
			dist[(int)pg.getZipfUserId() - 1]++;
		}
		Arrays.sort(dist);
		
		//前百分之5%的概率要占到95%
		int total = 0;
		int pct5 = (int)(bbTestOpt.getUserZipfPct() / 100.0 * userSize);
		for (int i = userSize - 1; i > userSize - pct5; i--) {
			total += dist[i];
		}
		
		double realReqPct = total * 100.0 / testTimes;
		System.out.println("realReqPct:" + realReqPct);
		assertTrue(realReqPct >= bbTestOpt.getUserZipfRes() * 0.99 && realReqPct <= bbTestOpt.getUserZipfRes() * 1.01);
	}

	public void testGetZipfRandomBlog() {
		int testTimes = 2000000;
		int tblSize = (int)bbTestOpt.getTbSize();
		int []dist = new int[tblSize];
		for (int i = 0; i < tblSize; i++) {
			dist[i] = 0;
		}
		for (int i = 0; i < testTimes; i++) {
			BlogInfoWithPub blog = pg.getZipfRandomBlog();
			dist[(int)blog.getBlogId() - 1]++;
		}
		Arrays.sort(dist);
		
		//前百分之5%的概率要占到95%
		int total = 0;
		int pct5 = (int)(bbTestOpt.getBlgZipfPct() / 100.0 * tblSize);
		for (int i = tblSize - 1; i > tblSize - pct5; i--) {
			total += dist[i];
		}
		
		double realReqPct = total * 100.0 / testTimes;
		System.out.println("realReqPct:" + realReqPct);
		assertTrue(realReqPct >= bbTestOpt.getBlgZipfRes() * 0.99 && realReqPct <= bbTestOpt.getBlgZipfRes() * 1.01);
	}

	public void testGetCurrentTime() {
		assertEquals(System.currentTimeMillis(), pg.getCurrentTime());
	}

	public void testGetTitle() {
		int testTimes = 10000;
		long total = 0;
		int min = bbTestOpt.getMaxTtlSize();
		int max = -1;
		for (int i = 0; i < testTimes; i++) {
			int len = pg.getTitle().length();
			total += len;
			if (len < min)
				min = len;
			if (len > max)
				max = len;
		}
		int avg = (int)(total / testTimes);
		System.out.println("avg:" + avg + ", min: " + min + ", max: " + max);
		assertEquals(avg, (bbTestOpt.getMinTtlSize() + bbTestOpt.getMaxTtlSize()) / 2);
		assertEquals(min, bbTestOpt.getMinTtlSize());
		assertEquals(max, bbTestOpt.getMaxTtlSize());
	}

	public void testGetAbs() {
		int testTimes = 10000;
		long total = 0;
		int min = bbTestOpt.getMaxAbsSize();
		int max = -1;
		byte[] contentForTest = new byte[bbTestOpt.getMaxAbsSize()];
		for (int i = 0; i < bbTestOpt.getMaxAbsSize(); i++)
			contentForTest[i] = 0;
		
		try {
			for (int i = 0; i < testTimes; i++) {
				int len = pg.getAbs(contentForTest).length();
				total += len;
				if (len < min)
					min = len;
				if (len > max)
					max = len;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		int avg = (int)(total / testTimes);
		//System.out.println("avg:" + avg + ", min: " + min + ", max: " + max);
		int exactAvg = (bbTestOpt.getMinAbsSize() + bbTestOpt.getMaxAbsSize()) / 2;
		assertTrue(avg < exactAvg * 1.05 && avg > exactAvg * 0.95);
		assertEquals(min, bbTestOpt.getMinAbsSize());
		assertEquals(max, bbTestOpt.getMaxAbsSize());
	}

	public void testGetContent() {
		int testTimes = 1000000;
		long total = 0;
		int min = bbTestOpt.getMaxCntSize();
		int max = bbTestOpt.getMinCntSize();
		
		int range = bbTestOpt.getMaxCntSize() - bbTestOpt.getMinAbsSize() + 1;
		int []distribute = new int[range];
		for (int i = 0; i < range; i++)
			distribute[i] = 0;
		
		try {
			for (int i = 0; i < testTimes; i++) {
				int len = pg.getContent().length;
				total += len;
				if (len < min)
					min = len;
				if (len > max)
					max = len;
				distribute[len - bbTestOpt.getMinCntSize()]++;
			}
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		
		int avg = (int) (total / testTimes);
		System.out.println("avg:" + avg + ", min: " + min + ", max: " + max);
		
		int exactAvg = bbTestOpt.getAvgCntSize();
		assertTrue(avg < exactAvg * 1.1 && avg > exactAvg * 0.9);
		assertTrue(min < bbTestOpt.getMinCntSize() * 1.1 && min > bbTestOpt.getMinCntSize() * 0.9);
		assertTrue(max < bbTestOpt.getMaxCntSize() * 1.1 && max > bbTestOpt.getMaxCntSize() * 0.9);
		
		int middleLine = (bbTestOpt.getMinCntSize() + bbTestOpt.getMaxCntSize()) / 2;
		int leftPart = 0;
		for (int i = 0; i < middleLine; i++)
			leftPart += distribute[i];
		int rightPart = 0;
		for (int i = middleLine; i < range; i++) {
			rightPart += distribute[i];
		}
		assertTrue(leftPart > rightPart);
	}

	public void testGetAllowView() {
		int []distCount = new int[3];
		for (int i = 0; i < 3; i++) 
			distCount[i] = 0;
		for (int i = 0; i < 100000; i++) {
			int av = pg.getAllowView();
			if (av <= -100)
				distCount[0]++;
			else if (av <= 100)
				distCount[1]++;
			else if (av <= 10000)
				distCount[2]++;
			else
				assertTrue(false);
		}
		int total = distCount[0] + distCount[1] + distCount[2];
		double d = distCount[0] * 1.0 / total;
		//System.out.println(d);
		assertTrue(d < 0.915 && d > 0.895);
		
		d = distCount[1] * 1.0 / total;
		//System.out.println(d);
		assertTrue(d < 0.015 && d > 0.005);
		
		d = distCount[2] * 1.0 / total;
		//System.out.println(d);
		assertTrue(d < 0.085 && d > 0.075);
	}

	public void testGetAccessCount() {
		assertTrue(pg.getAccessCount() == 0);
	}

	public void testGetCommentCount() {
		assertTrue(pg.getCommentCount() == 0);
	}

	public void testUpdateBlgMapArr() {		
		int testTimes = 2000000;
		int tblSize = (int)bbTestOpt.getTbSize();
		
		try {
			for (int i = 0; i < 10000; i++) {
				Blog blog = pg.generateNewBlog();
				pg.updateBlgMapArr(blog.getId(), blog.getUid(), blog.getPublishTime());
				tblSize++;
			}
		} catch (Exception e) {
			assertTrue(false);
		}
		
		int []dist = new int[tblSize];
		for (int i = 0; i < tblSize; i++) {
			dist[i] = 0;
		}
		for (int i = 0; i < testTimes; i++) {
			BlogInfoWithPub blog = pg.getZipfRandomBlog();
			dist[(int)blog.getBlogId() - 1]++;
		}
		Arrays.sort(dist);
		
		//前百分之5%的概率要占到95%
		int total = 0;
		int pct5 = (int)(bbTestOpt.getBlgZipfPct() / 100.0 * tblSize);
		for (int i = tblSize - 1; i > tblSize - pct5; i--) {
			total += dist[i];
		}
		
		double realReqPct = total * 100.0 / testTimes;
		System.out.println("realReqPct:" + realReqPct);
		assertTrue(realReqPct >= bbTestOpt.getBlgZipfRes() * 0.99 && realReqPct <= bbTestOpt.getBlgZipfRes() * 1.01);
	}

	public void testIncreaseAndGetMaxBlogId() {
		long begin = pg.increaseAndGetMaxBlogId();
		for (int i = 1; i <= 100; i++) {
			assertEquals(pg.increaseAndGetMaxBlogId(), begin + i);
		}
	}

	public void testGetBlogHottestPctFreq() {
		double f = pg.getBlogHottestPctFreq() * 100;
		assertTrue(f >= bbTestOpt.getBlgZipfRes() * 0.99 && f <= bbTestOpt.getBlgZipfRes() * 1.01);
	}

	public void testGetUserHottestPctFreq() {
		double f = pg.getUserHottestPctFreq() * 100;
		assertTrue(f >= bbTestOpt.getUserZipfRes() * 0.9 && f <= bbTestOpt.getUserZipfRes() * 1.1);
	}

	public void testGenerateNewBlog() {
		try {
			for (int i = 0; i < 100; i++) {
				Blog blog = pg.generateNewBlog();
				assertEquals(i + 1, blog.getId());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
