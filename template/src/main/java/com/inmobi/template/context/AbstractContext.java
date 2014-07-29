package com.inmobi.template.context;

import java.util.HashMap;
import java.util.Map;

import lombok.EqualsAndHashCode;

import com.inmobi.template.interfaces.Context;


@EqualsAndHashCode
public abstract class AbstractContext implements Context  {
	
	protected transient final Map<String, Object> params = new HashMap<String, Object>();
	
	
	@Override
	public Object get(String key){
		return params.get(key);
	}
	
	abstract void setValues(Map<String, Object> params);

}
