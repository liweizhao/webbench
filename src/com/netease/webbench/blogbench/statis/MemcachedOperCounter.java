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
package com.netease.webbench.blogbench.statis;

/**
 *  memcached operation counter
 *  @author LI WEIZHAO
 */
public class MemcachedOperCounter {	
	public static enum MemOperType { 
		GET_LIST,
		SET_LIST,
		DEL_LIST,
		GET_BLOG,
		SET_BLOG,
		RPC_BLOG,
		GET_ACS,
		INC_ACS,
		ADD_ACS,
		GET_CNT,
		SET_CNT,
		RPC_CNT;
		
		public static int getIndexOfType(MemOperType operType) throws Exception {
			return operType.compareTo(GET_LIST);
		}
	};
	
	public static final int MEM_OPER_TYPE_NUM = MemOperType.values().length;
	
	private boolean threadSafe = false;
	
	private long sucOper = 0;
	private long failedOper = 0;
	
	public MemcachedOperCounter() {
		this(false);
	}
	
	public MemcachedOperCounter(boolean threadSafe) {
		this.threadSafe = threadSafe;
	}
	
	public void addOper(boolean successful) {
		if (threadSafe) {
			synchronized(this) {
				if (!successful) {
					failedOper++;
				} else 
					sucOper++;
			}
		} else {
			if (!successful) {
				failedOper++;
			} else
				sucOper++;
		}
	}
	
	public long getTotalOper() {
		if (threadSafe) {
			synchronized(this) {
				return sucOper + failedOper;
			}
		} else {
			return sucOper + failedOper;
		}
	}
	
	public long getFailedOper() {
		if (threadSafe) {
			synchronized(this) {
				return failedOper;
			}
		} else {
			return failedOper;
		}		
	}
	
	public long getSuccessOper() {
		if (threadSafe) {
			synchronized(this) {
				return sucOper;
			}
		} else {
			return sucOper;
		}		
	}
	
	public double getHitRatio() {
		if (threadSafe) {
			synchronized(this) {
				long total = sucOper + failedOper;
				if (total != 0)
					return sucOper * 1.0 / total;
				else 
					return 0;
			}
		} else {
			long total = sucOper + failedOper;
			if (total != 0)
				return sucOper * 1.0 / total;
			else 
				return 0;
		}
	}
}
