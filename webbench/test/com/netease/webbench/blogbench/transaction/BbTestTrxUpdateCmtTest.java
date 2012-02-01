package com.netease.webbench.blogbench.transaction;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;

import junit.framework.TestCase;

import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.blogbench.misc.ParameterGenerator;
import com.netease.webbench.blogbench.misc.UnitTestHelper;
import com.netease.webbench.blogbench.operation.BlogbenchOperationType;
import com.netease.webbench.blogbench.statis.BlogbenchCounters;
import com.netease.webbench.common.DbOptions;
import com.netease.webbench.common.DbSession;

public class BbTestTrxUpdateCmtTest extends TestCase {
	
	private BbTestTrxUpdateCmt updateCmtTrx;
	private BbTestOptions bbTestOpt;
	private DbOptions dbOpt;
	private DbSession dbSession;
	private BlogbenchCounters counters;

	public BbTestTrxUpdateCmtTest(String name) {
		super(name);
		dbOpt = UnitTestHelper.createDflDbOpt();
		bbTestOpt = UnitTestHelper.createDflBbTestOpt();
		bbTestOpt.setOperType(BlogbenchOperationType.RUN);
		
		counters = new BlogbenchCounters(BbTestTrxType.TRX_TYPE_NUM);
	}

	protected void setUp() throws Exception {
		super.setUp();
		
		dbSession = new DbSession(dbOpt);	
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		dbSession.close();
	}

	public void testPrepareStatement() {
		try {
			updateCmtTrx = new BbTestTrxUpdateCmt(dbSession, bbTestOpt, counters);
			updateCmtTrx.prepare();

			Field field = updateCmtTrx.getClass().getSuperclass().getDeclaredField("prepareStatementPre");
			field.setAccessible(true);
			PreparedStatement ps = (PreparedStatement) field.get(updateCmtTrx);
			assertTrue(ps != null);
			field.setAccessible(false);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void testExeTrx() {
		try {
			ParameterGenerator paraGen = new ParameterGenerator();
			paraGen.init(bbTestOpt, dbOpt);
			bbTestOpt.setUsedMemcached(true);
			updateCmtTrx = new BbTestTrxUpdateCmt(dbSession, bbTestOpt,  counters);
			updateCmtTrx.prepare();
			updateCmtTrx.doExeTrx(null);

			bbTestOpt.setUsedMemcached(false);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void testGetPct() throws Exception {
		updateCmtTrx = new BbTestTrxUpdateCmt(dbSession, bbTestOpt, counters);
		assertEquals(bbTestOpt.getPctUpdateComment(), updateCmtTrx.getPct());
	}

	public void testGetTrxType()  throws Exception {
		updateCmtTrx = new BbTestTrxUpdateCmt(dbSession, bbTestOpt, counters);
		assertEquals(updateCmtTrx.getTrxType(), BbTestTrxType.UPDATE_CMT);
	}

	public void testGetTrxName()  throws Exception {
		updateCmtTrx = new BbTestTrxUpdateCmt(dbSession, bbTestOpt, counters);
		assertEquals(updateCmtTrx.getTrxName(), "UpdateComment");
	}

}
