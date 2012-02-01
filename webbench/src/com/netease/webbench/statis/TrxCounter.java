package com.netease.webbench.statis;

import java.util.concurrent.atomic.AtomicLong;

import com.netease.stat.Distribution;
import com.netease.stat.Histogram;
import com.netease.stat.HistogramFactory;
import com.netease.stat.HistogramType;
import com.netease.stat.SourceParameter;
import com.netease.webbench.visual.HistoGramChart;

public class TrxCounter {

	protected Distribution dis;
	protected AtomicLong failedTimes;

	public TrxCounter(String counterName) {
		failedTimes = new AtomicLong(0);
		dis = new Distribution(counterName, "milliseconds", 1, true);
	}

	/**
	 * add transaction statistic information
	 * @param rpTime transaction response time
	 */
	public void addTrx(long rpTime) {
		dis.addResult(rpTime >= 0 ? rpTime : 0);
	}

	/**
	 * increase transaction execution failed times and return new value
	 * @return
	 */
	public long incrFailedTimes() {
		return failedTimes.incrementAndGet();
	}

	/**
	 * get transaction execution failed times
	 * @return
	 */
	public long getFailedTimes() {
		return failedTimes.longValue();
	}

	public long getTrxCount() {
		return dis.getTotal(SourceParameter.REPEAT);
	}

	public long getMaxResponseTime() {
		return dis.getMax(SourceParameter.VALUE);
	}

	public long getMinResponseTime() {
		return dis.getMin(SourceParameter.VALUE);
	}

	public long getAvgResponseTime() {
		return (long)dis.getAverage(SourceParameter.VALUE);
	}

	public long getMostResponseTime() {
		return dis.getPercentThreshold(0.90, SourceParameter.REPEAT);
	}

	public String getTrxName() {
		return dis.getName();
	}

	/**
	 * create response time chart of this counter
	 * @param reportDir
	 * @return
	 * @throws Exception
	 */
	public String createResponseTimeChart(String reportDir) throws Exception {		
		HistogramFactory hisFac = HistogramFactory.getInstance();
		long range = dis.getMax(SourceParameter.VALUE) - dis.getMin(SourceParameter.VALUE);
		if (range > 0) {
			Histogram totalResponseTimeHistogram = hisFac.build(dis, HistogramType.EQUI_WIDTH, 
					SourceParameter.REPEAT, (int)(range / 10) + 1);
			HistoGramChart totalRTHistogramChart = new HistoGramChart(totalResponseTimeHistogram, 
					"response time", "count");
		
			String chartFileName = reportDir + dis.getName() + ".png";
			totalRTHistogramChart.savaToFile(chartFileName, "PNG");
			return chartFileName;
		} else {
			return null;
		}
	}

}