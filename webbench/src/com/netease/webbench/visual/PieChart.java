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

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.DecimalFormat;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.util.Rotation;
import org.jfree.chart.labels.*;

/**
 * Pie chart
 * @author LI WEIZHAO
 */

public class PieChart {
	private JFreeChart localJFreeChart;
	private DefaultPieDataset localDefaultPieDataset;
	private String title;

	public PieChart(String title) {
		localDefaultPieDataset = new DefaultPieDataset();
		this.title = title;
	}
		
	public void addPie(String pieName, double value) {
		localDefaultPieDataset.setValue(pieName, new Double(value));
	}

	public void createChart() {
	    localJFreeChart = ChartFactory.createPieChart3D(title, (PieDataset)localDefaultPieDataset, true, true, false);
	    PiePlot3D localPiePlot3D = (PiePlot3D)localJFreeChart.getPlot();
	    localPiePlot3D.setDarkerSides(true);
	    localPiePlot3D.setStartAngle(290.0D);
	    localPiePlot3D.setDirection(Rotation.CLOCKWISE);
	    localPiePlot3D.setForegroundAlpha(0.5F);

	    localPiePlot3D.setLabelGenerator(new StandardPieSectionLabelGenerator( 
	            "{0}={1}({2})", NumberFormat.getNumberInstance(), 
	            new DecimalFormat("0.00%"))); 
	    localPiePlot3D.setLegendLabelGenerator(new StandardPieSectionLabelGenerator( 
	            "{0}={1}({2})"));
	    
	    localPiePlot3D.setNoDataMessage("No data to display");
	}
	
	/**
	 * save chart to file
	 * @param fileName         file name
	 * @param imageFormat image format, PNG&JPEG
	 * @return
	 */
	public void savaToFile(String fileName, String imageFormat) throws IOException {		
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
