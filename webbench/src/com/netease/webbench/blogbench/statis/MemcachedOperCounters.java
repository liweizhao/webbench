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

import java.util.ArrayList;
import java.util.List;

import com.netease.webbench.blogbench.statis.MemcachedOperCounter.MemOperType;

/**
 * 
 * @author LI WEIZHAO
 *
 */
public class MemcachedOperCounters {
	private List<MemcachedOperCounter> memOperCntList;
	
	public MemcachedOperCounters() {
		memOperCntList = new ArrayList<MemcachedOperCounter>(MemcachedOperCounter.MEM_OPER_TYPE_NUM);
		for (int i = 0; i < MemcachedOperCounter.MEM_OPER_TYPE_NUM; i++) {
			memOperCntList.add(new MemcachedOperCounter(false));
		}
	}
	
	public double getMemHitRatio(MemOperType operType) throws Exception {
		int idx = MemOperType.getIndexOfType(operType);
		return memOperCntList.get(idx).getHitRatio();
	}
	
	public double getMemHitRatio(int idx) throws Exception {
		return memOperCntList.get(idx).getHitRatio();
	}
	
	public int size() {
		return memOperCntList.size();
	}
	
	public MemcachedOperCounter getMemOperCounter(MemOperType operType) throws Exception {
		int idx = MemOperType.getIndexOfType(operType);
		return memOperCntList.get(idx);
	}
	
	/**
	 * add memcached operation statistic information
	 * @param operType
	 * @param successful
	 * @throws Exception
	 */
	public void addMemOper(MemOperType operType, boolean successful) throws Exception {
		int idx = MemOperType.getIndexOfType(operType);
		memOperCntList.get(idx).addOper(successful);
	}
	
	public long getMemTotalOper(MemOperType operType) throws Exception {
		int idx = MemOperType.getIndexOfType(operType);
		return memOperCntList.get(idx).getTotalOper();
	}
	
	public long getMemFailedOper(MemOperType operType) throws Exception {
		int idx = MemOperType.getIndexOfType(operType);
		return memOperCntList.get(idx).getFailedOper();
	}
	
	public long getMemSucOper(MemOperType operType) throws Exception {
		int idx = MemOperType.getIndexOfType(operType);
		return memOperCntList.get(idx).getSuccessOper();	
	}
}
