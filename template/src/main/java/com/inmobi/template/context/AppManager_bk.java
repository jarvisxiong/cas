package com.inmobi.template.context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class AppManager_bk {

	private static Gson gson = null;
	static GsonBuilder gsonBuilder = new GsonBuilder();
	static {
		gson = gsonBuilder.create();
	}

	public static App getAppFromString(final String appStr) {
		return gson.fromJson(appStr, App.class);
	}

}
