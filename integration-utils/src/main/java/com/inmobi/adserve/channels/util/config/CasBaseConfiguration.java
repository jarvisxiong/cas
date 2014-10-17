package com.inmobi.adserve.channels.util.config;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.NotImplementedException;

/**
 * This class was created because original getProperity in parent method has a synchronized block in it, which was
 * causing performance bottlenecks at high cpu utilization. In this implementation we store all the values in our local
 * map and the gets happen via the local map
 * 
 * @author rajashekhar.c
 * 
 */
public class CasBaseConfiguration extends PropertiesConfiguration {

  final static private Object lock = new Object();

  final private Map<String, Object> map;

  public CasBaseConfiguration(final String configFile) throws ConfigurationException {
    super(configFile);
    map = new HashMap<>();
    cloneMap();
  }

  public CasBaseConfiguration(final URL resource) throws ConfigurationException {
    super(resource);
    map = new HashMap<>();
    cloneMap();
  }

  @Override
  public void setProperty(final String key, final Object value) {
    synchronized (lock) {
      super.setProperty(key, value);
      map.put(key, value);
    }
  }

  @Override
  public Object getProperty(final String key) {
    if (map == null) {
      return super.getProperty(key);
    }
    return map.get(key);
  }

  private void cloneMap() {
    final Iterator<String> keys = getKeys();
    while (keys.hasNext()) {
      final String key = keys.next();
      final Object value = super.getProperty(key);
      map.put(key, value);
    }
  }

  @Override
  public void clearProperty(final String key) {
    synchronized (lock) {
      super.clearProperty(key);
      map.remove(key);
    }
  }

  @Override
  public void clear() {
    throw new NotImplementedException();
  }



  @Override
  public Configuration subset(final String prefix) {
    return new CasSubsetConfiguration(this, prefix, ".");
  }
}
