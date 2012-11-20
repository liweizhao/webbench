package com.netease.webbench.blogbench;

import java.io.File;

import com.netease.util.Pair;
import com.netease.webbench.WebbenchTest;
import com.netease.webbench.blogbench.misc.BbTestOptParser;
import com.netease.webbench.blogbench.misc.BbTestOptions;
import com.netease.webbench.blogbench.operation.BlogbenchOperType;
import com.netease.webbench.blogbench.operation.BlogbenchOperation;
import com.netease.webbench.common.DbOptParser;
import com.netease.webbench.common.DbOptions;

public abstract class BlogbenchTest implements WebbenchTest {
	protected BbTestOptions bbTestOpt;
	protected DbOptions dbOpt;
	
	public BlogbenchTest() {
	}
	
	@Override
	public void setUp(String[] args) throws Exception {
		System.out.println("Blogbench test is initilizing...");
		parseArgs(args);
	}
	
	@Override
	public void tearDown() {
	}
	
	@Override
	public void run() throws Exception {		
		/* make directory of test report */
		makeReportDir();
		
		BlogbenchOperType operType = BlogbenchOperType.LOAD;
		String operTypeStr = bbTestOpt.getOperType();
		if ("load".compareToIgnoreCase(operTypeStr) == 0)
			operType = BlogbenchOperType.LOAD;
		else if ("run".compareToIgnoreCase(operTypeStr) == 0)
			operType = BlogbenchOperType.RUN;
		else
			throw new IllegalArgumentException("Illegal operation type: " + operTypeStr);
		
		BlogbenchOperation oper = createOper(operType);
		oper.execute();
	}
	
	public abstract BlogbenchOperation createOper(BlogbenchOperType type) throws Exception;
	
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
	
	/**
	 * parse database options from command line arguments
	 * @param args
	 * @return
	 */
	protected String[] parseDbOption(String[] args) throws IllegalArgumentException {
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
	protected String[] parseCommon(String[] args) throws IllegalArgumentException {
		Pair<BbTestOptions, String[]> commonOptPair = null;
		commonOptPair = BbTestOptParser.parse(args);
		if (commonOptPair != null) {
			bbTestOpt = commonOptPair.getFirst();
			return commonOptPair.getSecond();
		} else
			return null;
	}

	protected void parseArgs(String[] args) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		
		try {
			String[] unparseArgs = parseDbOption(args);
			
			if(unparseArgs == null) {
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
				throw new IllegalArgumentException("No valid blogbench action type specified!");
			}
		} catch (IllegalArgumentException e) {
			showHelp();
			throw e;
		}
	}
	
	public void showHelp() {
		// TODO Auto-generated method stub
		System.out.println("blogbench V0.3");
		System.out.println("Uses: \n\tjava com.netease.webbench.blogbench.Main " +
				"OPTIONS ACTION");
		System.out.println("");
		System.out.println("OPTIONS:");
		DbOptParser.showDbOptionHelp();
		BbTestOptParser.showCommonOptHelp();
	}
}
