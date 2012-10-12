package com.netease.webbench.blogbench.transaction.wrapped;

import com.netease.webbench.blogbench.blog.BlogInfoWithPub;
import com.netease.webbench.blogbench.blog.LightBlog;
import com.netease.webbench.blogbench.memcached.MemcachedClientIF;
import com.netease.webbench.blogbench.memcached.MemcachedManager;
import com.netease.webbench.blogbench.misc.ParameterGenerator;
import com.netease.webbench.blogbench.statis.BlogbenchCounters;
import com.netease.webbench.blogbench.statis.MemcachedOperCounter.MemOperType;
import com.netease.webbench.blogbench.transaction.BbTestTransaction;
import com.netease.webbench.blogbench.transaction.BbTestTrxShowSiblings;

public class ShowSiblingsTrxWithMem extends BbTestTransaction {
	private BbTestTrxShowSiblings wrappedTrx;
	
	protected ShowSiblingsTrxWithMem(BbTestTrxShowSiblings wrappedTrx,
			BlogbenchCounters counters) throws Exception {
		super(wrappedTrx, counters);
		this.wrappedTrx = wrappedTrx;
	}

	@Override
	public void prepare() throws Exception {
		wrappedTrx.prepare();		
	}

	@Override
	public void doExeTrx(ParameterGenerator paraGen) throws Exception {
		BlogInfoWithPub blogInfo = paraGen.getZipfRandomBlog();
		
		int[] siblings = wrappedTrx.getSiblingsFromDb(blogInfo, paraGen);
		int idPre = siblings[0];
		int idNext = siblings[1];
		
		MemcachedClientIF mcm = MemcachedManager.getInstance().getMajorMcc();
		if (idPre > 0) {
			LightBlog lightBlogPre = new LightBlog();
			boolean readSuc = lightBlogPre.readFromBytes(
					(byte[])mcm.get("lblog:" + idPre));

			myTrxCounter.addMemOper(MemOperType.GET_BLOG, readSuc);
			
			if (!readSuc) {
				lightBlogPre = blogFetcher.getLightBlog(idPre, blogInfo.getUId());
				if (lightBlogPre != null) {
					boolean hit = mcm.set("lblog:" + idPre, 
							lightBlogPre.writeToBytes());
					myTrxCounter.addMemOper(MemOperType.SET_BLOG, hit);
				} else {
					System.out.println("Error: failed to fetch previous " +
							"blog record from database(show siblings transaction)!");
				}
			}
		}
		if (idNext > 0) {
			LightBlog lightBlogNext = new LightBlog();
			boolean readSuc = lightBlogNext.readFromBytes(
					(byte[])mcm.get("lblog:" + idNext));
			
			myTrxCounter.addMemOper(MemOperType.GET_BLOG, readSuc);
			
			if (!readSuc) {
				lightBlogNext = blogFetcher.getLightBlog(idNext, blogInfo.getUId());
				if (lightBlogNext != null) {
					boolean hit = mcm.set("lblog:" + idNext, 
							lightBlogNext.writeToBytes());
					myTrxCounter.addMemOper(MemOperType.SET_BLOG, hit);
				} else {
					throw new Exception("Error: failed to fetch blog record " +
							"from database(show siblings transaction)!");
				}
			}
		}		
	}

}
