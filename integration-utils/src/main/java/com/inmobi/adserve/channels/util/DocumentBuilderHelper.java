package com.inmobi.adserve.channels.util;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.google.inject.Singleton;


/**
 * @author abhishek.parwal
 * 
 */
@Singleton
public class DocumentBuilderHelper {

  private GenericObjectPoolConfig poolConfig = getPoolConfig();

  private final GenericObjectPool<DocumentBuilder> documentBuilderPool = new GenericObjectPool<DocumentBuilder>(
      new DocumentBuilderFactoryPool(), poolConfig);

  public GenericObjectPoolConfig getPoolConfig() {
    poolConfig = new GenericObjectPoolConfig();
    poolConfig.setMaxTotal(GenericKeyedObjectPoolConfig.DEFAULT_MAX_TOTAL);
    poolConfig.setBlockWhenExhausted(false);
    poolConfig.setMaxWaitMillis(0);
    poolConfig.setTestWhileIdle(false);
    poolConfig.setTestOnBorrow(true);
    return poolConfig;
  }

  private class DocumentBuilderFactoryPool extends BasePooledObjectFactory<DocumentBuilder> {

    @Override
    public DocumentBuilder create() throws Exception {

      final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      return factory.newDocumentBuilder();
    }

    @Override
    public PooledObject<DocumentBuilder> wrap(final DocumentBuilder obj) {
      return new DefaultPooledObject<DocumentBuilder>(obj);
    }

  }

  /**
   * parses the xml and returns the Document
   * 
   * @param data
   * @return
   * @throws Exception
   */
  public Document parse(final String data) throws Exception {

    final DocumentBuilder documentBuilder = documentBuilderPool.borrowObject();

    try {
      final Document document = documentBuilder.parse(new InputSource(new StringReader(data)));
      documentBuilderPool.returnObject(documentBuilder);
      return document;
    } catch (final Exception e) {
      documentBuilderPool.invalidateObject(documentBuilder);
      throw new RuntimeException(e);
    }

  }
}
