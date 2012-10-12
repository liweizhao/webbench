package com.netease.webbench.blogbench.transaction.wrapped;

import com.netease.webbench.blogbench.blog.BlogInfoWithAcs;
import com.netease.webbench.blogbench.blog.BlogInfoWithPub;
import com.netease.webbench.blogbench.blog.LightBlog;
import com.netease.webbench.blogbench.memcached.AccessCountCache;
import com.netease.webbench.blogbench.memcached.MemcachedClientIF;
import com.netease.webbench.blogbench.memcached.MemcachedManager;
import com.netease.webbench.blogbench.misc.ParameterGenerator;
import com.netease.webbench.blogbench.statis.BlogbenchCounters;
import com.netease.webbench.blogbench.statis.MemcachedOperCounter.MemOperType;
import com.netease.webbench.blogbench.transaction.BbTestTransaction;
import com.netease.webbench.blogbench.transaction.BbTestTrxUpdateAcs;

public class UpdateAcsTrxWithMem extends BbTestTransaction {
	private BbTestTrxUpdateAcs wrappedTrx;
	private AccessCountCache accessCountCache;
	
	protected UpdateAcsTrxWithMem(BbTestTrxUpdateAcs wrappedTrx,
			BlogbenchCounters counters) throws Exception {
		super(wrappedTrx, counters);
		this.wrappedTrx = wrappedTrx;
	}
	
	public void setAcsCntUpdateCache(AccessCountCache cache) {
		this.accessCountCache = cache;
	}

	@Override
	public void prepare() throws Exception {
		wrappedTrx.prepare();		
	}

	@Override
	public void doExeTrx(ParameterGenerator paraGen) throws Exception {
		MemcachedClientIF mcm = MemcachedManager.getInstance().getMajorMcc();
		MemcachedClientIF counterMcm = MemcachedManager.getInstance().getMinorMcc();
		
		BlogInfoWithPub blogInfo = paraGen.getZipfRandomBlog();
		
		/* get newest access count of a blog in memcached server */
		long newestAcsCnt = counterMcm.getCounter("blog:" + blogInfo.getBlogId());
		
		myTrxCounter.addMemOper(MemOperType.GET_ACS, newestAcsCnt >= 0 ? true : false);//ͳ��memcached��ȡ�����������ĳɹ�����
		
		if (newestAcsCnt < 0) {
			
			/* fetch blog record in memcached server */
			LightBlog lightBlog = new LightBlog();
			boolean readSuc = lightBlog.readFromBytes((byte[])mcm.get("lblog:" + 
					blogInfo.getBlogId()));
			
			myTrxCounter.addMemOper(MemOperType.GET_BLOG, readSuc);
			
			if (!readSuc) {
				/* if we can't get blog, fetch it from database */
				lightBlog = blogFetcher.getLightBlog(blogInfo.getBlogId(), 
						blogInfo.getUId());
				
				if (lightBlog != null) {
					boolean hit = mcm.set("lblog:" + lightBlog.getId(), 
							lightBlog.writeToBytes());
					myTrxCounter.addMemOper(MemOperType.SET_BLOG, hit);
				} else {
					throw new Exception("Error: failed to fetch blog record " +
							"from database(update blog access transaction)!");
				}
			}
			newestAcsCnt = lightBlog.getAccessCount();
		} 
		
		/* update access count information in memcached */
		BlogInfoWithAcs blogInfoWithAcs = new BlogInfoWithAcs(blogInfo.getBlogId(),
				blogInfo.getUId(), (int)newestAcsCnt + 1);
		
		long incrRtn = counterMcm.incr("blog:" + blogInfo.getBlogId());
		if (incrRtn < 0) {
			myTrxCounter.addMemOper(MemOperType.INC_ACS, false);
			
			boolean isAddSuc = counterMcm.addOrIncr("blog:" + 
					blogInfo.getBlogId()) < 0 ? false : true;
			myTrxCounter.addMemOper(MemOperType.ADD_ACS, isAddSuc);
		} else {
			myTrxCounter.addMemOper(MemOperType.INC_ACS, true);
		}
		
		/**
		 * update access count in local cache
		 * if cache of access count is full, will update in database directly
		 */
		if (!accessCountCache.cacheUpdate(blogInfoWithAcs))
			wrappedTrx.updateAcsToDb(blogInfoWithAcs);		
	}

}
