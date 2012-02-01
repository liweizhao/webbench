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

/**
 * Gamma random number generator
 * @author LI WEIZHAO
 */
public class GammaGenerator {
	/*  alpha parameter of gamma distribution */
	private double alpha;
	/*  lambda parameter of gamma distribution */
	private double lambda;
	/*  beta parameter of gamma distribution */
	private double beta;
	/* probabilities array */
	private double[] probabilites;
	/* max number */
	private int max;
	/* if return numbers can contail zero */
	private boolean RECORD_INCLUDE_ZERO = false; 
	
	/**
	 * constructor
	 * @param beta             beta parameter
	 * @param perspect     expectation
	 * @param max              max number
	 * @throws IllegalArgumentException
	 */
	public GammaGenerator(double beta, int perspect, int max, boolean includeZero) throws IllegalArgumentException {
		if (beta <= 0) {
			throw new IllegalArgumentException("Illegal beta parameter of gamma random number generator specified!");
		}
		if (perspect >= max || perspect < 0 || max <= 0) {
			throw new IllegalArgumentException("Illegal perspect or max value of gamma random number generator!");
		}
		/* if beta is too large, it's possible that gamma distribution approach exponential distribution */
		if (beta >= perspect) {
			System.err.println(String.format("Warnning: Gamma distribution may approach exponential distribution(beta: %f, perspect:%d)! ", 
					beta, perspect));
		}
		
		this.RECORD_INCLUDE_ZERO = includeZero;
		this.beta = beta;
		this.lambda = 1 / beta;
		this.alpha = perspect * this.lambda;
		this.max = max;
		probabilites = new double[max];
		init();
	}
	
	/**
	 * initialize probabilities array
	 */
	private void init() {
		double sum = 0;
		for (int i = 0; i < max; i++) {
			probabilites[i] = getGammaPdf(i + 1);
			sum += probabilites[i];
		}

		for (int i = 0; i < max; i++) {
			probabilites[i] /= sum;
		}		
		
		/* calculate sum of probabilities */
		sum = 0;
		for (int i = 0; i < max; i++) {
			sum += probabilites[i];
			probabilites[i] = sum;
		}
		if (probabilites[max - 1] < 0.99 || probabilites[max - 1] > 1.01) {
			throw new IllegalArgumentException("Error occured when constructed gamma distribution!");
		} 
	}
	
	/**
	 *  get gamma random number
	 * @return
	 */
	public int getGammaRandomNum() {
		int record = -1;
		
		double v = 0;
		int low = 0;
		int high = 0;
		
		v = Math.random();
		high = max -1;
		
		while(low <= high) {
			int n = ((high + low) / 2 );
			if (probabilites[n] < v){
				low = n + 1;
			} else if (probabilites[n]  > v) {
				high = n - 1;
			} else {
				record = n;
				break;
			}
		}
		
		if (record < 0) {
			if (high < 0) {
				record = 0;
			} else {
				record = high + 1;
			}		
		} 
		
		if(RECORD_INCLUDE_ZERO) {
			return record;
		} else {
			return record + 1;
		}
	}
	
	/**
	 * get natural log of the complete Gamma function
	 * Converted from the Fortran subroutine "GAMMLN" in:
	 * Numerical Recipes, Press et al., Cambridge, 1986.
	 * @param xx
	 * @return the log of the gamma function
	 */
	private double gammaln(double xx)
	{
	    double[] cof = {
	    		76.18009173, 
	    		-86.50532033,
	    		24.01409822,
	    		-1.231739516,
	    		0.00120858003,
	    		-0.00000536382
	    };
	    double stp, half , one, fpf, x, tmp, ser;

	    stp = 2.50662827465;
	    half = 0.5;
	    one = 1.0;
	    fpf = 5.5;
	    x = xx - one;
	    tmp = x + fpf;
	    tmp = (x + half) * Math.log(tmp) - tmp;
	    ser = one;
	    for (int j = 0; j < cof.length; j++) {
	        x = x + one;
	        ser = ser + cof[j] / x;
	    }
	    double temp = tmp + Math.log(stp * ser);
		return temp;
	}
	
	/**
	 * get probability density value of gamma distribution 
	 * @param x
	 * @return 
	 */
	private double getGammaPdf(int x) {
		 /* 
		  * probability density function of gamma distribution :
		  *  f(x) = (lambda^alpha) * (x^(alpha-1)) * (e^(-lambda * x)) / gamma(alpha) 
		  */
		double z = x * lambda;
	    double u = (alpha - 1) * Math.log(z) - z - gammaln(alpha);
	    
	    if ( z == 0 && alpha == 1) {
	    	u = 0;
	    }	    
		return (Math.exp(u) * lambda);
	}

	public double getBeta() {
		return beta;
	}
}
