package com.netease.webbench.blogbench.misc;

import junit.framework.TestCase;

import com.netease.webbench.blogbench.operation.BlogbenchOperationType;

public class BbTestOptionsTest extends TestCase {
	
	BbTestOptions opt;

	public BbTestOptionsTest(String name) {
		super(name);
		opt = new BbTestOptions();
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGetSetTbName() {
		assertEquals(opt.getTbName(), "Blog");
		String tbName = "Yaoming";
		opt.setTbName(tbName);
		assertEquals(tbName, opt.getTbName());
	}

	public void testGetSetTbSize() {
		assertEquals(opt.getTbSize(), 1000000);
		long tbSize = 20000;
		opt.setTbSize(tbSize);
		assertEquals(opt.getTbSize(), tbSize);
	}

	public void testGetSetAvgCntSize() {
		assertEquals(opt.getAvgCntSize(), 2000);
		int size = 20000;
		opt.setAvgCntSize(size);
		assertEquals(opt.getAvgCntSize(), size);
	}

	public void testGetSetBlgZipfPart() {
		assertEquals(opt.getBlgZipfPart(), 200);
		int size = 500;
		opt.setBlgZipfPart(size);
		assertEquals(opt.getBlgZipfPart(), size);
	}

	public void testGetSetBlgZipfPct() {
		assertEquals(opt.getBlgZipfPct(), 5);
		int size = 50;
		opt.setBlgZipfPct(size);
		assertEquals(opt.getBlgZipfPct(), size);
	}

	public void testGetSetBlgZipfRes() {
		assertEquals(opt.getBlgZipfRes(), 95);
		int size = 90;
		opt.setBlgZipfRes(size);
		assertEquals(opt.getBlgZipfRes(), size);
	}

	public void testIsDeferIndex() {
		assertEquals(opt.isDeferIndex(), false);
		assertEquals(opt.specifiedDeferIdx(), false);
		opt.setDeferIndex(true);
		assertEquals(opt.specifiedDeferIdx(), true);
		assertEquals(opt.isDeferIndex(), true);;
	}

	public void testGetSetMaxAbsSize() {
		assertEquals(opt.getMaxAbsSize(), 500);
		int size = 100;
		opt.setMaxAbsSize(size);
		assertEquals(opt.getMaxAbsSize(), size);
	}

	public void testGetSetMaxCntSize() {
		assertEquals(opt.getMaxCntSize(), 20000);
		int size = 30000;
		opt.setMaxCntSize(size);
		assertEquals(opt.getMaxCntSize(), size);
	}

	public void testGetSetMaxTime() {
		assertEquals(opt.getMaxTime(), Long.MAX_VALUE);
		int size = 3600;
		opt.setMaxTime(size);
		assertEquals(opt.getMaxTime(), size);
	}

	public void testGetSetMaxTran() {
		assertEquals(opt.getMaxTran(), Long.MAX_VALUE);
		int size = 600000;
		opt.setMaxTran(size);
		assertEquals(opt.getMaxTran(), size);
	}

	public void testGetSetMaxTtlSize() {
		assertEquals(opt.getMaxTtlSize(), 30);
		int size = 50;
		opt.setMaxTtlSize(size);
		assertEquals(opt.getMaxTtlSize(), size);
	}

	public void testGetSetMinAbsSize() {
		assertEquals(opt.getMinAbsSize(), 10);
		int size = 20;
		opt.setMinAbsSize(size);
		assertEquals(opt.getMinAbsSize(), size);
	}

	public void testGetSetMinCntSize() {
		assertEquals(opt.getMinCntSize(), 20);
		int size = 60;
		opt.setMinCntSize(size);
		assertEquals(opt.getMinCntSize(), size);
	}

	public void testGetSetMinTtlSize() {
		assertEquals(opt.getMinTtlSize(), 10);
		int size = 20;
		opt.setMinTtlSize(size);
		assertEquals(opt.getMinTtlSize(), size);
	}

	public void testGetSetNtseCreateTblArgs() {
		assertEquals(opt.getNtseCreateTblArgs(), "\"\"");
		String arg = "\"this is test\"";
		opt.setNtseCreateTblArgs(arg);
		assertEquals(opt.getNtseCreateTblArgs(), arg);
	}

	public void testGetSetTbEngine() {
		assertEquals(opt.getTbEngine(), "ntse");
		String arg = "myisam";
		opt.setTbEngine(arg);
		assertEquals(opt.getTbEngine(), arg);
	}

	public void testGetSetThreads() {
		assertEquals(opt.getThreads(), 100);
		int size = 200;
		opt.setThreads(size);
		assertEquals(opt.getThreads(), size);
	}

	public void testGetSetUserZipfPart() {
		assertEquals(opt.getUserZipfPart(), 200);
		int size = 300;
		opt.setUserZipfPart(size);
		assertEquals(opt.getUserZipfPart(), size);
	}

	public void testGetSetUserZipfPct() {
		assertEquals(opt.getUserZipfPct(), 5);
		int size = 10;
		opt.setUserZipfPct(size);
		assertEquals(opt.getUserZipfPct(), size);
	}

	public void testGetSetUserZipfRes() {
		assertEquals(opt.getUserZipfRes(), 95);
		int size = 80;
		opt.setUserZipfRes(size);
		assertEquals(opt.getUserZipfRes(), size);
	}

	public void testGetSetPctListBlg() {
		assertEquals(opt.getPctListBlg(),30);
		int size = 80;
		opt.setPctListBlg(size);
		assertEquals(opt.getPctListBlg(), size);
	}

	public void testGetSetPctPublishBlg() {
		assertEquals(opt.getPctPublishBlg(),10);
		int size = 20;
		opt.setPctPublishBlg(size);
		assertEquals(opt.getPctPublishBlg(), size);
	}

	public void testGetSetPctShowBlg() {
		assertEquals(opt.getPctShowBlg(),60);
		int size = 30;
		opt.setPctShowBlg(size);
		assertEquals(opt.getPctShowBlg(), size);
	}

	public void testGetSetPctShowSibs() {
		assertEquals(opt.getPctShowSibs(),60);
		int size = 20;
		opt.setPctShowSibs(size);
		assertEquals(opt.getPctShowSibs(), size);
	}

	public void testGetSetPctUpdateAccess() {
		assertEquals(opt.getPctUpdateAccess(),60);
		int size = 20;
		opt.setPctUpdateAccess(size);
		assertEquals(opt.getPctUpdateAccess(), size);
	}

	public void testGetSetPctUpdateBlg() {
		assertEquals(opt.getPctUpdateBlg(), 2);
		int size = 10;
		opt.setPctUpdateBlg(size);
		assertEquals(opt.getPctUpdateBlg(), size);
	}

	public void testGetSetPctUpdateComment() {
		assertEquals(opt.getPctUpdateComment(), 10);
		int size = 12;
		opt.setPctUpdateComment(size);
		assertEquals(opt.getPctUpdateComment(), size);
	}

	public void testGetSetActionType() {
		assertEquals(opt.getOperType(), BlogbenchOperationType.LOAD);
		opt.setOperType(BlogbenchOperationType.RUN);
		assertEquals(opt.getOperType(), BlogbenchOperationType.RUN);
	}

	public void testGetSetReportDir() {
		assertEquals(opt.getReportDir(), "./report/");
		String dir = "./test_dir/";
		opt.setReportDir(dir);
		assertEquals(opt.getReportDir(), dir);
	}

	public void testIsCollectSysstat() {
		assertEquals(opt.isCollectSysstat(), true);
		opt.setCollectSysstat(false);
		assertEquals(opt.isCollectSysstat(), false);
	}

	public void testIsExtraLargeBlog() {
		assertEquals(opt.isExtraLargeBlog(), true);
		opt.setExtraLargeBlog(false);
		assertEquals(opt.isExtraLargeBlog(), false);
	}

	public void testGetSetPrintThoughputPeriod() {
		assertEquals(opt.getPrintThoughputPeriod(), 60);
		int size = 30;
		opt.setPrintThoughputPeriod(size);
		assertEquals(opt.getPrintThoughputPeriod(), size);
	}

	public void testIsUsedMemcached() {
		assertEquals(opt.isUsedMemcached(), false);
		opt.setUsedMemcached(true);
		assertEquals(opt.isUsedMemcached(), true);
	}


	public void testGetSetMainMemcachedHost() {
		assertEquals(opt.getMainMemcachedHost(), "127.0.0.1");
		String arg = "172.1.1.1";
		opt.setMainMemcachedHost(arg);
		assertEquals(opt.getMainMemcachedHost(), arg);
	}

	public void testGetSetMainMemcachedPort() {
		assertEquals(opt.getMainMemcachedPort(), 8609);
		int size = 1111;
		opt.setMainMemcachedPort(size);
		assertEquals(opt.getMainMemcachedPort(), size);
	}

	public void testGetSetMinorMemcachedHost() {
		assertEquals(opt.getMinorMemcachedHost(), "127.0.0.1");
		String arg = "172.1.1.1";
		opt.setMinorMemcachedHost(arg);
		assertEquals(opt.getMinorMemcachedHost(), arg);
	}

	public void testGetSetMinorMemcachedPort() {
		assertEquals(opt.getMinorMemcachedPort(), 8608);
		int size = 1111;
		opt.setMinorMemcachedPort(size);
		assertEquals(opt.getMinorMemcachedPort(), size);
	}

	public void testIsDebug() {
		assertEquals(opt.isDebug(), false);
		opt.setDebug(true);
		assertEquals(opt.isDebug(), true);
	}

	public void testGetSetLoadThreads() {
		assertEquals(opt.getLoadThreads(), 8);
		int size = 45;
		opt.setLoadThreads(size);
		assertEquals(opt.getLoadThreads(), size);
	}

	public void testGetSetAcsCountTrxCacheSize() {
		assertEquals(opt.getAcsCountTrxCacheSize(), 50000);
		int size = 10000;
		opt.setAcsCountTrxCacheSize(size);
		assertEquals(opt.getAcsCountTrxCacheSize(), size);
	}

	public void testGetSetFlushAcsCountInterval() {
		assertEquals(opt.getFlushAcsCountInterval(), 10000);
		int size = 20000;
		opt.setFlushAcsCountInterval(size);
		assertEquals(opt.getFlushAcsCountInterval(), size);
	}

	public void testGetSetFlushAcsCountThreads() {
		assertEquals(opt.getFlushAcsCountThreads(), 8);
		int size = 24;
		opt.setFlushAcsCountThreads(size);
		assertEquals(opt.getFlushAcsCountThreads(), size);
	}
}
