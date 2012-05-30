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
package com.netease.webbench.random;

import java.lang.Math;
import java.util.Random;

import com.netease.webbench.common.DynamicArray;

/**
 * Zipf random number generator
 * @author LI WEIZHAO
 */
public class ZipfGenerator {
	/* probabilities of each sample */
	private DynamicArray<Double> probabilites;
	/* probabilities of each partition */
	private double[] partProb;
	/* probability sum of all samples */
	private double sum;
	/* screw factor */
	private double screw;
	/* percentage of hot records in all records */
	private int pct;
	/* percentage of request hit hot records */
	private int res;
	/* number of partitions */
	private int part;
	/* records in each partition */
	private long partRcds;
	/* range of Zipf distribution random number */
	private long size;
	/* random number generator */
	private Random ranGen;
	/* if return numbers can contain zero */
	private boolean recordIncludeZero = false;
	/* if must ensure multi threads safe */
	private boolean multiThreadSafe = false;
	/* frequency of the hottest partition */
	private double hottestPartionFreq = 0;
	/* probability sum of the pct% hottest record */
	private double hottestPctFreq = 0;

	/**
	 * constructor
	 * 
	 * @param zipfPercent    Zipf percent parameter
	 * @param zipfRequest   Zipf request parameter
	 * @param zipfPart           Zipf partition parameter
	 * @param size                  size of samples
	 * @throws IllegalArgumentException argument is illegal
	 */
	public ZipfGenerator(int zipfPercent, int zipfRequest, int zipfPart,
			long size, boolean recordIncludeZero, boolean multiThreadSafe)
			throws IllegalArgumentException {

		if (screw < 0 || pct < 0 || pct > 1 || res < 0 || res > 1 || part < 0) {
			throw new IllegalArgumentException(
					"Illegal zipf construct parameter specified!");
		}

		this.multiThreadSafe = multiThreadSafe;
		this.sum = 0;
		this.pct = zipfPercent;
		this.res = zipfRequest;
		this.part = zipfPart;
		this.size = size;
		this.recordIncludeZero = recordIncludeZero;

		this.partRcds = size / zipfPart;
		ranGen = new Random();

		init();
	}

	/**
     * initialize probability density
	 */
	private void init() {
		partProb = new double[part];
		probabilites = new DynamicArray<Double>(size);

		/* calculate screw factor */
		culculateScrew();

		double partProbSum = 0;
		for (int i = 0; i <= part - 1; i++) {
			partProb[i] = 1.0 / Math.pow(i + 1, screw);
			partProbSum += partProb[i];
		}
		for (int i = 0; i <= part - 1; i++) {
			partProb[i] /= partProbSum;
		}
		partProbSum = 0;
		for (int i = 0; i <= part - 1; i++) {
			partProbSum += partProb[i];
		}
		if (partProbSum < 0.99 || partProbSum > 1.01) {
			throw new IllegalArgumentException(
					"Error occured when constructed zipf distribution!");
		}

		/* calculate probability density of  each record in partition */
		for (int i = 0; i <= part - 1; i++) {
			for (int j = 0; j < partRcds; j++) {
				probabilites.append(partProb[i] / partRcds);
			}
		}
		for (int i = 0; i < size % part; i++) {
			probabilites.append(partProb[part - 1] / (size % part));
		}

		hottestPartionFreq = partProb[0];
		hottestPctFreq = 0;
		for (int i = 0; i < (size * pct / 100.0); i++) {
			hottestPctFreq += probabilites.get(i);
		}

		/* exchange the first 10% hot records, so that hot records don't gather in some area */
		for (long i = 0; i < size / 10; i++) {
			long target = (long)(Math.random() * size);
			Double temp = probabilites.get(i);
			probabilites.set(i, probabilites.get(target));
			probabilites.set(target, temp);
		}

		/* calculate probability distribution */
		for (long i = 0; i < size; i++) {
			sum += probabilites.get(i);
			probabilites.set(i, sum);
		}
		/* Verify the correctness of the probability generated */
		if (probabilites.get(size - 1).doubleValue() < 0.99
				|| probabilites.get(size - 1).doubleValue() > 1.01) {
			throw new IllegalArgumentException(
					"Error occured when constructed zipf distribution!");
		}
	}

	/**
	 * get a Zipf random number
	 * 
	 * @return
	 */
	public long getZipfRandomNum() {
		long record = -1;

		double v = 0;
		long low = 0;
		long high = 0;

		if (multiThreadSafe) {
			synchronized (this) {
				v = ranGen.nextDouble() * sum;
				high = size - 1;
			}
		} else {
			v = ranGen.nextDouble() * sum;
			high = size - 1;
		}

		while (low <= high) {
			long n = ((high + low) / 2);
			if ((probabilites.get(n)).doubleValue() < v) {
				low = n + 1;
			} else if ((probabilites.get(n)).doubleValue() > v) {
				high = n - 1;
			} else {
				record = n;
				break;
			}
		}

		if (record < 0) {
			if (high < 0) {
				assert (high == -1);
				record = 0;
			} else {
				assert (v > probabilites.get(high).doubleValue());
				assert (v <= probabilites.get(high + 1).doubleValue());
				record = high;
			}
		}

		if (recordIncludeZero) {
			return record;
		} else {
			return record + 1;
		}
	}

	/**
	 * update probabilities array
	 */
	public void updateProb() {
		if (multiThreadSafe) {
			synchronized (this) {
				doUpdateProb();
			}
		} else {
			doUpdateProb();
		}
	}
	
	private void doUpdateProb() {
		double pro = 0;
		long recordId = (long)(Math.random() * size);
		
		if (recordId == 0) {
			pro = probabilites.get(recordId);
		} else {
			pro = probabilites.get(recordId)
				- probabilites.get(recordId - 1);
		}
		sum += pro;
		probabilites.append(sum);
		size++;
	}

	/**
	 * get request probability of the hottest partition
	 * 
	 * @return 
	 */
	public double getHottestPartionFreq() {
		return hottestPartionFreq;
	}

	/**
	 * get sum of probabilities of the hottest records
	 * @return
	 */
	public double getHottestPctFreq() {
		return hottestPctFreq;
	}

	/**
	 * calculate screw factor according percentage of hot records
	 * 
	 * @throws IllegalArgumentException    failed to calculate screw according specified pct and res parameter
	 */
	private void culculateScrew() throws IllegalArgumentException {
		int i = (int) this.pct * this.part / 100;
		int n = this.part;
		double screw1 = 0.5;
		double screw2 = 1.2;
		double f1 = 0;
		double f2 = 0;
		while (true) {
			f1 = getFunctionValue(i, n, screw1, this.res);
			f2 = getFunctionValue(i, n, screw2, this.res);
			if (Math.abs(f1) < 0.000001) {
				break;
			}
			if (screw1 == screw2 || f1 == f2) {
				String msg = "The zipf distribution parameter pct and res is improper!";
				throw new IllegalArgumentException(msg);
			}
			double sn = (f1 - f2) / (screw1 - screw2);
			screw2 = screw1;
			screw1 = screw1 - f1 / sn;
		}
		this.screw = screw1;
	}

	/**
	 * get function value about screw
	 *
	 * Area of first N part of probability density curve is: 
	 * Area(N) = 1^(-screw) + 2^(-screw)+...+ N^(-screw)
	 * Note that Area(i) / Area(n) = res,
	 * So function about screw is : f(x)=[1^(-x) + 2^(-x)+...+ i^(-x)] - [1^(-x) + 2^(-x)+...+ n^(-x)] * res
	 * 
	 * @param i         the first i hot parts
	 * @param n        total number of parts
	 * @param scrw  screw factor of Zipf distribution
	 * @param res     area percent of hot parts
	 * @return            value of function 
	 * @throws IllegalArgumentException
	 */
	private double getFunctionValue(int i, int n, double scrw, int res)
			throws IllegalArgumentException {
		return getSeriesSum(i, scrw) - res / 100.0 * getSeriesSum(n, scrw);
	}

	/**
	 * calculate probability sum of first N sample
	 * 
	 * @param n          first n sample
	 * @param screw  screw factor   
	 *  @return             probability sum
	 */
	private double getSeriesSum(int n, double scrw)
			throws IllegalArgumentException {
		if (n < 0) {
			throw new IllegalArgumentException(
					"Error occured when culculate zipf probabilites sum!");
		}
		double s = 0;
		for (int i = 1; i <= n; i++) {
			s += 1 / Math.pow(i, scrw);
		}
		return s;
	}

	/**
	 * get screw factor
	 * 
	 * @return screw factor
	 */
	public double getScrew() {
		return screw;
	}
}
