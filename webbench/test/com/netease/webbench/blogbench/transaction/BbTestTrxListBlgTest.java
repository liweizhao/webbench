package com.netease.webbench.blogbench.transaction;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import com.netease.webbench.blogbench.blog.Blog;
import com.netease.webbench.blogbench.blog.BlogIdPair;
import com.netease.webbench.blogbench.blog.LightBlog;
import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.blogbench.misc.ParameterGenerator;
import com.netease.webbench.blogbench.misc.UnitTestHelper;
import com.netease.webbench.blogbench.operation.BlogbenchOperationType;
import com.netease.webbench.blogbench.sql.SQLConfigure;
import com.netease.webbench.blogbench.statis.BlogbenchCounters;
import com.netease.webbench.common.DbOptions;
import com.netease.webbench.common.DbSession;

public class BbTestTrxListBlgTest extends TestCase {
	
	private BbTestTrxListBlg listTrx;
	private BbTestOptions bbTestOpt;
	private DbOptions dbOpt;
	private DbSession dbSession;
	private BlogbenchCounters counters;
	private ParameterGenerator paraGen;

	public BbTestTrxListBlgTest(String name) {
		super(name);
		
		dbOpt = UnitTestHelper.createDflDbOpt();
		bbTestOpt = UnitTestHelper.createDflBbTestOpt();
		bbTestOpt.setOperType(BlogbenchOperationType.RUN);
		bbTestOpt.setTbName("unit_test_list_blog");
		bbTestOpt.setTbSize(1000);
		
		counters = new BlogbenchCounters(BbTestTrxType.TRX_TYPE_NUM);
	}

	protected void setUp() throws Exception {
		super.setUp();
		
		//if (super.getName().equals("testExeTrx")) {
			UnitTestHelper.createTestTable(dbOpt, bbTestOpt);
			bbTestOpt.setOperType(BlogbenchOperationType.RUN);
		//} 
		
			dbSession = new DbSession(dbOpt);		
		listTrx = new BbTestTrxListBlg(dbSession, bbTestOpt, counters);
		
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
			Field field = listTrx.getClass().getSuperclass().getDeclaredField("prepareStatementPre");
			field.setAccessible(true);
			PreparedStatement ps = (PreparedStatement)field.get(listTrx);
			assertTrue(ps == null);	
			field.setAccessible(false);
			
			listTrx.prepare();
			
			Field field2 = listTrx.getClass().getSuperclass().getDeclaredField("prepareStatementPre");
			field2.setAccessible(true);
			ps = (PreparedStatement)field2.get(listTrx);
			assertTrue(ps != null);			
			field2.setAccessible(false);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void testExeTrx() {
		try {
			assertEquals(0, counters.getTotalTrxCounter().getTrxCount());
			assertEquals(0, counters.getSingleTrxCounter(BbTestTrxType.LIST_BLGS).getTrxCount());
			
			listTrx.prepare();
			for (int i = 0; i < 1000; i++) {
				listTrx.doExeTrx(null);
				assertEquals(i + 1, counters.getTotalTrxCounter().getTrxCount());
				assertEquals(i + 1, counters.getSingleTrxCounter(BbTestTrxType.LIST_BLGS).getTrxCount());
			}
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void testMultiGetBlogFromDb() {
		try {
			bbTestOpt.setOperType(BlogbenchOperationType.RUN);
			dbSession.update("truncate table " + bbTestOpt.getTbName());
			
			listTrx.prepare();
			
			List<LightBlog> preInsertList = new ArrayList<LightBlog>();
			List<BlogIdPair> list = new ArrayList<BlogIdPair>();
			for (int i = 0; i < 10; i++) {
				Blog blog = new Blog(i + 1, 1, "abcdefghijklmn", "这是单元测试", "这是单元测试这是单元测试这是单元测试", 
						-100, System.currentTimeMillis(), 0, 0);
				preInsertList.add(blog.getLightBlog());
				insertTestRecord(dbSession, blog);
				list.add(new BlogIdPair(i + 1, 1));
			}
			
			Method method = listTrx.getClass().getDeclaredMethod("multiGetBlogFromDb", new Class[] { List.class });
			method.setAccessible(true);
			List<LightBlog>  blogList = null;
			try {
				blogList = (List<LightBlog>)method.invoke(listTrx, new Object[] { list });
			} catch (InvocationTargetException ie) {
				throw new Exception(ie.getTargetException());
			}
			assertEquals(blogList.size(), 10);
			
			Collections.sort(blogList);
			for (int i = 0; i < 10; i++) {
				System.out.println(preInsertList.get(i).getId() + " " + blogList.get(i).getId());
				assertEquals(preInsertList.get(i), blogList.get(i));
			}
			method.setAccessible(false);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void testGetListFromDb() {
		try {
			bbTestOpt.setOperType(BlogbenchOperationType.RUN);
			dbSession.update("truncate table " + bbTestOpt.getTbName());
			
			listTrx.prepare();
			
			for (int i = 0; i < 10; i++) {
				Blog blog = new Blog(i + 1, 1, "abcdefghijklmn", "这是单元测试", "这是单元测试这是单元测试这是单元测试", 
						-100, System.currentTimeMillis(), 0, 0);
				insertTestRecord(dbSession, blog);
			}
			
			List<Long> idList = listTrx.getListFromDb(dbSession, 1);
			Collections.sort(idList);
			for (int i = 0; i < 10; i++) {
				assertEquals(idList.get(i), new Long(i + 1));
			}			
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	private void insertTestRecord(DbSession dbSession, Blog blog) throws Exception {
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
