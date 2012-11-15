package com.netease.webbench.blogbench.dao;

import com.netease.webbench.blogbench.nosql.RedisBlogDao;
import com.netease.webbench.blogbench.rdbms.RdbmsBlogDao;
import com.netease.webbench.common.DbOptions;
import com.netease.webbench.common.DbSession;
import com.netease.webbench.common.Util;

public class BlogDAOFactory {
	public static BlogDAO getBlogDAO(DbOptions dbOpt, 
			boolean useTwoTable) throws Exception {
		if (Util.isRdbms(dbOpt.getDbType())) {
			if (!useTwoTable) {
				DbSession dbSession = new DbSession(dbOpt);
				dbSession.setClientCharaSet();
				return new RdbmsBlogDao(dbSession);
			} else
				throw new Exception("Not support two table test!");
		} else {
			return new RedisBlogDao();
		}
	}
}
