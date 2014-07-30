package com.inmobi.adserve.channels.util;

import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.NotImplementedException;

import com.google.common.collect.Maps;

/**
 * This class was created because original getProperity in parent method has a
 * synchronized block in it, which was causing performance bottlenecks at high
 * cpu utilization. In this implementation we store all the values in our local
 * map and the gets happen via the local map
 * 
 * @author rajashekhar.c
 * 
 */
public class CasBaseConfiguration extends PropertiesConfiguration {

	final static private Object lock = new Object();

	final static private Map<String, Object> map = Maps.newHashMap();

	public CasBaseConfiguration(String configFile) throws ConfigurationException {
		super(configFile);
		cloneMap();
	}

	public CasBaseConfiguration(URL resource) throws ConfigurationException {
		super(resource);
		cloneMap();
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
			return super.getProperty(key);
		}
		return map.get(key);
	}

	private void cloneMap() {
		Iterator keys = getKeys();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			Object value = super.getProperty(key);
			map.put(key, value);
		}
	}

	@Override
	public void clearProperty(String key) {
		synchronized (lock) {
			super.clearProperty(key);
			map.remove(key);
		}
	}

	@Override
	public void clear() {
		throw new NotImplementedException();
	}
}
