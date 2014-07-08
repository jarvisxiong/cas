package com.inmobi.template.tool;

import com.google.gson.Gson;
import com.inmobi.template.interfaces.Context;
import com.inmobi.template.interfaces.Tools;

public class ToolsImpl extends Tools {
	
	Gson gson = new Gson();
	
	public ToolsImpl(){
		
	}
	

	@Override
	public Object jpath(Context context, String key) {
		return context.get(key);
	}

	@Override
	public String jpathStr(Context context, String key) {
		return (String)jpath(context, key);
	}

	@Override
	public String jsonEncode(Object map) {
		return gson.toJson(map);
	}
	
	 public String nativeAd(Context creativeContext, String pubContent) {
	        return gson.toJson(pubContent);
	    }

}
