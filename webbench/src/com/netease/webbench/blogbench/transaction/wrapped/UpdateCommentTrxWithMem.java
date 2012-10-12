package com.netease.webbench.blogbench.transaction.wrapped;

import com.netease.webbench.blogbench.blog.BlogInfoWithPub;
import com.netease.webbench.blogbench.blog.LightBlog;
import com.netease.webbench.blogbench.memcached.MemcachedClientIF;
import com.netease.webbench.blogbench.memcached.MemcachedManager;
import com.netease.webbench.blogbench.misc.ParameterGenerator;
import com.netease.webbench.blogbench.statis.BlogbenchCounters;
import com.netease.webbench.blogbench.statis.MemcachedOperCounter.MemOperType;
import com.netease.webbench.blogbench.transaction.BbTestTransaction;
import com.netease.webbench.blogbench.transaction.BbTestTrxUpdateCmt;

public class UpdateCommentTrxWithMem extends BbTestTransaction {
	private BbTestTrxUpdateCmt wrappedTrx;

	protected UpdateCommentTrxWithMem(BbTestTrxUpdateCmt wrappedTrx,
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
		// TODO Auto-generated method stub
		BlogInfoWithPub blogInfo = paraGen.getZipfRandomBlog();
		long blogId = blogInfo.getBlogId();
		long uId = blogInfo.getUId();
		
		wrappedTrx.updateComment(blogId, uId);
		
		updateMemcached(blogId, uId);
	}
	
	private void updateMemcached(long blogId, long uId) {
		MemcachedClientIF mcm = MemcachedManager.getInstance().getMajorMcc();
		LightBlog lightBlog = new LightBlog();
		boolean readSuc = lightBlog.readFromBytes((byte[])mcm.get("lblog:" + blogId));
		
		myTrxCounter.addMemOper(MemOperType.GET_BLOG, readSuc);
		
		if (readSuc) {
			lightBlog.increaseCmtCnt();
			boolean hit = mcm.set("lblog:" + blogId, lightBlog.writeToBytes());
			myTrxCounter.addMemOper(MemOperType.SET_BLOG, hit);
		} else {
			lightBlog = blogFetcher.getLightBlog(blogId, uId);
			boolean hit = mcm.set("lblog:" + blogId, lightBlog.writeToBytes());
			myTrxCounter.addMemOper(MemOperType.SET_BLOG, hit);
		}
	}
}
