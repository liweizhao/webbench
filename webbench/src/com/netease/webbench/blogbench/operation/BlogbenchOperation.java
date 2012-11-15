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

import sun.misc.Signal;

import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.common.DbOptions;
import com.netease.webbench.common.WebbenchSignalHandler;
import com.netease.webbench.common.WebbenchSignalRegister;

/**
 * blogbench operation
 * @author LI WEIZHAO
 *
 */
@SuppressWarnings("restriction")
public abstract class BlogbenchOperation implements WebbenchSignalHandler {
	/* database options */
	protected DbOptions dbOpt;
	
	/* blogbench test options */
	protected BbTestOptions bbTestOpt;
	
	/* operation type */
	protected final BlogbenchOperType operType;
	
	protected WebbenchSignalRegister signalRegister;
	
	/**
	 * constuctor
	 * @param actionType
	 * @param dbOpt
	 * @param bbTestOpt
	 * @throws Exception
	 */
	protected BlogbenchOperation(BlogbenchOperType operType, 
			DbOptions dbOpt, BbTestOptions bbTestOpt) throws Exception {
		this.operType = operType;
		this.dbOpt = dbOpt;
		this.bbTestOpt = bbTestOpt;
		this.signalRegister = new WebbenchSignalRegister(this);
	}
	
	/**
	 * execute blogbench test operation(LOAD / RUN)
	 * @throws Exception
	 */
	public abstract void execute() throws Exception;
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.BbTestSignalHandle#singleAction()
	 */
	public void signalAction(Signal signal) {
		System.exit(-1);
	}
}
