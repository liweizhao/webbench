/**
  * Copyright (c) <2011>, <NetEase Corporation>
  * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *    3. Neither the name of the <ORGANIZATION> nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.netease.webbench.statis;

import java.io.IOException;
import java.util.Properties;

import com.netease.webbench.common.DbOptions;
/**
 * Run time information collector
 * @author LI WEIZHAO
 */
public class RunTimeInfoCollector {
	/* current run time information collector instance */
	private static RunTimeInfoCollector instance;
	/* if system information collecting process is running */
	private boolean running = false;
	/* system information collecting process */
	private Process proc;
	/* directory of result report */
	private String reportDir;
	/* directory of system information collecting scripts */
	private String scriptsDir;
	/* duration of system information collecting process */
	private long duration;
	private String tableEngine = "ntse";
	private DbOptions dbOpt;

	public void setDuration(long duration) {
		this.duration = duration;
	}

	private RunTimeInfoCollector() {
		running = false;
		duration = 0;

		reportDir = "./report/";
		scriptsDir = "./scripts/";
	}

	/**
	 * get system information collector instance
	 * 
	 * @return 
	 */
	public static RunTimeInfoCollector getInstance() {
		if (instance == null)
			instance = new RunTimeInfoCollector();
		return instance;
	}

	/**
	 * begin collecting system information
	 */
	public void beginCollectInfo() throws Exception {		
		Properties props=System.getProperties();
		String osName = props.getProperty("os.name");
		if (!osName.startsWith("Windows")) {
			Runtime rt = Runtime.getRuntime();
			
			//check that gnuplot command is available
			try {
				Process testP = rt.exec("gnuplot -e 'set term png'");
				testP.destroy();
				testP = null;
			} catch (IOException e) {
				String msg = "Please check that 'gnuplot', 'libgd' and 'libpng' tools have been installed!" ;
				throw new Exception(msg);
			}
			
			System.out.println("Try to run system status collecting scripts...");
			String command = scriptsDir + "run_status.sh -d " + duration + " -r " 
				+ reportDir + " -e " + tableEngine + " -H " + dbOpt.getHost() + " -l " + dbOpt.getPort() 
				+ " -u " + dbOpt.getUser() + " -D " + dbOpt.getDbType().toLowerCase();
			
			if (dbOpt.getPassword() != null && !dbOpt.getPassword().equals("")) {
				command = command + " -p " + dbOpt.getPassword();
			}
			
			System.out.println(command);
			
			proc = rt.exec(command);
			if (proc != null) {
				running = true;
				System.out.println("Successfull to run scripts!");
			} else {
				System.err.println("[Warning] Fail to run systems status collecting scripts!");
			}
		} else {
			System.err.println("[Warning] Collecting Windows system information is not supported!");
		}
	}
	/**
	 * stop collecting system information
	 * @param nomalExit   if not normal exit, force to kill system information collecting process
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void stopCollectInfo(boolean nomalExit) throws IOException, InterruptedException {	
		if (running) {
			Runtime rt = Runtime.getRuntime();
			if (!nomalExit)
				rt.exec("killall -2 run_status.sh");
			proc.waitFor();
		} else {
			System.err.println("No system status collecting process is running!");
		}
	}
	
	public boolean isRunning() {
		return running;
	}
	
	public String getReportDir() {
		return reportDir;
	}
	
	public void setReportDir(String reportDir) {
		this.reportDir = reportDir;
	}

	public void setDbOpt(DbOptions dbOpt) {
		this.dbOpt = dbOpt;
	}

	public void setTableEngine(String tableEngine) {
		this.tableEngine = tableEngine;
	}
}
