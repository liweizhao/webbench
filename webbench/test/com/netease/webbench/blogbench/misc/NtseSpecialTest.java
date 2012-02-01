package com.netease.webbench.blogbench.misc;

import java.sql.ResultSet;

import junit.framework.TestCase;

import com.netease.webbench.blogbench.ntse.NtseSpecialOper;
import com.netease.webbench.blogbench.operation.BlogbenchOperationType;
import com.netease.webbench.common.DbOptions;
import com.netease.webbench.common.DbSession;

public class NtseSpecialTest extends TestCase {
	
	private DbOptions dbOpt;
	private BbTestOptions bbTestOpt;
	private DbSession dbSession;

	public NtseSpecialTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {	
		super.setUp();
		
		dbOpt = UnitTestHelper.createDflDbOpt();
		
		try {
			dbSession = new DbSession(dbOpt);
			bbTestOpt = UnitTestHelper.createDflBbTestOpt();
			bbTestOpt.setOperType(BlogbenchOperationType.LOAD);
			
			UnitTestHelper.createTestTable(dbOpt, bbTestOpt);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		
		dbSession.close();
	}

	public void testSetNtseIndexBuildAlgorithm() {
		try {
			String result = null;
			ResultSet rs = dbSession.query("show variables like 'ntse_index_build_algorithm'");
			if (rs.next()) {
				result = rs.getString(2);
			}
			rs.close();
			String oldSetting = NtseSpecialOper.setNtseIndexBuildAlgorithm(dbSession, "readonly");
			
			assertEquals(oldSetting, result);
			
			String newResult = null;
			rs = dbSession.query("show variables like 'ntse_index_build_algorithm'");
			if (rs.next()) {
				newResult = rs.getString("Value");
			} 	
			rs.close();
			
			assertEquals("readonly", newResult);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void testDisableMms() {
		try {			
			//UnitTestHelper.createTestTable(dbOpt, bbTestOpt);
			
			NtseSpecialOper.disableMms(dbSession, dbOpt, bbTestOpt);
			
			dbSession.query("SELECT * FROM " + bbTestOpt.getTbName() + " LIMIT 10");
			
			ResultSet rs = dbSession.query("SELECT QUERY FROM information_schema.NTSE_MMS_STATS");
			if (rs != null) {
				assertFalse(rs.next());
			} else {
				assertTrue(false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void testEnableMms() {
		try {
			
			//UnitTestHelper.createTestTable(dbOpt, bbTestOpt);
			
			NtseSpecialOper.enableMms(dbSession, dbOpt, bbTestOpt, true);
			
			dbSession.query("SELECT * FROM " + bbTestOpt.getTbName() + " LIMIT 10");
			
			ResultSet rs = dbSession.query("SELECT QUERY FROM information_schema.NTSE_MMS_STATS");
			if (rs != null) {
				if (rs.next()) {
					long mmsQueries = rs.getInt(1);
					assertTrue(mmsQueries > 0);
				} else {
					assertTrue(false);
				}
			} else {
				assertTrue(false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
