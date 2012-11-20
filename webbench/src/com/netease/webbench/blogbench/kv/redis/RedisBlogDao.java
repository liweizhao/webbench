package com.netease.webbench.blogbench.kv.redis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import com.netease.util.Pair;
import com.netease.webbench.blogbench.dao.BlogDAO;
import com.netease.webbench.blogbench.model.Blog;
import com.netease.webbench.blogbench.model.BlogInfoWithPub;
import com.netease.webbench.common.DynamicArray;

/**
 * Redis数据库设计：
 * 
 * 
 * @author LI WEIZHAO
 *
 */
public class RedisBlogDao implements BlogDAO {
	private String host;
	private int port;
	private Jedis jedis;
	
	public RedisBlogDao(String host, int port) {
		this.host = host;
		this.port = port;
		this.jedis = new Jedis(host, port, 0);
	}
	
	private byte[] buildBlogKey(long blogId, long uId) {
		return String.format("uId:%d:bId:%d:post", uId, blogId).getBytes();
	}
	
	private String buildAccessKey(long blogId, long uId) {
		return String.format("uId:%d:bId:%d:acs", uId, blogId);
	}
	
	private String buildCommentKey(long blogId, long uId) {
		return String.format("uId:%d:bId:%d:cmt", uId, blogId);
	}
	
	private String buildListKey(long uId) {
		return String.format("uId:%d:list", uId);
	}

	@Override
	public Blog selectBlog(long blogId, long uId) throws IOException {
		Blog blog = new Blog();
		byte[] val = jedis.get(buildBlogKey(blogId, uId));
		String accessCount = jedis.get(buildAccessKey(blogId, uId));
		String commentCount = jedis.get(buildCommentKey(blogId, uId));
		if (val != null) {
			blog.readFromBytes(val);
			blog.setAccessCount(Integer.parseInt(accessCount));
			blog.setCommentCount(Integer.parseInt(commentCount));
			return blog;
		}
		return null;
	}

	@Override
	public List<Long> selBlogList(long uId) throws IOException {
		List<String> list = jedis.lrange(buildListKey(uId), 0, -1);
		if (list != null) {
			List<Long> rl = new ArrayList<Long>(list.size());
			for (String s : list) {
				rl.add(Long.parseLong(s));
			}
			return rl;
		}
		return null;
	}

	@Override
	public Pair<Long, Long> selSiblings(long uId, long time) throws IOException {
		//List<String> list = jedis.lrange(buildUserKey(uId), 0, -1);
		//Collections.sort(list);
		return new Pair<Long, Long>(1L, 1L);
	}

	@Override
	public DynamicArray<BlogInfoWithPub> selAllBlogIds() throws IOException {
		Set<String> keys = jedis.keys("*:post");
		DynamicArray<BlogInfoWithPub> arr = new DynamicArray<BlogInfoWithPub>(keys.size());
		for (String key : keys) {
			byte[] val = jedis.get(key.getBytes());
			Blog blog = new Blog();
			blog.readFromBytes(val);
			arr.append(new BlogInfoWithPub(blog.getId(), blog.getUid(), blog.getPublishTime()));
		}
		return arr;
	}

	@Override
	public long selBlogNums() throws IOException {
		return jedis.keys("*:post").size();
	}

	@Override
	public int insertBlog(Blog b) throws Exception {		
		Transaction t = jedis.multi();
		t.set(buildBlogKey(b.getId(), b.getUid()), b.writeToBytes());
		t.set(buildAccessKey(b.getId(), b.getUid()), 
				String.format("%d", b.getAccessCount()));
		t.set(buildCommentKey(b.getId(), b.getUid()), 
				String.format("%d", b.getCommentCount()));
		t.lpush(buildListKey(b.getUid()), String.format("%d", b.getId()));
		t.exec();
		return 1;
	}

	@Override
	public int updateAccess(long blogId, long uId) throws Exception {
		jedis.incr(buildAccessKey(blogId, uId));
		return 1;
	}

	@Override
	public int updateComment(long blogId, long uId) throws Exception {
		jedis.incr(buildCommentKey(blogId, uId));
		return 1;
	}

	@Override
	public int updateBlog(Blog blog) throws Exception {
		jedis.set(buildBlogKey(blog.getId(), blog.getUid()), blog.writeToBytes());
		return 1;
	}

	@Override
	public int batchInsert(List<Blog> blogList) throws Exception {
		Transaction t = jedis.multi();
		for (Blog b : blogList) {
			t.set(buildBlogKey(b.getId(), b.getUid()), b.writeToBytes());
			t.set(buildAccessKey(b.getId(), b.getUid()), 
					String.format("%d", b.getAccessCount()));
			t.set(buildCommentKey(b.getId(), b.getUid()), 
					String.format("%d", b.getCommentCount()));
			t.lpush(buildListKey(b.getUid()), String.format("%d", b.getId()));
		}
		t.exec();
		return blogList.size();
	}

	@Override
	public void close() {
		if (jedis != null)
			jedis.disconnect();
	}

}
