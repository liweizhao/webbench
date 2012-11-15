package com.netease.webbench.blogbench.transaction;

import com.netease.webbench.blogbench.dao.BlogDAO;
import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.blogbench.statis.BlogbenchCounters;

public class TransactionFactory {
	private static class Loader {
		public static TransactionFactory instance = new TransactionFactory();
	}
	
	public static TransactionFactory getInstance() {
		return Loader.instance;		
	}
	
	protected BbTestTransaction createTrx(BlogDAO blogDao, BbTestOptions bbTestOpt, 
			BlogbenchCounters trxCounters, BbTestTrxType type) throws IllegalArgumentException {
		switch (type) {
		case LIST_BLGS:
			return createListBlgTrx(blogDao, bbTestOpt, trxCounters);
		case SHOW_BLG:
			return createShowBlgTrx(blogDao, bbTestOpt, trxCounters); 
		case UPDATE_ACS:
			return createUpdateAcsTrx(blogDao, bbTestOpt, trxCounters); 
		case UPDATE_CMT:
			return createUpdateCmtTrx(blogDao, bbTestOpt, trxCounters); 
		case SHOW_SIBS:
			return createShowSiblingsTrx(blogDao, bbTestOpt, trxCounters); 
		case PUBLISH_BLG:
			return createPublishBlgTrx(blogDao, bbTestOpt, trxCounters); 
		case UPDATE_BLOG:
			return createUpdateBlgTrx(blogDao, bbTestOpt, trxCounters); 
		default:
			throw new IllegalArgumentException("Unknown transaction type!");
		}
	}
	
	public BbTestTransaction createListBlgTrx(BlogDAO blogDAO, 
			BbTestOptions bbTestOpt, BlogbenchCounters trxCounters) {
		return new BbTestTrxListBlg(blogDAO, bbTestOpt, trxCounters);
	}

	public BbTestTransaction createShowBlgTrx(BlogDAO blogDao, 
			BbTestOptions bbTestOpt, BlogbenchCounters trxCounters) {
		return new BbTestTrxShowBlg(blogDao, bbTestOpt, trxCounters);
	}

	public BbTestTransaction createUpdateAcsTrx(BlogDAO blogDao, 
			BbTestOptions bbTestOpt, BlogbenchCounters trxCounters) {
		return new BbTestTrxUpdateAcs(blogDao, bbTestOpt, trxCounters);
	}

	public BbTestTransaction createUpdateCmtTrx(BlogDAO blogDao, 
			BbTestOptions bbTestOpt, BlogbenchCounters trxCounters) {
		return new BbTestTrxUpdateCmt(blogDao, bbTestOpt, trxCounters);
	}

	public BbTestTransaction createShowSiblingsTrx(BlogDAO blogDao, 
			BbTestOptions bbTestOpt, BlogbenchCounters trxCounters) {
		return new BbTestTrxShowSiblings(blogDao, bbTestOpt, trxCounters);
	}

	public BbTestTransaction createPublishBlgTrx(BlogDAO blogDao, 
			BbTestOptions bbTestOpt, BlogbenchCounters trxCounters) {
		return new BbTestTrxPublishBlg(blogDao, bbTestOpt, trxCounters);
	}

	public BbTestTransaction createUpdateBlgTrx(BlogDAO blogDao, 
			BbTestOptions bbTestOpt, BlogbenchCounters trxCounters) {
		return new BbTestTrxUpdateBlg(blogDao, bbTestOpt, trxCounters);
	}
}
