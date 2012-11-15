package com.netease.webbench.blogbench.statis;

public class LoadDataStatis {
	private long totalTimeWaste = 0;
	private long createTableTimeWaste = 0;
	private long loadDataTimeWaste = 0;
	private long createIndexTimeWaste = 0;
	
	public long getTotalTimeWaste() {
		return totalTimeWaste;
	}
	public long getCreateTableTimeWaste() {
		return createTableTimeWaste;
	}
	public long getLoadDataTimeWaste() {
		return loadDataTimeWaste;
	}
	public long getCreateIndexTimeWaste() {
		return createIndexTimeWaste;
	}
	public void addCreateTableTimeWaste(long createTableTimeWaste) {
		this.createTableTimeWaste += createTableTimeWaste;
		this.totalTimeWaste += createTableTimeWaste;
	}
	public void addLoadDataTimeWaste(long loadDataTimeWaste) {
		this.loadDataTimeWaste += loadDataTimeWaste;
		this.totalTimeWaste += loadDataTimeWaste;
	}
	public void addCreateIndexTimeWaste(long createIndexTimeWaste) {
		this.createIndexTimeWaste += createIndexTimeWaste;
		this.totalTimeWaste += createIndexTimeWaste;
	}
}
