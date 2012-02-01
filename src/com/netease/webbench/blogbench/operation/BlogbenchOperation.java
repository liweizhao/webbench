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

import java.io.File;

import sun.misc.Signal;

import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.blogbench.misc.ParameterGenerator;
import com.netease.webbench.common.DbOptions;
import com.netease.webbench.common.WebbenchSignalHandler;
import com.netease.webbench.common.WebbenchSignalRegister;

/**
 * blogbench operation
 * @author LI WEIZHAO
 *
 */
public abstract class BlogbenchOperation implements WebbenchSignalHandler {
	/* database options */
	protected DbOptions dbOpt;
	
	/* blogbench test options */
	protected BbTestOptions bbTestOpt;
	
	/* query parameter generator */
	protected ParameterGenerator paraGen;	
	
	/* operation type */
	protected BlogbenchOperationType operType;
	
	protected WebbenchSignalRegister signalRegister;
	
	/**
	 * constuctor
	 * @param actionType
	 * @param dbOpt
	 * @param bbTestOpt
	 * @throws Exception
	 */
	protected BlogbenchOperation(BlogbenchOperationType operType, DbOptions dbOpt, 
			BbTestOptions bbTestOpt) throws Exception {
		this.operType = operType;
		this.dbOpt = dbOpt;
		this.bbTestOpt = bbTestOpt;
		this.signalRegister = new WebbenchSignalRegister(this);
		
		/* create query parameter generator */
		paraGen = new ParameterGenerator();
		
		/* initialize query parameter generator */
		paraGen.init(bbTestOpt, dbOpt);
	}
	
	/**
	 * create specified blogbench operation
	 * @param actionType   operation type
	 * @param dbOpt           database options
	 * @param bbTestOpt   blogbench test options
	 * @return                         blogbench operation
	 * @throws Exception
	 */
	public static BlogbenchOperation createBlogbenchOperation(BlogbenchOperationType operType, 
			DbOptions dbOpt, BbTestOptions bbTestOpt) throws Exception  {
		if (operType == BlogbenchOperationType.LOAD) {
			return new BlogbenchLoadOperation(dbOpt, bbTestOpt);
		} else  if (operType == BlogbenchOperationType.RUN) {
			return new BlogbenchRunOperation(dbOpt, bbTestOpt);
		} else {
			throw new Exception("Wrong blogbench operation !");
		}
	}
	
	/**
	 * execute blogbench test operation(LOAD / RUN)
	 * @throws Exception
	 */
	public abstract void executeOper() throws Exception;
	
	public BlogbenchOperationType getOperationType() {
		return operType;
	}
		
	/**
	 * create test result directory
	 * @throws Exception create report directory failed
	 */
	protected void makeReportDir() throws Exception {
		String reportDirPath = bbTestOpt.getReportDir();
		File reportDir = new File(reportDirPath);
		if (reportDir.exists() && !reportDir.isDirectory()) {
			throw new Exception("A file of the same name with the specified report directory exists, please specify another report directory name!");
		} else if (!reportDir.exists()) {
			System.out.print("Report directory doesn't exist, now create it...");
			if (reportDir.mkdirs()) {
				System.out.println("done.");
			} else {
				throw new Exception("Failed to make directories:" + reportDir.getName());
			}
		}
		String blogbenchPath = bbTestOpt.getReportDir() + "/blogbench-tmp";
		File blogbenchDir = new File(blogbenchPath);
		if (!blogbenchDir.exists() && !blogbenchDir.mkdir()) {
			throw new Exception("Faild to create blogbench test results saved diretory!");
		}
		if (bbTestOpt.isUsedMemcached()) {
			String memcachedReport = bbTestOpt.getReportDir() + "/memcached-statistic/";
			File memcachedReportDir = new File(memcachedReport);
			memcachedReportDir.mkdir();
		}
	}
	
	public DbOptions getDbOpt() {
		return dbOpt;
	}
	
	public BbTestOptions getBbTestOpt() {
		return bbTestOpt;
	}
	
	public ParameterGenerator getParaGen() {
		return paraGen;
	}
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.BbTestSignalHandle#singleAction()
	 */
	public void signalAction(Signal signal) {
		System.exit(-1);
	}
}
