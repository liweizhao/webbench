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
package com.netease.webbench.blogbench.misc.ntse;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.common.DbOptions;
import com.netease.webbench.common.DbSession;

/**
 * 
 *  Special operation only for MySQL NTSE storage engine
 * @author LI WEIZHAO
 */

public class NtseSpecialOper {
	
	public static final String NTSE_COMMAND_SQL = "SET ntse_command = \"ALTER TABLE SET ";
	
	private NtseSpecialOper() {}
	
	/**
	 * set NTSE index build algorithm
	 * @param dbSession
	 * @param setting
	 * @return
	 * @throws SQLException
	 */
	public static String setNtseIndexBuildAlgorithm(DbSession dbSession, String setting) throws SQLException, Exception {
		if (null == setting || (!setting.equals("readonly") && !setting.equals("online"))) {
			throw new Exception("Illegal ntse_index_build_algorithm: " + setting + ", only \"readonly\" or \"online\" is valid.");
		}
		String oldSetting = "readonly";
		ResultSet rs = dbSession.query("SHOW VARIABLES LIKE \"ntse_index_build_algorithm\"");
		if (rs.next()) {
			oldSetting = rs.getString("Value");
		}
		rs.close();
		
		dbSession.update("SET ntse_index_build_algorithm = " + setting);
		
		return oldSetting;
	}
	
	/**
	 * disable MMS
	 * @param dbSession
	 * @param dbOpt
	 * @param bbTestOpt
	 * @throws SQLException
	 */
	public static void disableMms(DbSession dbSession, DbOptions dbOpt, BbTestOptions bbTestOpt) throws SQLException {
		System.out.print("Disable mms...");
		String sql = NTSE_COMMAND_SQL + dbOpt.getDatabase() + ".Blog.usemms=false\"";
		dbSession.update(sql);
		sql = NTSE_COMMAND_SQL + dbOpt.getDatabase() + ".BlogContent.usemms=false\"";
		dbSession.update(sql);
		System.out.println("done.");
	}
	
	/**
	 * enable MMS
	 * @param dbSession
	 * @param dbOpt
	 * @param bbTestOpt
	 * @throws SQLException
	 */
	public static void enableMms(DbSession dbSession, DbOptions dbOpt, BbTestOptions bbTestOpt, boolean cachedUpdate) throws SQLException {
		System.out.print("Enable mms...");
		
		String sql = NTSE_COMMAND_SQL + dbOpt.getDatabase() + ".Blog";
		
		dbSession.update(sql + ".usemms=true\"");
		if (cachedUpdate) {
			dbSession.update(sql + ".cache_update=true\"");
			dbSession.update(sql + ".cached_columns=enable AccessCount\"");
		} 
		
		sql = NTSE_COMMAND_SQL + dbOpt.getDatabase() + ".BlogContent";		
		dbSession.update(sql + ".usemms=true\"");
		
		System.out.println("done.");
	}
	
	/**
	 * change NTSE non standard MySQL options
	 * @throws SQLException
	 */
	public static void excNonStandartStmt(DbSession dbSession, DbOptions dbOpt, BbTestOptions bbTestOpt) throws SQLException {
    		String ntseCreateTblArgs = bbTestOpt.getNtseCreateTblArgs();
    		
    		if (bbTestOpt.getNtseCreateTblArgs().equals("\"\""))     			
    			ntseCreateTblArgs = "usemms:true;cache_update:true;cached_columns:AccessCount";
    		
    		String[] createArgs = ntseCreateTblArgs.split(";");
			for (int i = 0; i < createArgs.length; i++) {
				if (createArgs[i].length() > 0) {
					String[] statements = createArgs[i].split(":");
					if (statements[0].equalsIgnoreCase("cached_columns")) {
						statements[1] = "ENABLE " + statements[1];
					}
					StringBuilder strBuilder = new StringBuilder();
					strBuilder.append("set ntse_command=\"alter table set ");
					strBuilder.append(dbOpt.getDatabase());
					strBuilder.append(".Blog.");
					strBuilder.append(statements[0]);
					strBuilder.append('=');
					strBuilder.append(statements[1]);
					strBuilder.append("\";");
					String createArgStatement = strBuilder.toString();
					
					System.out.println("Excute command: " + createArgStatement);
					dbSession.update(createArgStatement);
				}
			}
		}
}
