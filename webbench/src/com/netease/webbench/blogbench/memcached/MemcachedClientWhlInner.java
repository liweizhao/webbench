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

import java.util.Date;
import java.util.Map;

import com.danga.MemCached.MemCachedClient;
import com.danga.MemCached.SockIOPool;

/**
 *  @author LI WEIZHAO
 */
public class MemcachedClientWhlInner {
	public static final int DEFAULT_MIN_CONCURRENT_CONNS = 5;
	public static final int DEFAULT_MAX_CONCURRENT_CONNS = 1024;
	
	private String mccConnectName = "default";
	private String[] serverAddrs;
	private MemCachedClient mcc;
	private SockIOPool pool;

	public MemcachedClientWhlInner(String mccConnectName, String []servers) {
		this.mccConnectName = mccConnectName;
		this.serverAddrs = servers;
		
		Integer[] weights = new Integer[servers.length];
		for (int i = 0; i < servers.length; i++) {
			weights[i] = 1;
		}
		// grab an instance of our connection pool
		pool = SockIOPool.getInstance(this.mccConnectName);

		// set the servers and the weights		
		pool.setServers(servers);
		pool.setWeights(weights);

		// set some basic pool settings
		// 5 initial, 5 min, and 250 max conns
		// and set the max idle time for a conn
		// to 6 hours
		pool.setInitConn(DEFAULT_MIN_CONCURRENT_CONNS);
		pool.setMinConn(DEFAULT_MIN_CONCURRENT_CONNS);
		pool.setMaxConn(DEFAULT_MAX_CONCURRENT_CONNS);
		pool.setMaxIdle(1000 * 60 * 60 * 6);

		// set the sleep for the maint thread
		// it will wake up every x seconds and
		// maintain the pool size
		pool.setMaintSleep( 30 );

		// set some TCP settings
		// disable nagle
		// set the read timeout to 3 secs
		// and don't set a connect timeout
		pool.setNagle(false);
		pool.setSocketTO( 20000 );
		pool.setSocketConnectTO(0);

		// initialise the connection pool
		pool.initialize();
		
		mcc = new MemCachedClient(this.mccConnectName);
		
		// lets set some compression on for the client
		// compress anything larger than 64k
		//mcc.setCompressEnable( false );
		//mcc.setCompressThreshold( 64 * 1024 );
		
		mcc.setSanitizeKeys(false);
	}
	
	public boolean add(String _key, Object _value) {
		return mcc.add(_key, _value);
	}
	public boolean add(String _key, Object _value, long exp) {
		return mcc.add(_key, _value, new Date(System.currentTimeMillis() + exp));
	}
	public boolean delete(String _key) {
		return mcc.delete(_key);
	}
	public boolean flushAll() {
		return mcc.flushAll();
	}
	public long getCounter(String key) {
		return mcc.getCounter(key);
	}
	
	public long addOrIncr(String key) {
		long rtn = mcc.addOrIncr(key);
		if (rtn == 0) {
			rtn = mcc.incr(key);
		}
		return rtn;
	}
	
	public long	incr(String key)  {
		return mcc.incr(key);
	}
	
	public long	incr(String key, int inc) {
		return mcc.incr(key, inc);
	}
	
	public Object get(String _key) {
		return mcc.get(_key);
	}
	public Map<String, Map<String, String>> getStats() {
		return mcc.stats(serverAddrs);
	}
	public boolean set(String _key, Object _value) {
		return mcc.set(_key, _value);
	}
	public boolean set(String _key, Object _value, long exp) {
		return mcc.set(_key, _value, new Date(System.currentTimeMillis() + exp));
	}
	public boolean replace(String _key, Object _value) {
		return mcc.replace(_key, _value);
	}
	public boolean replace(String _key, Object _value, long exp) {
		return mcc.replace(_key, _value, new Date(System.currentTimeMillis() + exp));
	}
	public Object[] getMultiArray(String[] keys) {
		return mcc.getMultiArray(keys);
	}
	public Map<String, Object> getMulti(String[] keys) {
		return mcc.getMulti(keys);
	}
	public void shutdown() {
		pool.shutDown();
	}
}
