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
package com.netease.webbench.blogbench.thread;

import com.netease.webbench.blogbench.misc.BbTestOptPair;
import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.blogbench.misc.ParameterGenerator;
import com.netease.webbench.common.DbOptions;
import com.netease.webbench.common.DbSession;

/**
 * blogbench test thread
 * @author LI WEIZHAO
 */
public abstract class BbTestThread extends Thread {	
	/* database connection */
	protected DbSession dbSession;
	
	/* database option */
	protected DbOptions dbOpt;

	/* blogbench test option */
	protected BbTestOptions bbTestOpt;

	/* query parameter generator */
	protected ParameterGenerator paraGen;
	
	protected ThreadBarrier barrier;
	
	/* if thread is suspend */
	protected boolean isWaiting = false;
	
	/* thread error code when exit, 0 for normal exit */
	protected int exitErrorCode = 0;
	
	protected BbTestThread(BbTestOptPair bbTestOptPair, ParameterGenerator paraGen, ThreadBarrier barrier) throws Exception {
		this.dbOpt = bbTestOptPair.getDbOpt();
		
		this.bbTestOpt = bbTestOptPair.getBbTestOpt();
		
		//create database connection with database options
		dbSession = new DbSession(dbOpt);
		
		dbSession.setClientCharaSet();
		
		this.paraGen = paraGen;
		
		this.barrier = barrier;
	}

	/**
	 * suspend thread
	 * @throws InterruptedException
	 */
	protected void myWait() throws InterruptedException {
		if (barrier != null) {
			setIsWaiting(true);
			barrier.waitBarrier();
			setIsWaiting(false);
		}
	}
	
	/**
	 * check thread is suspend
	 * @return
	 */
	public synchronized boolean isWaiting() {
		return isWaiting;
	}
	
	/**
	 * set is waiting
	 * @param isWaiting
	 */
	protected synchronized void setIsWaiting(boolean isWaiting) {
		this.isWaiting = isWaiting;
	}
	
	/**
	 * get exit error code
	 * @return
	 */
	public int getErrorCode() {
		return exitErrorCode;
	}
}
