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
package com.netease.webbench.blogbench;

import com.netease.util.Pair;
import com.netease.webbench.blogbench.misc.BbTestOptParser;
import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.common.DbOptions;
import com.netease.webbench.common.DbOptParser;

/**
 * blogbench main class
 * @author LI WEIZHAO
 */

public class Main {
	/* database optioins */
	private static DbOptions dbOpt;
	/* blogbench test opitons */
	private static BbTestOptions bbTestOpt;
	/* arguments can't be parsed */
	private static String[] unparseArgs = null;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub		
		try {
			/* parse command line options */
			parseArgs(args);

			/* get blogbench test instance */
			BlogbenchTest bbTest = BlogbenchTest.getInstance();
			
			/* initialise */
			bbTest.init(dbOpt, bbTestOpt);

			/* begin test */
			bbTest.runTest();
			
		} catch (Exception e) {
			System.out.println("\nFatal error! Please see error message for more detail!");
			e.printStackTrace();
		}
	}
	

	/**
	 * parse database options from command line arguments
	 * @param args
	 * @return
	 */
	private static String[] parseDbOption(String[] args) throws IllegalArgumentException {
		Pair<DbOptions, String[]> dbOptPair = null;
		dbOptPair = DbOptParser.parse(args);
		if (dbOptPair != null) {
			dbOpt = dbOptPair.getFirst();
			return dbOptPair.getSecond();
		} else 
			return null;
	}
	
	/**
	 * parse blogbench test options from command line arguments
	 * @param args
	 * @return
	 */
	private static String[] parseCommon(String[] args) throws IllegalArgumentException {
		Pair<BbTestOptions, String[]> commonOptPair = null;
		commonOptPair = BbTestOptParser.parse(args);
		if (commonOptPair != null) {
			bbTestOpt = commonOptPair.getFirst();
			return commonOptPair.getSecond();
		} else
			return null;
	}
	
	/**
	 * parse command line arguments
	 * @param args
	 * @return
	 */
	private static void parseArgs(String[] args) throws IllegalArgumentException {
		unparseArgs = parseDbOption(args);
		
		if(unparseArgs == null) {
			showHelp();
			throw new IllegalArgumentException("Lack of common command line options!");
		}
		
		unparseArgs = parseCommon(unparseArgs);
		
		if (unparseArgs != null && unparseArgs.length != 0) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < unparseArgs.length; i++) {
				if (i > 0 )
					sb.append(", ");
				sb.append(unparseArgs[i]);
			}
			throw new IllegalArgumentException("Can't parsed arguments:" + sb.toString());
		}
		if (bbTestOpt.getOperType() == null ) {
			showHelp();
			throw new IllegalArgumentException("No valid blogbench action type specified!");
		}

	}
	
	/**
	 * show help information
	 *
	 */
	public static void showHelp() {
		System.out.println("blogbench V0.1");
		System.out.println("Uses: \n\tjava com.netease.webbench.blogbench.Main OPTIONS ACTION");
		System.out.println("");
		System.out.println("OPTIONS:");	
		DbOptParser.showDbOptionHelp();		
		BbTestOptParser.showCommonOptHelp();
	}
}
