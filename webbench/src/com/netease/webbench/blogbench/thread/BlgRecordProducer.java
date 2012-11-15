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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.netease.webbench.blogbench.misc.ParameterGenerator;
import com.netease.webbench.blogbench.model.Blog;

/**
 *  blog record producer
 *  @author LI WEIZHAO
 */

public class BlgRecordProducer extends Thread {
	public static final int DEFAULT_QUEUE_SIZE = 8192;
	
	private ParameterGenerator paraGen;
	
	private BlockingQueue<Blog> blockingQueue;
	private int queueMaxSize = DEFAULT_QUEUE_SIZE;
	private long produceNum;
	private long hasProduced = 0;
	private volatile boolean shouldExit = false;
	
	public BlgRecordProducer(ParameterGenerator paraGen, 
			long produceNum) throws Exception {	
		blockingQueue = new ArrayBlockingQueue<Blog>(queueMaxSize);
		this.produceNum = produceNum;
		this.paraGen = paraGen;
	}
	
	public void run() {
		try {
			while (!shouldExit && hasProduced < produceNum) {
				blockingQueue.put(paraGen.generateNewBlog());
				hasProduced++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * force blog record produce thread to exit
	 * @throws Exception
	 */
	public void forceExit() throws Exception {
		shouldExit = true;
		blockingQueue.poll(0, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * get a blog from queue
	 * @return if the queue has data, then return a blog, otherwise block
	 * @throws Exception
	 */
	public Blog getBlog() throws Exception {
		return blockingQueue.take();
	} 
}
