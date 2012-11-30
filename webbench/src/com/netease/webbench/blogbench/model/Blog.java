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
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

/**
 * Blog
 * @author LI WEIZHAO
 */

public class Blog implements Externalizable {
	private LightBlog lightBlog;
	private BlogContent content;
	
	public Blog() {
		lightBlog = new LightBlog();
		content = new BlogContent();
	}
	
	public Blog(long id, long uid, String title, 
			String abs, String cnt,	int allowView, 
			long publishTime, int accessCount, int commentCount) {
		this.lightBlog = new LightBlog(id, uid, title, abs, allowView, publishTime, 
				accessCount, commentCount);
		this.content = new BlogContent(cnt);
	}
	
	public Blog(LightBlog lightBlog, BlogContent blogContent) {
		this.lightBlog = lightBlog;
		this.content = blogContent;
	}
	
	public void increaseCmtCnt() {
		this.lightBlog.increaseCmtCnt();
	}
	public long getId() {
		return lightBlog.getId();
	}
	public void setId(long id) {
		this.lightBlog.setId(id);
	}
	public void setUid(long uId) {
		this.lightBlog.setUid(uId);
	}
	public void setPublishTime(long time) {
		this.lightBlog.setPublishTime(time);
	}
	public long getUid() {
		return this.lightBlog.getUid();
	}
	
	public long getPublishTime() {
		return this.lightBlog.getPublishTime();
	}
	public String getTitle() {
		return lightBlog.getTitle();
	}

	public void setTitle(String title) {
		lightBlog.setTitle(title);
	}

	public String getAbs() {
		return lightBlog.getAbs();
	}

	public void setAbs(String abs) {
		this.lightBlog.setAbs(abs);
	}

	public String getCnt() {
		return content.getContent();
	}

	public void setCnt(String cnt) {
		this.content.setContent(cnt);
	}

	public int getAllowView() {
		return lightBlog.getAllowView();
	}

	public void setAllowView(int allowView) {
		this.lightBlog.setAllowView(allowView);
	}
	public int getAccessCount() {
		return lightBlog.getAccessCount();
	}
	public void setAccessCount(int accessCount) {
		this.lightBlog.setAccessCount(accessCount);
	}
	public int getCommentCount() {
		return lightBlog.getCommentCount();
	}
	public void setCommentCount(int commentCount) {
		this.lightBlog.setCommentCount(commentCount);
	}
	
	public LightBlog getLightBlog() {
		return this.lightBlog;
	}
	
	public BlogContent getBlogContent() {
		return this.content;
	}
	
	/**
	 * serialize blog to byte array
	 * @return
	 * @throws IOException
	 */
	public void writeExternal(ObjectOutput out) throws  IOException {	
		try {
			this.lightBlog.writeExternal(out);
			this.content.writeExternal(out);
		} finally {
			out.close();
		}
	}
	
	
	/**
	 * deserialize  blog from byte array
	 * @param bytes
	 * @return
	 * @throws IOException
	 */
	public void readExternal(ObjectInput in) throws IOException {		
		try {
			this.lightBlog.readExternal(in);
			this.content.readExternal(in);
		} finally {
			in.close();
		}
	}
	
	public byte[] writeToBytes() throws IOException {
	    ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(bout);
		writeExternal(out);
		return bout.toByteArray();
	}
	
	public void readFromBytes(byte [] arr) throws IOException {
		ByteArrayInputStream bin = new ByteArrayInputStream(arr);
		ObjectInputStream in = new ObjectInputStream(bin);
		readExternal(in);
	}
}
