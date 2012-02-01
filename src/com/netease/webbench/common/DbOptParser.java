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
package com.netease.webbench.common;

import java.util.LinkedList;
import java.util.List;

import com.netease.util.Pair;

/**
 * Database options parser
 * @author LI WEIZHAO
 */
public class DbOptParser {
	public static Pair<DbOptions, String[]> parse(String[] args) {
		int nextArg = 0;
		List<String> unparsedOptionList = new LinkedList<String>();
		DbOptions dbOption = new DbOptions();
		
		while (nextArg < args.length) {
			if (args[nextArg].equals("--user") || args[nextArg].equals("-u")) {
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No user specified");
				dbOption.setUser(args[++nextArg]);
			} else if (args[nextArg].equals("--password") || args[nextArg].equals("-p")) {
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No password specified");
				dbOption.setPassword(args[++nextArg]);
			} else if (args[nextArg].equals("--host") || args[nextArg].equals("-h")) {
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No host specified");
				dbOption.setHost(args[++nextArg]);
			} else if (args[nextArg].equals("--port") || args[nextArg].equals("-P")) {
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No port specified");
				dbOption.setPort(Integer.parseInt(args[++nextArg]));
			} else if (args[nextArg].equals("--database") || args[nextArg].equals("-D")) {
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No database specified");
				dbOption.setDatabase(args[++nextArg]);
			} else if (args[nextArg].equals("--database-type") || args[nextArg].equals("-T")) {
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No database type specified");
				dbOption.setDbType(args[++nextArg]);
			} else if (args[nextArg].equals("--driver-name")) {
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No database jdbc driver name specified");
				dbOption.setDriverName(args[++nextArg]);		
			} else if (args[nextArg].equals("--jdbc-url")) {
				if (nextArg == args.length - 1)
					throw new IllegalArgumentException("No database jdbc url specified");
				dbOption.setJdbcUrl(args[++nextArg]);
			} else
				unparsedOptionList.add(args[nextArg]);
			nextArg++;
		}
		
		String[] unparsedOptions = new String[unparsedOptionList.size()];
		for (int i = 0; i < unparsedOptions.length; i++)
			unparsedOptions[i] = unparsedOptionList.get(i);
		return new Pair<DbOptions, String[]>(dbOption, unparsedOptions);
	}
	
	/**
	 * show help message
	 */
	public static void showDbOptionHelp() {
		System.out.println("  -u, --user USER\tConnect to database as USER");
		System.out.println("  -p, --password PASS\tUse PASS for database user's password");
		System.out.println("  -h, --host ADDRESS\tConnect to database at ADDRESS");
		System.out.println("  -P, --port PORT\tConnect to database at PORT");
		System.out.println("  -D, --database DB\tUse database DB");
		System.out.println("  -T, --database-type DATABASETYPE\tDatabase type");
		System.out.println("  --driver-name DRIVERNAME\tDatabase JDBC driver name");
		System.out.println("  --jdbc-url JDBCURL\tDatabase JDBC url");
	}
}
