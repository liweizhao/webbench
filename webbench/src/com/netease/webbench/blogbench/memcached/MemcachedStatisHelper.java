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
package com.netease.webbench.blogbench.memcached;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;

import com.netease.webbench.blogbench.memcached.MemcachedManager.MemcachedClientImplType;


/**
 * memcached statistic information helper
 *  @author LI WEIZHAO
 */
public class MemcachedStatisHelper {
	/**
	 * query major memcached server statistic information
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private static Map getMajorMemcachedStats() throws Exception {
		MemcachedClientIF majorMcc = MemcachedManager.getInstance().getMajorMcc();
		return majorMcc.getStats();
	}
	
	/**
	 * query minor memcached server statistic information
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private static Map getMinorMemcachedStats() throws Exception {
		MemcachedClientIF acsCountMcc = MemcachedManager.getInstance().getMinorMcc();
		return acsCountMcc.getStats();
	}
		
	/**
	 * get hit ratio of get operation
	 * @param host memcached server host
	 * @param port memcached server port 
	 * @param isMajor query major memcached server
	 * @return hit ratio of get operation
	 */
	public static double getDftMmcGetHitRate(Map<String, String> statisticMap) throws Exception {
	
		if (statisticMap == null) {
			System.out.println("Map of memcached key stats is null!");
			return 0;
		}
		String hits = statisticMap.get("get_hits").trim();	
		String cmdGets = statisticMap.get("cmd_get").trim();
		if (hits == null || cmdGets == null) {
			System.out.println("No get_hits and cmd_gets values found!");
			return 0;
		}
		long getHits = Long.parseLong(hits);
		long gets = Long.parseLong(cmdGets);
		
		if (gets != 0)
			return (double)(getHits) / gets;
		else
			return 0;
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, String> getMemcachedStatistic(String host, int port, boolean isMajor) throws Exception {
		Map<String, String> statisticMap = null;
		
		if (MemcachedManager.getMcImplType() == MemcachedClientImplType.SPY_IMPL) {
			InetSocketAddress iAddr = new InetSocketAddress(host, port);
			Map<SocketAddress, Map<String, String>> map;
			if (isMajor) {
				map = (Map<SocketAddress, Map<String, String>>)getMajorMemcachedStats();
			} else {
				map = (Map<SocketAddress, Map<String, String>>)getMinorMemcachedStats();
			}
			if (map == null) {
				System.out.println("Map of memcached server stats is null!");
				return null;
			}
			statisticMap = map.get(iAddr);
		} else if (MemcachedManager.getMcImplType() == MemcachedClientImplType.WHL_IMPL) {
			String serverAddr = host + ":" + port;
			Map<String, Map<String, String>> map;
			if (isMajor) {
				map = (Map<String, Map<String, String>>)getMajorMemcachedStats();
			} else {
				map = (Map<String, Map<String, String>>)getMinorMemcachedStats();
			}
			if (map == null) {
				System.out.println("Map of memcached server stats is null!");
				return null;
			}
			statisticMap = map.get(serverAddr);
		}
		return statisticMap;
	}
}
