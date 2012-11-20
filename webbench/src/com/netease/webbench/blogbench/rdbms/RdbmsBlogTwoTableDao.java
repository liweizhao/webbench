package com.netease.webbench.blogbench.rdbms;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.netease.webbench.blogbench.model.Blog;
import com.netease.webbench.common.DbSession;

public class RdbmsBlogTwoTableDao extends RdbmsBlogDao {
	public RdbmsBlogTwoTableDao(DbSession dbSession) {
		super(dbSession);
	}

	@Override
	public Blog selectBlog(long blogId, long uId) throws SQLException {
		PreparedStatement ps = getPreparedStatement("selectBlog",
				sqlConfig.getShowWeightBlogSql(true));
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

	@Override
	public int insertBlog(Blog blog) throws SQLException {
		dbSession.setAutoCommit(false);
		PreparedStatement ps = getPreparedStatement("insertBlog",
				sqlConfig.getPublishBlogSql(true));
		PreparedStatement ps2 = getPreparedStatement("insertContent",
				sqlConfig.getInsertContentSql());
		ps.setLong(1, blog.getId());
		ps.setLong(2, blog.getUid());
		ps.setString(3, blog.getTitle());
		ps.setString(4, blog.getAbs());
		ps.setInt(5, blog.getAllowView());
		ps.setLong(6, blog.getPublishTime());
		
		ps2.setLong(1, blog.getId());
		ps2.setLong(2, blog.getUid());
		ps2.setString(3, blog.getCnt());
		
		dbSession.update(ps);
		dbSession.update(ps2);
		dbSession.commit();
		dbSession.setAutoCommit(true);
		return 1;
	}

	@Override
	public int batchInsert(List<Blog> blogList) throws SQLException {
		final int insertRows = blogList.size();
		final int blogFieldSize = 6;
		final int blogFieldSize2 = 3;
		
		PreparedStatement ps = getPreparedStatement("insertBlog" + insertRows,
				sqlConfig.getMultiInsertBlogSql(insertRows, true));
		PreparedStatement ps2 = getPreparedStatement("insertContent" + insertRows,
				sqlConfig.getMultiInsertContentSql(insertRows));
		dbSession.setAutoCommit(false);
		for (int j = 0; j < insertRows; j++) {			
			Blog blog = blogList.get(j);
			ps.setLong(1 + j * blogFieldSize, blog.getId());
			ps.setLong(2 + j * blogFieldSize, blog.getUid());
			ps.setString(3 + j * blogFieldSize, blog.getTitle());
			ps.setString(4 + j * blogFieldSize, blog.getAbs());
			ps.setInt(5 + j * blogFieldSize, blog.getAllowView());
			ps.setLong(6 + j * blogFieldSize, blog.getPublishTime());
			
			ps2.setLong(1 + j * blogFieldSize2, blog.getId());
			ps2.setLong(2 + j * blogFieldSize2, blog.getUid());
			ps2.setString(3 + j * blogFieldSize2, blog.getCnt());
		}
		dbSession.update(ps);
		dbSession.update(ps2);
		dbSession.commit();
		dbSession.setAutoCommit(true);
		return blogList.size();
	}

	@Override
	public int updateBlog(Blog blog) throws SQLException {
		dbSession.setAutoCommit(false);
		PreparedStatement ps = getPreparedStatement("updateBlog",
				sqlConfig.getUpdateBlogSql(true));
		PreparedStatement ps2 = getPreparedStatement("updateContent",
				sqlConfig.getUpdateContentSql());
		ps.setLong(1, blog.getPublishTime());
		ps.setString(2, blog.getTitle());
		ps.setString(3, blog.getAbs());
		ps.setLong(4, blog.getId());
		ps.setLong(5, blog.getUid());
		
		ps2.setString(1, blog.getCnt());
		ps2.setLong(2, blog.getId());
		ps2.setLong(3, blog.getUid());
		dbSession.update(ps);
		dbSession.update(ps2);
		dbSession.commit();
		dbSession.setAutoCommit(true);
		return 1;
	}

}
