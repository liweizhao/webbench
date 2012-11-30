package com.netease.webbench.blogbench.dao;

import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.common.DbOptions;

public interface BlogDaoFactory {
	public BlogDAO getBlogDao(DbOptions dbOpt, BbTestOptions bbTestOpt
			) throws Exception;
}
