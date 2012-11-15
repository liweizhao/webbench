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
package com.netease.webbench.statis;

import java.util.Iterator;

import com.netease.webbench.common.DynamicArray;

/**
 * 
 * @author LI WEIZHAO
 *
 */
public class PeriodNodes implements Iterable<PeriodNodes.TimeNode> {
	private long periodCount = 0;
	private DynamicArray<TimeNode> timeValueList;
	
	public PeriodNodes() {
		timeValueList = new DynamicArray<TimeNode>(1024);
	}
	
	public void addNode(Number value) {
		periodCount++;
		timeValueList.append(new TimeNode(value));
	}
	
	public void addNode(long msTime, Number value) {
		periodCount++;
		timeValueList.append(new TimeNode(msTime, value));
	}
	
	public long getPeriodCount() {
		return periodCount;
	}
	
	public long getFirstTime() {
		return timeValueList.size() > 0 ? timeValueList.get(0).time : -1;
	}
	
	public long getLastTime() {
		return timeValueList.size() > 0 ? timeValueList.get(timeValueList.size() - 1).time : -1;
	}
	
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public Iterator iterator() {
        return new InnerIterator();
    }
	
	private class InnerIterator implements Iterator<TimeNode> {
        private int index = 0;
        public boolean hasNext() {
            return index < timeValueList.size();
        }
        public TimeNode next() {
            TimeNode tn = timeValueList.get(index);
            index++;
            return tn;
        }
        public void remove() {}
    }
	
	public class TimeNode {
		public long time;
		public Number value;
		public TimeNode() {
			time = 0;
			value = 0;
		}
		public TimeNode(Number value) {
			time = System.currentTimeMillis();
			this.value = value;
		}
		public TimeNode(long time, Number value) {
			this.time = time;
			this.value = value;
		}
	}
}
