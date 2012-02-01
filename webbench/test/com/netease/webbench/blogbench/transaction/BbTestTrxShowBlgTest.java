package com.netease.webbench.blogbench.transaction;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;

import junit.framework.TestCase;

import com.netease.webbench.blogbench.blog.Blog;
import com.netease.webbench.blogbench.blog.BlogContent;
import com.netease.webbench.blogbench.blog.LightBlog;
import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.blogbench.misc.ParameterGenerator;
import com.netease.webbench.blogbench.misc.UnitTestHelper;
import com.netease.webbench.blogbench.operation.BlogbenchOperationType;
import com.netease.webbench.blogbench.sql.SQLConfigure;
import com.netease.webbench.blogbench.statis.BlogbenchCounters;
import com.netease.webbench.common.DbOptions;
import com.netease.webbench.common.DbSession;

public class BbTestTrxShowBlgTest extends TestCase {
	
	private BbTestTrxShowBlg showTrx;
	private BbTestOptions bbTestOpt;
	private DbOptions dbOpt;
	private DbSession dbSession;
	private BlogbenchCounters counters;
	private ParameterGenerator paraGen;

	public BbTestTrxShowBlgTest(String name) {
		super(name);
		dbOpt = UnitTestHelper.createDflDbOpt();
		bbTestOpt = UnitTestHelper.createDflBbTestOpt();
		bbTestOpt.setOperType(BlogbenchOperationType.RUN);
		bbTestOpt.setTbName("unit_test_show_blog");
		bbTestOpt.setTbSize(1000);
		
		counters = new BlogbenchCounters(BbTestTrxType.TRX_TYPE_NUM);
	}

	protected void setUp() throws Exception {
		super.setUp();
		
		UnitTestHelper.createTestTable(dbOpt, bbTestOpt);
		bbTestOpt.setOperType(BlogbenchOperationType.RUN);
		
		dbSession = new DbSession(dbOpt);		
		showTrx = new BbTestTrxShowBlg(dbSession, bbTestOpt, counters);
		
		paraGen = new ParameterGenerator();
		paraGen.init(bbTestOpt, dbOpt);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		
		dbSession.update("drop table if EXISTS " + bbTestOpt.getTbName());
		
		dbSession.close();
	}

	public void testPrepareStatement() {
		try {
			showTrx.prepare();
			
			Field field = showTrx.getClass().getSuperclass().getDeclaredField("prepareStatementPre");
			field.setAccessible(true);
			PreparedStatement ps = (PreparedStatement) field.get(showTrx);
			assertTrue(ps != null);
			field.setAccessible(false);
			
			Field field2 = showTrx.getClass().getDeclaredField("lightPrpStmt");
			field2.setAccessible(true);
			PreparedStatement ps2 = (PreparedStatement) field2.get(showTrx);
			assertTrue(ps2 != null);
			field2.setAccessible(false);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void testExeTrx() {
		try {
			assertEquals(0, counters.getTotalTrxCounter().getTrxCount());
			assertEquals(0, counters.getSingleTrxCounter(BbTestTrxType.SHOW_BLG).getTrxCount());
			
			showTrx.prepare();
			for (int i = 0; i < 1000; i++) {
				showTrx.doExeTrx(null);
				assertEquals(i + 1, counters.getTotalTrxCounter().getTrxCount());
				assertEquals(i + 1, counters.getSingleTrxCounter(BbTestTrxType.SHOW_BLG).getTrxCount());
			}
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void testGetWholeBlogFromDb() {
		Blog blog = new Blog(1, 1, "abcdefghijklmn", "这是单元测试", "这是单元测试这是单元测试这是单元测试这是单元测试这是" +
				"单元测试这是单元测试这是单元测试这是单元测试这是单元测试这是单元测试这是单元测试这是单元测试这是单元测试这是单元测试", 
				-100, System.currentTimeMillis(), 0, 0);
		
		try {
			showTrx.prepare();
			
			insertTestRecord(dbSession, blog);
			
			Blog dbBlog = showTrx.getWholeBlog(1, 1);
			
			assertEquals(dbBlog.getId(), blog.getId());
			assertEquals(dbBlog.getUid(), blog.getUid());
			assertEquals(dbBlog.getTitle(), blog.getTitle());
			assertEquals(dbBlog.getAbs(), blog.getAbs());
			assertEquals(dbBlog.getCnt(), blog.getCnt());
			assertEquals(dbBlog.getAllowView(), blog.getAllowView());
			assertEquals(dbBlog.getPublishTime(), blog.getPublishTime());
			assertEquals(dbBlog.getAccessCount(), blog.getAccessCount());
			assertEquals(dbBlog.getCommentCount(), blog.getCommentCount());
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void testGetLightBlogFromDb() {
		Blog blog = new Blog(1, 1, "abcdefghijklmn", "这是单元测试", "这是单元测试这是单元测试这是单元测试这是单元测试这是" +
				"单元测试这是单元测试这是单元测试这是单元测试这是单元测试这是单元测试这是单元测试这是单元测试这是单元测试这是单元测试", 
				-100, System.currentTimeMillis(), 0, 0);
		
		try {
			showTrx.prepare();
			
			insertTestRecord(dbSession, blog);
			
			LightBlog dbBlog = showTrx.getLightBlog(1, 1);
			
			assertEquals(dbBlog.getId(), blog.getId());
			assertEquals(dbBlog.getUid(), blog.getUid());
			assertEquals(dbBlog.getTitle(), blog.getTitle());
			assertEquals(dbBlog.getAbs(), blog.getAbs());
			assertEquals(dbBlog.getAllowView(), blog.getAllowView());
			assertEquals(dbBlog.getPublishTime(), blog.getPublishTime());
			assertEquals(dbBlog.getAccessCount(), blog.getAccessCount());
			assertEquals(dbBlog.getCommentCount(), blog.getCommentCount());
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void testGetContentFromDb() {
		Blog blog = new Blog(1, 1, "abcdefghijklmn", "这是单元测试", "这是单元测试这是单元测试这是单元测试这是单元测试这是" +
				"单元测试这是单元测试这是单元测试这是单元测试这是单元测试这是单元测试这是单元测试这是单元测试这是单元测试这是单元测试", 
				-100, System.currentTimeMillis(), 0, 0);
		
		try {
			showTrx.prepare();
			
			insertTestRecord(dbSession, blog);
			
			BlogContent content = showTrx.getContentFromDb(1, 1);
			
			assertEquals(content.getContent(), blog.getCnt());
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void testGetPct() {
		assertEquals(showTrx.getPct(), bbTestOpt.getPctShowBlg());
	}

	public void testGetTrxType() {
		assertEquals(showTrx.getTrxType(), BbTestTrxType.SHOW_BLG);
	}

	public void testGetTrxName() {
		assertEquals(showTrx.getTrxName(), "ShowBlog");
	}
	
	private void insertTestRecord(DbSession dbSession, Blog blog) throws Exception {
		dbSession.update("truncate table " + bbTestOpt.getTbName());
		SQLConfigure sqlConfig = SQLConfigure.getInstance(dbSession.getDbOpt().getDbType());
		String sql = sqlConfig.getPublishBlogSql(bbTestOpt.getTbName(), false);
		sql = sql.replaceAll("_TableName", bbTestOpt.getTbName());
		PreparedStatement ps = dbSession.createPreparedStatement(sql);
		ps.setLong(1, blog.getId());
		ps.setLong(2, blog.getUid());
		ps.setString(3, blog.getTitle());
		ps.setString(4, blog.getAbs());
		ps.setString(5, blog.getCnt());
		ps.setInt(6, blog.getAllowView());
		ps.setLong(7, blog.getPublishTime());
		dbSession.update(ps);
	}
}
