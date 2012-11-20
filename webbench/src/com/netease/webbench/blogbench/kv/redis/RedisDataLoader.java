package com.netease.webbench.blogbench.kv.redis;

import com.netease.webbench.blogbench.dao.SimpleDataLoader;
import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.blogbench.misc.ParameterGenerator;
import com.netease.webbench.common.DbOptions;

public class RedisDataLoader extends SimpleDataLoader {
		
	public RedisDataLoader(DbOptions dbOpt, BbTestOptions bbTestOpt,
			ParameterGenerator paraGen) throws Exception {
		super(dbOpt, bbTestOpt, paraGen);
	}
	
	@Override
	public void post() throws Exception {
		super.post();
		printStatistics();
	}
	
	/**
	 *  print statistics
	 */
	private void printStatistics() {
		System.out.println("Total time waste:  " 
				+ statis.getTotalTimeWaste() + "  milliseconds");
		System.out.println("Load data waste: " + statis.getLoadDataTimeWaste() 
				+ "  milliseconds");
	}

	@Override
	public String getLoadSummary() {
		StringBuilder buf = new StringBuilder(512);
		buf.append("Test table name: Blog\n")
			.append("Test table engine: " + bbTestOpt.getTbEngine() + "\n")
			.append("Test table size: " + bbTestOpt.getTbSize() + "\n")
			.append("Total time waste: " + statis.getTotalTimeWaste() + " milliseconds\n")
			.append("Create table waste: " + statis.getCreateTableTimeWaste() + "  milliseconds\n")
			.append("Load data waste: " + statis.getLoadDataTimeWaste() + "  milliseconds\n");
		return buf.toString();
	}
}
