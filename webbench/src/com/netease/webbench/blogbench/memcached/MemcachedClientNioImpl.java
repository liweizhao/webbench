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

import java.net.SocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import netease.net.spy.memcached.ConnectionFactory;
import netease.net.spy.memcached.DefaultConnectionFactory;
import netease.net.spy.memcached.MemcachedClient;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;

/**
 * memcached client nio implementation
 *  @author LI WEIZHAO
 */
public class MemcachedClientNioImpl implements MemcachedClientIF {
	static {
		BasicConfigurator.configure();
		org.apache.log4j.Logger.getRootLogger().setLevel(Level.FATAL);
	}
	
	public static final int CONCURRENT_THRDS = 32;//使用的并发连接线程数
	public static final long DEFAULT_EXPIRE_MILLI_SECONDS = 60*60*24*1000;//默认记录的过期时间
	public static final int DEFAULT_TIMEOUT_SECONDS = 5;//默认请求的等待响应时间
	public static final int DEFAULT_READ_BUF_SIZE = 512 * 1024;//默认读取缓冲区大小
	
	private MemcachedClient mcc;

	/**
	 * 
	 * @param mccServerList
	 * @throws Exception
	 */
	public MemcachedClientNioImpl(List<String> mccServerList) throws Exception {
		ConnectionFactory cf = new DefaultConnectionFactory(DefaultConnectionFactory.DEFAULT_OP_QUEUE_LEN, DEFAULT_READ_BUF_SIZE);
		if (mccServerList.size() != 0 && mccServerList.size() != 0) {
			mcc = new MemcachedClient(cf, mccServerList, CONCURRENT_THRDS);
			flushAll();
		} else {
			throw new Exception("Uninitialised memcached server lists passed to initial memcached connection!");
		}
	}
	
	/**
	 * 
	 * @param mccServerList
	 * @param concurrentThread
	 * @param bufferSize
	 * @param queueLen
	 * @throws Exception
	 */
	public MemcachedClientNioImpl(List<String> mccServerList, int concurrentThread, 
			int bufferSize, int queueLen) throws Exception {
		ConnectionFactory cf = new DefaultConnectionFactory(queueLen, bufferSize);
		if (mccServerList.size() != 0 && mccServerList.size() != 0) {
			mcc = new MemcachedClient(cf, mccServerList, concurrentThread);
			flushAll();
		} else {
			throw new Exception("Uninitialised memcached server lists passed to initial memcached connection!");
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.memcached.MemcachedClientIF#add(java.lang.String, java.lang.Object)
	 */
	public boolean add(String _key, Object _value) throws Exception {
		boolean rtn = false;
		Future<Boolean> future = mcc.asyncAdd(_key, _value, DEFAULT_EXPIRE_MILLI_SECONDS, null);
		try {
			rtn = future.get(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			future.cancel(false);
		}
		return rtn;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.memcached.MemcachedClientIF#add(java.lang.String, java.lang.Object, long)
	 */
	public boolean add(String _key, Object _value, long exp) throws Exception {
		boolean rtn = false;
		Future<Boolean> future = mcc.asyncAdd(_key, _value, exp, null);
		try {
			rtn = future.get(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			future.cancel(false);
		}
		return rtn;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.memcached.MemcachedClientIF#delete(java.lang.String)
	 */
	public boolean delete(String _key) throws Exception {
		boolean rtn = false;
		Future<Boolean> future = mcc.asyncDelete(_key, DEFAULT_TIMEOUT_SECONDS, null);
		try {
			rtn = future.get(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			future.cancel(false);
		}
		return rtn;
	}
	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.memcached.MemcachedClientIF#flushAll()
	 */
	public boolean flushAll() throws Exception {
		boolean rtn = false;
		Future<Boolean> future = mcc.asyncFlushAll();
		try {
			rtn = future.get(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			future.cancel(false);
		}
		return rtn;
	}
	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.memcached.MemcachedClientIF#getCounter(java.lang.String)
	 */
	public long getCounter(String key) throws Exception {
		return mcc.getCounter(key);
	}
	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.memcached.MemcachedClientIF#addOrIncr(java.lang.String)
	 */
	public long addOrIncr(String key) throws Exception {
		long rtn = mcc.incr(key);
		if (rtn < 0) {
			mcc.addCounter(key, new Long(1));
			rtn = 1;
		}
		return rtn;
	}
	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.memcached.MemcachedClientIF#incr(java.lang.String)
	 */
	public long	incr(String key) throws Exception {
		return mcc.incr(key);	
	}
	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.memcached.MemcachedClientIF#incr(java.lang.String, int)
	 */
	public long incr(String key, int inc) throws Exception {
		return mcc.incr(key, inc); 
	}
	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.memcached.MemcachedClientIF#get(java.lang.String)
	 */
	public Object get(String _key) throws Exception {
		Object rtn = null;
		Future<Object> future = mcc.asyncGet(_key, null, false);
		try {
			rtn = future.get(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			future.cancel(false);
		}
		return rtn;
	}
	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.memcached.MemcachedClientIF#getMulti(java.lang.String[])
	 */
	public Map<String, Object> getMulti(String[] keys) throws Exception {
		Map<String, Object> rtn = null;
		Future<Map<String, Object>> future = mcc.asyncGetBulk(keys, (Integer[])null);
		try {
			rtn= future.get(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			future.cancel(false);
		}
		return rtn;
	}
	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.memcached.MemcachedClientIF#getMulti(java.util.Collection)
	 */
	public Map<String, Object> getMulti(Collection<String> keys) throws Exception {
		Map<String, Object> rtn = null;
		Future<Map<String, Object>> future = mcc.asyncGetBulk(keys.toArray(new String[keys.size()]), (Integer[])null);
		try {
			rtn= future.get(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			future.cancel(false);
		}
		return rtn;		
	}
	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.memcached.MemcachedClientIF#getStats()
	 */
	public Map<SocketAddress, Map<String, String>> getStats() throws Exception {
		return mcc.getStats();
	}
	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.memcached.MemcachedClientIF#set(java.lang.String, java.lang.Object)
	 */
	public boolean set(String _key, Object _value) throws Exception {
		boolean rtn = false;
		Future<Boolean> future = mcc.asyncSet(_key, _value, DEFAULT_EXPIRE_MILLI_SECONDS, null);
		try {
			rtn = future.get(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			future.cancel(false);
		}
		return rtn;
	}
	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.memcached.MemcachedClientIF#set(java.lang.String, java.lang.Object, long)
	 */
	public boolean set(String _key, Object _value, long exp) throws Exception {
		boolean rtn = false;
		Future<Boolean> future = mcc.asyncSet(_key, _value, exp, null);
		try {
			rtn = future.get(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			future.cancel(false);
		}
		return rtn;
	}
	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.memcached.MemcachedClientIF#replace(java.lang.String, java.lang.Object)
	 */
	public boolean replace(String _key, Object _value) throws Exception {
		boolean rtn = false;
		Future<Boolean> future = mcc.asyncReplace(_key, _value, DEFAULT_EXPIRE_MILLI_SECONDS, null);
		try {
			rtn = future.get(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			future.cancel(false);
		}
		return rtn;
	}
	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.memcached.MemcachedClientIF#replace(java.lang.String, java.lang.Object, long)
	 */
	public boolean replace(String _key, Object _value, long exp) throws Exception {		
		boolean rtn = false;
		Future<Boolean> future = mcc.asyncReplace(_key, _value, exp, null);
		try {
			rtn = future.get(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			future.cancel(false);
		}
		return rtn;
	}
	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.memcached.MemcachedClientIF#shutdown()
	 */
	public void shutdown() {
		mcc.shutdown();
	}
}
