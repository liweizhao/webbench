package com.netease.webbench.blogbench.dao;

import java.util.List;

import com.netease.util.Pair;
import com.netease.webbench.blogbench.model.Blog;
import com.netease.webbench.blogbench.model.BlogInfoWithPub;
import com.netease.webbench.common.DynamicArray;

public interface BlogDAO {
	public void close();
	
	public Blog selectBlog(long blogId, long uId) throws Exception;
	
	public List<Long> selBlogList(long uId) throws Exception;
	
	public Pair<Long, Long> selSiblings(long uId, long time) throws Exception;
	
	public DynamicArray<BlogInfoWithPub> selAllBlogIds() throws Exception;
	
	public long selBlogNums() throws Exception;
	
	public int insertBlog(Blog blog) throws Exception;
	
	public int batchInsert(List<Blog> blogList) throws Exception;
	
	public int updateAccess(long blogId, long uId) throws Exception;
	
	public int updateComment(long blogId, long uId) throws Exception;
	
	public int updateBlog(Blog blog) throws Exception;
}
