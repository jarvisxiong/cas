package com.inmobi.template.context;

import java.util.HashMap;
import java.util.Map;

public class CreativeBean {
	
	private transient Map<String, Object> params = new HashMap<String, Object>(); 
	
	public CreativeBean(String key,Object value){
		set(key, value);
	}
	
	public Object get(String key){
		return params.get(key);
	}
	
	public void set(String key, Object value){
		params.put(key, value);
	}
}
