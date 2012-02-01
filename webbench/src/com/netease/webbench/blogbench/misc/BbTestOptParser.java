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
package com.netease.webbench.blogbench.misc;

import java.util.LinkedList;
import java.util.List;
import com.netease.webbench.blogbench.operation.BlogbenchOperationType;
import com.netease.util.Pair;

/**
 * blogbench test options parser
 * @author LI WEIZHAO
 */
public class BbTestOptParser {
	public static Pair<BbTestOptions, String[]> parse(String[] args) {
		int nextArg = 0;
		List<String> unparsedOptionList = new LinkedList<String>();
		BbTestOptions cmnOpt = new BbTestOptions();	
		boolean operTypeSpecified = false;
		
		while (nextArg < args.length) {
			if (args[nextArg].equals("--table-name")) {
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No table name specified");
				cmnOpt.setTbName(args[++nextArg]);
			} else if (args[nextArg].equals("--table-size")) {
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No table size specified");
				cmnOpt.setTbSize(Long.parseLong(args[++nextArg]));
			} else if (args[nextArg].equals("--table-engine")) {
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No table engine specified");
				cmnOpt.setTbEngine(args[++nextArg]);			
			}  else if (args[nextArg].equals("--table-comment")) {
				throw new IllegalArgumentException("The argument --table-comment is no longer used, " +
						"please use --ntse-create-table-args instead!");
			} else if (args[nextArg].equals("--ntse-create-table-args")) {
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No ntse create table arguments specified");
				cmnOpt.setNtseCreateTblArgs(args[++nextArg]);
			} else if (args[nextArg].equals("--list-blogs")) {
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No percent of list-blogs specified");
				cmnOpt.setPctListBlg(Integer.parseInt(args[++nextArg]));				
			} else if (args[nextArg].equals("--show-blog")) {
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No percent of show-blog specified");
				cmnOpt.setPctShowBlg(Integer.parseInt(args[++nextArg]));				
			} else if (args[nextArg].equals("--update-access")) {
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No percent of update-access specified");
				cmnOpt.setPctUpdateAccess(Integer.parseInt(args[++nextArg]));				
			} else if (args[nextArg].equals("--update-comment")) {
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No percent of update-comment specified");
				cmnOpt.setPctUpdateComment(Integer.parseInt(args[++nextArg]));					
			} else if (args[nextArg].equals("--show-siblings")) {
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No percent of show-siblings specified");
				cmnOpt.setPctShowSibs(Integer.parseInt(args[++nextArg]));					
			} else if (args[nextArg].equals("--publish-blog")) {
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No percent of publish-blog specified");
				cmnOpt.setPctPublishBlg(Integer.parseInt(args[++nextArg]));				
			} else if (args[nextArg].equals("--update-blog")) {
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No percent of update-blog specified");
				cmnOpt.setPctUpdateBlg(Integer.parseInt(args[++nextArg]));				
			} else if (args[nextArg].equals("--blog-zipf-pct")) {
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No percent of blog zipf distribution specified");
				cmnOpt.setBlgZipfPct(Integer.parseInt(args[++nextArg]));				
			} else if (args[nextArg].equals("--blog-zipf-res")) {
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No request of blog zipf distribution specified");
				cmnOpt.setBlgZipfRes(Integer.parseInt(args[++nextArg]));				
			} else if (args[nextArg].equals("--blog-zipf-part")) {
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No partition of blog zipf distribution specified");
				cmnOpt.setBlgZipfPart(Integer.parseInt(args[++nextArg]));				
			} else if (args[nextArg].equals("--user-zipf-pct")) {
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No percent of user zipf distribution specified");
				cmnOpt.setUserZipfPct(Integer.parseInt(args[++nextArg]));		
			} else if (args[nextArg].equals("--user-zipf-res")) {
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No request of user zipf distribution specified");
				cmnOpt.setUserZipfRes(Integer.parseInt(args[++nextArg]));			
			} else if (args[nextArg].equals("--user-zipf-part")) {
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No partition of user zipf distribution specified");
				cmnOpt.setUserZipfPart(Integer.parseInt(args[++nextArg]));			
			}
			else if (args[nextArg].equals("--min-title-size")) {
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No min title size specified");
				cmnOpt.setMinTtlSize(Integer.parseInt(args[++nextArg]));					
			} else if (args[nextArg].equals("--max-title-size")) {
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No max title size specified");
				cmnOpt.setMaxTtlSize(Integer.parseInt(args[++nextArg]));			
			} else if (args[nextArg].equals("--min-abs-size")) {
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No min abstract size specified");
				cmnOpt.setMinAbsSize(Integer.parseInt(args[++nextArg]));		
			} else if (args[nextArg].equals("--max-abs-size")) {
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No max abstract size specified");
				cmnOpt.setMaxAbsSize(Integer.parseInt(args[++nextArg]));	
			} else if (args[nextArg].equals("--min-cnt-size")) {
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No min content size specified");
				cmnOpt.setMinCntSize(Integer.parseInt(args[++nextArg]));		
			} else if (args[nextArg].equals("--max-cnt-size")) {
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No max content size specified");
				cmnOpt.setMaxCntSize(Integer.parseInt(args[++nextArg]));
			} else if (args[nextArg].equals("--avg-cnt-size")) {
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No average content size specified");
				cmnOpt.setAvgCntSize(Integer.parseInt(args[++nextArg]));			
			} else if (args[nextArg].equals("--threads")) {
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No num of threads specified");
				cmnOpt.setThreads(Integer.parseInt(args[++nextArg]));			
			} else if (args[nextArg].equals("--max-tran")) {
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No max num of transactions specified");
				cmnOpt.setMaxTran(Integer.parseInt(args[++nextArg]));		
			} else if (args[nextArg].equals("--max-time")) {
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No max time of test specified");
				cmnOpt.setMaxTime(Integer.parseInt(args[++nextArg]));						
			} else if (args[nextArg].equals("--defer-index")) {
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No whether defer creating index specified");
				cmnOpt.setDeferIndex(parseBoolean("--defer-index", args[++nextArg]));
			} else if (args[nextArg].equals("--report-dir")) {
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No report diretory specified");
				String temp = args[++nextArg];
				if (temp.charAt(temp.length() - 1) != '/') {
					temp += '/';
				}
				cmnOpt.setReportDir(temp);
			} else if (args[nextArg].equals("--collect-sysstat")) {
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No whether collect sysstat specified");
				cmnOpt.setCollectSysstat(parseBoolean("--collect-sysstat", args[++nextArg]));
			} else if (args[nextArg].equals("--large-blog")) {
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No specify whether generate large blog");
				cmnOpt.setExtraLargeBlog(parseBoolean("--large-blog", args[++nextArg]));
			} else if (args[nextArg].equals("--create-table")) {
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No specify whether create table");
				cmnOpt.setCreateTable(parseBoolean("--create-table", args[++nextArg]));
			} else if (args[nextArg].equals("--parallel-dml")) {
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No specify whether parallel dml");
				cmnOpt.setParallelDml(parseBoolean("--parallel-dml", args[++nextArg]));
			} else if (args[nextArg].equals("--use-memcached")) {	
				System.out.println("you hava specified to use memcached!");				
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No specify whether use memcached");
				cmnOpt.setUsedMemcached(parseBoolean("--use-memcached", args[++nextArg]));
			} else if (args[nextArg].equals("--print-period")) {
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No specify whether generate large blog");
				cmnOpt.setPrintThoughputPeriod(Integer.parseInt(args[++nextArg]));	
			} else if (args[nextArg].equals("--main-memcached-host")) {
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No memcached host specified!");
				cmnOpt.setMainMemcachedHost(args[++nextArg]);	
			} else if (args[nextArg].equals("--main-memcached-port")) {
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No memcached port specified!");
				cmnOpt.setMainMemcachedPort(Integer.parseInt(args[++nextArg]));	
			} else if (args[nextArg].equals("--minor-memcached-host")) {
					if (nextArg == args.length - 1)
						throw new IllegalArgumentException("No memcached host specified!");
					cmnOpt.setMinorMemcachedHost(args[++nextArg]);	
			} else if (args[nextArg].equals("--minor-memcached-port")) {
					if (nextArg == args.length - 1)
						throw new IllegalArgumentException("No memcached port specified!");
					cmnOpt.setMinorMemcachedPort(Integer.parseInt(args[++nextArg]));	
			}  else if (args[nextArg].equals("--load-threads")) {
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No num of load threads specified");
				cmnOpt.setLoadThreads(Integer.parseInt(args[++nextArg]));
			} else if (args[nextArg].equals("--debug")) {
				cmnOpt.setDebug(parseBoolean("--debug", args[++nextArg]));	
			} else if (args[nextArg].equals("--access-update-cache-size")) {
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No access count update cache size specified!");
				cmnOpt.setAcsCountTrxCacheSize(Integer.parseInt(args[++nextArg]));
			} else if (args[nextArg].equals("--access-update-flush-interval")) { 
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No access count update flush interval specified!");
				cmnOpt.setFlushAcsCountInterval(Long.parseLong(args[++nextArg]));			
			} else if (args[nextArg].equals("--access-count-flush-threads")) { 
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No access count flush threads specified!");
				cmnOpt.setFlushAcsCountThreads(Integer.parseInt(args[++nextArg]));
			} else if (args[nextArg].equals("--use-two-tables")) {
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No access count flush threads specified!");
				cmnOpt.setUseTwoTable(parseBoolean("--use-two-tables", args[++nextArg]));	
			} else if (args[nextArg].equalsIgnoreCase("run")) {
				if (operTypeSpecified)
					throw new IllegalArgumentException("Duplicate operation specified(LOAD/RUN).");
				cmnOpt.setOperType(BlogbenchOperationType.RUN);
				operTypeSpecified = true;
			} else if (args[nextArg].equalsIgnoreCase("load")) {
				if (operTypeSpecified)
					throw new IllegalArgumentException("Duplicate operation specified(LOAD/RUN).");
				cmnOpt.setOperType(BlogbenchOperationType.LOAD);
				operTypeSpecified = true;
			} else
				unparsedOptionList.add(args[nextArg]);
			nextArg++;
		}
		
		String[] unparsedOptions = new String[unparsedOptionList.size()];
		for (int i = 0; i < unparsedOptions.length; i++)
			unparsedOptions[i] = unparsedOptionList.get(i);		
		return new Pair<BbTestOptions, String[]>(cmnOpt, unparsedOptions);
	}

	public static boolean parseBoolean(String optName, String value) throws IllegalArgumentException {
		if (value == null) {
			throw new IllegalArgumentException("Option value of " + optName + " to parse is null!");
	    }else if (value.equalsIgnoreCase("true")) {
			return true;
		} else if (value.equalsIgnoreCase("false")) {
			return false;
		} else
			throw new IllegalArgumentException("Illegal boolean value \"" + value + "\" in option \"" + optName + "\", should be true or false! ");
	}
	
	public static void showCommonOptHelp() {
		System.out.println("  --table-name: test table name [Blog];");	
		System.out.println("  -- number of records in test table [1000000], only when ACTION is 'load' this option is usefull;");
		System.out.println("  --table-engine: table engine used in test table [NTSE];");	
		System.out.println("  --table-comment: comment used when creating a test table;");	
		System.out.println("  --TRX N: TRX is the name of transaction,N is the relative pecentage, N is 0 when this transaction is skipped");
		System.out.println("\t\tavailable TRX is:llist-blogs, show-blog, update-access, update-comment, show-siblings, " +
			"publish-blog, update-blog");
		System.out.println("  --blog-zipf-pct N, --blog-zipf-res N, --blog-zipf-part N: Zipf distribution when choose a blog id" +
			"\n\t\t--blog-zipf-pct [5], --blog-zipf-res [95], --blog-zipf-part[200];");	
		System.out.println("  --user-zipf-pct N, --user-zipf-res N, --user-zipf-part N: Zipf distribution when choose a user id" +
			"\n\t\t--user-zipf-pct [5], --user-zipf-res [95], --user-zipf-part [200]; ");	
		System.out.println("  --min-title-size: minimum length of blog title [10];");	
		System.out.println("  --max-title-size: maximum length of blog title [30];");	
		System.out.println("  --min-abs-size: minimum length of blog abstract [10];");	
		System.out.println("  --max-abs-size: minimum length of blog abstract [500];");		
		System.out.println("  --min-cnt-size: minimum length of blog content [20];");	
		System.out.println("  --max-cnt-size: maxmum length of blog content [20000];");	
		System.out.println("  --avg-cnt-size: average length of blog content [2000];");	
		System.out.println("  --threads: number of threads to use [100];");	
		System.out.println("  --load-threads: number of load threads to use [8]");
		System.out.println("  --max-tran: limit for total number of transactions to excute [Long.MAX];");
		System.out.println("  --max-time: limit for total execution time in millseconds [Long.MAX];");
		System.out.println("  --defer-index: defer to create index,only when ACTION is 'load this option is usefull;");	
		System.out.println("  --report-dir: specifed the output directory of test result [./report];");
		System.out.println("  --collect-sysstat: collect system status automatically [false]");
		System.out.println("  --large-blog: generate more 1% large blogs, attention that the average blog content length may not be what " +
			"you specified! [false]");
		System.out.println("  --use-memcached: whether use memcached client [false]");
		System.out.println("  --print-period: period of printting transaction throughput in seconds [60]");
		System.out.println("  --main-memcached-host: common memcached server host [127.0.0.1]");
		System.out.println("  --main-memcached-port: common memcached server port [8609]");
		System.out.println("  --minor-memcached-host: access count memcached server host [127.0.0.1]");
		System.out.println("  --minor-memcached-port: access count memcached server port [8608]");
		System.out.println("  --debug: print debug information, used only for developers [false]");
		System.out.println("  --clean-mms: whether clean mms before run test, only useful to ntse [true]");
		System.out.println("");			
		System.out.println("ACTION:");
		System.out.println("  load: load test data");	
		System.out.println("  run: run test");
	}// end else
}
