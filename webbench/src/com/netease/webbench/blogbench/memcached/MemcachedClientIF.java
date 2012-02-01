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

import java.util.Collection;
import java.util.Map;

/**
 * interface of different memcached client
 * @author LI WEIZHAO
 */
public interface MemcachedClientIF {
	/**
	 * add object to memcached server, never expire
	 * @param _key		key
	 * @param _value	object to add
	 * @return	 true for add successfully, otherwise for fail (key exists etc.)
	 * @throws Exception
	 */
	public boolean add(String _key, Object _value) throws Exception;
	
	/**
	 * add object to memcached server,
	 * @param _key		key
	 * @param _value	object to add
	 * @param _expiry	expire time, unit is ms£¬value larger than 30 days (60*60*24*30*1000)£¬is treated as absolute time£¬otherwise relative time£¬
	 *                                  for example 60000 means expire after 1 minute£¬and 1256851800000 means expire after this time
	 * @return	 true for add successfully, otherwise for fail (key exists etc.)
	 * @throws Exception
	 */
	public boolean add(String _key, Object _value, long exp) throws Exception;
	
	/**
	 * delete object from memcached server
	 * @param _key
	 * @return if delete successful
	 * 	@throws Exception
	 */
	public boolean delete(String _key) throws Exception;
	
	/**
	 * clean all objects in server
	 * @return
	 * @throws Exception
	 */
	public boolean flushAll() throws Exception;
	
	/**
	 * get value of object with specified key
	 * @param _key
	 * @return
	 * @throws Exception
	 */
	public Object get(String _key) throws Exception;
	
	/**
	 * get multi objects once
	 * @param keys   keys of objects
	 * @return if keys is null, return null, otherwise return Map of key-values
	 * @throws Exception
	 */
	public Map<String, Object> getMulti(String[] keys) throws Exception;
	
	/**
	 * get multi objects once
	 * @param keys   keys of objects
	 * @return if keys is null, return null, otherwise return Map of key-values
	 * @throws Exception
	 */
	public Map<String, Object> getMulti(Collection<String> keys) throws Exception;
	
	/**
	 * get statistic information of this client
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Map getStats() throws Exception;
	
	/**
	 * set value of a object
	 * if object of the specified key not exists, then add this object, otherwise replace the value of this object
	 * @param _key		key
	 * @param _value	object value
	 * @return
	 * @throws Exception
	 */
	public boolean set(String _key, Object _value) throws Exception;
	
	/**
	 * set value of a object
	 * if object of the specified key not exists, then add this object, otherwise replace the value of this object
	 * @param _key		key
	 * @param _value	object value
	 * @param _expiry   expire time
	 * @return
	 * @throws Exception
	 */
	public boolean set(String _key, Object _value, long exp) throws Exception;
	
	/**
	 * replace a object
	 * if object of the specified key not exists, then do nothing, otherwise replace the value of this object
	 * @param _key		key
	 * @param _value	new object value
	 * @return
	 * @throws Exception
	 */
	public boolean replace(String _key, Object _value) throws Exception;
	
	/**
	 * replace a object
	 * if object of the specified key not exists, then do nothing, otherwise replace the value of this object
	 * @param _key		key
	 * @param _value	new object value
	 * @param _expiry   expire time
	 * @return
	 * @throws Exception
	 */
	public boolean replace(String _key, Object _value, long exp) throws Exception;
		
	/**
	 * increase counter 
	 * @param key
	 * @return if counter with specified key exists, return new value of counter, otherwise return -1
	 * @throws Exception
	 */
	public long	incr(String key) throws Exception;
	
	/**
	 *  add  increment to counter with specified key 
	 * @param key 
	 * @param inc increment
	 * @return if counter with specified key exists, return new value of counter, otherwise return -1
	 * @throws Exception
	 */
	public long	incr(String key, int inc) throws Exception;
	
	/**
	 *  increase counter or add new counter if counter not exists
	 * @param key 
	 * @return if counter with specified key exists, return new value of counter, otherwise return initial value 0
	 * @throws Exception
	 */
	public long addOrIncr(String key) throws Exception;
	
	/**
	 * get counter of specified key
	 * @param key
	 * @return if counter with specified key exists, return counter, otherwise return -1
	 * @throws Exception
	 */
	public long getCounter(String key) throws Exception;
	
	/**
	 * close client connection
	 */
	public void shutdown();
}
