package com.netease.webbench.blogbench.nosql;

import java.util.List;

import com.netease.util.Pair;
import com.netease.webbench.blogbench.dao.BlogDAO;
import com.netease.webbench.blogbench.model.Blog;
import com.netease.webbench.blogbench.model.BlogInfoWithPub;
import com.netease.webbench.common.DynamicArray;

public class RedisBlogDao implements BlogDAO {

	@Override
	public Blog selectBlog(long blogId, long uId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Long> selBlogList(long uId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Pair<Long, Long> selSiblings(long uId, long time) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DynamicArray<BlogInfoWithPub> selAllBlogIds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long selBlogNums() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int insertBlog(Blog blog) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int updateAccess(long blogId, long uId) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int updateComment(long blogId, long uId) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int updateBlog(Blog blog) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int batchInsert(List<Blog> blogList) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

}
