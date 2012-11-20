package com.netease.webbench;

public interface WebbenchTest {
	
	public abstract void setUp(String[] args) throws Exception;
	
	public abstract void run() throws Exception;
	
	public abstract void tearDown() throws Exception;
}