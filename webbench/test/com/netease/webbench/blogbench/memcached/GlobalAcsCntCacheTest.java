package com.netease.webbench.blogbench.memcached;

import java.lang.reflect.Field;
import java.util.Map;

import junit.framework.TestCase;

import com.netease.webbench.blogbench.blog.BlogInfoWithAcs;
import com.netease.webbench.blogbench.statis.UpdateAccessStatis;

public class GlobalAcsCntCacheTest extends TestCase {

	private BlogInfoWithAcs[] blogInfos;
	private int mapSize = 10000;
	private GlobalAcsCntCache cache;
	
	public GlobalAcsCntCacheTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		
		cache = new GlobalAcsCntCache(mapSize);
		
		blogInfos = new BlogInfoWithAcs[mapSize];
		for (int i = 0; i < mapSize; i++) {
			blogInfos[i] = new BlogInfoWithAcs(i + 1, i + 1, i + 1);
		}
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testCacheUpdate() {
		try {
			for (int i = 0; i < mapSize; i++) {
				assertTrue(cache.cacheUpdate(blogInfos[i]));
			}
			assertFalse(cache.cacheUpdate(new BlogInfoWithAcs(mapSize + 1, mapSize + 1, mapSize + 1)));			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void testGetNextMapToFlush() {
		try {
			Field field = cache.getClass().getDeclaredField("mapListSize");
			field.setAccessible(true);
			int mapCnt = (Integer) field.get(cache);
			field.setAccessible(false);
			
			for (int i = 0; i < mapSize; i++) {
				cache.cacheUpdate(blogInfos[i]);
			}
			
			for (int i = 0; i < mapCnt; i++) {
				Map<Long, BlogInfoWithAcs> map = cache.getNextMapToFlush();
				assertTrue(map != null && map.size() == mapSize / mapCnt);
			}
			
			Map<Long, BlogInfoWithAcs> map = cache.getNextMapToFlush();
			assertTrue(map == null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void testDecrTotalMapSize() {
		try {
			for (int i = 0; i < mapSize; i++) {
				cache.cacheUpdate(blogInfos[i]);
			}
			cache.decrTotalMapSize();
			assertEquals(cache.getUpdateAccessStatistic().getTotalMapSize(), mapSize - 1);
			assertEquals(cache.getTotalMapSize(), mapSize - 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void testGetUpdateAccessStatistic() {
		try {
			for (int i = 0; i < mapSize; i++) {
				cache.cacheUpdate(blogInfos[i]);
				UpdateAccessStatis stats = cache.getUpdateAccessStatistic();
				assertEquals(stats.getTotalMapSize(), i + 1);
				assertEquals(stats.getMergeCount(), 0);
				assertEquals(stats.getTotalCachedReqCount(), i + 1);
				assertEquals(stats.getTotalRealcachedCount(), i + 1);
			}
			
			cache.cacheUpdate(new BlogInfoWithAcs(1, 1, 1));				
			UpdateAccessStatis stats = cache.getUpdateAccessStatistic();
			assertEquals(stats.getTotalMapSize(), mapSize);
			assertEquals(stats.getMergeCount(), 1);
			assertEquals(stats.getTotalCachedReqCount(), mapSize + 1);
			assertEquals(stats.getTotalRealcachedCount(), mapSize + 1);
			
			cache.cacheUpdate(new BlogInfoWithAcs(mapSize + 1, mapSize + 1, mapSize + 1));				
			stats = cache.getUpdateAccessStatistic();
			assertEquals(stats.getTotalMapSize(), mapSize);
			assertEquals(stats.getMergeCount(), 1);
			assertEquals(stats.getTotalCachedReqCount(), mapSize + 2);
			assertEquals(stats.getTotalRealcachedCount(), mapSize + 1);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void testGetTotalMapSize() {
		try {
			for (int i = 0; i < mapSize; i++) {
				cache.cacheUpdate(blogInfos[i]);
				assertEquals(cache.getTotalMapSize(), i + 1);
			}
			cache.cacheUpdate(new BlogInfoWithAcs(mapSize + 1, mapSize + 1, mapSize + 1));
			assertEquals(cache.getTotalMapSize(), mapSize);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
