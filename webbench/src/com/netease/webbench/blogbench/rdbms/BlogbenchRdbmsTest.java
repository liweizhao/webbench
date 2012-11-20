/**
  * Copyright (c) <2011>, <NetEase Corporation>
  * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *    3. Neither the name of the <ORGANIZATION> nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.netease.webbench.blogbench.rdbms;

import com.netease.webbench.blogbench.BlogbenchTest;
import com.netease.webbench.blogbench.dao.DataLoader;
import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.blogbench.misc.ParameterGenerator;
import com.netease.webbench.blogbench.misc.Portable;
import com.netease.webbench.blogbench.operation.BlogbenchLoadOperation;
import com.netease.webbench.blogbench.operation.BlogbenchOperType;
import com.netease.webbench.blogbench.operation.BlogbenchOperation;
import com.netease.webbench.blogbench.operation.BlogbenchRunOperation;
import com.netease.webbench.common.DbOptions;
import com.netease.webbench.common.DbSession;
import com.netease.webbench.common.Util;

public class BlogbenchRdbmsTest extends BlogbenchTest {
	@Override
	public void setUp(String[] args) throws Exception {
		super.setUp(args);
		checkOptions();
		checkServerIsAlive();
	}

	@Override
	public BlogbenchOperation createOper(BlogbenchOperType type) throws Exception {
		// TODO Auto-generated method stub
		ParameterGenerator paraGen = new ParameterGenerator();
		paraGen.init(bbTestOpt, dbOpt);
		
		if (type == BlogbenchOperType.LOAD) {
			DataLoader dataLoader = new RdbmsDataLoader(dbOpt, bbTestOpt, paraGen);
			return new BlogbenchLoadOperation(dbOpt, bbTestOpt, dataLoader);
		} else if (type == BlogbenchOperType.RUN) {
			return new BlogbenchRunOperation(dbOpt, bbTestOpt, paraGen);
		}
		return null;
	}

	/**
	 * initialise blogbench test
	 * @param dbOpt
	 * @param bbTestOpt
	 * @throws Exception
	 */
	private void checkOptions() throws IllegalArgumentException {
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
	
	/**
	 *  check if database server is able to connect
	 * @return 
	 */
	protected void checkServerIsAlive() throws Exception {
		DbSession testSession = null;
		try {
			testSession = new DbSession(dbOpt);
		} finally {
			try {
				if (testSession != null) {
					testSession.close();
				}
			} catch (Exception e) {}
		}
	}
}
