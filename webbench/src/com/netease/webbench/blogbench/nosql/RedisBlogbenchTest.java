package com.netease.webbench.blogbench.nosql;

import java.io.File;

import com.netease.webbench.blogbench.BlogbenchTest;
import com.netease.webbench.blogbench.dao.DataLoader;
import com.netease.webbench.blogbench.misc.ParameterGenerator;
import com.netease.webbench.blogbench.operation.BlogbenchLoadOperation;
import com.netease.webbench.blogbench.operation.BlogbenchOperation;
import com.netease.webbench.blogbench.operation.BlogbenchRunOperation;

public class RedisBlogbenchTest extends BlogbenchTest {
	public RedisBlogbenchTest() {
		super("Redis-Test");
	}
	
	@Override
	public void run() throws Exception {
		/* make directory of test report */
		makeReportDir();		
		
		String operTypeStr = bbTestOpt.getOperType();
		ParameterGenerator paraGen = new ParameterGenerator();
		paraGen.init(bbTestOpt, dbOpt);
		
		BlogbenchOperation oper = null;
		if ("load".compareToIgnoreCase(operTypeStr) == 0) {
			DataLoader dataLoader = new RedisDataLoader(dbOpt, bbTestOpt, paraGen);
			oper = new BlogbenchLoadOperation(dbOpt, bbTestOpt, dataLoader);
		} else if ("run".compareToIgnoreCase(operTypeStr) == 0) {
			oper = new BlogbenchRunOperation(dbOpt, bbTestOpt, paraGen);
		} else {
			throw new IllegalArgumentException("Wrong blogbench operation!");
		}
		
		oper.execute();
	}
	
	/**
	 * create test result directory
	 * @throws Exception create report directory failed
	 */
	protected void makeReportDir() throws Exception {
		String reportDirPath = bbTestOpt.getReportDir();
		File reportDir = new File(reportDirPath);
		if (reportDir.exists() && !reportDir.isDirectory()) {
			throw new Exception("A file of the same name with the specified report " +
					"directory exists, please specify another report directory name!");
		} else if (!reportDir.exists()) {
			System.out.print("Report directory doesn't exist, now create it...");
			if (reportDir.mkdirs()) {
				System.out.println("done.");
			} else {
				throw new Exception("Failed to make directories:" + reportDir.getName());
			}
		}
		String blogbenchPath = bbTestOpt.getReportDir() + "/blogbench-tmp";
		File blogbenchDir = new File(blogbenchPath);
		if (!blogbenchDir.exists() && !blogbenchDir.mkdir()) {
			throw new Exception("Faild to create blogbench test results saved diretory!");
		}
	}
}
