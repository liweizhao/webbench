package com.netease.webbench.blogbench;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class BlogbenchTestSuite extends TestCase {

	public BlogbenchTestSuite(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public static Test suite() { 
		TestSuite suite= new TestSuite(); 
		
		//suite.addTest(new NtseSpecialTest("testSetNtseIndexBuildAlgorithm")); 
		//suite.addTest(new NtseSpecialTest("testDisableMms")); 
		//suite.addTest(new NtseSpecialTest("testEnableMms"));
		
		//suite.addTest(new BbTestRunThreadTest("testRun")); 
		
		//suite.addTest(new BlgBnchTestTest("testGetInstance")); 
		//suite.addTest(new BlgBnchTestTest("testRunTest")); 
		//suite.addTest(new BlgBnchTestTest("testInitMemcached")); 
		
		//suite.addTest(new BbTestLoadThreadTest("testCreateTable"));
		//suite.addTest(new BbTestLoadThreadTest("testCreatPrimaryKey"));
		//suite.addTest(new BbTestLoadThreadTest("testCreateSecondaryIndex"));
		//suite.addTest(new BbTestLoadThreadTest("testCreateInsertThrdGrp"));
		//suite.addTest(new BbTestLoadThreadTest("testExcNonStandartStmt"));
		
		return suite; 
	} 

}
