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
package com.netease.webbench.blogbench.operation;

import java.io.FileWriter;
import java.sql.SQLException;
import java.util.Timer;

import com.netease.webbench.blogbench.misc.BbTestOptPair;
import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.blogbench.misc.LoadProgress;
import com.netease.webbench.blogbench.misc.LoadProgressTask;
import com.netease.webbench.blogbench.misc.Portable;
import com.netease.webbench.blogbench.ntse.NtseSpecialOper;
import com.netease.webbench.blogbench.sql.SQLConfigure;
import com.netease.webbench.blogbench.sql.SQLConfigureFactory;
import com.netease.webbench.blogbench.thread.BbTestInsertThread;
import com.netease.webbench.blogbench.thread.BlgRecordProducer;
import com.netease.webbench.blogbench.thread.ThreadBarrier;
import com.netease.webbench.common.DbOptions;
import com.netease.webbench.common.DbSession;
import com.netease.webbench.common.Util;

/**
 * blogbench load operation
 * @author LI WEIZHAO
 *
 */
public class BlogbenchLoadOperation extends BlogbenchOperation implements LoadProgress {	
	/* default number of threads to execute insert */
	public static final int DEFAULT_INSERT_THREAD_CNT = 8;
		
	/* relate to  statistic */
	private long totalTimeWaste = 0;
	private long createTableTimeWaste = 0;
	private long loadDataTimeWaste = 0;
	private long createIndexTimeWaste = 0;
	
	/* progress bar task */
	private Timer loadProgressTimer;
	private LoadProgressTask loadProgressTask;
	
	/* insert threads group */
	private BbTestInsertThread[] insertThrdGrp;
	
	private DbSession dbSession;
	/**
	 * constructor
	 * @param dbOpt
	 * @param bbTestOpt
	 * @throws Exception
	 */
	public BlogbenchLoadOperation(DbOptions dbOpt, BbTestOptions bbTestOpt) throws Exception {
		super(BlogbenchOperationType.LOAD, dbOpt, bbTestOpt);
		dbSession = new DbSession(dbOpt);
		dbSession.setClientCharaSet();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.BlogbenchOperation#executeOper()
	 */
	public void executeOper() throws Exception {

		/* make directory of test report */
		makeReportDir();

		try {
			System.out.println("Is loading data, please wait...");
			
			load();
			
			FileWriter fw = new FileWriter(bbTestOpt.getReportDir()
					+ "/loaddata_performance.txt");
			fw.write(getLoadSummary());
			fw.close();
			
			System.out.println("Load data finished!");	
		} catch (Exception e) {
			System.err.println("Failed to load data!");
			throw e;
		} finally {
			try {
				if (dbSession != null)
					dbSession.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	protected void load() throws Exception {
		//create test table
		long timeStart = Util.currentTimeMillis();
		
		if (bbTestOpt.isCreateTable()) {
			//if old test table exists, drop it
			dropOldTable();	
		
			System.out.println("Defer creating index: " + bbTestOpt.isDeferIndex());
			createTable();
		
			createTableTimeWaste = Util.currentTimeMillis() - timeStart;
			totalTimeWaste += createTableTimeWaste;
		}
		
		BlgRecordProducer producer = new BlgRecordProducer(paraGen, bbTestOpt.getTbSize());
		producer.start();
		
		//create insert threads and load data
		boolean isLoadSuccessful = createThrdsAndLoadData(producer);
		
		if (isLoadSuccessful) {
			producer.join();
		
			System.out.println("\n\nSuccessful to insert all "
					+ bbTestOpt.getTbSize() + " records!");

			timeStart = Util.currentTimeMillis();
		
			//if defer creating index is set,  create primary index and secondary index
			if (bbTestOpt.isCreateTable() && bbTestOpt.isDeferIndex()) {
				createPrimaryKey();
				createSecondaryIndex();
			}
		
			/* calculate time wastes */
			createIndexTimeWaste = Util.currentTimeMillis() - timeStart;
			totalTimeWaste += createIndexTimeWaste;
		
			timeStart = Util.currentTimeMillis();
			if (dbOpt.getDbType().equalsIgnoreCase("mysql") && bbTestOpt.getTbEngine().equalsIgnoreCase("ntse")) {
				//only for MySQL NTSE storage engine, execute addition statement
				NtseSpecialOper.excNonStandartStmt(dbSession, dbOpt, bbTestOpt);
			}
			totalTimeWaste += (Util.currentTimeMillis() - timeStart);

			printStatistics();
		} else {
			producer.forceExit();
			throw new Exception("Load data failed!");
		}
	}
	
	/**
	 * get load operation summary
	 * @return load operation summary
	 */
	protected String getLoadSummary()  {
		StringBuilder buf = new StringBuilder(512);
		buf.append("Test table name: " + bbTestOpt.getTbName() + "\n");
		buf.append("Test table engine: " + bbTestOpt.getTbEngine() + "\n");
		buf.append("Test table size: " + bbTestOpt.getTbSize() + "\n");
		buf.append("defer creating index: " + bbTestOpt.isDeferIndex() + "\n");
		buf.append("Total time waste: " + totalTimeWaste	+ " milliseconds\n");
		buf.append("Create table waste: " + createTableTimeWaste + "  milliseconds\n");
		buf.append("Load data waste: " + loadDataTimeWaste + "  milliseconds\n");
		buf.append("Create index waste: " + createIndexTimeWaste 	+ "  milliseconds\n");
		return buf.toString();
	}
	
	/**
	 * create insert threads group and load data
	 * @return is loading data successful ?
	 * @throws Exception 
	 */
	private boolean createThrdsAndLoadData(BlgRecordProducer producer) throws Exception {		
		int insertThrdCnt = bbTestOpt.getLoadThreads();
		ThreadBarrier barrier = new ThreadBarrier();
		createInsertThrdGrp(insertThrdCnt, barrier, producer);
		
		int index = 0;
		while (index < insertThrdCnt) {
			if (bbTestOpt.isDebug()) {
				if (insertThrdGrp[index] == null)
					throw new Exception("Create insert thread  " + index + " failed��");
			}
			if (insertThrdGrp[index].isWaiting()) {
				index++;
			} else {
				Thread.sleep(50);
			}
		}
		
		long start = Util.currentTimeMillis();
		System.out.print("Wake up all " + insertThrdCnt + " loading thread to work...");
		
		//wake up all threads to work
		barrier.removeBarrier();
		System.out.println("OK!");
		
		createProgressBar();
		
		//wait for all insert threads to exit
		boolean caughtErr = false;
		for (int i = 0; i < insertThrdCnt; i++) {
			insertThrdGrp[i].join();
			if (0 != insertThrdGrp[i].getErrorCode())
				caughtErr = true;
		}
		
		long stop = Util.currentTimeMillis();
		loadDataTimeWaste = (stop - start);	
		totalTimeWaste += loadDataTimeWaste;
		
		removeProgressBar(!caughtErr);
		
		return !caughtErr;
	}
	
	/**
	 *  print statistics
	 */
	private void printStatistics() {
		System.out.println("Total time waste:  " + totalTimeWaste + "  milliseconds");
		System.out.println("Create table waste: " + createTableTimeWaste + "  milliseconds");
		System.out.println("Load data waste: " + loadDataTimeWaste + "  milliseconds");
		System.out.println("Create index waste: " + createIndexTimeWaste + "  milliseconds");
	}
	
	/**
	 * create test table
	 * @throws SQLException
	 */
	private void createTable() throws SQLException, Exception {
		System.out.print("Creating test table(" + bbTestOpt.getTbName() + ")...");
		
		if (dbOpt.getDbType().equalsIgnoreCase("mysql")) {
			dbSession.update("SET storage_engine=" + bbTestOpt.getTbEngine());
		}
		
		//create blog table
		SQLConfigure sqlConfig = SQLConfigureFactory.getSQLConfigure(dbOpt.getDbType());
		String blogTableName = bbTestOpt.getTbName();
		String createBlogSql = sqlConfig.getCreateBlogTblSql(blogTableName, 	!bbTestOpt.isDeferIndex(), 
				bbTestOpt.getUseTwoTable());
    	
		dbSession.update(createBlogSql);
    	if (!bbTestOpt.isDeferIndex()) {
    		createSecondaryIndex();
    	}
	    
	    //create blog content table
	    if (bbTestOpt.getUseTwoTable()) {
	    	String blogContentTableName = Portable.getBlogContentTableName(bbTestOpt.getTbName());
	    	String createContentSql = sqlConfig.getCreateContentTblSql(blogContentTableName,
	    			!bbTestOpt.isDeferIndex());
	    	dbSession.update(createContentSql); 
	    }
	    
		System.out.println("OK!");
	}
	
	/**
	 * drop old table
	 * @throws SQLException 
	 */
	private void dropOldTable() throws Exception {
		try {
			String tableName = bbTestOpt.getTbName();
			System.out.print("Drop old test table(" + tableName + ")...");
			
			SQLConfigure sqlConfig = SQLConfigureFactory.getSQLConfigure(dbOpt.getDbType());
			dbSession.update(sqlConfig.getDropTblSql(tableName));
			System.out.println("OK!");
			if (bbTestOpt.getUseTwoTable()) {
				String cntTableName = Portable.getBlogContentTableName(bbTestOpt.getTbName());
				System.out.print("Drop old test table(" + cntTableName + ")...");
				dbSession.update(sqlConfig.getDropTblSql(cntTableName));
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
		String createPrimarySql = sqlConfig.getCreatePrimaryIndexSql(bbTestOpt.getTbName());
		dbSession.update(createPrimarySql);
		
		if (bbTestOpt.getUseTwoTable()) {
			String contentTblName = Portable.getBlogContentTableName(bbTestOpt.getTbName());
			String createContentPrimarySql = sqlConfig.getCreatePrimaryIndexSql(contentTblName);
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
		String createIndexSql = sqlConfig.getCreateSecondaryIndexSql(bbTestOpt.getTbName());
		createIndexSql = createIndexSql.replaceAll("_TableName", bbTestOpt.getTbName());
		dbSession.update(createIndexSql);	
		System.out.println("OK!");	
	}
	
	/**
	 *  create insert threads group
	 *  @param thrdCnt
	 *  @param barrier
	 *  @return
	 */
	private void createInsertThrdGrp(int thrdCnt, ThreadBarrier barrier, BlgRecordProducer producer) throws Exception {
		System.out.print("Create all " + thrdCnt + " loading thread...");
		
		if (thrdCnt < 1) {
			throw new Exception("Number of load threads can't be " + thrdCnt + "!");
		}
		insertThrdGrp = new BbTestInsertThread[thrdCnt];
		
		long rcdToInsert = bbTestOpt.getTbSize() / thrdCnt;
		
		for (int i = 0; i < thrdCnt; i++) {
			if (i == 0) {
				insertThrdGrp[i] = new BbTestInsertThread(new BbTestOptPair(bbTestOpt, dbOpt), paraGen, 
						rcdToInsert + bbTestOpt.getTbSize() % thrdCnt, 
						barrier, producer);
			} else {
				insertThrdGrp[i] = new BbTestInsertThread(new BbTestOptPair(bbTestOpt, dbOpt), paraGen, 
					rcdToInsert, barrier, producer);
			}
			insertThrdGrp[i].start();
		}
		
		System.out.println("OK!");
	}
	
	/**
	 * create progress bar
	 */
	private void createProgressBar() {
		loadProgressTimer = new Timer("Load progress print task thread");
		loadProgressTask = new LoadProgressTask(this);
		loadProgressTimer.schedule(loadProgressTask, 2000, 1000);
	}
	
	/**
	 * remove progress bar
	 * @throws Exception
	 */
	private void removeProgressBar(boolean waitForFinish) throws Exception {
		while (waitForFinish && !loadProgressTask.isFinish()) {
			Thread.sleep(10);
		}
		loadProgressTimer.cancel();
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
		for (int i = 0; i <  bbTestOpt.getLoadThreads(); i++) {
			if (insertThrdGrp[i] != null) 
				recordInserted += insertThrdGrp[i].getRecordInserted();
		}
		if (bbTestOpt.isDebug() && recordInserted > bbTestOpt.getTbSize()) {
			throw new Exception("Wrong num of records inserted��" + recordInserted);
		}
		return recordInserted;
	}	
}
