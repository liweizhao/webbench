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

import java.sql.SQLException;

import com.netease.webbench.blogbench.dao.SimpleDataLoader;
import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.blogbench.misc.ParameterGenerator;
import com.netease.webbench.blogbench.misc.ntse.NtseSpecialOper;
import com.netease.webbench.blogbench.rdbms.sql.SQLConfigure;
import com.netease.webbench.blogbench.rdbms.sql.SQLConfigureFactory;
import com.netease.webbench.common.DbOptions;
import com.netease.webbench.common.DbSession;
import com.netease.webbench.common.Util;

/**
 * 
 * @author LI WEIZHAO
 *
 */
public class RdbmsDataLoader extends SimpleDataLoader {
	private DbSession dbSession;
	
	public RdbmsDataLoader(DbOptions dbOpt, BbTestOptions bbTestOpt,
			ParameterGenerator paraGen) throws Exception {
		super(dbOpt, bbTestOpt, paraGen);		
		this.dbSession = new DbSession(dbOpt);
	}
	
	public void pre() throws Exception {
		//create test table
		long timeStart = Util.currentTimeMillis();
		if (bbTestOpt.isCreateTable()) {
			//if old test table exists, drop it
			dropOldTable();
		
			System.out.println("Defer creating index: " + bbTestOpt.isDeferIndex());
			
			createTable();
		
			statis.addCreateTableTimeWaste(Util.currentTimeMillis() - timeStart);
		}
	}
	
	public void post() throws Exception {
		super.post();
		
		if (isLoadSuccessful) {
			long timeStart = Util.currentTimeMillis();
			
			//if defer creating index is set,  create primary index and secondary index
			if (bbTestOpt.isCreateTable() && bbTestOpt.isDeferIndex()) {
				createPrimaryKey();
				createSecondaryIndex();
			}
		
			/* calculate time wastes */
			statis.addCreateIndexTimeWaste( Util.currentTimeMillis() - timeStart);
	
			if (dbOpt.getDbType().equalsIgnoreCase("mysql") && bbTestOpt.getTbEngine().equalsIgnoreCase("ntse")) {
				//only for MySQL NTSE storage engine, execute addition statement
				NtseSpecialOper.excNonStandartStmt(dbSession, dbOpt, bbTestOpt);
			}
		}
		
		printStatistics();
	}
	
	/**
	 *  print statistics
	 */
	private void printStatistics() {
		System.out.println("Total time waste:  " 
				+ statis.getTotalTimeWaste() + "  milliseconds");
		System.out.println("Create table waste: " 
				+ statis.getCreateTableTimeWaste() + "  milliseconds");
		System.out.println("Load data waste: " + statis.getLoadDataTimeWaste() 
				+ "  milliseconds");
		System.out.println("Create index waste: " + statis.getCreateIndexTimeWaste()
				+ "  milliseconds");
	}
	
	/**
	 * create test table
	 * @throws SQLException
	 */
	public void createTable() throws SQLException, Exception {
		System.out.print("Creating test Blog table...");
		
		if (dbOpt.getDbType().equalsIgnoreCase("mysql")) {
			dbSession.update("SET storage_engine=" + bbTestOpt.getTbEngine());
		}
		
		//create blog table
		SQLConfigure sqlConfig = SQLConfigureFactory.getSQLConfigure(dbOpt.getDbType());
		String createBlogSql = sqlConfig.getCreateBlogTblSql(!bbTestOpt.isDeferIndex(), 
				bbTestOpt.getUseTwoTable());
    	
		dbSession.update(createBlogSql);
    	if (!bbTestOpt.isDeferIndex()) {
    		createSecondaryIndex();
    	}
	    
	    //create blog content table
	    if (bbTestOpt.getUseTwoTable()) {
	    	String createContentSql = sqlConfig.getCreateContentTblSql(!bbTestOpt.isDeferIndex());
	    	dbSession.update(createContentSql); 
	    }
	    
		System.out.println("OK!");
	}
	
	/**
	 * drop old table
	 * @throws SQLException 
	 */
	public void dropOldTable() throws Exception {
		try {
			System.out.print("Drop old test table...");
			
			SQLConfigure sqlConfig = SQLConfigureFactory.getSQLConfigure(dbOpt.getDbType());
			dbSession.update(sqlConfig.getDropBlogTblSql());
			System.out.println("OK!");
			if (bbTestOpt.getUseTwoTable()) {
				System.out.print("Drop old test table...");
				dbSession.update(sqlConfig.getDropContentTblSql());
				System.out.println("OK!");
			}
		} catch (SQLException e) {
			if (dbOpt.getDbType().equalsIgnoreCase("oracle")) {
				if (e.getErrorCode() != 942)
					throw e;
			} else if (dbOpt.getDbType().equalsIgnoreCase("postgresql")) {
				/** Older versions of PostgreSQL don't support "DROP TABLE IF EXISTS" command. 
				    When drop a table that doesn't exists, an error will be caught. */	
				if (!e.getMessage().contains("does not exist")) {
					//Thought this method is silly, it actually works
					throw e;
				}
			}
		}
	}
		
	/**
	 * create indexes
	 * @throws SQLException
	 */
	private void createPrimaryKey() throws SQLException, Exception {
		System.out.println("Creating primary index on table...");

		SQLConfigure sqlConfig = SQLConfigureFactory.getSQLConfigure(dbOpt.getDbType());
		String createPrimarySql = sqlConfig.getCreateBlogTblPrimaryIndexSql();
		dbSession.update(createPrimarySql);
		
		if (bbTestOpt.getUseTwoTable()) {
			String createContentPrimarySql = sqlConfig.getCreateContentTblPrimaryIndexSql();
			dbSession.update(createContentPrimarySql);
		}

		System.out.println("OK!");
	}
	
	/**
	 * create secondary index
	 * @throws SQLException
	 */
	private void createSecondaryIndex() throws Exception {
		System.out.print("Creating secondary index on table...");
		SQLConfigure sqlConfig = SQLConfigureFactory.getSQLConfigure(dbOpt.getDbType());
		String createIndexSql = sqlConfig.getCreateSecondaryIndexSql();
		dbSession.update(createIndexSql);
		System.out.println("OK!");	
	}
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.misc.DataLoader#getLoadSummary()
	 */
	public String getLoadSummary()  {
		StringBuilder buf = new StringBuilder(512);
		buf.append("Test table name: Blog\n")
			.append("Test table engine: " + bbTestOpt.getTbEngine() + "\n")
			.append("Test table size: " + bbTestOpt.getTbSize() + "\n")
			.append("defer creating index: " + bbTestOpt.isDeferIndex() + "\n")
			.append("Total time waste: " + statis.getTotalTimeWaste() + " milliseconds\n")
			.append("Create table waste: " + statis.getCreateTableTimeWaste() + "  milliseconds\n")
			.append("Load data waste: " + statis.getLoadDataTimeWaste() + "  milliseconds\n")
			.append("Create index waste: " + statis.getCreateIndexTimeWaste() 	+ "  milliseconds\n");
		return buf.toString();
	}
}
