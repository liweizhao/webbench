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

import java.util.Date;

//import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;
import org.jfree.data.time.Second;

/**
 * Time sequence chart
 * @author LI WEIZHAO
 */
public class TimeLineChart {
	/* colours of curve can be chosen  */
	private static final Color[] COLOR = {Color.RED, Color.BLUE, Color.GREEN, Color.CYAN, Color.ORANGE, 
		Color.MAGENTA, Color.BLACK, Color.PINK, Color.YELLOW,  Color.DARK_GRAY, Color.LIGHT_GRAY};
	
	private static final int DEFAULT_MAX_LINE_COUNT = 7;
	
	private TimeSeries[] tsArr;
	
	/* main title of chart */
	private String title;
	/* JFreeChart object */
	private JFreeChart chart;
	/* number of curves in chart */
	private int lineCount;
	/* max number of curves can be created in this chart */
	private int lineMaxCount;
	
	private long startTime = Long.MAX_VALUE;
	private long endTime = -1;
	
	public TimeLineChart(String title) {
		this.lineCount = 0;
		this.lineMaxCount = DEFAULT_MAX_LINE_COUNT;
		tsArr = new TimeSeries[this.lineMaxCount];
		this.title = title;
	}
	
	public TimeLineChart(String title, int lineMaxCount) throws IllegalArgumentException {
		if (lineMaxCount > COLOR.length)
			throw new IllegalArgumentException("Max line count can't be set to larger than 11!");
		this.lineCount = 0;
		this.lineMaxCount = lineMaxCount;
		tsArr = new TimeSeries[this.lineMaxCount];
		this.title = title;
	}
	
	/**
	 * create a new curve
	 * @param lineName  name of new curve
	 * @return                     curve index assigned by chart
	 */
	public int newLine(String lineName) throws Exception {
		if (lineCount < lineMaxCount) {
			tsArr[lineCount] = new TimeSeries(lineName, org.jfree.data.time.Second.class);
			lineCount++;
			return lineCount;
		} else {
			throw new Exception("Too many lines to add to chart, at most is " + lineMaxCount + ".");
		}
	}	
	
	/**
	 * add point in a curve
	 * @param lineNum        index of curve
	 * @param millSeconds value of x axis
	 * @param value              value of y axis
	 */
	public void addPoint(int lineNum, long millSeconds, Number value) {
		Second time = new Second(new Date(millSeconds));
		tsArr[lineNum - 1].add(time, value);
		if (millSeconds < startTime)
			startTime = millSeconds;
		if (millSeconds > endTime)
			endTime = millSeconds;
	}

	/**
	 * create chart
	 * @param xLabel              label of x axis
	 * @param yLabel              label of y axis
	 * @param startTime        start time
	 * @param stopTime        stop time
	 * @param secondTick     tick in seconds
	 * @param XTickFormat   format of x tick
	 * @param lowerBound    min number of y axis, below zero means use default
	 * @param highBound      max number of y axis, below zero means use default
	 * @param isInt                  if number of y axis is integer 
	 */
	public void createChart(String xLabel, String yLabel, int secondTick, 
			String XTickFormat, double lowerBound, double highBound, boolean isInt) {		
			
		XYDataset dataset = finishCollectData();	
		//create JFreeChart
		chart = ChartFactory.createTimeSeriesChart(
				title, // title
				xLabel, // x-axis label
				yLabel, // y-axis label
				dataset, // data
				true, // create legend?
				true, // generate tooltips?
				false // generate URLs?
			);
		
		//chart.setBackgroundPaint(Color.white);
		Font font = new Font("宋体", Font.BOLD, 13);
		TextTitle mainTitle = new TextTitle(title, font);
		chart.setTitle(mainTitle);
		LegendTitle legengTitle = chart.getLegend();
		legengTitle.setItemFont(font);
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.white);
		plot.setDomainGridlinePaint(Color.gray);
		plot.setRangeGridlinePaint(Color.gray);
		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		plot.setDomainCrosshairVisible(true);
		plot.setRangeCrosshairVisible(true);
		
		XYItemRenderer r = plot.getRenderer();
		
		for (int i = 0; i < lineCount; i++) {
			GradientPaint gp = new GradientPaint(0.0f, 0.0f, COLOR[i],  
					0.0f, 0.0f, COLOR[i]);   
			r.setSeriesPaint(i, gp);  
		}
        
		/* setting y axis */
		NumberAxis numAxis = (NumberAxis) plot.getRangeAxis();
		
		if (isInt)
			numAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());  	
		
		numAxis.setAutoRange(true);
		numAxis.setLabelFont(font);
		if (lowerBound >= 0)
			numAxis.setLowerBound(lowerBound);
		if (highBound >= 0)
			numAxis.setUpperBound(highBound);

		/* setting x axis */
		DateAxis axis = (DateAxis) plot.getDomainAxis();
		SimpleDateFormat format = new SimpleDateFormat(XTickFormat);
		DateTickUnit unit = new DateTickUnit(DateTickUnit.SECOND, secondTick, format);
		axis.setTickUnit(unit);
		axis.setAutoRange(false);
		axis.setMinimumDate(new Date(startTime));
		axis.setMaximumDate(new Date(endTime));
		axis.setLabelFont(font);		 
	}
	/**
	 * save chart to file
	 * @param fileName         file name
	 * @param imageFormat image format, PNG&JPEG
	 */
	public void savaToFile(String fileName, String imageFormat) throws IOException {		
        File testFile = new File(fileName);
        if (imageFormat.equals("PNG") || imageFormat.equals("png")) {
        	ChartUtilities.saveChartAsPNG(testFile, chart, 900, 600);
        } else if (imageFormat.equals("JPEG") || imageFormat.equals("PNG")) {
        	ChartUtilities.saveChartAsJPEG(testFile, chart, 900, 600);
        } else {
        	System.err.println("Invalid graph format!");
        }
	}
	/**
	 * set chart title
	 * @param title
	 */
	public void setTitle(String title) {
		this.title = title;
		/* setting font, shape */
		Font font = new Font("黑体", Font.BOLD, 13);
		TextTitle mainTitle = new TextTitle(title, font);
		chart.setTitle(mainTitle);
	}
	/**
	 * set sub title
	 * @param subTile
	 */
	public void setSubTitle(String subTitle) {
		TextTitle sub = new TextTitle(subTitle, new Font("宋体", Font.BOLD, 12));
		chart.addSubtitle(sub);
	}

	/**
	 * create data set that have been collected
	 * 
	 * @return data set
	 */
	private XYDataset finishCollectData() {
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		for (int i = 0; i < lineCount; i++) {
			dataset.addSeries(tsArr[i]);
		}		
		return dataset;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
}
