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

/**
 * memcached servers manager
 * @author LI WEIZHAO
 */
import java.util.ArrayList;
import java.util.List;

import com.netease.webbench.blogbench.misc.BbTestOptions;

public class MemcachedManager {
	public static enum MemcachedClientImplType {
		WHL_IMPL,
		SPY_IMPL,
		INVALID_IMPL
	}
	
	protected static final int CONCURRENT_CONNECTIONS = 1;
	protected static MemcachedManager instance = new MemcachedManager();
	protected static String memcachedClientImplName = "MemcachedClientWhlImpl";	
	protected static MemcachedClientImplType mcImplType = MemcachedClientImplType.WHL_IMPL;
	
	protected BbTestOptions bbTestOpt;
	protected MemcachedClientIF majorMcc;
	protected MemcachedClientIF acsCountMcc;
	protected List<String> majorServerList;
	protected List<String> acsCntServerList; 
	protected boolean initialized = false;
	
	static {
		//System.setProperty("com.netease.webbench.common.memcachedClient", "MemcachedClientWhlImpl");
		
		String tmp = System.getProperty("com.netease.webbench.blogbench.memcached.memcachedClient");
		if (null != tmp) {
			memcachedClientImplName = tmp;
		}
		
		if (memcachedClientImplName.equals("MemcachedClientSpyImpl") || 
				memcachedClientImplName.equals("MemcachedClientNioImpl")) {
			mcImplType = MemcachedClientImplType.SPY_IMPL;
		} else if (memcachedClientImplName.equals("MemcachedClientWhlImpl")) {
			mcImplType = MemcachedClientImplType.WHL_IMPL;
		} else {
			mcImplType = MemcachedClientImplType.INVALID_IMPL;
		}
	}

	/**
	 * initialize memcached clients options
	 * @param bbTestOpt
	 * @throws Exception
	 */
	public void init(BbTestOptions bbTestOpt) throws Exception {	
		if (!initialized) {
			initialized = true;

			majorServerList = new ArrayList<String>();
			acsCntServerList = new ArrayList<String>();
			this.bbTestOpt = bbTestOpt;

			majorServerList.add(this.bbTestOpt.getMainMemcachedAddr());
			acsCntServerList.add(this.bbTestOpt.getMinorMemcachedAddr());

			System.out.println("Use memcached client: "
					+ memcachedClientImplName);

			if (memcachedClientImplName.equals("MemcachedClientNioImpl")) {
				if (majorServerList.size() > 0)
					majorMcc = new MemcachedClientNioImpl(majorServerList);
				if (acsCntServerList.size() > 0)
					acsCountMcc = new MemcachedClientNioImpl(acsCntServerList);
			} else if (memcachedClientImplName.equals("MemcachedClientSpyImpl")) {
				if (majorServerList.size() > 0)
					majorMcc = new MemcachedClientSpyImpl(majorServerList);
				if (acsCntServerList.size() > 0)
					acsCountMcc = new MemcachedClientSpyImpl(acsCntServerList);
			} else if (memcachedClientImplName.equals("MemcachedClientWhlImpl")) {
				if (majorServerList.size() > 0)
					majorMcc = new MemcachedClientWhlImpl("common memcached", majorServerList.toArray(new String[1]), CONCURRENT_CONNECTIONS);
				if (acsCntServerList.size() > 0)
					acsCountMcc = new MemcachedClientWhlImpl("access count memcached", acsCntServerList.toArray(new String[1]), 1);
			} else
				throw new Exception("Can't find memcached client class: "
						+ memcachedClientImplName);
		} else {
			System.out.println("[WARNING] memcached servers has been initialized!");
		}
	}

	public void flushAllServerCache() throws Exception {
		majorMcc.flushAll();
		acsCountMcc.flushAll();
	}
	
	public void shutdownAll() {
		if (initialized) {
			majorMcc.shutdown();
			acsCountMcc.shutdown();
			majorMcc = acsCountMcc = null;
			initialized = false;
		}
	}
			
	private MemcachedManager() {
		//do nothing
	}
	
	/**
	 * get instance of memcached servers manager
	 * @return
	 */
	public static MemcachedManager getInstance() {
		return instance;
	}
	/**
	 * get instance of major memcached client
	 * @return
	 */
	public MemcachedClientIF getMajorMcc() {
		return majorMcc;
	}
	/**
	 * get instance of minor memcached client
	 * @return
	 */
	public MemcachedClientIF getMinorMcc() {
		return acsCountMcc;
	}

	/**
	 * is using spy memcached client implementation
	 * @return
	 */
	public static boolean isUseSpy() {
		return mcImplType == MemcachedClientImplType.SPY_IMPL;
	}

	public static String getMemcachedClientImplName() {
		return memcachedClientImplName;
	}

	public List<String> getMajorServerList() {
		return majorServerList;
	}

	public List<String> getAcsCntServerList() {
		return acsCntServerList;
	}

	public static MemcachedClientImplType getMcImplType() {
		return mcImplType;
	}
	
	public static void main(String[] args) {
		MemcachedManager mm = MemcachedManager.getInstance();
		try {
			mm.init(new BbTestOptions());
			
			mm.getMajorMcc().set("1", "aaa");
			
			String str = (String)mm.getMajorMcc().get("1");
			
			if (str != null && str.equals("aaa"))
				System.out.println("Memcached server is OK!");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isInitialized() {
		return initialized;
	}
}
