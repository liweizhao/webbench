package com.netease.webbench.blogbench.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class BlogbenchConfig {
	public static final String DFL_PLUGIN_CLS = 
			"com.netease.webbench.blogbench.rdbms.BlogbenchRdbmsPlugin";
	private static final String PLUGIN_CLS_KEY = "plugin_class";
	
	private static volatile BlogbenchConfig instance = null;
	
	private String path = "config/plugin.properties";
	private Properties propertie = null;
	
	private BlogbenchConfig() {
		propertie = new Properties();
        try {
        	FileInputStream inputFile = new FileInputStream(path);
            propertie.load(inputFile);
            inputFile.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
	}
	
	public static BlogbenchConfig getInstance() {
		if (instance == null) {
			synchronized (BlogbenchConfig.class) {
				if (instance == null)
					instance = new BlogbenchConfig();
			}
		}
		return instance;
	}
	
	public String getPluginClsName() {	
		 if(propertie.containsKey(PLUGIN_CLS_KEY)) {
			 return propertie.getProperty(PLUGIN_CLS_KEY);
		 } else {
			 System.err.println("No plugin configure found, use default blogbench test.");
			 return DFL_PLUGIN_CLS;
		 }
	}
}
