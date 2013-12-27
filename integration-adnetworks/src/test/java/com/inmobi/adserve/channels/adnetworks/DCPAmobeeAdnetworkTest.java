package com.inmobi.adserve.channels.adnetworks;

import com.inmobi.adserve.channels.adnetworks.amobee.DCPAmobeeAdnetwork;
import com.inmobi.adserve.channels.api.*;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.util.DebugLogger;
import junit.framework.TestCase;
import org.apache.commons.configuration.Configuration;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;


public class DCPAmobeeAdnetworkTest extends TestCase {
    private Configuration         mockConfig        = null;
    private final String          debug             = "debug";
    private final String          loggerConf        = "/tmp/channel-server.properties";
    private final ClientBootstrap clientBootstrap   = null;
    private DebugLogger           logger;

    private DCPAmobeeAdnetwork    dcpAmobeeAdNetwork;
    private final String          amobeeHost        = "http://pulse-serving.amobee.com/upsteed/wap/adrequest";
    private final String          amobeeStatus      = "on";
    private final String          amobeeAdvId       = "amobeeadv1";
    private final String          amobeeTest        = "1";
    private final String          amobeeAdNetworkId = "test";

    public void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("amobee.host")).andReturn(amobeeHost).anyTimes();
        expect(mockConfig.getString("amobee.status")).andReturn(amobeeStatus).anyTimes();
        expect(mockConfig.getString("amobee.test")).andReturn(amobeeTest).anyTimes();
        expect(mockConfig.getString("amobee.advertiserId")).andReturn(amobeeAdvId).anyTimes();
        expect(mockConfig.getString("amobee.adnetworkId")).andReturn(amobeeAdNetworkId).anyTimes();
        expect(mockConfig.getString("debug")).andReturn(debug).anyTimes();
        expect(mockConfig.getString("slf4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/logger.xml");
        expect(mockConfig.getString("log4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties");
        replay(mockConfig);
    }

    @Override
    public void setUp() throws Exception {
        File f;
        f = new File(loggerConf);
        if (!f.exists()) {
            f.createNewFile();
        }
        MessageEvent serverEvent = createMock(MessageEvent.class);
        HttpRequestHandlerBase base = createMock(HttpRequestHandlerBase.class);
        prepareMockConfig();
        SlotSizeMapping.init();
        DebugLogger.init(mockConfig);
        Formatter.init();
        logger = new DebugLogger();
        dcpAmobeeAdNetwork = new DCPAmobeeAdnetwork(logger, mockConfig, clientBootstrap, base, serverEvent);
    }

    @Test
    public void testDCPAdelphicConfigureParameters() throws JSONException {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setSlot((short)11);
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        casInternalRequestParameters.uid = "23e2ewq445545";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            amobeeAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                    "{\"spot\":54235,\"pubId\":\"inmobi_1\"," + "\"site\":1234}"), new ArrayList<Integer>(), 0.0d,
            null, null, 32));
        assertEquals(true,
            dcpAmobeeAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPAmobeeConfigureParametersBlankIP() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            amobeeAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(false,
            dcpAmobeeAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPAmobeeConfigureParametersAdditionalParams() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            amobeeAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(false,
            dcpAmobeeAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPAmobeeConfigureParametersBlankUA() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(" ");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            amobeeAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(false,
            dcpAmobeeAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPAmobeeRequestUri() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.blockedCategories = new ArrayList<Long>(Arrays.asList(new Long[] { 50l, 51l }));
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("APP");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        List<Long> category = new ArrayList<Long>();
        category.add(3l);
        sasParams.setCategories(category);
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot((short)15);
        String externalKey = "0344343";
        SlotSizeMapping.init();
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            amobeeAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                    "{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"), new ArrayList<Integer>(), 0.0d,
            null, null, 0));
        if (dcpAmobeeAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, null)) {
            String actualUrl = dcpAmobeeAdNetwork.getRequestUri().toString();
            String t = actualUrl.substring(actualUrl.indexOf("&t=") + 3, actualUrl.indexOf("&tp="));
            String expectedUrl = "http://pulse-serving.amobee.com/upsteed/wap/adrequest?adn=test&site=00000000-0000-0000-0000-000000000000&ua=Mozilla&i=206.29.182.240&f=MobileBanner&uid=202cb962ac59075b964b07152d234b70&t=<time>&tp=4&kw=Business&adw=320&adh=50&lat=37.4429&long=-122.1514&nk=Law%2CGovermentt+%26+Politics%2CImmigration%2CLegal+Issues%2CU.S.+Government+Resources%2CPolitics%2CCommentary%2CNews%2CInternational+News%2CNational+News%2CLocal+News%2CHedge+Fund%2CInvesting%2CDating%2CDivorce+Support%2CGay+Life%2CParanormal+Phenomena%2CHunting%2FShooting%2CPagan%2FWiccan%2CAtheism%2FAgnosticism%2CLatter-Day+Saints%2CNon-Standard+Content%2CUnmoderated+UGC%2CExtreme+Graphic%2FExplicit+Violence%2CPornography%2CProfane+Content%2CHate+Content%2CIncentivized%2CIllegal+Content%2CIllegal+Content%2CWarez%2CSpyware%2FMalware%2CCopyright+Infringement%2CAdult+Education%2CPregnancy%2CHealth+%26+Fitness%2CBrain+Tumor%2CCancer%2CCholesterol%2CChronic+Fatigue+Syndrome%2CChronic+Pain%2CDeafness%2CDepression%2CDermatology%2CA.D.D.%2CDiabetes%2CEpilepsy%2CGERD%2FAcid+Reflux%2CHeart+Disease%2CHerbs+for+Health%2CIBS%2FCrohn%27s+Disease%2CIncest%2FAbuse+Support%2CIncontinence%2CAIDS%2FHIV%2CInfertility%2CMen%27s+Health%2CPanic%2FAnxiety+Disorders%2CPhysical+Therapy%2CPsychology%2FPsychiatry%2CSenor+Health%2CSexuality%2CAllergies%2CSleep+Disorders%2CSmoking+Cessation%2CWeight+Loss%2CWomen%27s+Health%2CAlternative+Medicine%2CArthritis%2CAutism%2FPDD%2CBipolar+Disorder%2CCocktails%2FBeer%2CCigars";
            assertEquals(expectedUrl.replace("<time>", t), actualUrl);
        }
    }

    @Test
    public void testDCPAmobeeRequestUriBlankLatLong() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("WAP");
        casInternalRequestParameters.blockedCategories = new ArrayList<Long>(Arrays.asList(new Long[] { 50l, 51l }));
        casInternalRequestParameters.latLong = "38.5,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot((short)15);
        List<Long> category = new ArrayList<Long>();
        category.add(3l);
        category.add(2l);
        sasParams.setCategories(category);

        String externalKey = "01212121";
        SlotSizeMapping.init();
        String clurl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                + "-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            amobeeAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                    "{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"), new ArrayList<Integer>(), 0.0d,
            null, null, 0));
        if (dcpAmobeeAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null)) {
            String actualUrl = dcpAmobeeAdNetwork.getRequestUri().toString();
            String t = actualUrl.substring(actualUrl.indexOf("&t=") + 3, actualUrl.indexOf("&tp="));
            String expectedUrl = "http://pulse-serving.amobee.com/upsteed/wap/adrequest?adn=test&site=00000000-0000-0000-0000-000000000000&ua=Mozilla&i=206.29.182.240&f=MobileBanner&uid=202cb962ac59075b964b07152d234b70&t=<time>&tp=4&kw=Business%2CBooks+%26+Reference&adw=320&adh=50&lat=38.5&long=-122.1514&nk=Law%2CGovermentt+%26+Politics%2CImmigration%2CLegal+Issues%2CU.S.+Government+Resources%2CPolitics%2CCommentary%2CNews%2CInternational+News%2CNational+News%2CLocal+News%2CHedge+Fund%2CInvesting%2CDating%2CDivorce+Support%2CGay+Life%2CParanormal+Phenomena%2CHunting%2FShooting%2CPagan%2FWiccan%2CAtheism%2FAgnosticism%2CLatter-Day+Saints%2CNon-Standard+Content%2CUnmoderated+UGC%2CExtreme+Graphic%2FExplicit+Violence%2CPornography%2CProfane+Content%2CHate+Content%2CIncentivized%2CIllegal+Content%2CIllegal+Content%2CWarez%2CSpyware%2FMalware%2CCopyright+Infringement%2CAdult+Education%2CPregnancy%2CHealth+%26+Fitness%2CBrain+Tumor%2CCancer%2CCholesterol%2CChronic+Fatigue+Syndrome%2CChronic+Pain%2CDeafness%2CDepression%2CDermatology%2CA.D.D.%2CDiabetes%2CEpilepsy%2CGERD%2FAcid+Reflux%2CHeart+Disease%2CHerbs+for+Health%2CIBS%2FCrohn%27s+Disease%2CIncest%2FAbuse+Support%2CIncontinence%2CAIDS%2FHIV%2CInfertility%2CMen%27s+Health%2CPanic%2FAnxiety+Disorders%2CPhysical+Therapy%2CPsychology%2FPsychiatry%2CSenor+Health%2CSexuality%2CAllergies%2CSleep+Disorders%2CSmoking+Cessation%2CWeight+Loss%2CWomen%27s+Health%2CAlternative+Medicine%2CArthritis%2CAutism%2FPDD%2CBipolar+Disorder%2CCocktails%2FBeer%2CCigars";
            assertEquals(expectedUrl.replaceAll("<time>", t), actualUrl);

        }
    }

    @Test
    public void testDCPAmobeeParseAd() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.blockedCategories = new ArrayList<Long>(Arrays.asList(new Long[] { 50l, 51l }));
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSlot((short)15);
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                + "-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";

        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            amobeeAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                    "{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"), new ArrayList<Integer>(), 0.0d,
            null, null, 32));
        dcpAmobeeAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl);
        String response = "<div style='margin:0px; padding:0px;'><a target=\"_top\" href=\"http://ad.ipredictive.com/d/track/click?zid=inmobi_1_0_1_testkey&sid=bde34925-a0da-11e2-851d-f112587bd4c2&crid=424&adid=8&oid=55&cid=50&spid=322&pubid=58&ez_p=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA&rd=http%3A%2F%2Fapp.appsflyer.com%2Fid381840917%3Fpid%3Dadelphic_int%26c%3DAdelphic%26clickid%3Dbde34925-a0da-11e2-851d-f112587bd4c2%253A424%253A58%253A8%253A50%253A55%253Ainmobi_1_0_1_testkey\"><img src=\"http://ad.ipredictive.com/d/img/image?zid=inmobi_1_0_1_testkey&sid=bde34925-a0da-11e2-851d-f112587bd4c2&crid=424&adid=8&oid=55&cid=50&spid=322&pubid=58&ez_p=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA&rd=http%3A%2F%2Fd299n2tvhpuett.cloudfront.net%2Fimage%2Fadelphic%2Fadelphic5.png&rr=43207\" width=\"320\" height=\"50\"></img></a></div>";
        dcpAmobeeAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(200, dcpAmobeeAdNetwork.getHttpResponseStatusCode());
        assertEquals(
            "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"mraid.js\"></script><div style='margin:0px; padding:0px;'><a target=\"_top\" href=\"http://ad.ipredictive.com/d/track/click?zid=inmobi_1_0_1_testkey&sid=bde34925-a0da-11e2-851d-f112587bd4c2&crid=424&adid=8&oid=55&cid=50&spid=322&pubid=58&ez_p=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA&rd=http%3A%2F%2Fapp.appsflyer.com%2Fid381840917%3Fpid%3Dadelphic_int%26c%3DAdelphic%26clickid%3Dbde34925-a0da-11e2-851d-f112587bd4c2%253A424%253A58%253A8%253A50%253A55%253Ainmobi_1_0_1_testkey\"><img src=\"http://ad.ipredictive.com/d/img/image?zid=inmobi_1_0_1_testkey&sid=bde34925-a0da-11e2-851d-f112587bd4c2&crid=424&adid=8&oid=55&cid=50&spid=322&pubid=58&ez_p=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA&rd=http%3A%2F%2Fd299n2tvhpuett.cloudfront.net%2Fimage%2Fadelphic%2Fadelphic5.png&rr=43207\" width=\"320\" height=\"50\"></img></a></div><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>",
            dcpAmobeeAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPAmobeeParseAppAd() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.blockedCategories = new ArrayList<Long>(Arrays.asList(new Long[] { 50l, 51l }));
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSlot((short)15);
        sasParams.setSource("APP");
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                + "-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";

        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            amobeeAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                    "{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"), new ArrayList<Integer>(), 0.0d,
            null, null, 32));
        dcpAmobeeAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl);
        String response = "<div style='margin:0px; padding:0px;'><a target=\"_top\" href=\"http://ad.ipredictive.com/d/track/click?zid=inmobi_1_0_1_testkey&sid=bde34925-a0da-11e2-851d-f112587bd4c2&crid=424&adid=8&oid=55&cid=50&spid=322&pubid=58&ez_p=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA&rd=http%3A%2F%2Fapp.appsflyer.com%2Fid381840917%3Fpid%3Dadelphic_int%26c%3DAdelphic%26clickid%3Dbde34925-a0da-11e2-851d-f112587bd4c2%253A424%253A58%253A8%253A50%253A55%253Ainmobi_1_0_1_testkey\"><img src=\"http://ad.ipredictive.com/d/img/image?zid=inmobi_1_0_1_testkey&sid=bde34925-a0da-11e2-851d-f112587bd4c2&crid=424&adid=8&oid=55&cid=50&spid=322&pubid=58&ez_p=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA&rd=http%3A%2F%2Fd299n2tvhpuett.cloudfront.net%2Fimage%2Fadelphic%2Fadelphic5.png&rr=43207\" width=\"320\" height=\"50\"></img></a></div>";
        dcpAmobeeAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(200, dcpAmobeeAdNetwork.getHttpResponseStatusCode());
        assertEquals(
            "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"mraid.js\"></script><div style='margin:0px; padding:0px;'><a target=\"_top\" href=\"http://ad.ipredictive.com/d/track/click?zid=inmobi_1_0_1_testkey&sid=bde34925-a0da-11e2-851d-f112587bd4c2&crid=424&adid=8&oid=55&cid=50&spid=322&pubid=58&ez_p=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA&rd=http%3A%2F%2Fapp.appsflyer.com%2Fid381840917%3Fpid%3Dadelphic_int%26c%3DAdelphic%26clickid%3Dbde34925-a0da-11e2-851d-f112587bd4c2%253A424%253A58%253A8%253A50%253A55%253Ainmobi_1_0_1_testkey\"><img src=\"http://ad.ipredictive.com/d/img/image?zid=inmobi_1_0_1_testkey&sid=bde34925-a0da-11e2-851d-f112587bd4c2&crid=424&adid=8&oid=55&cid=50&spid=322&pubid=58&ez_p=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA&rd=http%3A%2F%2Fd299n2tvhpuett.cloudfront.net%2Fimage%2Fadelphic%2Fadelphic5.png&rr=43207\" width=\"320\" height=\"50\"></img></a></div><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>",
            dcpAmobeeAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPAmobeeParseNoAd() throws Exception {
        String response = "";
        dcpAmobeeAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(500, dcpAmobeeAdNetwork.getHttpResponseStatusCode());
    }

    @Test
    public void testDCPAmobeeParseEmptyResponseCode() throws Exception {
        String response = "";
        dcpAmobeeAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(500, dcpAmobeeAdNetwork.getHttpResponseStatusCode());
        assertEquals("", dcpAmobeeAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPAmobeeGetId() throws Exception {
        assertEquals(amobeeAdvId, dcpAmobeeAdNetwork.getId());
    }

    @Test
    public void testDCPAmobeeGetImpressionId() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                + "-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            amobeeAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                    "{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"), new ArrayList<Integer>(), 0.0d,
            null, null, 32));
        dcpAmobeeAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null);
        assertEquals("4f8d98e2-4bbd-40bc-8795-22da170700f9", dcpAmobeeAdNetwork.getImpressionId());
    }

    @Test
    public void testDCPAmobeeGetName() throws Exception {
        assertEquals("amobee", dcpAmobeeAdNetwork.getName());
    }

    @Test
    public void testDCPAmobeeIsClickUrlReq() throws Exception {
        assertEquals(false, dcpAmobeeAdNetwork.isClickUrlRequired());
    }
}
