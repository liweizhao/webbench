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
package com.netease.webbench.blogbench.rdbms;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.netease.webbench.blogbench.dao.BlogDAO;
import com.netease.webbench.blogbench.misc.Portable;
import com.netease.webbench.blogbench.model.Blog;
import com.netease.webbench.blogbench.model.BlogIdWithTitle;
import com.netease.webbench.blogbench.model.BlogInfoWithPub;
import com.netease.webbench.blogbench.model.SiblingPair;
import com.netease.webbench.blogbench.rdbms.sql.SQLConfigure;
import com.netease.webbench.blogbench.rdbms.sql.SQLConfigureFactory;
import com.netease.webbench.common.DbOptions;
import com.netease.webbench.common.DbSession;
import com.netease.webbench.common.DynamicArray;

/**
 * 
 * @author LI WEIZHAO
 *
 */
public class RdbmsBlogDao implements BlogDAO {
	public static final int MULTI_INSERT_ROWS = 2000;
	public static final int BATCH_FETCH_SIZE = 10000;
	
	protected DbSession dbSession;
	protected Map<String, PreparedStatement> preStmtMap;
	protected SQLConfigure sqlConfig;
	
	public RdbmsBlogDao(DbOptions dbOpt) throws Exception {
		this.dbSession = new DbSession(dbOpt);
		// set client character encoding
		if (dbOpt.getDbType().equalsIgnoreCase("mysql") ||
			dbOpt.getDbType().equalsIgnoreCase("postgresql")) {
			SQLConfigure sqlConfig = SQLConfigureFactory.getSQLConfigure(
					dbOpt.getDbType());
			this.dbSession.update(sqlConfig.getSetEncodingSql(
					Portable.getCharacterSet()));
		}
		
		this.preStmtMap = new HashMap<String, PreparedStatement>();
		this.sqlConfig = SQLConfigureFactory.getSQLConfigure(dbOpt.getDbType());
	}
	
	protected PreparedStatement getPreparedStatement(String queryMethod, String sql) 
			throws SQLException {
		PreparedStatement ps = (PreparedStatement)preStmtMap.get(queryMethod);
		if (ps == null) {
			ps = dbSession.createPreparedStatement(sql);
			preStmtMap.put(queryMethod, ps);
		}
		return ps;
	}

	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.dao.BlogDAO#selectBlog(long, long)
	 */
	@Override
	public Blog selectBlog(long blogId, long uId) throws SQLException {		
		PreparedStatement ps = getPreparedStatement("selectBlog",
			sqlConfig.getShowWeightBlogSql(false));
		ps.setLong(1, blogId);
		ps.setLong(2, uId);
		ResultSet rs = dbSession.query(ps);
		try {
			if (rs.next()) {
				String title = rs.getString("Title");
				String abs = rs.getString("Abstract");
				String cnt = rs.getString("Content");
				int allowView = rs.getInt("AllowView");
				long publishTime = rs.getLong("PublishTime");
				int acsCount = rs.getInt("AccessCount");
				int cmtCount = rs.getInt("CommentCount");
				return new Blog(blogId, uId, title, abs, cnt, allowView, 
						publishTime, acsCount, cmtCount);
			}
			return null;
		} finally {
			rs.close();
			rs = null;
		}
	}

	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.dao.BlogDAO#selBlogList(long)
	 */
	@Override
	public List<Long> selBlogList(long uId) throws SQLException {
		PreparedStatement ps = getPreparedStatement("selBlogList",
			sqlConfig.getListBlogsSql());
		ps.setLong(1, uId);					
		ResultSet rs = dbSession.query(ps);						
		ArrayList<Long> resultList = new ArrayList<Long>();
		try {
			while (rs.next()) {
				resultList.add(rs.getLong("ID"));
			}
		} finally {
			rs.close();
		}
		return resultList;
	}

	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.dao.BlogDAO#selSiblings()
	 */
	@Override
	public SiblingPair selSiblings(
			long uId, long time) throws SQLException {
		PreparedStatement ps1 = getPreparedStatement("selSiblings-pre",
				sqlConfig.getShowPreSiblingsSql());
		PreparedStatement ps2 = getPreparedStatement("selSiblings-post",
				sqlConfig.getShowNextSiblingsSql());
		ps1.setLong(1, time);
		ps1.setLong(2, uId);	
		ps2.setLong(1, time);
		ps2.setLong(2, uId);
		
		BlogIdWithTitle pre = null;
		BlogIdWithTitle next = null;
		ResultSet rsPre = dbSession.query(ps1);
		ResultSet rsNext = dbSession.query(ps2);
		try {
			if (rsPre.next()) {
				pre = new BlogIdWithTitle();
				pre.setBlogId(rsPre.getInt("ID"));
				pre.setUId(rsPre.getInt("UserID"));
				pre.setTitle(rsPre.getString("Title"));
			}
			if (rsNext.next()) {
				next = new BlogIdWithTitle();
				next.setBlogId(rsNext.getInt("ID"));
				next.setUId(rsNext.getInt("UserID"));
				next.setTitle(rsNext.getString("Title"));
			}
		} finally {
			rsPre.close();
			rsNext.close();
		}
		return new SiblingPair(pre, next);
	}

	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.dao.BlogDAO#insertBlog(com.netease.webbench.blogbench.model.Blog)
	 */
	@Override
	public int insertBlog(Blog blog) throws SQLException {
		PreparedStatement ps = getPreparedStatement("insertBlog",
				sqlConfig.getPublishBlogSql(false));
		ps.setLong(1, blog.getId());
		ps.setLong(2, blog.getUid());
		ps.setString(3, blog.getTitle());
		ps.setString(4, blog.getAbs());
		ps.setString(5, blog.getCnt());
		ps.setInt(6, blog.getAllowView());
		ps.setLong(7, blog.getPublishTime());
		return dbSession.update(ps);
	}

	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.dao.BlogDAO#updateAccess(long, long)
	 */
	@Override
	public int updateAccess(long blogId, long uId) throws SQLException {
		PreparedStatement ps = getPreparedStatement("updateAccess",
				sqlConfig.getUpdateAccessSql());
		ps.setLong(1, uId);
		ps.setLong(2, blogId);
		return dbSession.update(ps);
	}

	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.dao.BlogDAO#updateComment(long, long)
	 */
	@Override
	public int updateComment(long blogId, long uId) throws SQLException {
		PreparedStatement ps = getPreparedStatement("updateComment",
				sqlConfig.getUpdateCommentSql());
		ps.setLong(1, blogId);
		ps.setLong(2, uId);
		return dbSession.update(ps);
	}

	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.dao.BlogDAO#updateBlog()
	 */
	@Override
	public int updateBlog(Blog blog) throws SQLException {
		PreparedStatement ps = getPreparedStatement("updateBlog",
				sqlConfig.getUpdateBlogSql(false));
		ps.setLong(1, blog.getPublishTime());
		ps.setString(2, blog.getTitle());
		ps.setString(3, blog.getAbs());
		ps.setString(4, blog.getCnt());
		ps.setLong(5, blog.getId());
		ps.setLong(6, blog.getUid());
		return dbSession.update(ps);
	}

	@Override
	public DynamicArray<BlogInfoWithPub> selAllBlogIds() throws SQLException {
		long tbSize = selBlogNums();			
		DynamicArray<BlogInfoWithPub> blgArr = 
				new DynamicArray<BlogInfoWithPub>(tbSize);
		 		
		String batchSelectRecords = sqlConfig.getBatchQueryBlogSql();
		PreparedStatement ps = dbSession.createPreparedStatement(batchSelectRecords);

		long low;
		long high;
		long fetchTimes = tbSize / BATCH_FETCH_SIZE;
		if ((tbSize % BATCH_FETCH_SIZE) != 0) {
			fetchTimes++;
		}			
		for (long i = 0; i < fetchTimes; i++) {
			low = i * BATCH_FETCH_SIZE + 1;
			high = (i + 1) * BATCH_FETCH_SIZE;

			ps.setLong(1, low);
			ps.setLong(2, high);
			ResultSet rs = dbSession.query(ps);
			try {
				while (rs != null && rs.next()) {
					blgArr.append(new BlogInfoWithPub(rs.getInt("ID"), 
							rs.getInt("UserID"), rs.getLong("PublishTime")));
				}
			} finally {
				rs.close();
			}		
		}
		
		return blgArr;
	}

	@Override
	public long selBlogNums() throws SQLException {
		PreparedStatement ps = getPreparedStatement("selBlogNums",
			sqlConfig.getQueryBlogCountSql());			
		ResultSet rs = dbSession.query(ps);
		try {
			if (rs.next()) {
				return rs.getLong("total");
			}
			return 0;
		} finally {
			rs.close();
		}
	}

	@Override	
	public int batchInsert(List<Blog> blogList) throws SQLException {
		final int insertRows = blogList.size();
		final int blogFieldSize = 7;
		
		String sql = sqlConfig.getMultiInsertBlogSql(insertRows, false);
		PreparedStatement ps = dbSession.createPreparedStatement(sql);
		
		for (int j = 0; j < insertRows; j++) {
			Blog blog = blogList.get(j);
			ps.setLong(1 + j * blogFieldSize, blog.getId());
			ps.setLong(2 + j * blogFieldSize, blog.getUid());
			ps.setString(3 + j * blogFieldSize, blog.getTitle());
			ps.setString(4 + j * blogFieldSize, blog.getAbs());
			ps.setString(5 + j * blogFieldSize, blog.getCnt());
			ps.setInt(6 + j * blogFieldSize, blog.getAllowView());
			ps.setLong(7 + j * blogFieldSize, blog.getPublishTime());
		}
		
		try {
			return dbSession.update(ps);
		} finally {
			ps.close();
		}
	}

	@Override
	public void close() {
		try {
			if (null != this.dbSession && !this.dbSession.isClosed()) {
				this.dbSession.close();	
				this.dbSession = null;
			}
		} catch (SQLException e) {}
	}
}
