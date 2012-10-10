package com.netease.webbench.blogbench.sql;

public class SQLConfigureFactory {
	private static SQLConfigure instance = null;
	
	public static void init(String dbType) throws Exception {
		instance = new SQLXMLConfigure(dbType);
	}
	
	public static SQLConfigure getSQLConfigure() throws Exception {	
		if (instance == null) 
			throw new Exception("SQL configuration has not been not initiated!");
		return instance;
	}
}
