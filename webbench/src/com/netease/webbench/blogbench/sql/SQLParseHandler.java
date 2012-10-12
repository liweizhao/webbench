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

import java.util.Hashtable;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.netease.webbench.common.XMLParseHandler;

/**
 * SQL XML file parse handler
 * @author LI WEIZHAO
 *
 */
public class SQLParseHandler implements XMLParseHandler {
	private String fileName; 
	private Map<String, Map<SQLStatementMeta, SQLStatement>> stmtMaps;
	
	public SQLParseHandler(String fileName) {
		this.fileName = fileName;
		stmtMaps = new Hashtable<String, Map<SQLStatementMeta, SQLStatement>>();
	}
	
	private SQLStatement parseDbSQL(Node n) {
		Element e = (Element)n;
		SQLStatement ss = new SQLStatement(e.getAttribute("name"),
				Boolean.parseBoolean(e.getAttribute("useMemcached")),
				Boolean.parseBoolean(e.getAttribute("twoTables")),
				e.getTextContent().trim()
		);
		return ss;
	}
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.misc.XMLParseHandler#parserXml()
	 */
	public void parserXml() throws Exception {
		// TODO Auto-generated method stub
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document document = db.parse(fileName);
			
			NodeList configurations = document.getElementsByTagName("configurations");
			for (int i = 0; i < configurations.getLength(); i++) {
				NodeList databaseConfig = configurations.item(i).getChildNodes();
				for (int j = 0; j < databaseConfig.getLength(); j++) {
					Node n = databaseConfig.item(j);
					switch ( n.getNodeType()) {		
					case Node.ELEMENT_NODE:
						Element ele = (Element)n;
						String dbType = ele.getAttribute("id");
						Map<SQLStatementMeta, SQLStatement> map = 
								new Hashtable<SQLStatementMeta, SQLStatement>();
						NodeList sqls = ele.getChildNodes();
						for (int k = 0; k < sqls.getLength(); k++) {
							Node sqlNode = sqls.item(k);
							if (Node.ELEMENT_NODE == sqlNode.getNodeType()) {
								SQLStatement stmt = parseDbSQL(sqlNode);						
								map.put(stmt.getStatementMeta(), stmt);
							}
						}
						stmtMaps.put(dbType, map);
						break;
					default:
						break;
					}
				}
			}
	}
	
	public Map<String, Map<SQLStatementMeta, SQLStatement>> getParseResult() {
		return stmtMaps;
	}
}
