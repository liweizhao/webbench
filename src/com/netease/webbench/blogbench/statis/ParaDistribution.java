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
package com.netease.webbench.blogbench.statis;

import java.util.ArrayList;
import java.util.List;

import com.netease.stat.Distribution;
import com.netease.stat.Histogram;
import com.netease.stat.HistogramFactory;
import com.netease.stat.HistogramType;
import com.netease.stat.SourceParameter;
import com.netease.webbench.statis.CreateChartHandler;
import com.netease.webbench.visual.HistoGramChart;

/**
 * 
 * @author LI WEIZHAO
 *
 */
public class ParaDistribution implements CreateChartHandler {
	public static final int DFL_HISTOGRAM_BUCKETS = 1024;
	
	private Distribution contentLengthDis;
	private Distribution userIDDis;
	private Distribution blogIDDis;
	
	public ParaDistribution() {
		contentLengthDis = new Distribution("blog content length distribution", "bytes", 1, false);
		userIDDis = new Distribution("User ID distribution", "user id", 1, false);
		blogIDDis = new Distribution("Blog ID distribution", "blog id", 1, false);
	}
	
	public Distribution getContentLengthDis() {
		return contentLengthDis;
	}
	public void setContentLengthDis(Distribution contentLengthDis) {
		this.contentLengthDis = contentLengthDis;
	}
	public Distribution getUserIDDis() {
		return userIDDis;
	}
	public void setUserIDDis(Distribution userIDDis) {
		this.userIDDis = userIDDis;
	}
	public Distribution getBlogIDDis() {
		return blogIDDis;
	}
	public void setBlogIDDis(Distribution blogIDDis) {
		this.blogIDDis = blogIDDis;
	}
	
	public List<String> createChartFiles(String reportPath) throws Exception {
		List<String> list = new ArrayList<String>();
		String fileName = doCreateDisChart(blogIDDis, reportPath);
		if (fileName != null)
			list.add(fileName);
		fileName =  doCreateDisChart(userIDDis, reportPath);
		if (fileName != null)
			list.add(fileName);
		fileName = doCreateDisChart(contentLengthDis, reportPath);
		if (fileName != null)
			list.add(fileName);
		return list;
	}
	
	private String doCreateDisChart(Distribution dis, String reportDir) {
		String distributeName = dis.getName();
		int buckets = DFL_HISTOGRAM_BUCKETS;
		try {
			long elementWidth = dis.getMax(SourceParameter.VALUE)
					- dis.getMin(SourceParameter.VALUE);
			while (elementWidth < buckets) {
				buckets /= 2;
				if (buckets < 2) {
					throw new Exception("The distribution("
							+ distributeName
							+ ") can't be used to create chart!");
				}
			}

			HistogramFactory hisFac = HistogramFactory.getInstance();
			Histogram his = hisFac.build(dis, HistogramType.EQUI_WIDTH, SourceParameter.REPEAT, buckets);

			HistoGramChart hisChart = new HistoGramChart(his, "", "");
			String chartFileName = reportDir + distributeName + ".png";
			hisChart.savaToFile(chartFileName, "PNG");

			return chartFileName;
		} catch (Exception e) {
			System.err.println("[WARNING] " + e.getMessage());
			return null;
		}
	}
}
