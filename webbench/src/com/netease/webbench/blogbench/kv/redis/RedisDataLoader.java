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
package com.netease.webbench.blogbench.kv.redis;

import com.netease.webbench.blogbench.dao.SimpleDataLoader;
import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.blogbench.misc.ParameterGenerator;
import com.netease.webbench.common.DbOptions;

public class RedisDataLoader extends SimpleDataLoader {
		
	public RedisDataLoader(DbOptions dbOpt, BbTestOptions bbTestOpt,
			ParameterGenerator paraGen) throws Exception {
		super(dbOpt, bbTestOpt, paraGen);
	}
	
	@Override
	public void post() throws Exception {
		super.post();
		printStatistics();
	}
	
	/**
	 *  print statistics
	 */
	private void printStatistics() {
		System.out.println("Total time waste:  " 
				+ statis.getTotalTimeWaste() + "  milliseconds");
		System.out.println("Load data waste: " + statis.getLoadDataTimeWaste() 
				+ "  milliseconds");
	}

	@Override
	public String getLoadSummary() {
		StringBuilder buf = new StringBuilder(512);
		buf.append("Test table name: Blog\n")
			.append("Test table engine: " + bbTestOpt.getTbEngine() + "\n")
			.append("Test table size: " + bbTestOpt.getTbSize() + "\n")
			.append("Total time waste: " + statis.getTotalTimeWaste() + " milliseconds\n")
			.append("Create table waste: " + statis.getCreateTableTimeWaste() + "  milliseconds\n")
			.append("Load data waste: " + statis.getLoadDataTimeWaste() + "  milliseconds\n");
		return buf.toString();
	}
}
