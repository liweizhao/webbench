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
package com.netease.webbench.blogbench.misc;

import java.util.TimerTask;


/**
 * load data progress print task 
 * @author LI WEIZHAO
 */

public class LoadProgressTask extends TimerTask {
	private final static int LINE_LENGTH = 80;
	private StringBuilder prgBuf;
	private int prgLastIndex = 0;
	
	private String tips;
	private LoadProgress progress;
	
	private boolean isFinish = false;
	private int lineLength = LINE_LENGTH;

	public LoadProgressTask(LoadProgress progress) {		
		this.progress = progress;
		prgBuf = new StringBuilder(lineLength);
		for (int i = 0; i < lineLength; i++) {
			prgBuf.append('<');
		}
		
		System.out.print(prgBuf.toString());
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.TimerTask#run()
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub

		try {
			for (int i = prgLastIndex; i < lineLength * progress.getProgress(); i++) {
				prgBuf.setCharAt(i, '=');
				prgLastIndex = (int) (LINE_LENGTH * progress.getProgress());
			}
			System.out.print("\r");

			System.out.print(prgBuf.toString());
			tips = String.format("\tProgress: %.1f %%",
					progress.getProgress() * 100);
			System.out.print(tips);

			if (progress.getProgress() == 1.0) {
				isFinish = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public boolean isFinish() {
		return isFinish;
	}
}
