package com.netease.webbench.blogbench.dao;

import com.netease.webbench.blogbench.misc.LoadProgress;
import com.netease.webbench.blogbench.statis.LoadDataStatis;

public interface DataLoader extends LoadProgress {
	/**
	 * do something before load data
	 * @throws Exception
	 */
	public void pre() throws Exception;
	
	/**
	 * load data into database
	 * @throws Exception
	 */
	public void load() throws Exception;
	
	/**
	 * do something after load data
	 * @throws Exception
	 */
	public void post() throws Exception;
	
	/**
	 * get load operation summary
	 * @return load operation summary
	 */
	public String getLoadSummary();
	
	/**
	 * get load data statistics
	 * @return
	 */
	public LoadDataStatis getStatistics();
}
