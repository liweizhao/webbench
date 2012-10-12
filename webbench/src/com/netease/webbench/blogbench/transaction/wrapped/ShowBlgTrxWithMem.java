package com.netease.webbench.blogbench.transaction.wrapped;

import java.io.Externalizable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.netease.webbench.blogbench.blog.Blog;
import com.netease.webbench.blogbench.blog.BlogContent;
import com.netease.webbench.blogbench.blog.BlogIdPair;
import com.netease.webbench.blogbench.blog.BlogInfoWithPub;
import com.netease.webbench.blogbench.blog.LightBlog;
import com.netease.webbench.blogbench.memcached.MemcachedClientIF;
import com.netease.webbench.blogbench.memcached.MemcachedManager;
import com.netease.webbench.blogbench.misc.ParameterGenerator;
import com.netease.webbench.blogbench.misc.Portable;
import com.netease.webbench.blogbench.sql.SQLConfigure;
import com.netease.webbench.blogbench.sql.SQLConfigureFactory;
import com.netease.webbench.blogbench.statis.BlogbenchCounters;
import com.netease.webbench.blogbench.statis.MemcachedOperCounter.MemOperType;
import com.netease.webbench.blogbench.transaction.BbTestTransaction;
import com.netease.webbench.blogbench.transaction.BbTestTrxShowBlg;
import com.netease.webbench.blogbench.transaction.BlogDBFetcher;

public class ShowBlgTrxWithMem extends BbTestTransaction 
		implements BlogDBFetcher{
	private BbTestTrxShowBlg wrappedTrx;
	protected PreparedStatement lightPrpStmt;
	protected PreparedStatement fetchCntPrpStmt;
	
	public ShowBlgTrxWithMem(BbTestTrxShowBlg wrappedTrx, 
			BlogbenchCounters counters) throws Exception {
		super(wrappedTrx, counters);
		this.wrappedTrx = wrappedTrx;
	}

	@Override
	public void prepare() throws Exception {
		wrappedTrx.prepare();
		
		SQLConfigure sqlConfig = SQLConfigureFactory.getSQLConfigure();
		
		String lightSql = sqlConfig.getShowLightBlogSql(bbTestOpt.getTbName());		
		lightPrpStmt = dbSession.createPreparedStatement(lightSql);
		
		String contentSql = sqlConfig.getBlogContentSql(bbTestOpt.getUseTwoTable() ? 
				Portable.getBlogContentTableName(bbTestOpt.getTbName())
				: bbTestOpt.getTbName());
		fetchCntPrpStmt = dbSession.createPreparedStatement(contentSql);		
	}
	
	@Override
	public void cleanRes() throws SQLException {
		if (null != lightPrpStmt) {
			lightPrpStmt.close();
		}
		if (null != fetchCntPrpStmt) {
			fetchCntPrpStmt.close();
		}
	}

	@Override
	public void doExeTrx(ParameterGenerator paraGen) throws Exception {
		BlogInfoWithPub blogInfo = paraGen.getZipfRandomBlog();
		
		/* first query the record in memcached server, 
		 * if it doesn't  exist, fetch it from database */
		MemcachedClientIF mcm = MemcachedManager.getInstance().getMajorMcc();
		
		Blog blog = null;
		LightBlog lightBlog = getLightBlogFromMemcached(mcm, blogInfo);
		
		if (lightBlog != null) {
			//if blog content doesn't exist in memcached, fetch it from database
			BlogContent blogCnt = getContentFromMemcached(mcm, blogInfo);				
			if (blogCnt == null) {
				blogCnt = getContentFromDb(blogInfo.getBlogId(), blogInfo.getUId());
				if (blogCnt != null) {
					setContentToMemcached(mcm, blogInfo, blogCnt);
				} else {
					//FIXED ME: this won't be execute
					System.out.println("[Error] failed to fetch blog " +
							"content from database!");
				}
			}
			blog = new Blog(lightBlog, blogCnt);
		} else {
			BlogContent blogCnt = getContentFromMemcached(mcm, blogInfo);
			if (blogCnt == null) {
				blog = wrappedTrx.getWholeBlogFromDb(blogInfo.getBlogId(), 
						blogInfo.getUId());
				if (blog != null) {
					setLightBlogToMemcached(mcm, blogInfo, blog.getLightBlog());
					setContentToMemcached(mcm, blogInfo, blog.getBlogContent());
				} else {
					throw new Exception("[Error] failed to fetch blog record " +
							"from database!");
				}	
			} else {
				lightBlog = getLightBlogFromDb(blogInfo.getBlogId(), 
						blogInfo.getUId());
				if (lightBlog != null) {
					setLightBlogToMemcached(mcm, blogInfo, lightBlog);
					blog = new Blog(lightBlog, blogCnt);
				} else {
					System.out.println("[Error] failed to fetch blog without " +
							"content from database!");
				}
			}				
		}
	}
	
	/**
	 * 
	 * @param mcm
	 * @param blogInfo
	 * @param lightBlog
	 * @throws Exception
	 */
	protected void setLightBlogToMemcached(MemcachedClientIF mcm, 
			BlogIdPair blogInfo, LightBlog lightBlog) throws Exception {
		boolean isSetBlogSuc =mcm.set("lblog:" + blogInfo.getBlogId(), 
				lightBlog.writeToBytes());
		myTrxCounter.addMemOper(MemOperType.SET_BLOG, isSetBlogSuc);	
	}
	
	/**
	 * 
	 * @param mcm
	 * @param blogInfo
	 * @return
	 * @throws Exception
	 */
	protected LightBlog getLightBlogFromMemcached(MemcachedClientIF mcm, 
			BlogIdPair blogInfo) throws Exception {
		LightBlog lightBlog = new LightBlog();
		byte[] blogSerialData = (byte[])mcm.get("lblog:" + blogInfo.getBlogId());
		
		boolean isReadBlogSuc = lightBlog.readFromBytes(blogSerialData);
		
		myTrxCounter.addMemOper(MemOperType.GET_BLOG, isReadBlogSuc);
		
		return isReadBlogSuc ? lightBlog : null;
	}	
	
	/**
	 * get blog content from memcached
	 * @param mcm
	 * @param blogInfo
	 * @return
	 * @throws Exception
	 */
	protected BlogContent getContentFromMemcached(MemcachedClientIF mcm, 
			BlogIdPair blogInfo) throws Exception {
		BlogContent blogCnt = (BlogContent)(Externalizable)mcm.get(
				"cnt:" + blogInfo.getBlogId());
		myTrxCounter.addMemOper(MemOperType.GET_CNT, blogCnt == null ? false : true);
		return blogCnt;
	}
	
	/**
	 * 
	 * @param mcm
	 * @param blogInfo
	 * @param blogContent
	 * @throws Exception
	 */
	protected void setContentToMemcached(MemcachedClientIF mcm, 
			BlogIdPair blogInfo, BlogContent blogContent) throws Exception {
		boolean isSetCntSuc = mcm.set("cnt:" + blogInfo.getBlogId(), 
				(Externalizable)blogContent);
		myTrxCounter.addMemOper(MemOperType.SET_CNT, isSetCntSuc);
	}
	
	/**
	 * fetch a light blog from database
	 * @param blogId
	 * @param uId
	 * @return
	 * @throws SQLException
	 */
	protected LightBlog getLightBlogFromDb(long blogId, long uId) 
			throws SQLException {
		bindParameter(lightPrpStmt, blogId, uId);
		
		ResultSet rs = dbSession.query(lightPrpStmt);
		LightBlog lightBlog = null;
		if (rs.next()) {
			String title = rs.getString("Title");
			String abs = rs.getString("Abstract");
			int allowView = rs.getInt("AllowView");
			long publishTime = rs.getLong("PublishTime");
			int acsCount = rs.getInt("AccessCount");
			int cmtCount = rs.getInt("CommentCount");
			lightBlog = new LightBlog(blogId, uId, title, abs, allowView,
					publishTime, acsCount, cmtCount);
		}
		rs.close();
		return lightBlog;
	}
	
	/**
	 * fetch blog content from database
	 * @param blogId
	 * @param uId
	 * @return
	 * @throws SQLException
	 */
	protected BlogContent getContentFromDb(long blogId, long uId) 
			throws SQLException {
		bindParameter(fetchCntPrpStmt, blogId, uId);
		
		ResultSet rs = dbSession.query(fetchCntPrpStmt);
		BlogContent blogContent = null;
		while (rs.next()) {
			String content = rs.getString(1);
			blogContent = new BlogContent(content);
			break;
		}
		rs.close();
		return blogContent;
	}
	
	/**
	 * bind prepared statement parameter
	 * @param localPrpStmt
	 * @param blogId
	 * @param uId
	 * @throws SQLException
	 */
	protected void bindParameter(PreparedStatement localPrpStmt, 
			long blogId, long uId) throws SQLException {
		localPrpStmt.setLong(1, blogId);
		localPrpStmt.setLong(2, uId);
	}

	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.transaction.BlogDBFetcher#getWholeBlog(long, long)
	 */
	@Override
	public Blog getWholeBlog(long blogId, long uId) 
			throws SQLException {
		// TODO Auto-generated method stub
		return wrappedTrx.getWholeBlogFromDb(blogId, uId);
	}
	
	/* (non-Javadoc)
	 * @see com.netease.webbench.blogbench.BlogFetcher#getLightBlog()
	 */
	@Override
	public LightBlog getLightBlog(long blogId, long UserId) 
			throws SQLException {
		return getLightBlogFromDb(blogId, UserId);
	}
}
