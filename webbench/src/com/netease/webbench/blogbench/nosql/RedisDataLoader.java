package com.netease.webbench.blogbench.nosql;

import com.netease.webbench.blogbench.dao.DataLoader;
import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.blogbench.misc.ParameterGenerator;
import com.netease.webbench.blogbench.statis.LoadDataStatis;
import com.netease.webbench.common.DbOptions;

public class RedisDataLoader implements DataLoader {
	private LoadDataStatis loadDataStatis;
	
	public RedisDataLoader(DbOptions dbOpt, BbTestOptions bbTestOpt,
			ParameterGenerator paraGen) throws Exception {
		loadDataStatis = new LoadDataStatis();
	}

	@Override
	public double getProgress() throws Exception {
		// TODO Auto-generated method stub
		return 1.0;
	}

	@Override
	public void pre() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void load() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void post() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public String getLoadSummary() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public LoadDataStatis getStatistics() {
		// TODO Auto-generated method stub
		return loadDataStatis;
	}

}
