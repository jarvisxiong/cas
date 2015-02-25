package com.inmobi.adserve.channels.adnetworks;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.awt.Dimension;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.configuration.Configuration;
import org.easymock.EasyMock;
import org.json.JSONException;
import org.testng.annotations.Test;

import com.inmobi.adserve.channels.adnetworks.amobeeplatform.DCPAmobeePlatformAdnetwork;
import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.IPRepository;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;


public class DCPAmobeePlatformAdnetworkTest extends TestCase {
    private Configuration mockConfig = null;
    private final String debug = "debug";
    private final String loggerConf = "/tmp/channel-server.properties";
    private final Bootstrap clientBootstrap = null;

    private DCPAmobeePlatformAdnetwork dcpDmgAdNetwork;
    private final String dmgHost = "http://ad.dmg-mobile.com/upsteed/wap/adrequest?acc=17067024";
    private final String dmgStatus = "on";
    private final String dmgAdvId = "dmgadv1";
    private final String dmgTest = "1";
    private final String dmgAdNetworkId = "test";
    private RepositoryHelper repositoryHelper;

    public void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("dmg.host")).andReturn(dmgHost).anyTimes();
        expect(mockConfig.getString("dmg.status")).andReturn(dmgStatus).anyTimes();
        expect(mockConfig.getString("dmg.test")).andReturn(dmgTest).anyTimes();
        expect(mockConfig.getString("dmg.advertiserId")).andReturn(dmgAdvId).anyTimes();
        expect(mockConfig.getString("dmg.adnetworkId")).andReturn(dmgAdNetworkId).anyTimes();
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
        final Channel serverChannel = createMock(Channel.class);
        final HttpRequestHandlerBase base = createMock(HttpRequestHandlerBase.class);
        prepareMockConfig();
        Formatter.init();
        final SlotSizeMapEntity slotSizeMapEntityFor4 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor4.getDimension()).andReturn(new Dimension(300, 50)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor4);
        final SlotSizeMapEntity slotSizeMapEntityFor9 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor9.getDimension()).andReturn(new Dimension(320, 48)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor9);
        final SlotSizeMapEntity slotSizeMapEntityFor11 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor11.getDimension()).andReturn(new Dimension(728, 90)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor11);
        final SlotSizeMapEntity slotSizeMapEntityFor14 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor14.getDimension()).andReturn(new Dimension(320, 480)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor14);
        final SlotSizeMapEntity slotSizeMapEntityFor15 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor15.getDimension()).andReturn(new Dimension(320, 50)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor15);
        repositoryHelper = EasyMock.createMock(RepositoryHelper.class);
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)4))
                .andReturn(slotSizeMapEntityFor4).anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)9))
                .andReturn(slotSizeMapEntityFor9).anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)11))
                .andReturn(slotSizeMapEntityFor11).anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)14))
                .andReturn(slotSizeMapEntityFor14).anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)15))
                .andReturn(slotSizeMapEntityFor15).anyTimes();
        EasyMock.replay(repositoryHelper);
        dcpDmgAdNetwork = new DCPAmobeePlatformAdnetwork(mockConfig, clientBootstrap, base, serverChannel);
        dcpDmgAdNetwork.setName("dmg");
        
        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);
        
        dcpDmgAdNetwork.setHost(dmgHost);
    }

    @Test
    public void testDCPDmgConfigureParameters() throws JSONException {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        casInternalRequestParameters.setUid("23e2ewq445545");
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(dmgAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(true,
                dcpDmgAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 11, repositoryHelper));
    }

    @Test
    public void testDCPDmgConfigureParametersBlankIP() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(dmgAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(false,
                dcpDmgAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 11, repositoryHelper));
    }

    @Test
    public void testDCPDmgConfigureParametersAdditionalParams() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(dmgAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(false,
                dcpDmgAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 11, repositoryHelper));
    }

    @Test
    public void testDCPDmgConfigureParametersBlankUA() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(" ");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(dmgAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(false,
                dcpDmgAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 11, repositoryHelper));
    }

    @Test
    public void testDCPDmgRequestUri() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.setBlockedIabCategories(Arrays.asList(new String[] {"IAB10", "IAB21", "IAB12"}));
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("APP");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final List<Long> category = new ArrayList<Long>();
        category.add(3l);
        sasParams.setCategories(category);
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        final String externalKey = "49538";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(dmgAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        if (dcpDmgAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, null, (short) 15, repositoryHelper)) {
            final String actualUrl = dcpDmgAdNetwork.getRequestUri().toString();
            final String t = actualUrl.substring(actualUrl.indexOf("&t=") + 3, actualUrl.indexOf("&kw="));
            final String expectedUrl =
                    "http://ad.dmg-mobile.com/upsteed/wap/adrequest?acc=17067024&as=49538&ua=Mozilla&i=206.29.182.240&uid=202cb962ac59075b964b07152d234b70&t=<time>&kw=Business&adw=320&adh=50&lat=37.4429&long=-122.1514&nk=Immigration%2CLegal+Issues%2CGay+Life%2CAtheism%2FAgnosticism%2CExtreme+Graphic%2FExplicit+Violence%2CPornography%2CProfane+Content%2CHate+Content%2CIllegal+Content%2CWarez%2CSpyware%2FMalware%2CCopyright+Infringement%2CAdult+Education%2CPregnancy%2CBrain+Tumor%2CCancer%2CCholesterol%2CChronic+Fatigue+Syndrome%2CChronic+Pain%2CDeafness%2CDepression%2CDermatology%2CA.D.D.%2CDiabetes%2CEpilepsy%2CGERD%2FAcid+Reflux%2CHeart+Disease%2CHerbs+for+Health%2CIBS%2FCrohn%27s+Disease%2CIncest%2FAbuse+Support%2CIncontinence%2CAIDS%2FHIV%2CInfertility%2CMen%27s+Health%2CPanic%2FAnxiety+Disorders%2CPhysical+Therapy%2CPsychology%2FPsychiatry%2CSenor+Health%2CSexuality%2CSleep+Disorders%2CWeight+Loss%2CWomen%27s+Health%2CAlternative+Medicine%2CAutism%2FPDD%2CBipolar+Disorder%2CCocktails%2FBeer";
            assertEquals(expectedUrl.replace("<time>", t), actualUrl);
        }
    }

    @Test
    public void testDCPDmgRequestUriBlankLatLong() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("WAP");
        casInternalRequestParameters.setBlockedIabCategories(Arrays.asList(new String[] {"IAB10", "IAB21", "IAB12"}));
        casInternalRequestParameters.setLatLong("38.5,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        final List<Long> category = new ArrayList<Long>();
        category.add(3l);
        category.add(2l);
        sasParams.setCategories(category);

        final String externalKey = "01212121";
        final String clurl =
                "http://c2.w.inmobi.com/c"
                        + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                        + "-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(dmgAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        if (dcpDmgAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 15, repositoryHelper)) {
            final String actualUrl = dcpDmgAdNetwork.getRequestUri().toString();
            final String t = actualUrl.substring(actualUrl.indexOf("&t=") + 3, actualUrl.indexOf("&kw="));
            final String expectedUrl =
                    "http://ad.dmg-mobile.com/upsteed/wap/adrequest?acc=17067024&as=01212121&ua=Mozilla&i=206.29.182.240&uid=202cb962ac59075b964b07152d234b70&t=<time>&kw=Business%2CBooks+%26+Reference&adw=320&adh=50&lat=38.5&long=-122.1514&nk=Immigration%2CLegal+Issues%2CGay+Life%2CAtheism%2FAgnosticism%2CExtreme+Graphic%2FExplicit+Violence%2CPornography%2CProfane+Content%2CHate+Content%2CIllegal+Content%2CWarez%2CSpyware%2FMalware%2CCopyright+Infringement%2CAdult+Education%2CPregnancy%2CBrain+Tumor%2CCancer%2CCholesterol%2CChronic+Fatigue+Syndrome%2CChronic+Pain%2CDeafness%2CDepression%2CDermatology%2CA.D.D.%2CDiabetes%2CEpilepsy%2CGERD%2FAcid+Reflux%2CHeart+Disease%2CHerbs+for+Health%2CIBS%2FCrohn%27s+Disease%2CIncest%2FAbuse+Support%2CIncontinence%2CAIDS%2FHIV%2CInfertility%2CMen%27s+Health%2CPanic%2FAnxiety+Disorders%2CPhysical+Therapy%2CPsychology%2FPsychiatry%2CSenor+Health%2CSexuality%2CSleep+Disorders%2CWeight+Loss%2CWomen%27s+Health%2CAlternative+Medicine%2CAutism%2FPDD%2CBipolar+Disorder%2CCocktails%2FBeer";
            assertEquals(expectedUrl.replaceAll("<time>", t), actualUrl);

        }
    }

    @Test
    public void testDCPDmgParseAd() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.setBlockedIabCategories(Arrays.asList(new String[] {"IAB10", "IAB21", "IAB12"}));
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        final String externalKey = "19100";
        final String beaconUrl =
                "http://c2.w.inmobi.com/c"
                        + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                        + "-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";

        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(dmgAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        dcpDmgAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl, (short) 15, repositoryHelper);
        final String response =
                "<div style='margin:0px; padding:0px;'><a target=\"_top\" href=\"http://ad.ipredictive.com/d/track/click?zid=inmobi_1_0_1_testkey&sid=bde34925-a0da-11e2-851d-f112587bd4c2&crid=424&adid=8&oid=55&cid=50&spid=322&pubid=58&ez_p=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA&rd=http%3A%2F%2Fapp.appsflyer.com%2Fid381840917%3Fpid%3Dadelphic_int%26c%3DAdelphic%26clickid%3Dbde34925-a0da-11e2-851d-f112587bd4c2%253A424%253A58%253A8%253A50%253A55%253Ainmobi_1_0_1_testkey\"><img src=\"http://ad.ipredictive.com/d/img/image?zid=inmobi_1_0_1_testkey&sid=bde34925-a0da-11e2-851d-f112587bd4c2&crid=424&adid=8&oid=55&cid=50&spid=322&pubid=58&ez_p=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA&rd=http%3A%2F%2Fd299n2tvhpuett.cloudfront.net%2Fimage%2Fadelphic%2Fadelphic5.png&rr=43207\" width=\"320\" height=\"50\"></img></a></div>";
        dcpDmgAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(200, dcpDmgAdNetwork.getHttpResponseStatusCode());
        assertEquals(
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><div style='margin:0px; padding:0px;'><a target=\"_top\" href=\"http://ad.ipredictive.com/d/track/click?zid=inmobi_1_0_1_testkey&sid=bde34925-a0da-11e2-851d-f112587bd4c2&crid=424&adid=8&oid=55&cid=50&spid=322&pubid=58&ez_p=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA&rd=http%3A%2F%2Fapp.appsflyer.com%2Fid381840917%3Fpid%3Dadelphic_int%26c%3DAdelphic%26clickid%3Dbde34925-a0da-11e2-851d-f112587bd4c2%253A424%253A58%253A8%253A50%253A55%253Ainmobi_1_0_1_testkey\"><img src=\"http://ad.ipredictive.com/d/img/image?zid=inmobi_1_0_1_testkey&sid=bde34925-a0da-11e2-851d-f112587bd4c2&crid=424&adid=8&oid=55&cid=50&spid=322&pubid=58&ez_p=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA&rd=http%3A%2F%2Fd299n2tvhpuett.cloudfront.net%2Fimage%2Fadelphic%2Fadelphic5.png&rr=43207\" width=\"320\" height=\"50\"></img></a></div><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>",
                dcpDmgAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPDmgParseAppAd() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.setBlockedIabCategories(Arrays.asList(new String[] {"IAB10", "IAB21", "IAB12"}));
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("APP");
        final String externalKey = "19100";
        final String beaconUrl =
                "http://c2.w.inmobi.com/c"
                        + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                        + "-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";

        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(dmgAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        dcpDmgAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl, (short) 15, repositoryHelper);
        final String response =
                "<div style='margin:0px; padding:0px;'><a target=\"_top\" href=\"http://ad.ipredictive.com/d/track/click?zid=inmobi_1_0_1_testkey&sid=bde34925-a0da-11e2-851d-f112587bd4c2&crid=424&adid=8&oid=55&cid=50&spid=322&pubid=58&ez_p=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA&rd=http%3A%2F%2Fapp.appsflyer.com%2Fid381840917%3Fpid%3Dadelphic_int%26c%3DAdelphic%26clickid%3Dbde34925-a0da-11e2-851d-f112587bd4c2%253A424%253A58%253A8%253A50%253A55%253Ainmobi_1_0_1_testkey\"><img src=\"http://ad.ipredictive.com/d/img/image?zid=inmobi_1_0_1_testkey&sid=bde34925-a0da-11e2-851d-f112587bd4c2&crid=424&adid=8&oid=55&cid=50&spid=322&pubid=58&ez_p=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA&rd=http%3A%2F%2Fd299n2tvhpuett.cloudfront.net%2Fimage%2Fadelphic%2Fadelphic5.png&rr=43207\" width=\"320\" height=\"50\"></img></a></div>";
        dcpDmgAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(200, dcpDmgAdNetwork.getHttpResponseStatusCode());
        assertEquals(
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><div style='margin:0px; padding:0px;'><a target=\"_top\" href=\"http://ad.ipredictive.com/d/track/click?zid=inmobi_1_0_1_testkey&sid=bde34925-a0da-11e2-851d-f112587bd4c2&crid=424&adid=8&oid=55&cid=50&spid=322&pubid=58&ez_p=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA&rd=http%3A%2F%2Fapp.appsflyer.com%2Fid381840917%3Fpid%3Dadelphic_int%26c%3DAdelphic%26clickid%3Dbde34925-a0da-11e2-851d-f112587bd4c2%253A424%253A58%253A8%253A50%253A55%253Ainmobi_1_0_1_testkey\"><img src=\"http://ad.ipredictive.com/d/img/image?zid=inmobi_1_0_1_testkey&sid=bde34925-a0da-11e2-851d-f112587bd4c2&crid=424&adid=8&oid=55&cid=50&spid=322&pubid=58&ez_p=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA&rd=http%3A%2F%2Fd299n2tvhpuett.cloudfront.net%2Fimage%2Fadelphic%2Fadelphic5.png&rr=43207\" width=\"320\" height=\"50\"></img></a></div><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>",
                dcpDmgAdNetwork.getHttpResponseContent().trim());
    }

    @Test
    public void testDCPDmgParseNoAd() throws Exception {
        final String response = "";
        dcpDmgAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(500, dcpDmgAdNetwork.getHttpResponseStatusCode());
    }

    @Test
    public void testDCPDmgParseEmptyResponseCode() throws Exception {
        final String response = "";
        dcpDmgAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(500, dcpDmgAdNetwork.getHttpResponseStatusCode());
        assertEquals("", dcpDmgAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPDmgGetId() throws Exception {
        assertEquals(dmgAdvId, dcpDmgAdNetwork.getId());
    }

    @Test
    public void testDCPDmgGetImpressionId() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        final String clurl =
                "http://c2.w.inmobi.com/c"
                        + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                        + "-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(dmgAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        dcpDmgAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 11, repositoryHelper);
        assertEquals("4f8d98e2-4bbd-40bc-8795-22da170700f9", dcpDmgAdNetwork.getImpressionId());
    }

    @Test
    public void testDCPDmgGetName() throws Exception {
        assertEquals("dmgDCP", dcpDmgAdNetwork.getName());
    }

    @Test
    public void testDCPDmgIsClickUrlReq() throws Exception {
        assertEquals(false, dcpDmgAdNetwork.isClickUrlRequired());
    }
}
