package com.netease.webbench.blogbench.misc;

import com.netease.webbench.blogbench.operation.*;
import com.netease.webbench.common.DbOptions;

public class UnitTestHelper {

	public static DbOptions createDflDbOpt() {
		DbOptions dbOpt = new DbOptions();		
		dbOpt.setHost("127.0.0.1");
		dbOpt.setPort(5330);
		dbOpt.setUser("root");
		dbOpt.setPassword("");
		dbOpt.setJdbcUrl("jdbc:mysql://" + dbOpt.getHost() + ":" + dbOpt.getPort() + "/" + 
				dbOpt.getDatabase() + "?useUnicode=true&characterEncoding=UTF8");
		dbOpt.setDriverName("com.mysql.jdbc.Driver");
		
		return dbOpt;
	}
	
	public static BbTestOptions createDflBbTestOpt() {
		BbTestOptions bbTestOpt = new BbTestOptions();
		bbTestOpt.setTbSize(10000);
		bbTestOpt.setTbEngine("ntse");
		bbTestOpt.setDeferIndex(true);	
		return bbTestOpt;
	}
	
	public static void createTestTable(DbOptions dbOpt, BbTestOptions bbTestOpt) throws Exception {	
		BlogbenchOperationType old = bbTestOpt.getOperType();
		ParameterGenerator paraGen = new ParameterGenerator();	
		bbTestOpt.setOperType(BlogbenchOperationType.LOAD);
		paraGen.init(bbTestOpt, dbOpt);
		
		BlogbenchLoadOperation loadOper = new BlogbenchLoadOperation(dbOpt, bbTestOpt);
		loadOper.executeOper();
		
		bbTestOpt.setOperType(old);
	}
}
