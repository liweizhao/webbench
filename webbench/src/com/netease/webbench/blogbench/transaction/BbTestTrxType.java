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

/**
 * 
 * @author LI WEIZHAO
 *
 */
public enum BbTestTrxType {
	LIST_BLGS ("list-blogs"),
	SHOW_BLG ("show-blog"),
	UPDATE_ACS ("update-access"),
	UPDATE_CMT ("update-comment"),
	SHOW_SIBS ("show-siblings"),
	PUBLISH_BLG ("publish-blog"),
	UPDATE_BLOG ("update-blog");
	
	public static final int TRX_TYPE_NUM = 7;
	
	protected String name;
	
	/**
	 * constructor
	 * @param name
	 */
	private BbTestTrxType(String name) {
		this.name = name;
	}
		
	/**
	 * get transaction index
	 * @param trxType
	 * @return
	 */
	public static int getTrxIndex(BbTestTrxType trxType) {
		return trxType.compareTo(LIST_BLGS);
	}
	/**
	 * get transaction name
	 * @param trxType transaction type
	 * @return
	 */
	public static String getTrxName(BbTestTrxType trxType) {
		return trxType.name;
	}
	/**
	 * get transaction name
	 * @param index transaction index
	 * @return
	 */
	public static String getTrxName(int index) throws IllegalArgumentException {
		if (index < 0 || index >= TRX_TYPE_NUM)
			throw new IllegalArgumentException("Index is out of range [0 ," + (TRX_TYPE_NUM - 1) + "].");
		BbTestTrxType[] vls = BbTestTrxType.values();
		return vls[index].name;
	}
}
