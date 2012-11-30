package com.netease.webbench.blogbench.rdbms;

import com.netease.webbench.blogbench.dao.BlogDAO;
import com.netease.webbench.blogbench.dao.BlogDaoFactory;
import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.common.DbOptions;

public class RdbmsBlogDaoFactory implements BlogDaoFactory {

	@Override
	public BlogDAO getBlogDao(DbOptions dbOpt, BbTestOptions bbTestOpt)
			throws Exception {
		if (!bbTestOpt.getUseTwoTable()) {
			return new RdbmsBlogDao(dbOpt);
		} else {
			return new RdbmsBlogTwoTableDao(dbOpt);
		}
	}
}
