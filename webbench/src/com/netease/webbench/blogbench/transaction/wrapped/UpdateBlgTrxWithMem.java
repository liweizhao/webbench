package com.netease.webbench.blogbench.transaction.wrapped;

import java.io.Externalizable;

import com.netease.webbench.blogbench.blog.Blog;
import com.netease.webbench.blogbench.memcached.MemcachedClientIF;
import com.netease.webbench.blogbench.memcached.MemcachedManager;
import com.netease.webbench.blogbench.misc.ParameterGenerator;
import com.netease.webbench.blogbench.statis.BlogbenchCounters;
import com.netease.webbench.blogbench.statis.MemcachedOperCounter.MemOperType;
import com.netease.webbench.blogbench.transaction.BbTestTransaction;
import com.netease.webbench.blogbench.transaction.BbTestTrxUpdateBlg;

public class UpdateBlgTrxWithMem extends BbTestTransaction {
	private BbTestTrxUpdateBlg wrappedTrx;

	protected UpdateBlgTrxWithMem(BbTestTrxUpdateBlg wrappedTrx,
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
		Blog blog = paraGen.generateZipfDistrBlog();
		
		wrappedTrx.updateBlog(blog);
		
		MemcachedClientIF mcm = MemcachedManager.getInstance().getMajorMcc();
		boolean isReplaceBlgSuc = mcm.replace("lblog:" + 
		blog.getId(), blog.getLightBlog().writeToBytes());
		myTrxCounter.addMemOper(MemOperType.RPC_BLOG, isReplaceBlgSuc);
		
		boolean isReplaceCntSuc = mcm.replace("cnt:" + 
		blog.getId(), (Externalizable)blog.getBlogContent());
		myTrxCounter.addMemOper(MemOperType.RPC_CNT, isReplaceCntSuc);	
	}

}
