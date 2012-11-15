package com.netease.webbench;

public interface WebbenchTest {
	
	public void setUp(String[] args) throws Exception;
	
	public abstract void run() throws Exception;
	
	public void tearDown();
}