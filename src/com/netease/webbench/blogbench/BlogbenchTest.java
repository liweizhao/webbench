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
package com.netease.webbench.blogbench;

import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.blogbench.misc.Portable;
import com.netease.webbench.blogbench.operation.BlogbenchOperation;
import com.netease.webbench.common.DbOptions;
import com.netease.webbench.common.DbSession;

/**
 * blogbench test
 * @author LI WEIZHAO
 */
public class BlogbenchTest {
	/* single instance of blogbench test */
	private static BlogbenchTest instance;
	
	/* database options */
	private DbOptions dbOpt;
	
	/* blogbench test options */
	private BbTestOptions bbTestOpt;

	private BlogbenchTest() {}
			
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
			dbOpt.setJdbcUrl(Portable.getDflJdbcUrl(dbOpt.getDbType(), dbOpt.getHost(), 
					dbOpt.getPort(), dbOpt.getDatabase()));
		}
		if (! bbTestOpt.specifiedDeferIdx() &&
				dbOpt.getDbType().equalsIgnoreCase("mysql") &&
				bbTestOpt.getTbEngine().equalsIgnoreCase("ntse")) {
			bbTestOpt.setDeferIndex(true);
		}		
	}
	
	/**
	 * get blogbench test instance
	 * @return current blogbench test instance
	 */
	public static BlogbenchTest getInstance() {
		if (instance == null){
			instance = new BlogbenchTest();
		}
		return instance;
	}

	/**
	 * initialise blogbench test
	 * @param dbOpt
	 * @param bbTestOpt
	 * @throws Exception
	 */
	public void init(DbOptions dbOpt, BbTestOptions bbTestOpt) throws Exception {
		System.out.println("Blogbench test is initilizing...");
		this.dbOpt = dbOpt;
		this.bbTestOpt = bbTestOpt;
		
		checkDflOptions(dbOpt, bbTestOpt);
		
		//now only support mysql, oracle, postgreSQL
		if (!dbOpt.getDbType().equalsIgnoreCase("mysql") &&
				!dbOpt.getDbType().equalsIgnoreCase("oracle") &&
				!dbOpt.getDbType().equalsIgnoreCase("postgresql")) {
			throw new Exception("Unsuported database type :" + dbOpt.getDbType());
		}
		
		checkServerIsAlive();
		
		//checkServerCharaSet();
	}
	
	/***
	 * run blogbench test
	 * @throws Exception
	 */
	public void runTest() throws Exception {
		BlogbenchOperation blogbenchOperation = BlogbenchOperation.createBlogbenchOperation(
				bbTestOpt.getOperType(), dbOpt, bbTestOpt);
		blogbenchOperation.executeOper();
	}
	
	/**
	 *  check character set of database server
	 * @throws Exception
	 */
	protected void checkServerCharaSet() throws Exception {
		/*DbSession dbSession = new DbSession(dbOpt);
		dbSession.checkServerCharaSet();
		dbSession.close();*/
	}
	
	/**
	 *  check if database server is able to connect
	 * @return 
	 */
	protected void checkServerIsAlive() {
		DbSession testSession = null;
		try {
			testSession = new DbSession(dbOpt);
		} catch (Exception e) {
			e.printStackTrace();
		}	finally {
			try {
				Thread.sleep(300);
				if (testSession != null) {
					testSession.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
