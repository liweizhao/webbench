package com.netease.webbench.blogbench.transaction.wrapped;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.netease.webbench.blogbench.blog.BlogIdPair;
import com.netease.webbench.blogbench.blog.LightBlog;
import com.netease.webbench.blogbench.memcached.MemcachedClientIF;
import com.netease.webbench.blogbench.memcached.MemcachedManager;
import com.netease.webbench.blogbench.misc.ParameterGenerator;
import com.netease.webbench.blogbench.statis.BlogbenchCounters;
import com.netease.webbench.blogbench.statis.MemcachedOperCounter.MemOperType;
import com.netease.webbench.blogbench.transaction.BbTestTransaction;
import com.netease.webbench.blogbench.transaction.BbTestTrxListBlg;

public class ListBlgTrxWithMem extends BbTestTransaction {
	private BbTestTrxListBlg wrappedTrx;
	
	public ListBlgTrxWithMem(BbTestTrxListBlg wrappedTrx,
			BlogbenchCounters counters) throws Exception {
		super(wrappedTrx, counters);
		this.wrappedTrx = wrappedTrx;
	}

	@Override
	public void prepare() throws Exception {
		wrappedTrx.prepare();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void doExeTrx(ParameterGenerator paraGen) throws Exception {
		long uId = paraGen.getZipfUserId();
		
		MemcachedClientIF mcc = MemcachedManager.getInstance().getMajorMcc();
	
		ArrayList<Long> blogsList = (ArrayList<Long>)(Serializable)mcc.get(
				"blog:ids:" + uId);

		if (blogsList == null) {
			myTrxCounter.addMemOper(MemOperType.GET_LIST, false);
			blogsList = wrappedTrx.getListFromDb(dbSession, uId);
			boolean isSetSuc = mcc.set("blog:ids:" + uId, (Serializable)blogsList);
			myTrxCounter.addMemOper(MemOperType.SET_LIST, isSetSuc);
		} else {
			myTrxCounter.addMemOper(MemOperType.GET_LIST, true);
		}

		//may be some users don't have any blogs
		if (blogsList != null && blogsList.size() > 0) {

			//now we have got blog id list, will query memcached first
			ArrayList<String> keyList = new ArrayList<String>(blogsList.size());
			for (Long blogId : blogsList) {
				keyList.add("lblog:" + blogId);
			}

			//execute multi-get in memcached
			Map<String, Object> memBlogItems = mcc.getMulti(keyList);

			if (memBlogItems != null) {
				List<BlogIdPair> blogIdPairList = new ArrayList<BlogIdPair>();
				for (Long blogId : blogsList) {

					byte[] blogSerialData = (byte[]) memBlogItems.get("lblog:"
							+ blogId);

					myTrxCounter.addMemOper(MemOperType.GET_BLOG, 
							blogSerialData == null ? false 	: true);
					
					if (blogSerialData == null)
						blogIdPairList.add(new BlogIdPair(blogId, uId));
				}

				//fetch blog records from database
				List<LightBlog> lightBlogList = wrappedTrx.multiGetLightBlogFromDb(
						blogIdPairList);

				//put records to memcached
				if (lightBlogList != null) {
					if (lightBlogList.size() > 0) {
						for (LightBlog blogFromDb : lightBlogList) {
							boolean setSuc = mcc.set("lblog:" + blogFromDb.getId(), blogFromDb.writeToBytes());
							myTrxCounter.addMemOper(MemOperType.SET_BLOG, setSuc);
						}
					}
				} else {
					throw new Exception("BlogIdPairList is null");
				}
			} else {
				throw new Exception("Fatal error occured when do multi get from memcached!");
			}
		} 
	}

}
