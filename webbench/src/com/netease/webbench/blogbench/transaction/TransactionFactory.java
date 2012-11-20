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
package com.netease.webbench.blogbench.transaction;

import com.netease.webbench.blogbench.dao.BlogDAO;
import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.blogbench.statis.BlogbenchCounters;

public class TransactionFactory {
	private static class Loader {
		public static TransactionFactory instance = new TransactionFactory();
	}
	
	public static TransactionFactory getInstance() {
		return Loader.instance;		
	}
	
	protected BbTestTransaction createTrx(BlogDAO blogDao, BbTestOptions bbTestOpt, 
			BlogbenchCounters trxCounters, BbTestTrxType type) throws IllegalArgumentException {
		switch (type) {
		case LIST_BLGS:
			return createListBlgTrx(blogDao, bbTestOpt, trxCounters);
		case SHOW_BLG:
			return createShowBlgTrx(blogDao, bbTestOpt, trxCounters); 
		case UPDATE_ACS:
			return createUpdateAcsTrx(blogDao, bbTestOpt, trxCounters); 
		case UPDATE_CMT:
			return createUpdateCmtTrx(blogDao, bbTestOpt, trxCounters); 
		case SHOW_SIBS:
			return createShowSiblingsTrx(blogDao, bbTestOpt, trxCounters); 
		case PUBLISH_BLG:
			return createPublishBlgTrx(blogDao, bbTestOpt, trxCounters); 
		case UPDATE_BLOG:
			return createUpdateBlgTrx(blogDao, bbTestOpt, trxCounters); 
		default:
			throw new IllegalArgumentException("Unknown transaction type!");
		}
	}
	
	public BbTestTransaction createListBlgTrx(BlogDAO blogDAO, 
			BbTestOptions bbTestOpt, BlogbenchCounters trxCounters) {
		return new BbTestTrxListBlg(blogDAO, bbTestOpt, trxCounters);
	}

	public BbTestTransaction createShowBlgTrx(BlogDAO blogDao, 
			BbTestOptions bbTestOpt, BlogbenchCounters trxCounters) {
		return new BbTestTrxShowBlg(blogDao, bbTestOpt, trxCounters);
	}

	public BbTestTransaction createUpdateAcsTrx(BlogDAO blogDao, 
			BbTestOptions bbTestOpt, BlogbenchCounters trxCounters) {
		return new BbTestTrxUpdateAcs(blogDao, bbTestOpt, trxCounters);
	}

	public BbTestTransaction createUpdateCmtTrx(BlogDAO blogDao, 
			BbTestOptions bbTestOpt, BlogbenchCounters trxCounters) {
		return new BbTestTrxUpdateCmt(blogDao, bbTestOpt, trxCounters);
	}

	public BbTestTransaction createShowSiblingsTrx(BlogDAO blogDao, 
			BbTestOptions bbTestOpt, BlogbenchCounters trxCounters) {
		return new BbTestTrxShowSiblings(blogDao, bbTestOpt, trxCounters);
	}

	public BbTestTransaction createPublishBlgTrx(BlogDAO blogDao, 
			BbTestOptions bbTestOpt, BlogbenchCounters trxCounters) {
		return new BbTestTrxPublishBlg(blogDao, bbTestOpt, trxCounters);
	}

	public BbTestTransaction createUpdateBlgTrx(BlogDAO blogDao, 
			BbTestOptions bbTestOpt, BlogbenchCounters trxCounters) {
		return new BbTestTrxUpdateBlg(blogDao, bbTestOpt, trxCounters);
	}
}
