package com.inmobi.adserve.channels.server;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;
import com.inmobi.messaging.publisher.AbstractMessagePublisher;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.repository.ChannelAdGroupRepository;
import com.inmobi.adserve.channels.util.ConfigurationLoader;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.DebugLogger;

public class ServerTest extends TestCase {

  private HttpRequestHandler httpRequestHandler;
  private Configuration mockConfig = null;
  private static ConfigurationLoader config;
  private String debug = "debug";
  private String key = "2";
  private String keyType = "HmacSha1Crc";
  private String keyValue = "SystemManagerScottTiger";
  private int ipFileVersion = 2;
  private String clickURLPrefix = "http://c2.w.inmobi.com/c.asm";
  private String beaconURLPrefix = "http://c3.w.inmobi.com/c.asm";
  private String secretKeyVersion = "1";
  private SASRequestParameters sasParam;
  private String loggerConf = "target/channel-server.properties";
  private static String rrFile = "";
  private static String channelFile = "";
  private static int count = 0;
  private static int percentRollout = 100;
  private DebugLogger logger;
  private static List<String> siteType = Arrays.asList("FAMILYSAFE", "PERFORMANCE", "MATURE");

  public void setUp() throws Exception {
    if(count == 0) {
      prepareLogging();
      config = ConfigurationLoader.getInstance("/opt/mkhoj/conf/cas/channel-server.properties");
      count++;
    }
    prepareConfig();
    DebugLogger.init(mockConfig);
    logger = new DebugLogger();
    InspectorStats.initializeWorkflow("WorkFlow");
    ServletHandler.init(config, null);
    httpRequestHandler = new HttpRequestHandler();
    AbstractMessagePublisher mockAbstractMessagePublisher = createMock(AbstractMessagePublisher.class);
    Logging.init(mockAbstractMessagePublisher, "cas-rr", "cas-channel", "cas-advertisement", mockConfig);
  }

  public void prepareLogging() throws Exception {
    FileWriter fstream = new FileWriter("target/channel-server.properties");
    BufferedWriter out = new BufferedWriter(fstream);
    out.write("log4j.logger.app = DEBUG, channel\n");
    out.write("log4j.additivity.app = false\n");
    out.write("log4j.appender.channel=org.apache.log4j.DailyRollingFileAppender\n");
    out.write("log4j.appender.channel.layout=org.apache.log4j.PatternLayout\n");
    out.write("log4j.appender.channel.DatePattern='.'yyyy-MM-dd-HH\n");
    channelFile = "/tmp/channel.log." + System.currentTimeMillis();
    out.write("log4j.appender.channel.File=" + channelFile + "\n");

    out.write("log4j.logger.app = DEBUG, rr\n");
    out.write("log4j.additivity.rr.app = false\n");
    out.write("log4j.appender.rr=org.apache.log4j.DailyRollingFileAppender\n");
    out.write("log4j.appender.rr.layout=org.apache.log4j.PatternLayout\n");
    out.write("log4j.appender.rr.DatePattern='.'yyyy-MM-dd-HH\n");
    System.out.println("here rr file name is " + rrFile);
    rrFile = "/tmp/rr.log." + System.currentTimeMillis();
    out.write("log4j.appender.rr.File=" + rrFile + "\n");
    out.write("log4j.category.rr=DEBUG,rr\n");
    out.write("log4j.category.channel=DEBUG,channel\n");
    out.write("server.percentRollout=100 \nserver.siteType=PERFORMANCE,FAMILYSAFE,MATURE\n");
    out.write("server.enableDatabusLogging=true\nserver.enableFileLogging=true\n");
    out.write("server.maxconnections=100");
    out.close();
  }

  public void prepareConfig() {
    mockConfig = createMock(Configuration.class);
    expect(mockConfig.getString("debug")).andReturn(debug).anyTimes();
    expect(mockConfig.getInt("clickmaker.ipFileVersion")).andReturn(ipFileVersion).anyTimes();
    expect(mockConfig.getString("clickmaker.clickURLHashingSecretKeyVersion")).andReturn(secretKeyVersion).anyTimes();
    expect(mockConfig.getString("clickmaker.key.1.value")).andReturn(keyValue).anyTimes();
    expect(mockConfig.getString("clickmaker.clickURLPrefix")).andReturn(clickURLPrefix).anyTimes();
    expect(mockConfig.getString("clickmaker.beaconURLPrefix")).andReturn(beaconURLPrefix).anyTimes();
    expect(mockConfig.getString("clickmaker.key.1.type")).andReturn(keyType).anyTimes();
    expect(mockConfig.getString("clickmaker.key")).andReturn(key).anyTimes();
    expect(mockConfig.getString("loggerConf")).andReturn(loggerConf).anyTimes();
    expect(mockConfig.getString("rr")).andReturn("rr").anyTimes();
    expect(mockConfig.getString("channel")).andReturn("channel").anyTimes();
    expect(mockConfig.getInt("percentRollout")).andReturn(percentRollout).anyTimes();
    expect(mockConfig.getList("siteType")).andReturn(siteType).anyTimes();
    expect(mockConfig.getBoolean("enableFileLogging")).andReturn(true).anyTimes();
    expect(mockConfig.getBoolean("enableDatabusLogging")).andReturn(true).anyTimes();
    expect(mockConfig.getInt("sampledadvertisercount")).andReturn(3).anyTimes();
    replay(mockConfig);
  }

  public JSONObject prepareParameters() throws Exception {
    JSONObject args = new JSONObject();
    sasParam = new SASRequestParameters();
    sasParam.setAge("35");
    sasParam.setGender("m");
    sasParam.setImpressionId("4f8d98e2-4bbd-40bc-8729-22da000900f9");
    int myarr[] = { 1, 2 };
    long category[] = { 1, 2 };
    long site[] = { 334, 50 };
    HashMap<String, String> userParams = new HashMap<String, String>();
    userParams.put("u-gender", "m");
    userParams.put("u-age", "35");
    args.put("uparams", new JSONObject(userParams));
    args.put("testarr", new JSONArray(myarr));
    args.put("category", new JSONArray(category));
    args.put("site", new JSONArray(site));
    args.put("tid", "4f8d98e2-4bbd-40bc-87cf-22da572032f9");
    args.put("remoteHostIp", "10.14.110.100");
    return args;
  }

  @Test
  public void testStringify() throws Exception {
    DebugLogger logger = new DebugLogger();
    JSONObject jsonObject = prepareParameters();
    assertEquals(RequestParser.stringify(jsonObject, "remoteHostIp", logger), "10.14.110.100");
  }

  @Test
  public void testResponseFormat() throws Exception {
    assertEquals(httpRequestHandler.responseSender.getResponseFormat(), "html");
  }

  @Test
  public void testParseArray() throws Exception {
    JSONObject jsonObject = prepareParameters();
    assertEquals(RequestParser.parseArray(jsonObject, "testarr", 1), "2");
  }

  @Test
  public void testGetUserParams() throws Exception {
    JSONObject jsonObject = prepareParameters();
    SASRequestParameters params = new SASRequestParameters();
    DebugLogger logger = new DebugLogger();
    params = RequestParser.getUserParams(params, jsonObject, logger);
    assertEquals(params.getAge(), "35");
  }

  /*
   * @Test public void testImpressionId() throws Exception { JSONObject
   * jsonObject = prepareParameters();
   * assertEquals(httpRequestHandler.getImpressionId(jsonObject, 2345l),
   * "4f8d98e2-4bbd-40bc-8729-22da000900f9"); }
   */
  @Test
  public void testGetCategory() throws Exception {
    JSONObject jsonObject = prepareParameters();
    Long[] category = { 1l, 2l };
    assertTrue("Category are expected to be equal",
        RequestParser.getCategory(jsonObject, new DebugLogger(), "category").equals(Arrays.asList(category)));
  }

  /*
   * @Test public void testRRLogging() throws Exception { JSONObject jObject =
   * prepareParameters(); DebugLogger logger; Logging.rrLogging(jObject, null,
   * logger, mockConfig, sasParam, "NO"); File fis = new File(rrFile);
   * assertTrue(fis.length() != 0); fis.delete(); }
   * 
   * @Test public void testChannelLogging() throws Exception { JSONObject
   * jObject = prepareParameters(); DebugLogger logger;
   * Logging.rrLogging(jObject, null, logger, mockConfig, sasParam, "NO"); File
   * fis = new File(rrFile); assertTrue(fis.length() != 0); fis.delete(); }
   */
}
