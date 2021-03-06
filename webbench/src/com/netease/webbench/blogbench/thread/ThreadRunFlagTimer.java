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
package com.netease.webbench.blogbench.thread;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Thread run flag timer
 * @author LI WEIZHAO
 */
public class ThreadRunFlagTimer {
	private final Timer timer = new Timer();
	/* run flag , if time expires, it's set to false */
	private volatile boolean runFlag = true;
	private volatile boolean isTimeout = false;
	
	public boolean getRunFlag() {
		return this.runFlag;
	}
	
	/**
	 * start timer
	 * @param timeout, unit is milliseconds
	 */
	public void start(long timeout) {
		 timer.schedule(new TimerTask() {
	            public void run() {
	            	runFlag = false;
	            	isTimeout = true;
	                timer.cancel();
	            }
	     }, timeout);
	}
	
	public void setExpired() {
		if (runFlag) {
			runFlag = false;
			timer.cancel();
		}
	}
	
	public boolean getIsTimeout() {
		return isTimeout;
	}
}
