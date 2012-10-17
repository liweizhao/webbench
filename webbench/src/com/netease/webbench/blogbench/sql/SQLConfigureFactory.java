package com.netease.webbench.blogbench.sql;

public class SQLConfigureFactory {
	private static SQLConfigure instance = null;
	
	public static SQLConfigure getSQLConfigure(String dbType) throws Exception {	
		synchronized(SQLConfigureFactory.class) {
			if (instance == null) {
				instance = new SQLXMLConfigure(dbType);
			}
		}
		return instance;
	}
}
