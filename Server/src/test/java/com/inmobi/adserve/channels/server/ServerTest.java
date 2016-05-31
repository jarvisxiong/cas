package com.inmobi.adserve.channels.server;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;

import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.server.requesthandler.Logging;
import com.inmobi.adserve.channels.server.requesthandler.ResponseFormat;
import com.inmobi.adserve.channels.server.requesthandler.ResponseSender;
import com.inmobi.adserve.channels.util.ConfigurationLoader;
import com.inmobi.messaging.publisher.AbstractMessagePublisher;

import junit.framework.TestCase;

public class ServerTest extends TestCase {
    private ResponseSender responseSender;
    private Configuration mockConfig = null;
    private static ConfigurationLoader config;
    private final String debug = "debug";
    private final String key = "2";
    private final String keyType = "HmacSha1Crc";
    private final String keyValue = "SystemManagerScottTiger";
    private final int ipFileVersion = 2;
    private final String clickURLPrefix = "http://c2.w.inmobi.com/c.asm";
    private final String beaconURLPrefix = "http://c3.w.inmobi.com/c.asm";
    private final String secretKeyVersion = "1";
    private SASRequestParameters sasParam;
    private static String rrFile = StringUtils.EMPTY;
    private static String channelFile = StringUtils.EMPTY;
    private static int count = 0;
    private static int percentRollout = 100;
    private static List<String> siteType = Arrays.asList("FAMILYSAFE", "PERFORMANCE", "MATURE");

    @Override
    public void setUp() throws Exception {
        if (count == 0) {
            prepareLogging();
            config = ConfigurationLoader.getInstance("channel-server.properties");
            count++;
        }
        prepareConfig();
        CasConfigUtil.init(config, null);

        ResponseSender.setTraceMarkerProvider(null);
        responseSender = new ResponseSender();

        final AbstractMessagePublisher mockAbstractMessagePublisher = createMock(AbstractMessagePublisher.class);
        Logging.init(mockAbstractMessagePublisher, "cas-rr", "cas-advertisement", "null", mockConfig, "hostName", "corp");
    }

    public void prepareLogging() throws Exception {
        final FileWriter fstream = new FileWriter("target/channel-server.properties");
        final BufferedWriter out = new BufferedWriter(fstream);
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
        expect(mockConfig.getString("clickmaker.clickURLHashingSecretKeyVersion")).andReturn(secretKeyVersion)
                .anyTimes();
        expect(mockConfig.getString("clickmaker.key.1.value")).andReturn(keyValue).anyTimes();
        expect(mockConfig.getString("clickmaker.clickURLPrefix")).andReturn(clickURLPrefix).anyTimes();
        expect(mockConfig.getString("clickmaker.beaconURLPrefix")).andReturn(beaconURLPrefix).anyTimes();
        expect(mockConfig.getString("clickmaker.key.1.type")).andReturn(keyType).anyTimes();
        expect(mockConfig.getString("clickmaker.key")).andReturn(key).anyTimes();
        expect(mockConfig.getString("slf4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/logger.xml");
        expect(mockConfig.getString("log4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties");
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
        final JSONObject args = new JSONObject();
        sasParam = new SASRequestParameters();
        sasParam.setAge((short) 35);
        sasParam.setGender("m");
        sasParam.setImpressionId("4f8d98e2-4bbd-40bc-8729-22da000900f9");
        final int myarr[] = {1, 2};
        final long category[] = {1, 2};
        final long site[] = {334, 50};
        final HashMap<String, String> userParams = new HashMap<String, String>();
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
    public void testResponseFormat() throws Exception {
        assertEquals(responseSender.getResponseFormat(), ResponseFormat.HTML);
    }

}
