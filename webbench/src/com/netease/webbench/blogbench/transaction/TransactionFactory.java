package com.netease.webbench.blogbench.transaction;

import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.blogbench.statis.BlogbenchCounters;
import com.netease.webbench.common.DbSession;

public class TransactionFactory {
	private static class Loader {
		public static TransactionFactory instance = new TransactionFactory();
	}
	
	public static TransactionFactory getInstance() {
		return Loader.instance;		
	}
	
	protected BbTestTransaction createTrx(
			DbSession dbSession, BbTestOptions bbTestOpt, 
			BlogbenchCounters trxCounters, BbTestTrxType type) throws Exception {
		switch (type) {
		case LIST_BLGS:
			return createListBlgTrx(dbSession, bbTestOpt, trxCounters);
		case SHOW_BLG:
			return createShowBlgTrx(dbSession, bbTestOpt, trxCounters); 
		case UPDATE_ACS:
			return createUpdateAcsTrx(dbSession, bbTestOpt, trxCounters); 
		case UPDATE_CMT:
			return createUpdateCmtTrx(dbSession, bbTestOpt, trxCounters); 
		case SHOW_SIBS:
			return createShowSiblingsTrx(dbSession, bbTestOpt, trxCounters); 
		case PUBLISH_BLG:
			return createPublishBlgTrx(dbSession, bbTestOpt, trxCounters); 
		case UPDATE_BLOG:
			return createUpdateBlgTrx(dbSession, bbTestOpt, trxCounters); 
		default:
			throw new IllegalArgumentException("Unknown transaction type!");
		}
	}
	
	public BbTestTransaction createListBlgTrx(DbSession dbSession, 
			BbTestOptions bbTestOpt, BlogbenchCounters trxCounters) throws Exception {
		return new BbTestTrxListBlg(dbSession, bbTestOpt, trxCounters);
	}

	public BbTestTransaction createShowBlgTrx(DbSession dbSession, 
			BbTestOptions bbTestOpt, BlogbenchCounters trxCounters) throws Exception {
		return new BbTestTrxShowBlg(dbSession, bbTestOpt, trxCounters);
	}

	public BbTestTransaction createUpdateAcsTrx(DbSession dbSession, 
			BbTestOptions bbTestOpt, BlogbenchCounters trxCounters) throws Exception {
		return new BbTestTrxUpdateAcs(dbSession, bbTestOpt, trxCounters);
	}

	public BbTestTransaction createUpdateCmtTrx(DbSession dbSession, 
			BbTestOptions bbTestOpt, BlogbenchCounters trxCounters) throws Exception {
		return new BbTestTrxUpdateCmt(dbSession, bbTestOpt, trxCounters);
	}

	public BbTestTransaction createShowSiblingsTrx(DbSession dbSession, 
			BbTestOptions bbTestOpt, BlogbenchCounters trxCounters) throws Exception {
		return new BbTestTrxShowSiblings(dbSession, bbTestOpt, trxCounters);
	}

	public BbTestTransaction createPublishBlgTrx(DbSession dbSession, 
			BbTestOptions bbTestOpt, BlogbenchCounters trxCounters) throws Exception {
		return new BbTestTrxPublishBlg(dbSession, bbTestOpt, trxCounters);
	}

	public BbTestTransaction createUpdateBlgTrx(DbSession dbSession, 
			BbTestOptions bbTestOpt, BlogbenchCounters trxCounters) throws Exception {
		return new BbTestTrxUpdateBlg(dbSession, bbTestOpt, trxCounters);
	}
}
