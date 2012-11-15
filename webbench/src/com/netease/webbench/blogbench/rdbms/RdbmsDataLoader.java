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

import com.netease.webbench.blogbench.dao.DataLoader;
import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.blogbench.misc.ParameterGenerator;
import com.netease.webbench.blogbench.misc.ntse.NtseSpecialOper;
import com.netease.webbench.blogbench.rdbms.sql.SQLConfigure;
import com.netease.webbench.blogbench.rdbms.sql.SQLConfigureFactory;
import com.netease.webbench.blogbench.statis.LoadDataStatis;
import com.netease.webbench.blogbench.thread.BbTestInsertThread;
import com.netease.webbench.blogbench.thread.BlgRecordProducer;
import com.netease.webbench.blogbench.thread.ThreadBarrier;
import com.netease.webbench.common.DbOptions;
import com.netease.webbench.common.DbSession;
import com.netease.webbench.common.Util;

/**
 * 
 * @author LI WEIZHAO
 *
 */
public class RdbmsDataLoader implements DataLoader {
	/* default number of threads to execute insert */
	public static final int DEFAULT_INSERT_THREAD_CNT = 8;

	/* insert threads group */
	private BbTestInsertThread[] insertThrdGrp = null;
	
	private DbOptions dbOpt;
	private BbTestOptions bbTestOpt;
	private ParameterGenerator paraGen;
	private DbSession dbSession;
	private final LoadDataStatis statis;
	
	private BlgRecordProducer producer = null;
	private boolean isLoadSuccessful = false;
	
	public RdbmsDataLoader(DbOptions dbOpt, BbTestOptions bbTestOpt,
			ParameterGenerator paraGen) throws Exception {
		this.dbOpt = dbOpt;
		this.bbTestOpt = bbTestOpt;
		this.dbSession = new DbSession(dbOpt);
		this.statis = new LoadDataStatis();
		this.paraGen = paraGen;
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

	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.misc.DataLoader#load()
	 */
	@Override
	public void load() throws Exception {
		// TODO Auto-generated method stub
		producer = new BlgRecordProducer(paraGen, bbTestOpt.getTbSize());
		producer.start();
		
		//create insert threads and load data
		isLoadSuccessful = createThreadsAndLoadData(producer);
	}
	
	public void post() throws Exception {
		if (isLoadSuccessful) {
			if (producer != null)
				producer.join();
		
			System.out.println("\n\nSuccessful to insert all "
					+ bbTestOpt.getTbSize() + " records!");

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
		} else {
			if (producer != null)
				producer.forceExit();
			throw new Exception("Load data failed!");
		}
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
			dbSession.update(sqlConfig.getDropTblSql());
			System.out.println("OK!");
			if (bbTestOpt.getUseTwoTable()) {
				System.out.print("Drop old test table...");
				dbSession.update(sqlConfig.getDropTblSql());
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
		String createPrimarySql = sqlConfig.getCreatePrimaryIndexSql();
		dbSession.update(createPrimarySql);
		
		if (bbTestOpt.getUseTwoTable()) {
			String createContentPrimarySql = sqlConfig.getCreatePrimaryIndexSql();
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

	/**
	 * create insert threads group and load data
	 * @return is loading data successful ?
	 * @throws Exception 
	 */
	private boolean createThreadsAndLoadData(BlgRecordProducer producer) throws Exception {		
		int insertThrdCnt = bbTestOpt.getLoadThreads();
		ThreadBarrier barrier = new ThreadBarrier();
		createInsertThrdGrp(insertThrdCnt, barrier, producer);
		
		int index = 0;
		while (index < insertThrdCnt) {
			if (bbTestOpt.isDebug()) {
				if (insertThrdGrp[index] == null)
					throw new Exception("Create insert thread  " + index + " failed!");
			}
			if (insertThrdGrp[index].isWaiting()) {
				index++;
			} else {
				Thread.sleep(50);
			}
		}
		
		long start = Util.currentTimeMillis();
		
		//wake up all threads to work
		barrier.removeBarrier();
		
		//wait for all insert threads to exit
		boolean caughtErr = false;
		for (int i = 0; i < insertThrdCnt; i++) {
			insertThrdGrp[i].join();
			insertThrdGrp[i].clean();
			if (0 != insertThrdGrp[i].getErrorCode())
				caughtErr = true;
		}
		
		statis.addLoadDataTimeWaste(Util.currentTimeMillis() - start);		
		return !caughtErr;
	}
	
	/**
	 *  create insert threads group
	 *  @param thrdCnt
	 *  @param barrier
	 *  @return
	 */
	private void createInsertThrdGrp(int thrdCnt, ThreadBarrier barrier, BlgRecordProducer producer) throws Exception {	
		if (thrdCnt < 1) {
			throw new Exception("Number of load threads can't be " + thrdCnt + "!");
		}
		insertThrdGrp = new BbTestInsertThread[thrdCnt];
		
		long rcdToInsert = bbTestOpt.getTbSize() / thrdCnt;
		
		for (int i = 0; i < thrdCnt; i++) {
			long r = rcdToInsert;
			if (i == 0) {
				r = rcdToInsert + bbTestOpt.getTbSize() % thrdCnt;
			}
			insertThrdGrp[i] = new BbTestInsertThread(dbOpt, bbTestOpt, 
					r, barrier, producer);
			insertThrdGrp[i].start();
		}
	}
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.misc.LoadProgress#getProgress()
	 */
	public double getProgress() throws Exception {
		return getRecordInserted() * 1.0 / bbTestOpt.getTbSize(); 
	}
	
	/**
	 * get number of blog records already inserted
	 * @return
	 * @throws Exception
	 */
	public long getRecordInserted() throws Exception {
		long recordInserted = 0;
		if (insertThrdGrp != null) {
			for (int i = 0; i <  bbTestOpt.getLoadThreads(); i++) {
				if (insertThrdGrp[i] != null) 
					recordInserted += insertThrdGrp[i].getRecordInserted();
			}
			if (bbTestOpt.isDebug() && recordInserted > bbTestOpt.getTbSize()) {
				throw new Exception("Wrong num of records inserted!" + recordInserted);
			}
		}
		return recordInserted;
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

	@Override
	public LoadDataStatis getStatistics() {
		return statis;
	}
}
