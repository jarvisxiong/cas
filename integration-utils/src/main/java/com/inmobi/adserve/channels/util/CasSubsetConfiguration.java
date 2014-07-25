package com.inmobi.adserve.channels.util;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Iterator;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.iterators.TransformIterator;
import org.apache.commons.configuration.Configuration;

/**
 * <p>
 * A subset of another configuration. The new Configuration object contains
 * every key from the parent Configuration that starts with prefix. The prefix
 * is removed from the keys in the subset.
 * </p>
 * <p>
 * It is usually not necessary to use this class directly. Instead the
 * <code>{@link Configuration#subset(String)}</code> method should be used,
 * which will return a correctly initialized instance.
 * </p>
 * 
 * @author rajashekhar.c
 */
public class CasSubsetConfiguration extends CasBaseConfiguration {
	/** The parent configuration. */
	protected Configuration parent;

	/** The prefix used to select the properties. */
	protected String prefix;

	/** The prefix delimiter */
	protected String delimiter;

	/**
	 * Create a subset of the specified configuration
	 * 
	 * @param parent
	 *            The parent configuration
	 * @param prefix
	 *            The prefix used to select the properties
	 */
	public CasSubsetConfiguration(Configuration parent, String prefix) {
		super(parent);
		this.parent = parent;
		this.prefix = prefix;
	}

	/**
	 * Create a subset of the specified configuration
	 * 
	 * @param parent
	 *            The parent configuration
	 * @param prefix
	 *            The prefix used to select the properties
	 * @param delimiter
	 *            The prefix delimiter
	 */
	public CasSubsetConfiguration(Configuration parent, String prefix, String delimiter) {
		super(parent);
		this.parent = parent;
		this.prefix = prefix;
		this.delimiter = delimiter;
	}

	/**
	 * Return the key in the parent configuration associated to the specified
	 * key in this subset.
	 * 
	 * @param key
	 *            The key in the subset.
	 * @return the key as to be used by the parent
	 */
	protected String getParentKey(String key) {
		if ("".equals(key) || key == null) {
			return prefix;
		} else {
			return delimiter == null ? prefix + key : prefix + delimiter + key;
		}
	}

	/**
	 * Return the key in the subset configuration associated to the specified
	 * key in the parent configuration.
	 * 
	 * @param key
	 *            The key in the parent configuration.
	 * @return the key in the context of this subset configuration
	 */
	protected String getChildKey(String key) {
		if (!key.startsWith(prefix)) {
			throw new IllegalArgumentException("The parent key '" + key + "' is not in the subset.");
		} else {
			String modifiedKey = null;
			if (key.length() == prefix.length()) {
				modifiedKey = "";
			} else {
				int i = prefix.length() + (delimiter != null ? delimiter.length() : 0);
				modifiedKey = key.substring(i);
			}

			return modifiedKey;
		}
	}

	/**
	 * Return the parent configuation for this subset.
	 * 
	 * @return the parent configuration
	 */
	public Configuration getParent() {
		return parent;
	}

	/**
	 * Return the prefix used to select the properties in the parent
	 * configuration.
	 * 
	 * @return the prefix used by this subset
	 */
	public String getPrefix() {
		return prefix;
	}

	/**
	 * Set the prefix used to select the properties in the parent configuration.
	 * 
	 * @param prefix
	 *            the prefix
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public Configuration subset(String prefix) {
		return parent.subset(getParentKey(prefix));
	}

	public boolean isEmpty() {
		return !getKeys().hasNext();
	}

	public boolean containsKey(String key) {
		return parent.containsKey(getParentKey(key));
	}

	public void addPropertyDirect(String key, Object value) {
		parent.addProperty(getParentKey(key), value);
	}

	public void setProperty(String key, Object value) {
		parent.setProperty(getParentKey(key), value);
	}

	public void clearProperty(String key) {
		parent.clearProperty(getParentKey(key));
	}

	public Object getProperty(String key) {
		return parent.getProperty(getParentKey(key));
	}

	public Iterator getKeys(String prefix) {
		return new TransformIterator(parent.getKeys(getParentKey(prefix)), new Transformer() {
			public Object transform(Object obj) {
				return getChildKey((String) obj);
			}
		});
	}

	public Iterator getKeys() {
		return new TransformIterator(parent.getKeys(prefix), new Transformer() {
			public Object transform(Object obj) {
				return getChildKey((String) obj);
			}
		});
	}
}
