package com.netease.webbench.blogbench.rdbms;

import com.netease.webbench.blogbench.BlogbenchPlugin;
import com.netease.webbench.blogbench.dao.BlogDaoFactory;
import com.netease.webbench.blogbench.dao.DataLoader;
import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.blogbench.misc.ParameterGenerator;
import com.netease.webbench.blogbench.misc.Portable;
import com.netease.webbench.common.DbOptions;
import com.netease.webbench.common.Util;

public class BlogbenchRdbmsPlugin implements BlogbenchPlugin {
	public BlogDaoFactory daoFacory = new RdbmsBlogDaoFactory();
	
	public BlogbenchRdbmsPlugin() {
	}

	@Override
	public void validateOptions(DbOptions dbOpt, BbTestOptions bbTestOpt)
			throws IllegalArgumentException {
		if (Util.isRdbms(dbOpt.getDbType())) {
			checkDflOptions(dbOpt, bbTestOpt);
			//now only support mysql, oracle, postgreSQL
			if (!dbOpt.getDbType().equalsIgnoreCase("mysql") &&
					!dbOpt.getDbType().equalsIgnoreCase("oracle") &&
					!dbOpt.getDbType().equalsIgnoreCase("postgresql")) {
				throw new IllegalArgumentException(
						"Unsuported database type :" + dbOpt.getDbType());
			}
		}			
	}
	
	/**
	 * check default options is correctly set
	 * @param dbOpt
	 * @param bbTestOpt
	 */
	private void checkDflOptions(DbOptions dbOpt, BbTestOptions bbTestOpt) {
		if (dbOpt.getDriverName() == null || dbOpt.getDriverName().equals("")) {
			dbOpt.setDriverName(Portable.getDflJdbcDrvName(dbOpt.getDbType()));
		}
		if (dbOpt.getJdbcUrl() == null || dbOpt.getJdbcUrl().equals("")) {
			dbOpt.setJdbcUrl(Portable.getDflJdbcUrl(dbOpt.getDbType(), 
					dbOpt.getHost(), dbOpt.getPort(), dbOpt.getDatabase()));
		}
		
		if (! bbTestOpt.specifiedDeferIdx() &&
				dbOpt.getDbType().equalsIgnoreCase("mysql") &&
				bbTestOpt.getTbEngine().equalsIgnoreCase("ntse")) {
			bbTestOpt.setDeferIndex(true);
		}
	}
	
	@Override
	public DataLoader getDataLoader(DbOptions dbOpt, BbTestOptions bbTestOpt, 
			ParameterGenerator parGen) throws Exception {
		return new RdbmsDataLoader(dbOpt, bbTestOpt, parGen, daoFacory);
	}

	@Override
	public BlogDaoFactory getBlogDaoFacory() throws Exception {
		return daoFacory;
	}
}
