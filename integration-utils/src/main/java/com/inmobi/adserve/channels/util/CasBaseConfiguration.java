package com.inmobi.adserve.channels.util;

import java.util.Map;
import java.util.Properties;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.NotImplementedException;

import com.google.common.collect.Maps;

/**
 * 
 * @author rajashekhar.c
 *
 */
public class CasBaseConfiguration extends PropertiesConfiguration {

	private Configuration configuration;
	
	private Object lock = new Object();

	private Map<String, Object> map = Maps.newHashMap();

	public CasBaseConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	@Override
	public void setProperty(String key, Object value) {
		synchronized (lock) {
			map.put(key, value);
			configuration.setProperty(key, value);
		}
	}

	@Override
	public Object getProperty(String key) {
		if (map.get(key) == null) {
			synchronized (lock) {
				if (map.get(key) == null) {
					map.put(key, configuration.getProperty(key));
				}
			}
		}
		return map.get(key);
	}


	@Override
	public void clearProperty(String key) {
		synchronized (lock) {
			configuration.clearProperty(key);
			map.remove(key);
		}
	}
	
	@Override
	public void clear(){
		synchronized (lock) {
			configuration.clear();
			map.clear();
		}
	}
}
