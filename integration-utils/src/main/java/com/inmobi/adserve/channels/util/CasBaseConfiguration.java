package com.inmobi.adserve.channels.util;

import java.net.URL;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.google.common.collect.Maps;

/**
 * This class was created because original getProperity in parent method has a synchronized block in it, which was causing performance bottlenecks at high cpu utilization. 
 * In this implementation we store all the values in our local map and the gets happen via the local map
 * @author rajashekhar.c
 *
 */
public class CasBaseConfiguration extends PropertiesConfiguration {

	
	final static private Object lock = new Object();

	final static private Map<String, Object> map  = Maps.newHashMap();

	
	public CasBaseConfiguration(String configFile) throws ConfigurationException {
		super(configFile);
	}

	public CasBaseConfiguration(URL resource) throws ConfigurationException {
		super(resource);
	}
	
	@Override
	public void setProperty(String key, Object value) {
		synchronized (lock) {
			map.put(key, value);
			super.setProperty(key, value);
		}
	}

	@Override
	public Object getProperty(String key) {
		if (map.get(key) == null) {
			synchronized (lock) {
				if (map.get(key) == null) {
					map.put(key, super.getProperty(key));
				}
			}
		}
		return map.get(key);
	}


	@Override
	public void clearProperty(String key) {
		synchronized (lock) {
			super.clearProperty(key);
			map.remove(key);
		}
	}
	
	@Override
	public void clear(){
		synchronized (lock) {
			super.clear();
			map.clear();
		}
	}
}
