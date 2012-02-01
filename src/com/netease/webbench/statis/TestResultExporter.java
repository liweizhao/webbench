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
package com.netease.webbench.statis;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.netease.webbench.visual.PdfFile;

/**
 * blogbench run operation result exporter
 *  @author LI WEIZHAO
 */
public class TestResultExporter {	
	private String benchmarkName;
	private String runSummary;
	private List<CreateChartHandler> createChartHandleList;
	private List<CreateTableHandler> createTableHandlerList;
	
	public TestResultExporter(String benchmarkName, List<CreateChartHandler> createChartHandleList, 
			List<CreateTableHandler> createTableHandlerList) {
		this.benchmarkName = benchmarkName;
		this.runSummary = "";
		this.createChartHandleList = createChartHandleList;
		this.createTableHandlerList = createTableHandlerList;
	}
	
	public void setRunSummary(String runSummary) {
		this.runSummary = runSummary;
	}
	
	/**
	 *  export test result to file
	 * @param reportDir
	 * @throws Exception
	 */
	public void export(String reportDir) throws Exception {
		System.out.println("Create report files...");
		String reportPath = reportDir + "/" + benchmarkName + "-tmp/";
		File blogbenchTmpDir = new File(reportPath);
		if (!blogbenchTmpDir.exists() && !blogbenchTmpDir.mkdir()) {
			throw new Exception("Faild to create tmp diretory: " + blogbenchTmpDir + "!");
		}
		
		List<String> imgFilePathList = createChartFiles(reportPath);
		createPDFFile(reportDir, imgFilePathList);
		System.out.println("done.");
		
		System.out.print("Delete temporary files and directories...");
		/* delete temporary directory */
		File tmpChartFileDir = new File(reportPath);
		if (tmpChartFileDir != null && tmpChartFileDir.exists()
				&& tmpChartFileDir.isDirectory()) {
			for (File file : tmpChartFileDir.listFiles()) {
				if (file.isFile())
					file.delete();
			}
			tmpChartFileDir.delete();
		}
		System.out.println("done.");
	}
	
	/**
	 * 
	 * @param reportDir
	 * @return
	 * @throws Exception
	 */
	private List<String> createChartFiles(String reportDir) throws Exception {
		List<String> filePathList = new ArrayList<String>(20);
		if (null != createChartHandleList) {
			for (CreateChartHandler handler : createChartHandleList) {
				List<String> tmpList = handler.createChartFiles(reportDir);
				if (tmpList != null)
					filePathList.addAll(tmpList);
			}
		}
		return filePathList;
	}
	
	/**
	 * create PDF report file
	 * @param reportDir
	 * @param imgFilePathList
	 * @throws Exception
	 */
	private void createPDFFile(String reportDir, List<String> imgFilePathList) 
	throws Exception {
		/* create result charts and pdf file */	
		PdfFile pdfFile = new PdfFile(reportDir + "/" + benchmarkName + "-report.pdf");
		pdfFile.beginAddElement();
		
		/* add pdf file cover */
		java.net.InetAddress localMachine = java.net.InetAddress.getLocalHost();
		
		pdfFile.addCover(benchmarkName + " test report", localMachine.getHostName(), 50, 30);	
		pdfFile.addTextPage(benchmarkName + " test summary", runSummary, 20, 5);
		
		if (null != createTableHandlerList) {
			for (CreateTableHandler handler : createTableHandlerList) {
				List<PdfTable> list = handler.createTables();
				for (PdfTable table : list) {
					pdfFile.addTablePage(table);
				}
			}
		}
		if (null != imgFilePathList) {
			pdfFile.addAllImageFile(imgFilePathList);
		}
		pdfFile.endAddElement();
	}
}
