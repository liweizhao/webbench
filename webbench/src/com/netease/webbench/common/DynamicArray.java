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
package com.netease.webbench.common;

import java.util.ArrayList;
/**
 * Dynamic Array
 * 
 * @author LI WEIZHAO
 */
public class DynamicArray<E> {
	/* page list */
	private ArrayList<Page<E>> pageList;
	/* the index of last unused page */
	private int lastUnusedPageIndex;
	/* actual size of dynamic array */
	private int size;
	/* capacity of dynamic array */
	private long capacity;
	
	/**
	 * constructor
	 * @param initialCapacity initial capacity
	 */
	public DynamicArray(long initialCapacity) {
		int pageCount = (int)initialCapacity / Page.PAGE_SIZE;
		if ((initialCapacity % Page.PAGE_SIZE) != 0) {
			pageCount += 1;
		}
		pageList = new ArrayList<Page<E>>(pageCount);
		for (int i = 0; i < pageCount; i++) {
			Page<E> newPage = new Page<E>(i);
			pageList.add(newPage);
		}
		/**FIXME:capacity = initialCapacity*/
		capacity = pageCount * Page.PAGE_SIZE;
		size = 0;
		lastUnusedPageIndex = 0;
	}
	
	/**
	 * check if index is out of bounds
	 * @param index
	 */
	private void rangeCheck(long index) throws IndexOutOfBoundsException {
		if (index >= size || index < 0) {
			System.out.println("Dynamic array size:" + size +", index:" + index);
			throw new IndexOutOfBoundsException();
		}
	}	
	/**
	 * append a element at the tail of dynamic array
	 * @param element   element to append
	 * @return  true if successful, false otherwise
	 */	
	public synchronized boolean append(E element) {
		if (pageList.get(lastUnusedPageIndex).add(element)) {		
			size++;
			return true;
		} else if (lastUnusedPageIndex == (pageList.size() - 1)) {
			Page<E> newPage = new Page<E>(lastUnusedPageIndex++);
			pageList.add(newPage);
			capacity += Page.PAGE_SIZE;
			if (pageList.get(lastUnusedPageIndex).add(element)){
				size++;
				return true;
			}
		} else {
			lastUnusedPageIndex++;
			if (pageList.get(lastUnusedPageIndex).add(element)) {
				size++;
				return true;
			} 
		}
		return false;
	}
	
	/**
	 *  get value of array element
	 * @param index          index in dynamic array
	 * @return                      value of element
	 * @throws IndexOutOfBoundsException
	 */	
	public E get(long index) throws IndexOutOfBoundsException{		
		rangeCheck(index);		
		int pageNum = (int)(index >> Page.PAGE_SIZE_SHIFT);
		int offset = (int)(index & (Page.PAGE_SIZE - 1));	
		return pageList.get(pageNum).get(offset);
	}
	
	/**
	 * set value of array element
	 * @param index      index in dynamic array
	 * @param element new value of element
	 * @return                  old value of element
	 * @throws IndexOutOfBoundsException
	 */	
	public synchronized E set(long index, E element) throws IndexOutOfBoundsException{		
		rangeCheck(index);		
		int pageNum = (int)(index >> Page.PAGE_SIZE_SHIFT);
		int offset = (int)(index & (Page.PAGE_SIZE - 1));
		return pageList.get(pageNum).set(offset, element);
	}
	
	/**
	 * ensure current capacity is larger than minCapacity, expand capacity if necessary
     * @param minCapacity
	 */
	public synchronized void ensureCapacity(int minCapacity) {
		if (minCapacity > capacity) {
			expandCapacity(minCapacity);
		}
	}
	/**
	 * expand capacity of dynamic array
	 * @param capacity
	 */
	private void expandCapacity(int capacity) {
		if (capacity <= this.capacity) {
			return;
		} 
		int pageCount = capacity / Page.PAGE_SIZE + 1;
		int oldPageCount = pageList.size();
		int less = pageCount - oldPageCount;
		for (int i = 0; i < less; i++) {
			Page<E>  newPage = new Page<E>(oldPageCount + i);
			pageList.add(newPage);
		}
	}
	/**
	 * get capacity
	 * @return capacity of this dynamic array
	 */
	public synchronized long getCapacity() {
		return capacity;
	}
	/**
	 * get actual size of this dynamic array
	 * @return actual size of this dynamic array
	 */
	public synchronized int size() {
		return size;
	}
		
	/**
     * data page
	 * @param <E>
	 */
	private static class Page<E> {
		public final static short PAGE_SIZE_SHIFT = 14;
		/* page size, default is 10k */
		public final static int PAGE_SIZE = 1 << PAGE_SIZE_SHIFT;
		/* page number */
		private int pageNum;
		/* number of elements stored in page */
		private int used;
		/* array to store elements */
		private Object[] elementData;
		
		public Page(int pageNum) {
			this.pageNum = pageNum;
			this.used = 0;
			this.elementData = new Object[PAGE_SIZE];
		}
		
		@SuppressWarnings("unchecked")
		private E elementData(int index) {
			return (E) elementData[index];
		}
		
		public boolean add(E element) {
			if (used >= PAGE_SIZE) {
				return false;
			} else {
				elementData[used] = element;
				used++;
				return true;
			}
		} 
		
		public E get(int index) throws IndexOutOfBoundsException {
			if (index < PAGE_SIZE) {
				return elementData(index);
			} else {
				throw new IndexOutOfBoundsException();
			}
		}
		
		public E set(int index, E element) throws IndexOutOfBoundsException{
			if (index < PAGE_SIZE) {
				E oldElement = elementData(index);
				elementData[index] = element;
				return oldElement;
			} else {
				throw new IndexOutOfBoundsException();
			}			
		}
		
		public int getPageNum() {
			return pageNum;
		}
	}
}
