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

import java.util.Random;

/**
 * binomial random number generator
 * 
 * @author qsfeng
 * @version 1.0 Dec 4, 2007
 */
public class BinomialGenerator {
	private double probabilites[];

	private int maxRecords;

	private double perspect;

	private boolean inited = false;

	private boolean RECORD_INCLUDE_ZERO = false; //if return numbers can contain zero

	private double LARGE_SCALE = 0; //the percentage of large number

	private double largeUserScale = 0; //the percentage of users of large number

	Random randomGenerator = null;

	/**
	 * genrate a probability that meet the binomial distribution
	 * 
	 * @param n           max number
	 * @param k           the k'th  element
	 * @param p           probability
	 * @return double the value of probability
	 */
	private double binomial(int n, int k, double p) {
		if (n < k || n < 0 || k < 0 || p > 1 || p < 0) {
			throw new IllegalArgumentException("Calculating binomial distribution probability value failed, input parameter is illegal !");
		}
		double propVal = 1;

		/**
		 * Avoid underflow and overflow problems when parameter is too big
		 */
		if (k != 0) {
			double x = n;

			int i = k, j = 0;
			while (i > 0 || j < n - k) {
				if (propVal < 999999 && i > 0) {
					for (int temp = 0; i > 0 && temp < 10; i--, temp++) {
						propVal = propVal * p * x / i;
						x--;
					}
				} else {
					int temp = Math.min(10, n - k - j);
					propVal = propVal * Math.pow(1 - p, temp);
					j += temp;
				}
			}

		} else {
			propVal = Math.pow(1 - p, n - k);
		}

		return propVal;
	}

	/**
	 * constructor
	 * 
	 * @param proportion expectation number
	 * @param max              maximum number
	 * @param hasZero       if return number can contain zero
	 * @param largeScale   percentage of large number
	 * @throws IllegalArgumentException
	 */
	public BinomialGenerator(double proportion, int max, boolean hasZero, double largeScale)
			throws IllegalArgumentException {
		if (proportion > max || proportion < 0 || max < 0 || largeScale < 0 || largeScale > 1) {
			throw new IllegalArgumentException("Arguments to generate binomial distribution are illegal !");
		}

		if (!hasZero && proportion == 1) {
			throw new IllegalArgumentException("According to arguments are given cannot construct binomial distribution model !");
		}

		if (0.75 * max < proportion * largeScale) {
			throw new IllegalArgumentException("According to arguments are given cannot construct binomial distribution model " +
					"that contains large numbers !");
		}

		RECORD_INCLUDE_ZERO = hasZero;
		LARGE_SCALE = largeScale;
		maxRecords = max;
		perspect = proportion;
		probabilites = new double[max + 2]; // generate (max + 1) records, 0--max+1

		/* If returns number cannot contain zero, 
		 * the graphics maximum, expectation need to minus one,
		 * then to move the entire graphic right one step */
		if (!RECORD_INCLUDE_ZERO) {
			maxRecords--;
			perspect--;
			proportion--;
		}

		double p = proportion / max;

		//correct the binomial probability distribution that has large record
		if (LARGE_SCALE > 0) {
			largeUserScale = (LARGE_SCALE * proportion) / (0.75 * max);
			p = ((1 - LARGE_SCALE) * proportion * 0.75 * max)
					/ ((0.75 * max - proportion * LARGE_SCALE) * max);
		}

		//generate probabilities
		for (int i = 0; i <= max; i++) {
			probabilites[i] = binomial(max, i, p);
		}

		//sum the probabilities
		for (int i = 1; i <= max; i++) {
			probabilites[i] += probabilites[i - 1];
		}

		//verify the correctness of the binomial probabilities
		if (probabilites[max] < 0.99 || probabilites[max] > 1.01) {
			throw new IllegalArgumentException("Faild to verify binomial probability distribution.");
		} else {
			randomGenerator = new Random();
			inited = true;
		}
	}

	/**
	 * get a binomial random number
	 * 
	 * @throws IllegalArgumentException
	 */
	public int getBinomialRandomNum() throws IllegalArgumentException {
		int record = 0;

		if (LARGE_SCALE > 0) {
			double randVal = randomGenerator.nextDouble();
			if (randVal < largeUserScale) {
				// generate uniform distribution big record number
				randVal = randomGenerator.nextDouble();
				record = (int) (randVal * maxRecords / 2) + (maxRecords / 2);
			} else {
				record = getBinomialRecord();
			}
		} else {
			record = getBinomialRecord();
		}
		return record;
	}

	/**
	 * 
	 * 
	 * @throws IllegalArgumentException
	 */
	private int getBinomialRecord() throws IllegalArgumentException {
		if (!inited) {
			throw new IllegalArgumentException("Please initialize first!");
		}

		int record = 0;
		double randomProb = randomGenerator.nextDouble(); //generate a random number
		if (maxRecords < 5) {
			//sequence search
			if (probabilites[0] > randomProb) {
				record = 0;
			} else {
				for (int i = maxRecords; i >= 0; i--) {
					if (probabilites[i] < randomProb) {
						record = i;
						break;
					}
				}
				if (record < maxRecords) {
					record++;
				}
			}

		} else {
			//binary search
			int min = 0, max = maxRecords, mid = (int) perspect;
			boolean bFind = false;
			if (probabilites[max] < randomProb) {
				record = max;
				bFind = true;
			} else {
				while (min <= max) {
					if (mid == 0) {
						if (probabilites[0] > randomProb) {
							record = 0;
							bFind = true;
							break;
						} else {
							min++; //It is possible that min=0,max=1,mid=0
						}
					} else if (probabilites[mid] >= randomProb && probabilites[mid - 1] < randomProb) {
						record = mid;
						bFind = true;
						break;
					} else if (probabilites[mid] > randomProb) {
						max = mid - 1;
					} else {
						min = mid + 1;
					}
					mid = (min + max) / 2;
				}
			}

			if (!bFind) {
				throw new IllegalArgumentException("Failed to get binomial distribution number, " +
						"can't find designated records in probability array.");
			}
		}

		if (!RECORD_INCLUDE_ZERO) {
			record++;
		}
		return record;
	}
}
