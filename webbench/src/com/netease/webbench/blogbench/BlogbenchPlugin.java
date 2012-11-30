package com.netease.webbench.blogbench;

import com.netease.webbench.blogbench.dao.BlogDaoFactory;
import com.netease.webbench.blogbench.dao.DataLoader;
import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.blogbench.misc.ParameterGenerator;
import com.netease.webbench.common.DbOptions;

public interface BlogbenchPlugin {
	
	public void validateOptions(DbOptions dbOpt, BbTestOptions bbTestOpt
			) throws IllegalArgumentException;
	
	public DataLoader getDataLoader(DbOptions dbOpt, BbTestOptions bbTestOpt, 
			ParameterGenerator parGen)  throws Exception;
	
	public BlogDaoFactory getBlogDaoFacory() throws Exception;
}
