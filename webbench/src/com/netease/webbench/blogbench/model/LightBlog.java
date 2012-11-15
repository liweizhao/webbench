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
package com.netease.webbench.blogbench.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.netease.webbench.blogbench.misc.Portable;

/**
 * Blog without content
 * @author LI WEIZHAO
 */

public class LightBlog implements Comparable<LightBlog> {
	private long blogId;
	private long uId;	
	private String title;
	private String abs;
	private int allowView;
	private long publishTime;	
	private int accessCount;
	private int commentCount;
	
	public LightBlog() {
	}
	
	public LightBlog(long id, long uid, String title, String abs, 
			int allowView, long publishTime, int accessCount, int commentCount) {
		this.blogId = id;
		this.uId = uid;
		this.publishTime = publishTime;
		this.title = title;
		this.abs = abs;
		this.allowView = allowView;
		this.accessCount = accessCount;
		this.commentCount = commentCount;
	}
	
	public void increaseCmtCnt() {
		this.commentCount++;
	}
	public long getId() {
		return this.blogId;
	}
	public void setId(long id) {
		this.blogId = id;
	}
	public void setUid(long uId) {
		this.uId = uId;
	}
	public void setPublishTime(long time) {
		this.publishTime = time;
	}
	public long getUid() {
		return this.uId;
	}
	
	public long getPublishTime() {
		return this.publishTime;
	}
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAbs() {
		return abs;
	}

	public void setAbs(String abs) {
		this.abs = abs;
	}

	public int getAllowView() {
		return allowView;
	}

	public void setAllowView(int allowView) {
		this.allowView = allowView;
	}
	public int getAccessCount() {
		return accessCount;
	}
	public void setAccessCount(int accessCount) {
		this.accessCount = accessCount;
	}
	public int getCommentCount() {
		return commentCount;
	}
	public void setCommentCount(int commentCount) {
		this.commentCount = commentCount;
	}
	
	/**
	 * serialize blog to byte array
	 * @return
	 * @throws IOException
	 */
	public byte[] writeToBytes() throws  IOException {
	    ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(bout);
		
		out.writeLong(blogId);
		out.writeLong(uId);
		out.writeInt(allowView);
		out.writeLong(publishTime);
		out.writeInt(accessCount);
		out.writeInt(commentCount);		

		byte[] tmp = title.getBytes();
		out.writeInt(tmp.length);
		out.write(tmp, 0, tmp.length);
		out.flush();

		tmp = abs.getBytes(Portable.getCharacterSet());
		out.writeInt(tmp.length);
		out.write(tmp, 0, tmp.length);
		out.flush();
				
		return bout.toByteArray();
	}
	
	
	/**
	 * deserialize  blog from byte array
	 * @param bytes
	 * @return
	 * @throws IOException
	 */
	public boolean readFromBytes(byte[] bytes) throws IOException {
		if (bytes == null) {
			return false;
		}
	    ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
		ObjectInputStream in = new ObjectInputStream(bin);	
		
		blogId = in.readLong();
		uId = in.readLong();
		allowView = in.readInt();
		publishTime = in.readLong();
		accessCount = in.readInt();
		commentCount = in.readInt();
		
		int len = in.readInt();
		byte[] tmp = new byte[len];
		in.readFully(tmp, 0, len);
		title = new String(tmp, Portable.getCharacterSet());
		
		len = in.readInt();	
		tmp = new byte[len];
		in.readFully(tmp, 0, len);
		abs = new String(tmp, Portable.getCharacterSet());
			
		return true;
	}
	
	public int compareTo(LightBlog another) {
		if (blogId > another.blogId)
			return 1;
		else if (blogId < another.blogId) 
			return -1;
		else
			return 0;
	}
	
	public boolean equals(LightBlog another) {
		if (blogId != another.blogId) 
			return false;
		else if (uId != another.uId)
			return false;
		else if (allowView != another.allowView)
			return false;
		else if (publishTime != another.publishTime)
			return false;
		else if (accessCount != another.accessCount) 
			return false;
		else if (commentCount != another.commentCount)
			return false;
		else if (title.equals(another.title))
			return false;
		else if (abs.equals(abs)) 
			return false;
		else
			return true;
			
		
	}
}
