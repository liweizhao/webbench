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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;

/**
 * memcached client of whalin implementation
 *  @author LI WEIZHAO
 */
public class MemcachedClientWhlImpl implements MemcachedClientIF {	
	static {
		BasicConfigurator.configure();
		org.apache.log4j.Logger.getRootLogger().setLevel(Level.FATAL);
	}
	private int clientCount = 0;
	
	private Random random = new Random(System.currentTimeMillis());
	
	private String mccConnectName = "default";

	private List<MemcachedClientWhlInner> clients = new ArrayList<MemcachedClientWhlInner>();
	
	/**
	 * constructor
	 * @param mccConnectName
	 * @param serverAddrs
	 * @param connCnt
	 * @throws IOException
	 */
	public MemcachedClientWhlImpl(String mccConnectName, String []serverAddrs, int connCnt) throws IOException {
		this.mccConnectName = mccConnectName;
		clientCount = connCnt;
		for (int i = 0; i < connCnt; i++) {
			clients.add(new MemcachedClientWhlInner(this.mccConnectName + i, serverAddrs));
		}
	}

	/**
	 * get internal client
	 * @return
	 */
	private MemcachedClientWhlInner getClient() {
		if (clientCount == 1) {
			return clients.get(0);
		} else {
			int num = random.nextInt();
			num = num % clientCount;
			if (num < 0)
				num += clientCount;
			return clients.get(num);			
		}
	}

   /*
    * (non-Javadoc)
    * @see com.netease.webbench.blogbench.memcached.MemcachedClientIF#add(java.lang.String, java.lang.Object, long)
    */
	public boolean add(String _key, Object _value, long _expiry) {
		return getClient().add(_key, _value, _expiry);
	}

   /*
    * (non-Javadoc)
    * @see com.netease.webbench.blogbench.memcached.MemcachedClientIF#add(java.lang.String, java.lang.Object)
    */
	public boolean add(String _key, Object _value) {
		return getClient().add(_key, _value);
	}

	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.memcached.MemcachedClientIF#delete(java.lang.String)
	 */
	public boolean delete(String _key) {
		return getClient().delete(_key);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.memcached.MemcachedClientIF#getCounter(java.lang.String)
	 */
	public long getCounter(String key) {
		return getClient().getCounter(key);
	}

	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.memcached.MemcachedClientIF#flushAll()
	 */
	public boolean flushAll() {
		return getClient().flushAll();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.memcached.MemcachedClientIF#addOrIncr(java.lang.String)
	 */
	public long addOrIncr(String key) {
		return getClient().addOrIncr(key);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.memcached.MemcachedClientIF#incr(java.lang.String)
	 */
	public long	incr(String key) throws Exception {
		return getClient().incr(key);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.memcached.MemcachedClientIF#incr(java.lang.String, int)
	 */
	public long	incr(String key, int inc) throws Exception {
		return getClient().incr(key, inc);
	}

	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.memcached.MemcachedClientIF#get(java.lang.String)
	 */
	public Object get(String _key) {
		return getClient().get(_key);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.memcached.MemcachedClientIF#getMulti(java.lang.String[])
	 */
	public Map<String, Object> getMulti(String[] keys) {
		return getClient().getMulti(keys);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.memcached.MemcachedClientIF#getMulti(java.util.Collection)
	 */
	public Map<String, Object> getMulti(Collection<String> keys) throws Exception {
		return getClient().getMulti(keys.toArray(new String[keys.size()]));
	}

	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.memcached.MemcachedClientIF#getStats()
	 */
	public Map<String, Map<String, String>> getStats() {
		return getClient().getStats();
	}

	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.memcached.MemcachedClientIF#replace(java.lang.String, java.lang.Object, long)
	 */
	public boolean replace(String _key, Object _value, long _expiry) {
		return getClient().replace(_key, _value, _expiry);
	}

	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.memcached.MemcachedClientIF#replace(java.lang.String, java.lang.Object)
	 */
	public boolean replace(String _key, Object _value) {
		return getClient().replace(_key, _value);
	}

	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.memcached.MemcachedClientIF#set(java.lang.String, java.lang.Object, long)
	 */
	public boolean set(String _key, Object _value, long _expiry) {
		return getClient().set(_key, _value, _expiry);
	}

	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.memcached.MemcachedClientIF#set(java.lang.String, java.lang.Object)
	 */
	public boolean set(String _key, Object _value) {
		return getClient().set(_key, _value);
	}

	/**
	 * get internal clients count
	 * @return
	 */
	public int getInnerClientCount() {
		return clientCount;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.netease.webbench.blogbench.memcached.MemcachedClientIF#shutdown()
	 */
	public void shutdown() {
		for (MemcachedClientWhlInner client : clients) {
			client.shutdown();
		}
	}
}
