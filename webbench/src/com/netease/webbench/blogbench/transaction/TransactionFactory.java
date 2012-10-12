package com.netease.webbench.blogbench.transaction;

import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.blogbench.statis.BlogbenchCounters;
import com.netease.webbench.blogbench.transaction.wrapped.ListBlgTrxWithMem;
import com.netease.webbench.common.DbSession;

public class TransactionFactory {
	private static class Loader {
		public static TransactionFactory instance = new TransactionFactory();
	}
	
	public static TransactionFactory getInstance() {
		return Loader.instance;
	}
	
	public BbTestTransaction createTrx(DbSession dbSession, BbTestOptions bbTestOpt, 
			BlogbenchCounters trxCounters, BbTestTrxType type) throws Exception {
		boolean useTwoTable = bbTestOpt.getUseTwoTable();
		boolean useMemcached = bbTestOpt.isUsedMemcached();
		
		switch (type) {
		case LIST_BLGS:
			BbTestTrxListBlg trx = new BbTestTrxListBlg(
					dbSession, bbTestOpt, trxCounters);
			return useMemcached ? new ListBlgTrxWithMem(trx, trxCounters) : trx;
		case SHOW_BLG:
			break;
		case UPDATE_ACS:
			break;
		case UPDATE_CMT:
			break;
		case SHOW_SIBS:
			break;
		case PUBLISH_BLG:
			break;
		case UPDATE_BLOG:
			break;
		default:
			throw new IllegalArgumentException("Unknown transaction type!");
		}
		
		return null;
	}
}
