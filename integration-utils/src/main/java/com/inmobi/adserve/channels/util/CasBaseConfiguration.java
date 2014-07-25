package com.inmobi.adserve.channels.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;

import lombok.Delegate;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.PropertyConverter;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.NotImplementedException;

import com.google.common.collect.Maps;

/**
 * 
 * @author rajashekhar.c
 *
 */
public class CasBaseConfiguration implements Configuration {

	@Delegate(excludes = SetterGetterConfiguration.class)
	private Configuration configuration;
	private Object lock = new Object();

	interface SetterGetterConfiguration {
		Object getProperty(String key);

		void setProperty(String key, Object value);

		boolean getBoolean(String key);

		boolean getBoolean(String key, boolean defaultValue);

		Boolean getBoolean(String key, Boolean defaultValue);

		byte getByte(String key);

		byte getByte(String key, byte defaultValue);

		Byte getByte(String key, Byte defaultValue);

		double getDouble(String key);

		double getDouble(String key, double defaultValue);

		Double getDouble(String key, Double defaultValue);

		float getFloat(String key);

		float getFloat(String key, float defaultValue);

		Float getFloat(String key, Float defaultValue);

		int getInt(String key);

		int getInt(String key, int defaultValue);

		Integer getInteger(String key, Integer defaultValue);

		long getLong(String key);

		long getLong(String key, long defaultValue);

		Long getLong(String key, Long defaultValue);

		short getShort(String key);

		short getShort(String key, short defaultValue);

		Short getShort(String key, Short defaultValue);

		BigDecimal getBigDecimal(String key);

		BigDecimal getBigDecimal(String key, BigDecimal defaultValue);

		BigInteger getBigInteger(String key);

		BigInteger getBigInteger(String key, BigInteger defaultValue);

		String getString(String key);

		String getString(String key, String defaultValue);

		String[] getStringArray(String key);

		List getList(String key);

		List getList(String key, List defaultValue);

		Configuration subset(String prefix);
		
		void clearProperty(String key);
		
		void clear();
		
		Properties getProperties(String key);
	}

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

	public Object getProperty(String key, Object defaultValue) {
		Object object = getProperty(key);
		if (object == null) {
			return defaultValue;
		}
		return object;
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
	
	/**
	 * {@inheritDoc}
	 * 
	 * @see PropertyConverter#toBoolean(Object)
	 */
	@Override
	public boolean getBoolean(String key) {
		Boolean b = getBoolean(key, null);
		if (b != null) {
			return b.booleanValue();
		} else {
			throw new NoSuchElementException('\'' + key + "' doesn't map to an existing object");
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see PropertyConverter#toBoolean(Object)
	 */
	@Override
	public boolean getBoolean(String key, boolean defaultValue) {
		return getBoolean(key, BooleanUtils.toBooleanObject(defaultValue)).booleanValue();
	}

	/**
	 * Obtains the value of the specified key and tries to convert it into a
	 * <code>Boolean</code> object. If the property has no value, the passed in
	 * default value will be used.
	 * 
	 * @param key
	 *            the key of the property
	 * @param defaultValue
	 *            the default value
	 * @return the value of this key converted to a <code>Boolean</code>
	 * @throws ConversionException
	 *             if the value cannot be converted to a <code>Boolean</code>
	 * @see PropertyConverter#toBoolean(Object)
	 */
	@Override
	public Boolean getBoolean(String key, Boolean defaultValue) {

		Object value = getProperty(key);

		if (value == null) {
			return defaultValue;
		} else {
			try {
				return PropertyConverter.toBoolean(value);
			} catch (ConversionException e) {
				throw new ConversionException('\'' + key + "' doesn't map to a Boolean object", e);
			}
		}

	}

	@Override
	public byte getByte(String key) {
		Byte b = getByte(key, null);
		if (b != null) {
			return b.byteValue();
		} else {
			throw new NoSuchElementException('\'' + key + " doesn't map to an existing object");
		}

	}

	@Override
	public byte getByte(String key, byte defaultValue) {
		return getByte(key, new Byte(defaultValue)).byteValue();
	}

	@Override
	public Byte getByte(String key, Byte defaultValue) {
		Object value = getProperty(key);

		if (value == null) {
			return defaultValue;
		} else {
			try {
				return PropertyConverter.toByte(value);
			} catch (ConversionException e) {
				throw new ConversionException('\'' + key + "' doesn't map to a Byte object", e);
			}
		}

	}

	@Override
	public double getDouble(String key) {
		Double d = getDouble(key, null);
		if (d != null) {
			return d.doubleValue();
		} else {
			throw new NoSuchElementException('\'' + key + "' doesn't map to an existing object");
		}
	}

	@Override
	public double getDouble(String key, double defaultValue) {
		return getDouble(key, new Double(defaultValue)).doubleValue();
	}

	@Override
	public Double getDouble(String key, Double defaultValue) {
		Object value = getProperty(key);

		if (value == null) {
			return defaultValue;
		} else {
			try {
				return PropertyConverter.toDouble(value);
			} catch (ConversionException e) {
				throw new ConversionException('\'' + key + "' doesn't map to a Double object", e);
			}
		}
	}

	@Override
	public float getFloat(String key) {
		Float f = getFloat(key, null);
		if (f != null) {
			return f.floatValue();
		} else {
			throw new NoSuchElementException('\'' + key + "' doesn't map to an existing object");
		}
	}

	@Override
	public float getFloat(String key, float defaultValue) {
		return getFloat(key, new Float(defaultValue)).floatValue();
	}

	@Override
	public Float getFloat(String key, Float defaultValue) {
		Object value = getProperty(key);

		if (value == null) {
			return defaultValue;
		} else {
			try {
				return PropertyConverter.toFloat(value);
			} catch (ConversionException e) {
				throw new ConversionException('\'' + key + "' doesn't map to a Float object", e);
			}
		}
	}

	@Override
	public int getInt(String key) {
		Integer i = getInteger(key, null);
		if (i != null) {
			return i.intValue();
		} else {
			throw new NoSuchElementException('\'' + key + "' doesn't map to an existing object");
		}
	}

	@Override
	public int getInt(String key, int defaultValue) {
		Integer i = getInteger(key, null);

		if (i == null) {
			return defaultValue;
		}

		return i.intValue();
	}

	@Override
	public Integer getInteger(String key, Integer defaultValue) {
		Object value = getProperty(key);

		if (value == null) {
			return defaultValue;
		} else {
			try {
				return PropertyConverter.toInteger(value);
			} catch (ConversionException e) {
				throw new ConversionException('\'' + key + "' doesn't map to an Integer object", e);
			}
		}
	}

	@Override
	public long getLong(String key) {
		Long l = getLong(key, null);
		if (l != null) {
			return l.longValue();
		} else {
			throw new NoSuchElementException('\'' + key + "' doesn't map to an existing object");
		}
	}

	@Override
	public long getLong(String key, long defaultValue) {
		return getLong(key, new Long(defaultValue)).longValue();
	}

	@Override
	public Long getLong(String key, Long defaultValue) {
		Object value = getProperty(key);

		if (value == null) {
			return defaultValue;
		} else {
			try {
				return PropertyConverter.toLong(value);
			} catch (ConversionException e) {
				throw new ConversionException('\'' + key + "' doesn't map to a Long object", e);
			}
		}
	}

	@Override
	public short getShort(String key) {
		Short s = getShort(key, null);
		if (s != null) {
			return s.shortValue();
		} else {
			throw new NoSuchElementException('\'' + key + "' doesn't map to an existing object");
		}
	}

	@Override
	public short getShort(String key, short defaultValue) {
		return getShort(key, new Short(defaultValue)).shortValue();
	}

	@Override
	public Short getShort(String key, Short defaultValue) {
		Object value = getProperty(key);

		if (value == null) {
			return defaultValue;
		} else {
			try {
				return PropertyConverter.toShort(value);
			} catch (ConversionException e) {
				throw new ConversionException('\'' + key + "' doesn't map to a Short object", e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see #setThrowExceptionOnMissing(boolean)
	 */
	@Override
	public BigDecimal getBigDecimal(String key) {
		BigDecimal number = getBigDecimal(key, null);
		if (number != null) {
			return number;
		} else {
			return null;
		}
	}

	@Override
	public BigDecimal getBigDecimal(String key, BigDecimal defaultValue) {
		Object value = getProperty(key);

		if (value == null) {
			return defaultValue;
		} else {
			try {
				return PropertyConverter.toBigDecimal(value);
			} catch (ConversionException e) {
				throw new ConversionException('\'' + key + "' doesn't map to a BigDecimal object", e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see #setThrowExceptionOnMissing(boolean)
	 */
	@Override
	public BigInteger getBigInteger(String key) {
		BigInteger number = getBigInteger(key, null);
		if (number != null) {
			return number;
		} else {
			return null;
		}
	}

	@Override
	public BigInteger getBigInteger(String key, BigInteger defaultValue) {
		Object value = getProperty(key);

		if (value == null) {
			return defaultValue;
		} else {
			try {
				return PropertyConverter.toBigInteger(value);
			} catch (ConversionException e) {
				throw new ConversionException('\'' + key + "' doesn't map to a BigDecimal object", e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see #setThrowExceptionOnMissing(boolean)
	 */
	@Override
	public String getString(String key) {
		String s = getString(key, null);
		if (s != null) {
			return s;
		} else {
			return null;
		}
	}

	@Override
	public String getString(String key, String defaultValue) {
		Object value = getProperty(key);

		if (value instanceof String) {
			return (String) value;
		} else if (value == null) {
			return defaultValue;
		} else {
			throw new ConversionException('\'' + key + "' doesn't map to a String object");
		}
	}

	/**
	 * Get an array of strings associated with the given configuration key. If
	 * the key doesn't map to an existing object, an empty array is returned. If
	 * a property is added to a configuration, it is checked whether it contains
	 * multiple values. This is obvious if the added object is a list or an
	 * array. For strings it is checked whether the string contains the list
	 * delimiter character that can be specified using the
	 * <code>setListDelimiter()</code> method. If this is the case, the string
	 * is splitted at these positions resulting in a property with multiple
	 * values.
	 * 
	 * @param key
	 *            The configuration key.
	 * @return The associated string array if key is found.
	 * 
	 * @throws ConversionException
	 *             is thrown if the key maps to an object that is not a
	 *             String/List of Strings.
	 * @see #setListDelimiter(char)
	 * @see #setDelimiterParsingDisabled(boolean)
	 */
	@Override
	public String[] getStringArray(String key) {
		Object value = getProperty(key);

		String[] array;

		if (value instanceof String) {
			array = new String[1];

			array[0] = (String) value;
		} else if (value instanceof List) {
			List list = (List) value;
			array = new String[list.size()];

			for (int i = 0; i < array.length; i++) {
				array[i] = (String) list.get(i);
			}
		} else if (value == null) {
			array = new String[0];
		} else {
			throw new ConversionException('\'' + key + "' doesn't map to a String/List object");
		}
		return array;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see #getStringArray(String)
	 */
	@Override
	public List getList(String key) {
		return getList(key, new ArrayList());
	}

	@Override
	public List getList(String key, List defaultValue) {
		Object value = getProperty(key);
		List list;

		if (value instanceof String) {
			list = new ArrayList(1);
			list.add((String) value);
		} else if (value instanceof List) {
			list = new ArrayList();
			List l = (List) value;

			// add the interpolated elements in the new list
			Iterator it = l.iterator();
			while (it.hasNext()) {
				list.add(it.next());
			}
		} else if (value == null) {
			list = defaultValue;
		} else if (value.getClass().isArray()) {
			return Arrays.asList((Object[]) value);
		} else {
			throw new ConversionException('\'' + key + "' doesn't map to a List object: " + value + ", a " + value.getClass().getName());
		}
		return list;
	}

	@Override
	public Configuration subset(String prefix) {
		return new CasSubsetConfiguration(this, prefix, ".");
	}

	@Override
	public Properties getProperties(String key) {
		throw new NotImplementedException();
	}
}
