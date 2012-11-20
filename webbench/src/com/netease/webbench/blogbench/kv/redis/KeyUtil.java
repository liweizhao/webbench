package com.netease.webbench.blogbench.kv.redis;

public abstract class KeyUtil {
	static final String UID = "uid:";
	
	static String uid(String uid) {
		return UID + uid;
	}
	
	static String blogs(String uid) {
		return UID + uid + ":blogs";
	}
}
