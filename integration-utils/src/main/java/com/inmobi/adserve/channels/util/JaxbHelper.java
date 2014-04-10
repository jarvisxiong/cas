package com.inmobi.adserve.channels.util;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;

import com.google.common.collect.Maps;
import com.google.inject.Singleton;


/**
 * @author abhishek.parwal
 * 
 */
@Singleton
public class JaxbHelper {

    private GenericKeyedObjectPoolConfig                        poolConfig           = getPoolConfig();

    private final GenericKeyedObjectPool<PoolKey, Marshaller>   marshallerPool       = new GenericKeyedObjectPool<PoolKey, Marshaller>(
                                                                                             new MarshallerFactory(),
                                                                                             poolConfig);
    private final GenericKeyedObjectPool<PoolKey, Unmarshaller> unmarshallerPool     = new GenericKeyedObjectPool<PoolKey, Unmarshaller>(
                                                                                             new UnmarshallerFactory(),
                                                                                             poolConfig);
    private final Map<PoolKey, JAXBContext>                     poolKeyToJaxbContext = Maps.newHashMap();

    private final Object                                        lock                 = new Object();

    public GenericKeyedObjectPoolConfig getPoolConfig() {
        poolConfig = new GenericKeyedObjectPoolConfig();
        poolConfig.setMaxIdlePerKey(GenericKeyedObjectPoolConfig.DEFAULT_MAX_IDLE_PER_KEY);
        poolConfig.setMaxTotal(GenericKeyedObjectPoolConfig.DEFAULT_MAX_TOTAL);
        poolConfig.setMaxTotalPerKey(-1);
        poolConfig.setBlockWhenExhausted(false);
        poolConfig.setMaxWaitMillis(0);
        poolConfig.setTestWhileIdle(false);
        poolConfig.setTestOnBorrow(true);
        return poolConfig;
    }

    @EqualsAndHashCode
    @Data
    private class PoolKey {
        @SuppressWarnings("rawtypes")
        private final Class clazz;
    }

    private class MarshallerFactory extends BaseKeyedPooledObjectFactory<PoolKey, Marshaller> {

        @Override
        public Marshaller create(final PoolKey key) throws Exception {
            JAXBContext jaxbContext = poolKeyToJaxbContext.get(key);

            if (jaxbContext == null) {
                synchronized (lock) {
                    jaxbContext = poolKeyToJaxbContext.get(key);
                    if (jaxbContext == null) {
                        jaxbContext = JAXBContext.newInstance(key.getClazz());
                        poolKeyToJaxbContext.put(key, jaxbContext);
                    }

                }
            }

            return jaxbContext.createMarshaller();
        }

        @Override
        public PooledObject<Marshaller> wrap(final Marshaller marshaller) {
            return new DefaultPooledObject<Marshaller>(marshaller);
        }
    }

    private class UnmarshallerFactory extends BaseKeyedPooledObjectFactory<PoolKey, Unmarshaller> {

        @Override
        public Unmarshaller create(final PoolKey key) throws Exception {
            JAXBContext jaxbContext = poolKeyToJaxbContext.get(key);

            if (jaxbContext == null) {
                synchronized (lock) {
                    jaxbContext = poolKeyToJaxbContext.get(key);
                    if (jaxbContext == null) {
                        jaxbContext = JAXBContext.newInstance(key.getClazz());
                        poolKeyToJaxbContext.put(key, jaxbContext);
                    }
                }
            }

            return jaxbContext.createUnmarshaller();
        }

        @Override
        public PooledObject<Unmarshaller> wrap(final Unmarshaller unmarshaller) {
            return new DefaultPooledObject<Unmarshaller>(unmarshaller);
        }

    }

    public <T> String marshal(final T instance) throws Exception {
        StringWriter result = new StringWriter();

        PoolKey poolKey = new PoolKey(instance.getClass());
        Marshaller marshaller = marshallerPool.borrowObject(poolKey);

        try {
            marshaller.marshal(instance, result);

            marshallerPool.returnObject(poolKey, marshaller);

            return result.toString();
        }
        catch (Exception e) {
            marshallerPool.invalidateObject(poolKey, marshaller);
            throw new RuntimeException(e);
        }
    }

    /**
     * parses the data and returns the clazz object
     * 
     * @param data
     * @param clazz
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public <T> T unmarshal(final String data, final Class<T> clazz) throws Exception {
        T result;

        PoolKey poolKey = new PoolKey(clazz);
        Unmarshaller unmarshaller = unmarshallerPool.borrowObject(poolKey);

        try {
            // noinspection unchecked
            result = (T) unmarshaller.unmarshal(new StringReader(data));

            unmarshallerPool.returnObject(poolKey, unmarshaller);

            return result;
        }
        catch (Exception e) {
            unmarshallerPool.invalidateObject(poolKey, unmarshaller);
            throw new RuntimeException(e);
        }
    }
}
