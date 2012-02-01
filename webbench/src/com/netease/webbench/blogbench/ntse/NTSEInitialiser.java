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
package com.netease.webbench.blogbench.ntse;

import java.sql.ResultSet;

import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.blogbench.misc.ParaInitialiseHandler;
import com.netease.webbench.common.DbOptions;
import com.netease.webbench.common.DbSession;

/**
 * 
 * @author LI WEIZHAO
 *
 */
public class NTSEInitialiser implements ParaInitialiseHandler {
	private boolean useMms = false;
	private boolean cachedUpdate = false;
	private DbOptions dbOpt;
	private BbTestOptions bbTestOpt;
	
	public NTSEInitialiser(DbOptions dbOpt, BbTestOptions bbTestOpt) 
	throws Exception {
		this.dbOpt = dbOpt;
		this.bbTestOpt = bbTestOpt;
		
		if (dbOpt.getDbType().equalsIgnoreCase("mysql")
				&& bbTestOpt.getTbEngine().equalsIgnoreCase("ntse")) {
			DbSession dbSession = new DbSession(dbOpt);	
			
			String ntseCreateArgs = "";
			String sql = "select CREATE_ARGS from INFORMATION_SCHEMA.NTSE_TABLE_DEF_EX "
					+ "WHERE TABLE_SCHEMA like '"
					+ dbOpt.getDatabase()
					+ "' and TABLE_NAME like '"
					+ bbTestOpt.getTbName()
					+ "'";
			ResultSet rs = dbSession.query(sql);
			if (rs != null && rs.next()) {
				ntseCreateArgs = rs.getString(1);
			} else {
				throw new Exception("Can't excute query \"" + sql + "\".");
			}
			if (ntseCreateArgs.contains("usemms:1"))
				useMms = true;
			if (ntseCreateArgs.contains("cache_update:1"))
				cachedUpdate = true;
			
			dbSession.close();
		}
	}

	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.misc.ParaInitialiseHandler#doAfterInit()
	 */
	public void doAfterInit() throws Exception {
		// TODO Auto-generated method stub
		//check if need clean MMS(only used in MySQL NTSE storage engine)
		if (useMms) {
			DbSession dbSession = new DbSession(dbOpt);	
			NtseSpecialOper.enableMms(dbSession, dbOpt, bbTestOpt, cachedUpdate);
			dbSession.close();
		}
	}

	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.misc.ParaInitialiseHandler#doBeforeInit()
	 */
	public void doBeforeInit() throws Exception {
		// TODO Auto-generated method stub
		if (useMms) {
			DbSession dbSession = new DbSession(dbOpt);	
			NtseSpecialOper.disableMms(dbSession, dbOpt, bbTestOpt);
			dbSession.close();
		}
	}

}
