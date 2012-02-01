package com.netease.webbench.blogbench;

import java.lang.reflect.Field;
import java.util.concurrent.BlockingQueue;

import junit.framework.TestCase;

import com.netease.webbench.blogbench.blog.Blog;
import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.blogbench.misc.ParameterGenerator;
import com.netease.webbench.blogbench.misc.UnitTestHelper;
import com.netease.webbench.blogbench.operation.BlogbenchOperationType;
import com.netease.webbench.blogbench.thread.BlgRecordProducer;
import com.netease.webbench.common.DbOptions;

public class BlgRecordProducerTest extends TestCase {
	
	private BlgRecordProducer producer;
	private BbTestOptions bbTestOpt;
	private DbOptions dbOpt;
	private ParameterGenerator pg;
	private long recordNum = BlgRecordProducer.DEFAULT_QUEUE_SIZE * 2;

	public BlgRecordProducerTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		
		dbOpt = UnitTestHelper.createDflDbOpt();
		bbTestOpt = UnitTestHelper.createDflBbTestOpt();
		bbTestOpt.setTbSize(100000);
		bbTestOpt.setOperType(BlogbenchOperationType.LOAD);
		pg = new ParameterGenerator();
		pg.init(bbTestOpt, dbOpt);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	@SuppressWarnings("unchecked")
	public void testBlgRecordProducer() {
		try {
			producer = new BlgRecordProducer(pg, recordNum);
			
			Field field1 = producer.getClass().getDeclaredField("queueMaxSize");
			field1.setAccessible(true);
			int maxQueLen = (Integer)field1.get(producer);
			assertEquals(maxQueLen, BlgRecordProducer.DEFAULT_QUEUE_SIZE);
			field1.setAccessible(false);
			
			Field field2 = producer.getClass().getDeclaredField("produceNum");
			field2.setAccessible(true);
			long produceNum = (Long)field2.get(producer);
			assertEquals(produceNum, recordNum);
			field2.setAccessible(false);
			
			Field field3 = producer.getClass().getDeclaredField("hasProduced");
			field3.setAccessible(true);
			long hasProduced = (Long)field3.get(producer);
			assertEquals(hasProduced, 0);
			field3.setAccessible(false);
		
			Field field4 = producer.getClass().getDeclaredField("blockingQueue");
			field4.setAccessible(true);
			BlockingQueue<Blog> queue = (BlockingQueue<Blog>)field4.get(producer);
			assertTrue(queue != null);
			field4.setAccessible(false);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void testRun() {
		
		try {
			producer = new BlgRecordProducer(pg, recordNum);
			producer.start();
			
			while (producer.getState() != Thread.State.WAITING) {
				Thread.sleep(100);
			}
			
			Field field3 = producer.getClass().getDeclaredField("hasProduced");
			field3.setAccessible(true);
			long hasProduced = (Long)field3.get(producer);
			System.out.println(hasProduced);
			assertEquals(hasProduced, BlgRecordProducer.DEFAULT_QUEUE_SIZE);//应该达到队列最大长度
			field3.setAccessible(false);
			
			for (int i = 0; i < recordNum; i++) {
				producer.getBlog();
			}
			
			Field field4 = producer.getClass().getDeclaredField("hasProduced");
			field4.setAccessible(true);
			hasProduced = (Long)field4.get(producer);
			assertEquals(hasProduced, recordNum);//应该指定数目记录的都创建了
			field4.setAccessible(false);			

			producer.join();
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void testGetBlog() {
		try {
			producer = new BlgRecordProducer(pg, recordNum);
			producer.start();

			for (int i = 0; i < recordNum; i++) {
				Blog blog = producer.getBlog();
				assertTrue(blog != null);
				assertEquals(blog.getId(), i + 1);//id一定是递增的
			}
			producer.join();
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

}
