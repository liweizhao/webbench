package com.netease.webbench.common;

public class Util {
	public static long currentTimeMillis() {
		return System.currentTimeMillis();
	}
	
	public static boolean isRdbms(String dbType) {
		return "mysql".compareToIgnoreCase(dbType) == 0 ||
				"oracle".compareToIgnoreCase(dbType) == 0 ||
				"postgresql".compareToIgnoreCase(dbType) == 0;
	}
}
