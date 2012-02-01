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
package com.netease.webbench.visual;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.statistics.SimpleHistogramBin;
import org.jfree.data.statistics.SimpleHistogramDataset;
import org.jfree.data.xy.IntervalXYDataset;

import com.netease.stat.*;

/** 
 *  Histogram Chart
 *  @author LI WEIZHAO
 */
public class HistoGramChart {

	private JFreeChart localJFreeChart;//jfreechart object
	private String title;//chart title
	private String xLabel;//label of x axis
	private String yLabel;//label of y axis
	private String xUnit;//unit of x axis

	public JFreeChart getLocalJFreeChart() {
		return localJFreeChart;
	}

	public HistoGramChart(Histogram histogram, String xLabel, String yLabel) {
		this.title = histogram.getName();
		this.xUnit = histogram.getUnitX();
		this.xLabel = xLabel;
		this.yLabel = yLabel;
		
		SimpleHistogramDataset localSimpleHistogramDataset = new SimpleHistogramDataset(
				histogram.getName());
		
		Bucket[] bucketArr = histogram.getBuckets();
		double lastHigh = -1;
		double range = bucketArr[0].getHigh() - bucketArr[0].getLow();
		for (int i = 0; i < bucketArr.length; i++) {
			double low = (double)bucketArr[i].getLow();
			double high = (double)bucketArr[i].getHigh();
			
			if (low <= lastHigh) {
				low = lastHigh;
			}
			if (high <= low) {
				high = low + range;
			}			
			SimpleHistogramBin localSimpleHistogramBin = new SimpleHistogramBin(
					 low, high, true, false);
			lastHigh = high;
			localSimpleHistogramBin.setItemCount((int)bucketArr[i].getArea());
			
			localSimpleHistogramDataset.addBin(localSimpleHistogramBin);
		}		
		createChart(localSimpleHistogramDataset);
	}

	private void createChart(IntervalXYDataset paramIntervalXYDataset) {
		localJFreeChart = ChartFactory.createHistogram(title, xLabel + "(" + xUnit + ")",
				yLabel, paramIntervalXYDataset, PlotOrientation.VERTICAL, true,
				true, false);
		XYPlot localXYPlot = (XYPlot) localJFreeChart.getPlot();
		localXYPlot.setForegroundAlpha(0.85000002384185791F);
		localXYPlot.setBackgroundPaint(Color.WHITE);
		localXYPlot.setRangeGridlinePaint(Color.gray);
		localXYPlot.setDomainGridlinesVisible(false);
		
		/* setting y axis */
		NumberAxis numAxis = (NumberAxis) localXYPlot.getRangeAxis();
		numAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());  
		numAxis.setAutoRange(true);

		XYBarRenderer localXYBarRenderer = (XYBarRenderer) localXYPlot
				.getRenderer();
		localXYBarRenderer.setDrawBarOutline(false);
		localXYBarRenderer.setBarPainter(new StandardXYBarPainter());
		localXYBarRenderer.setShadowVisible(false);
	}

	/**
	 *  save chart to file
	 * 
	 * @param fileName           file name
	 * @param imageFormat   image format, PNG & JPEG
	 */
	public void savaToFile(String fileName, String imageFormat)
			throws IOException {
		File testFile = new File(fileName);
		if (imageFormat.equals("PNG") || imageFormat.equals("png")) {
			ChartUtilities.saveChartAsPNG(testFile, localJFreeChart, 900, 600);
		} else if (imageFormat.equals("JPEG") || imageFormat.equals("PNG")) {
			ChartUtilities.saveChartAsJPEG(testFile, localJFreeChart, 900, 600);
		} else {
			System.err.println("Invalid graph format!");
		}
	}
}
