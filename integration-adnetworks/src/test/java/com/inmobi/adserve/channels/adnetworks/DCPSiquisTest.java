package com.inmobi.adserve.channels.adnetworks;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.configuration.Configuration;
import org.easymock.EasyMock;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;

import com.google.common.collect.Maps;
import com.inmobi.adserve.channels.adnetworks.siquis.DCPSiquisAdNetwork;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;


/**
 * @author tushara
 * 
 */
public class DCPSiquisTest extends TestCase {
    private Configuration         mockConfig         = null;
    private final String          debug              = "debug";

    private DCPSiquisAdNetwork    dcpSiquisAdNetwork;
    private final String          siquisHost         = "http://api.adsquare.co.kr/api/ovmob_sh.php?charset=UTF-8";
    private final String          status             = "on";
    private final String          responseFormat     = "html";
    private final String          siquisAdvertiserId = "54321";
    private final String          testFlag           = "0";
    private final String          partnerId          = "inmobikorea";

    private final ClientBootstrap clientBootstrap    = null;

    public void prepareMockConfig() {
        mockConfig = EasyMock.createMock(Configuration.class);
        expect(mockConfig.getString("siquis.host")).andReturn(siquisHost).anyTimes();
        expect(mockConfig.getString("siquis.status")).andReturn(status).anyTimes();
        expect(mockConfig.getString("siquis.responseFormat")).andReturn(responseFormat).anyTimes();
        expect(mockConfig.getString("siquis.advertiserId")).andReturn(siquisAdvertiserId).anyTimes();
        expect(mockConfig.getString("siquis.test")).andReturn(testFlag).anyTimes();
        expect(mockConfig.getString("siquis.partnerId")).andReturn(partnerId).anyTimes();
        expect(mockConfig.getString("debug")).andReturn(debug).anyTimes();
        expect(mockConfig.getString("slf4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/logger.xml");
        expect(mockConfig.getString("log4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties");
        EasyMock.replay(mockConfig);
    }

    @Override
    public void setUp() throws Exception {
        File f;
        f = new File("/tmp/channel-server.properties");
        if (!f.exists()) {
            f.createNewFile();
        }
        MessageEvent serverEvent = createMock(MessageEvent.class);
        HttpRequestHandlerBase base = createMock(HttpRequestHandlerBase.class);
        prepareMockConfig();
        Formatter.init();
        dcpSiquisAdNetwork = new DCPSiquisAdNetwork(mockConfig, clientBootstrap, base, serverEvent);
    }

    @Test
    public void testSiquisConfigureParameters() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            siquisAdvertiserId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true,
            null, null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(
            dcpSiquisAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, null), true);
    }

    @Test
    public void testSiquisConfigureParametersBlankUserAgent() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(" ");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            siquisAdvertiserId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true,
            null, null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(
            dcpSiquisAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, null), false);
    }

    @Test
    public void testSiquisConfigureParametersExternalKey() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(" ");

        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = " ";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            siquisAdvertiserId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true,
            null, null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(
            dcpSiquisAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, null), false);
    }

    @Test
    public void testSiquisRequestUri() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        casInternalRequestParameters.uid = "123";
        sasParams.setUserAgent("Mozilla");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";

        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            siquisAdvertiserId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true,
            null, null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        dcpSiquisAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null);

        String actualUrl = dcpSiquisAdNetwork.getRequestUri().toString();

        String expectedUrl = "http://api\\.adsquare\\.co\\.kr/api/ovmob_sh.php\\?charset=UTF-8&app_id=f6wqjq1r5v&device_id=123&partner_id=inmobikorea";

        assertEquals(actualUrl.matches(expectedUrl), true);
    }

    // @Test
    // public void testSiquisRequestUriBlankUid() throws Exception
    // {
    //
    // SASRequestParameters sasParams = new
    // SASRequestParameters();CasInternalRequestParameters
    // casInternalRequestParameters = new CasInternalRequestParameters();
    // sasParams.setRemoteHostIp("206.29.182.240");
    // sasParams.setUserAgent("Mozilla");
    // sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
    // String externalKey = "f6wqjq1r5v";
    // String clurl =
    // "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
    // dcpSiquisAdNetwork.configureParameters(sasParams,
    // casInternalRequestParameters, externalKey, clurl);
    //
    // String actualUrl = dcpSiquisAdNetwork.getRequestUri().toString();
    //
    // System.out.println(actualUrl);
    //
    // String expectedUrl =
    // "http://api\\.adsquare\\.co\\.kr/api/ovmob_sh.php\\?charset=UTF-8&app_id=f6wqjq1r5v&device_id=7a31d90b848f70062e6281b058cc52fc&partner_id=inmobikorea";
    //
    // assertEquals(actualUrl.matches(expectedUrl), true);
    //
    // //
    // "http://api\\.yp\\.com/display/v1/ad\\?apikey=f6wqjq1r5v&ip=206\\.29\\.182\\.240&useragent=Mozilla&loc=37\\.4429:-122\\.1514&listingcount=1&visitorid=";
    // // /String expectedUrlSuffix =
    // //
    // "&clkpxl=http%3A%2F%2Fc2\\.w\\.inmobi\\.com%2Fc\\.asm%2F4%2Fb%2Fbx5%2Fyaz%2F2%2Fb%2Fa5%2Fm%2F0%2F0%2F0%2F202cb962ac59075b964b07152d234b70%2F4f8d98e2-4bbd-40bc-87e5-22da170600f9%2F-1%2F1%2F9cddca11%3Fds%3D1";
    // // assertEquals(actualUrl.matches(expectedUrlPrefix + "\\d+" +
    // expectedUrlSuffix), true);
    // }

    @Test
    public void testSiquisResponse() throws Exception {

        Map<String, String> testMap = Maps.newHashMap();

        testMap.put("title", "Ä¯ khamm");
        testMap
                .put(
                    "description",
                    "ÇÁ¸®¹Ì¾î È£ÅÚÄ§±¸, ½ÉÇÃº£µù, ±¸½º´Ù¿î, Ä¿Æ°, Äí¼Ç, ÇÚµå¸ÞÀÌµå ÆÐºê¸¯.");
        testMap
                .put(
                    "clickurl",
                    "http://click.adsquare.co.kr/click/ovmob_click.php?url=http%3A%2F%2Frc.us-west.srv.overture.com%2Fd%2Fsr%2F%3Fxargs%3D20AcvbtslD4B5-lkI1OH60WpHQjOMqSILVcSjSbJ2u3yf6MAOF3BvwZocMreqwcBcdBJ");
        JSONObject json = new JSONObject(testMap);
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(json);
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            siquisAdvertiserId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true,
            null, null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        dcpSiquisAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null);
        dcpSiquisAdNetwork.parseResponse(jsonArray.toString(), HttpResponseStatus.OK);
    }
}