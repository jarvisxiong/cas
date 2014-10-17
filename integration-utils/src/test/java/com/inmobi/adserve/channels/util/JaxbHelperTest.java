package com.inmobi.adserve.channels.util;

import static org.junit.Assert.assertNotNull;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.junit.Test;


/**
 * The class <code>JaxbHelperTest</code> contains tests for the class <code>{@link JaxbHelper}</code>.
 * 
 * @generatedBy CodePro at 4/10/14 9:04 PM
 * @author abhishek.parwal
 * @version $Revision: 1.0 $
 */
public class JaxbHelperTest {
  /**
   * Run the JaxbHelper() constructor test.
   * 
   * @generatedBy CodePro at 4/10/14 9:04 PM
   */
  @Test
  public void testJaxbHelper_1() throws Exception {
    final JaxbHelper result = new JaxbHelper();
    assertNotNull(result);
  }

  /**
   * Run the GenericKeyedObjectPoolConfig getPoolConfig() method test.
   * 
   * @throws Exception
   * 
   * @generatedBy CodePro at 4/10/14 9:04 PM
   */
  @Test
  public void testGetPoolConfig_1() throws Exception {
    final JaxbHelper fixture = new JaxbHelper();

    final GenericKeyedObjectPoolConfig result = fixture.getPoolConfig();

    assertNotNull(result);
  }

  /**
   * Run the String marshal(T) method test.
   * 
   * @throws Exception
   * 
   * @generatedBy CodePro at 4/10/14 9:04 PM
   */
  @Test
  public void testMarshal_1() throws Exception {
    final JaxbHelper fixture = new JaxbHelper();

    final Customer customer = new Customer();
    customer.setId(10);
    customer.setName("abhishek parwal");

    final String result = fixture.marshal(customer);

    assertNotNull(result);
  }

  /**
   * Run the Object unmarshal(String,Class<T>) method test.
   * 
   * @throws Exception
   * 
   * @generatedBy CodePro at 4/10/14 9:04 PM
   */
  @Test
  public void testUnmarshal_1() throws Exception {
    final JaxbHelper fixture = new JaxbHelper();
    final String data = "<customer id=\"100\"><name>abhishek</name></customer>";

    final Object result = fixture.unmarshal(data, Customer.class);

    assertNotNull(result);
  }

  /**
   * Run the Object unmarshal(String,Class<T>) method test.
   * 
   * @throws Exception
   * 
   * @generatedBy CodePro at 4/10/14 9:04 PM
   */
  @Test(expected = Exception.class)
  public void testUnmarshal_2() throws Exception {
    final JaxbHelper fixture = new JaxbHelper();
    final String data = "<ad id=\"100\"><name>abhishek</name></ad>";

    final Object result = fixture.unmarshal(data, Customer.class);

    assertNotNull(result);
  }

  @XmlRootElement
  public static class Customer {

    private String name;
    private int id;

    public String getName() {
      return name;
    }

    @XmlElement
    public void setName(final String name) {
      this.name = name;
    }

    public int getId() {
      return id;
    }

    @XmlAttribute
    public void setId(final int id) {
      this.id = id;
    }

  }
}
