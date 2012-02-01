/**
  * Copyright (c) <2011>, <NetEase Corporation>
  * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *    3. Neither the name of the <ORGANIZATION> nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.netease.webbench.blogbench.memcached;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import com.netease.webbench.blogbench.blog.BlogInfoWithAcs;
import com.netease.webbench.blogbench.statis.UpdateAccessStatis;

/**
 *  global access count update cache
 * @author LI WEIZHAO
 */

public class GlobalAcsCntCache implements AccessCountCache {
	/* default number of maps */
	public static final int DEFAULT_MAX_MAP_LIST_SIZE = 1 << 4;
	
	/* maximum total maps size */
	private int maxMapThreshold;
	
	/* number of maps */
	private int mapListSize = DEFAULT_MAX_MAP_LIST_SIZE;
	
	/* maps used to cache data */
	private List<Map<Long, BlogInfoWithAcs>> mapList;
	
	/* locks for synchronising map operation */
	private List<ReentrantLock> mapLockList;
	
	/* atomic integer for getting next map index */
	private AtomicInteger currentFlushMap;
	
	/* statistic information of update access */
	private UpdateAccessStatis updateAccessStatis;
	
	/**
	 * constructor
	 * @param maxMapThreshold
	 */
	public GlobalAcsCntCache(int maxMapThreshold) {
		this.maxMapThreshold = maxMapThreshold;
		
		mapList = new ArrayList<Map<Long, BlogInfoWithAcs>>(mapListSize);
		mapLockList = new ArrayList<ReentrantLock>(mapListSize);
		for (int i = 0; i < mapListSize; i++) {
			mapList.add(new HashMap<Long, BlogInfoWithAcs>(maxMapThreshold * 2 / mapListSize));
			mapLockList.add(new ReentrantLock());
		}
		
		updateAccessStatis = new UpdateAccessStatis();
		currentFlushMap = new AtomicInteger(0);
	}
	
	/**
	 * @see AccessCountCache#cacheUpdate(BlogInfoWithAcs)
	 * @param newblogInfoWithAcs
	 */
	public boolean cacheUpdate(BlogInfoWithAcs newblogInfoWithAcs)
		throws InterruptedException {
		// TODO Auto-generated method stub
		
		updateAccessStatis.incrTotalCacheReqCount();
		
		//find a map to cache data according user ID
		int mapIndex = (int)(newblogInfoWithAcs.getUId() % mapListSize);
		
		mapLockList.get(mapIndex).lock();
		try {
			BlogInfoWithAcs old = mapList.get(mapIndex).get(newblogInfoWithAcs.getBlogId());
			if (old == null) {	
				int tryTimes = 0;
				while (updateAccessStatis.getTotalMapSize() >= maxMapThreshold) {
					
					/* if current thread still get lock of map before sleep, it must unlock map, or else other threads may be blocked */
					if (mapLockList.get(mapIndex).isHeldByCurrentThread())
						mapLockList.get(mapIndex).unlock();
					
					tryTimes++;
					if (tryTimes >= 3) {
						return false;
					}
					Thread.sleep(5);
				}
				
				/* if the lock has been release,  lock it once more */
				if (!mapLockList.get(mapIndex).isHeldByCurrentThread())
					mapLockList.get(mapIndex).lock();
				
				if (null == mapList.get(mapIndex).put(newblogInfoWithAcs.getBlogId(), newblogInfoWithAcs)) {
					updateAccessStatis.incrTotalMapSize();
				}
			} else {
				updateAccessStatis.incrMergeCount();
				if (old.getBlogAcs() < newblogInfoWithAcs.getBlogAcs()) {/* if new value is larger than old value, update it */
					mapList.get(mapIndex).put(newblogInfoWithAcs.getBlogId(), newblogInfoWithAcs);
				}
			}
		} finally {
			if (mapLockList.get(mapIndex).isHeldByCurrentThread())
				mapLockList.get(mapIndex).unlock();
		}
		
		updateAccessStatis.incrTotalRealCachedCount();
		
		return true;
	}
	
	/**
	 * return next map that can be flush
	 * @return
	 */
	public Map<Long, BlogInfoWithAcs> getNextMapToFlush() {

		for (int i = 0; i < mapListSize; i++) {
			int nextMap = currentFlushMap.getAndIncrement() % mapListSize;
			mapLockList.get(nextMap).lock();
			try {
				Map<Long, BlogInfoWithAcs> tmp = mapList.get(nextMap);
				if (tmp.size() > 0) {
					mapList.set(nextMap, new HashMap<Long, BlogInfoWithAcs>(maxMapThreshold * 2 / mapListSize));
					return tmp;
				} else {
					continue;
				}
			} finally {
				mapLockList.get(nextMap).unlock();
			}
		}
		return null;
	}
	
	/**
	 * decrease total size of elements in all maps
	 * @return new value after decrease
	 */
	public int decrTotalMapSize() {
		return updateAccessStatis.decTotalMapSize();
	}

	/**
	 * get statistic information of update access
	 * @return
	 */
	public UpdateAccessStatis getUpdateAccessStatistic() {
		return updateAccessStatis;
	}
	
	/**
	 * get total size of elements in all maps
	 * @return
	 */
	public int getTotalMapSize() {
		return updateAccessStatis.getTotalMapSize();
	}

}
