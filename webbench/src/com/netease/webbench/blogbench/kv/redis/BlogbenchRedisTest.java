package com.netease.webbench.blogbench.kv.redis;

import com.netease.webbench.blogbench.BlogbenchTest;
import com.netease.webbench.blogbench.dao.DataLoader;
import com.netease.webbench.blogbench.misc.ParameterGenerator;
import com.netease.webbench.blogbench.operation.BlogbenchLoadOperation;
import com.netease.webbench.blogbench.operation.BlogbenchOperType;
import com.netease.webbench.blogbench.operation.BlogbenchOperation;
import com.netease.webbench.blogbench.operation.BlogbenchRunOperation;

public class BlogbenchRedisTest extends BlogbenchTest {
	@Override
	public BlogbenchOperation createOper(BlogbenchOperType type) throws Exception {
		// TODO Auto-generated method stub
		ParameterGenerator paraGen = new ParameterGenerator();
		paraGen.init(bbTestOpt, dbOpt);
		
		if (type == BlogbenchOperType.LOAD) {
			DataLoader dataLoader = new RedisDataLoader(dbOpt, bbTestOpt, paraGen);
			return new BlogbenchLoadOperation(dbOpt, bbTestOpt, dataLoader);
		} else if (type == BlogbenchOperType.RUN) {
			return new BlogbenchRunOperation(dbOpt, bbTestOpt, paraGen);
		}
		return null;
	}

}
