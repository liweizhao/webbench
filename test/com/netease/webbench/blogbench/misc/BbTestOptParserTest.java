package com.netease.webbench.blogbench.misc;

import junit.framework.TestCase;

import com.netease.util.Pair;
import com.netease.webbench.blogbench.operation.BlogbenchOperationType;

public class BbTestOptParserTest extends TestCase {

	public BbTestOptParserTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testParse() {
		String tbName = "BlogTest";
		long tbSize = 100000;
		String tbEngine = "innodb_test";
		String ntseCreateTblArgs = "\"zzzzzz\"";
		int  blgZipfPct = 15;
		int blgZipfRes = 25;
		int blgZipfPart = 3000;
		int userZipfPct = 35;
		int userZipfRes = 55;
		int userZipfPart = 2000;
		int minTtlSize = 10;
		int maxTtlSize = 30;
		int minAbsSize = 10;
		int maxAbsSize = 500;
		int minCntSize = 2;
		int maxCntSize = 2000;
		int avgCntSize = 200;
		int threads = 1000;
		long maxTran = 45524522;
		long maxTime = 42787878;
		boolean deferIndex = false;		
		String reportDir = "./testDir/";
		
		int pctListBlg = 10;
		int pctShowBlg = 50;
		int pctUpdateAccess = 40;
		int pctUpdateComment = 30;
		int pctShowSibs = 80;
		int pctPublishBlg = 20;
		int pctUpdateBlg = 12;
		
		boolean collectSysstat=true;
		boolean extraLargeBlog=true;
		int printThoughputPeriod = 80;
		boolean usedMemcached = false;
		String mainMemcachedHost = "192.168.1.1";
		long mainMemcachedPort = 869;
		String minorMemcachedHost = "192.168.1.2";
		long minorMemcachedPort = 868;
		boolean cleanMms = true;		
		int loadThreads = 64;
		
		String args = "";
		try {
			args = String.format("--table-name %s --table-size %d --table-engine %s --ntse-create-table-args %s " +
				"--blog-zipf-pct %d --blog-zipf-res %d --blog-zipf-part %d --user-zipf-pct %d --user-zipf-res %d --user-zipf-part %d " +
				"--min-title-size %d --max-title-size %d --min-abs-size %d --max-abs-size %d --min-cnt-size %d --max-cnt-size %d " +
				"--avg-cnt-size %d --threads %d --max-tran %d --max-time %d --defer-index %b --report-dir %s --collect-sysstat %b " +
				"--list-blogs %d --show-blog %d --update-access %d --update-comment %d --show-siblings %d --publish-blog %d --update-blog %d " +
				"--print-period %d --large-blog %b --use-memcached %b --main-memcached-host %s --main-memcached-port %d " +
				"--minor-memcached-host %s --minor-memcached-port %d --clean-mms %b --load-threads %d LOAD", 
				tbName, tbSize, tbEngine, ntseCreateTblArgs, 
				blgZipfPct, blgZipfRes, blgZipfPart, userZipfPct, userZipfRes, userZipfPart,
				minTtlSize, maxTtlSize, minAbsSize, maxAbsSize, minCntSize, maxCntSize, 
				avgCntSize, threads, maxTran, maxTime, deferIndex, reportDir, collectSysstat, 
				pctListBlg, pctShowBlg, pctUpdateAccess, pctUpdateComment, pctShowSibs, pctPublishBlg, pctUpdateBlg,  
				printThoughputPeriod, extraLargeBlog, usedMemcached, mainMemcachedHost, mainMemcachedPort, 
				minorMemcachedHost, minorMemcachedPort, cleanMms, loadThreads);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Pair<BbTestOptions, String[]> result = BbTestOptParser.parse(args.split(" "));
		
		BbTestOptions bbTestOpt = result.getFirst();
		
		assertEquals(tbName, bbTestOpt.getTbName());
		assertEquals(tbSize, bbTestOpt.getTbSize());
		assertEquals(tbEngine, bbTestOpt.getTbEngine());
		assertEquals(ntseCreateTblArgs, bbTestOpt.getNtseCreateTblArgs());
		assertEquals(blgZipfPct, bbTestOpt.getBlgZipfPct());
		assertEquals(blgZipfRes, bbTestOpt.getBlgZipfRes());
		assertEquals(blgZipfPart, bbTestOpt.getBlgZipfPart());
		assertEquals(userZipfPct, bbTestOpt.getUserZipfPct());
		assertEquals(userZipfRes, bbTestOpt.getUserZipfRes());
		assertEquals(userZipfPart, bbTestOpt.getUserZipfPart());
		assertEquals(minTtlSize, bbTestOpt.getMinTtlSize());
		assertEquals(maxTtlSize, bbTestOpt.getMaxTtlSize());
		assertEquals(minAbsSize, bbTestOpt.getMinAbsSize());
		assertEquals(maxAbsSize, bbTestOpt.getMaxAbsSize());
		assertEquals(minCntSize, bbTestOpt.getMinCntSize());
		assertEquals(maxCntSize, bbTestOpt.getMaxCntSize());
		assertEquals(avgCntSize, bbTestOpt.getAvgCntSize());
		assertEquals(threads, bbTestOpt.getThreads());
		assertEquals(maxTran, bbTestOpt.getMaxTran());
		assertEquals(maxTime, bbTestOpt.getMaxTime());
		assertEquals(deferIndex, bbTestOpt.isDeferIndex());
		assertEquals(reportDir, bbTestOpt.getReportDir());
		assertEquals(collectSysstat, bbTestOpt.isCollectSysstat());
		assertEquals(pctListBlg, bbTestOpt.getPctListBlg());
		assertEquals(pctShowBlg, bbTestOpt.getPctShowBlg());
		assertEquals(pctUpdateAccess, bbTestOpt.getPctUpdateAccess());
		assertEquals(pctUpdateComment, bbTestOpt.getPctUpdateComment());
		assertEquals(pctShowSibs, bbTestOpt.getPctShowSibs());
		assertEquals(pctPublishBlg, bbTestOpt.getPctPublishBlg());
		assertEquals(pctUpdateBlg, bbTestOpt.getPctUpdateBlg());		
		assertEquals(extraLargeBlog, bbTestOpt.isExtraLargeBlog());
		assertEquals(printThoughputPeriod, bbTestOpt.getPrintThoughputPeriod());
		assertEquals(usedMemcached, bbTestOpt.isUsedMemcached());
		assertEquals(mainMemcachedHost, bbTestOpt.getMainMemcachedHost());
		assertEquals(mainMemcachedPort, bbTestOpt.getMainMemcachedPort());
		assertEquals(minorMemcachedHost, bbTestOpt.getMinorMemcachedHost());
		assertEquals(minorMemcachedPort, bbTestOpt.getMinorMemcachedPort());
		assertEquals(loadThreads, bbTestOpt.getLoadThreads());
		assertEquals(BlogbenchOperationType.LOAD, bbTestOpt.getOperType());
	}

}
