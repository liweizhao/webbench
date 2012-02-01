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
package com.netease.webbench.blogbench.sql;

/**
 * meta information of SQL statement
 * @author LI WEIZHAO
 *
 */
public class SQLStatementMeta {
	private String name = "";
	private boolean useMemcached = false;
	private boolean useTwoTables = false;
	
	public SQLStatementMeta(String name, boolean useMemcached, boolean useTwoTables) {
		this.name = name;
		this.useMemcached = useMemcached;
		this.useTwoTables = useTwoTables;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isUseMemcached() {
		return useMemcached;
	}
	public void setUseMemcached(boolean useMemcached) {
		this.useMemcached = useMemcached;
	}
	public boolean isUseTwoTables() {
		return useTwoTables;
	}
	public void setUseTwoTables(boolean useTwoTables) {
		this.useTwoTables = useTwoTables;
	}
	public int hashCode() {
		 return (name + new Boolean(useMemcached).toString() + new Boolean(useTwoTables).toString()).hashCode();
	}
	
	public boolean equals(Object o) {
		SQLStatementMeta m = (SQLStatementMeta)o;
		if (!name.equals(m.getName()))
			return false;
		else if (useMemcached != m.isUseMemcached())
			return false;
		else if (useTwoTables != m.isUseTwoTables())
			return false;
		return true;
	}
}
