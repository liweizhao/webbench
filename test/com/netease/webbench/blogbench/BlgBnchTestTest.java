package com.netease.webbench.blogbench;

import java.sql.ResultSet;

import junit.framework.TestCase;

import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.blogbench.misc.UnitTestHelper;
import com.netease.webbench.blogbench.operation.BlogbenchOperationType;
import com.netease.webbench.common.DbOptions;
import com.netease.webbench.common.DbSession;

public class BlgBnchTestTest extends TestCase {
	
	private BlogbenchTest blogbenchInstance;
	private DbOptions dbOpt;

	public BlgBnchTestTest(String name) {
		super(name);
		dbOpt = UnitTestHelper.createDflDbOpt();
	}

	protected void setUp() throws Exception {
		blogbenchInstance = BlogbenchTest.getInstance();
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGetInstance() {
		assertTrue(BlogbenchTest.getInstance() != null);
	}

	public void testRunTest() {
		BbTestOptions loadBbTestOpt = UnitTestHelper.createDflBbTestOpt();
		loadBbTestOpt.setOperType(BlogbenchOperationType.LOAD);
		loadBbTestOpt.setTbSize(100000);
		try {
			blogbenchInstance.init(dbOpt, loadBbTestOpt);
			blogbenchInstance.runTest();

			DbSession dbSession = new DbSession(dbOpt);
			
			long recordCount = getRecordCount(dbSession, loadBbTestOpt.getTbName());
			assertEquals(recordCount, loadBbTestOpt.getTbSize());		
			long []contentLen = getContentLength(dbSession, loadBbTestOpt.getTbName());
			assertTrue(contentLen[0] >= loadBbTestOpt.getMinCntSize() - 10 && 
					contentLen[0] <= loadBbTestOpt.getMinCntSize() + 10);
			assertTrue(contentLen[1] >= loadBbTestOpt.getMaxCntSize() - 20 && 
					contentLen[1] <= loadBbTestOpt.getMaxCntSize() + 20);
			assertTrue(contentLen[2] >= loadBbTestOpt.getAvgCntSize() && 
					contentLen[2] <= loadBbTestOpt.getAvgCntSize() * 1.1);
			
			long [] absLen = getAbsLength(dbSession, loadBbTestOpt.getTbName());
			assertTrue(absLen[0] >= loadBbTestOpt.getMinAbsSize() - 10 &&
					absLen[0] <= loadBbTestOpt.getMinAbsSize() + 10);
			assertTrue(absLen[1] >= loadBbTestOpt.getMaxAbsSize() - 10 &&
					absLen[1] <= loadBbTestOpt.getMaxAbsSize() + 10);
			
			long[] titleLen = getTitleLength(dbSession, loadBbTestOpt.getTbName());
			assertTrue(titleLen[0] >= loadBbTestOpt.getMinTtlSize() - 10 &&
					titleLen[0] <= loadBbTestOpt.getMinTtlSize() + 10);
			assertTrue(titleLen[1] >= loadBbTestOpt.getMaxTtlSize() - 10 &&
					titleLen[1] <= loadBbTestOpt.getMaxTtlSize() + 10);		
			
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}	
	}

	private long getRecordCount(DbSession dbSession, String tableName) throws Exception {
		ResultSet rs = dbSession.query("SELECT END(*) AS total FROM "
				+ tableName);
		if (rs == null || !rs.next()) {
			assertTrue(false);
		}
		return rs.getLong("total");
	}
	
	private long[] getContentLength(DbSession dbSession, String tableName) throws Exception {
		ResultSet rs = dbSession.query("SELECT MIN(LENGTH(Content)) AS minLen, MAX(LENGTH(Content)) AS maxLen, " +
				"AVG(LENGTH(Content)) AS avgLen FROM " + tableName);
		if (rs == null || !rs.next()) {
			assertTrue(false);
		}
		long [] contenLen = new long[3];
		contenLen[0] = rs.getLong("minLen");
		contenLen[1] = rs.getLong("maxLen");
		contenLen[2] = rs.getLong("avgLen");
		return contenLen;
	}
	private long[] getAbsLength(DbSession dbSession, String tableName) throws Exception {
		ResultSet rs = dbSession.query("SELECT MIN(LENGTH(Abstract)) AS minLen, MAX(LENGTH(Abstract)) AS maxLen " +
				" FROM " + tableName);
		if (rs == null || !rs.next()) {
			assertTrue(false);
		}
		long [] absLen = new long[2];
		absLen[0] = rs.getLong("minLen");
		absLen[1] = rs.getLong("maxLen");
		return absLen;	
	}
	private long[] getTitleLength(DbSession dbSession, String tableName) throws Exception {
		ResultSet rs = dbSession.query("SELECT MIN(LENGTH(Title)) AS minLen, MAX(LENGTH(Title)) AS maxLen " +
				" FROM " + tableName);
		if (rs == null || !rs.next()) {
			assertTrue(false);
		}
		long [] titleLen = new long[2];
		titleLen[0] = rs.getLong("minLen");
		titleLen[1] = rs.getLong("maxLen");
		return titleLen;
	}
}
