package com.inmobi.adserve.channels.adnetworks;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.apache.commons.configuration.Configuration;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

import com.inmobi.adserve.channels.adnetworks.marimedia.DCPMarimediaAdNetwork;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;


public class DCPMarimediaAdNetworkTest extends TestCase {
    private Configuration mockConfig = null;
    private final String debug = "debug";
    private final String loggerConf = "/tmp/channel-server.properties";

    private DCPMarimediaAdNetwork dcpMarimediaAdNetwork;
    private final String marimediaHost = "http://ad.taptica.com/aff_ad?rt=0";
    private final String marimediaStatus = "on";
    private final String marimediaAdvId = "marimedia";
    private RepositoryHelper repositoryHelper;

    public void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("marimedia.host")).andReturn(marimediaHost).anyTimes();
        expect(mockConfig.getString("marimedia.status")).andReturn(marimediaStatus).anyTimes();
        expect(mockConfig.getString("marimedia.advertiserId")).andReturn(marimediaAdvId).anyTimes();
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
        Formatter.init();
        final Channel serverChannel = createMock(Channel.class);
        final HttpRequestHandlerBase base = createMock(HttpRequestHandlerBase.class);
        prepareMockConfig();
        final SlotSizeMapEntity slotSizeMapEntityFor3 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor3.getDimension()).andReturn(new Dimension(216, 36)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor3);
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
        final SlotSizeMapEntity slotSizeMapEntityFor21 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor21.getDimension()).andReturn(new Dimension(480, 75)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor21);
        repositoryHelper = EasyMock.createMock(RepositoryHelper.class);
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)3))
                .andReturn(slotSizeMapEntityFor3).anyTimes();
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
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)21))
                .andReturn(slotSizeMapEntityFor21).anyTimes();
        EasyMock.replay(repositoryHelper);
        dcpMarimediaAdNetwork = new DCPMarimediaAdNetwork(mockConfig, null, base, serverChannel);
    }

    @Test
    public void testDCPMarimediaConfigureParameters() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();

        // casInternalRequestParameters.blockedCategories = new ArrayList<Long>(Arrays.asList(new Long[]{50l, 51l}));
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        casInternalRequestParameters.setUidIFA("23e2ewq445545");
        casInternalRequestParameters.setUidADT("0");
        casInternalRequestParameters.setUidIDUS1("202cb962ac59075b964b07152d234b70");

        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.0.3; ko-kr; LG-L160L Build/IML74K) AppleWebkit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        sasParams.setSource("APP");
        sasParams.setOsId(SASRequestParameters.HandSetOS.iOS.getValue());
        final String externalKey = "918a1f78-811c-4145-912e-c1a45f7705a0";

        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(marimediaAdvId, "adgroupid",
                        null, null, 0, null, null, true, true, externalKey, null, null, null, new Long[] {1L}, true,
                        null, null, 0, null, false, false, false, false, false, false, false, false, false, false,
                        null, new ArrayList<Integer>(), 0.0d, null, null, 0, new Integer[] {0}));

        assertTrue(dcpMarimediaAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null,
                null, (short) 3, repositoryHelper));
    }

    @Test
    public void testDCPMarimediaConfigureParametersAndroidVersionSpecific() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();

        // casInternalRequestParameters.blockedCategories = new ArrayList<Long>(Arrays.asList(new Long[]{50l, 51l}));
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.0.3; ko-kr; LG-L160L Build/IML74K) AppleWebkit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        sasParams.setSource("APP");
        sasParams.setOsId(SASRequestParameters.HandSetOS.Android.getValue());
        final String externalKey = "918a1f78-811c-4145-912e-c1a45f7705a0";
        sasParams.setOsMajorVersion("2.0.1");
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(marimediaAdvId, "adgroupid",
                        null, null, 0, null, null, true, true, externalKey, null, null, null, new Long[] {1L}, true,
                        null, null, 0, null, false, false, false, false, false, false, false, false, false, false,
                        null, new ArrayList<Integer>(), 0.0d, null, null, 0, new Integer[] {0}));

        assertFalse(dcpMarimediaAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null,
                null, (short) 3, repositoryHelper));
        sasParams.setOsMajorVersion("2.3.1");
        assertTrue(dcpMarimediaAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null,
                null, (short) 3, repositoryHelper));

    }

    @Test
    public void testDCPMarimediaRequestUri() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();

        // casInternalRequestParameters.blockedCategories = new ArrayList<Long>(Arrays.asList(new Long[]{50l, 51l}));
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        casInternalRequestParameters.setUidIFA("23e2ewq445545");
        casInternalRequestParameters.setUidADT("0");
        casInternalRequestParameters.setUidIDUS1("202cb962ac59075b964b07152d234b70");

        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.0.3; ko-kr; LG-L160L Build/IML74K) AppleWebkit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        sasParams.setSource("APP");
        sasParams.setOsId(SASRequestParameters.HandSetOS.iOS.getValue());
        final String externalKey = "918a1f78-811c-4145-912e-c1a45f7705a0";

        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(marimediaAdvId, "adgroupid",
                        null, null, 0, null, null, true, true, externalKey, null, null, null, new Long[] {1L}, true,
                        null, null, 0, null, false, false, false, false, false, false, false, false, false, false,
                        null, new ArrayList<Integer>(), 0.0d, null, null, 0, new Integer[] {0}));

        final String beaconUrl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0"
                        + "/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1"
                        + "/9cddca11?beacon=true";

        dcpMarimediaAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl, (short) 3, repositoryHelper);

        // Compare the expected URL with actual URL.
        final String actualUrl = dcpMarimediaAdNetwork.getRequestUri().toString();
        final String expectedUrl =
                "http://ad.taptica.com/aff_ad?rt=0&u=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+4.0.3%3B+ko-kr%3B+LG-L160L+Build%2FIML74K%29+AppleWebkit%2F534.30+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F534.30&a=918a1f78-811c-4145-912e-c1a45f7705a0&i=206.29.182.240&t=2&r=216x36&lat=37.4429&lon=-122.1514&tt_udid=202cb962ac59075b964b07152d234b70&tt_idfa=23e2ewq445545&nt=carrier&tt_sub_aff=00000000-0000-0000-0000-000000000000";
        assertEquals(expectedUrl, actualUrl);
    }

    @Test
    public void testDCPMarimediaParseResponse() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();

        // casInternalRequestParameters.blockedCategories = new ArrayList<Long>(Arrays.asList(new Long[]{50l, 51l}));
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        casInternalRequestParameters.setUidIFA("23e2ewq445545");
        casInternalRequestParameters.setUidADT("0");
        casInternalRequestParameters.setUidIDUS1("202cb962ac59075b964b07152d234b70");

        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla/5.0 (Linux; U; Android 4.0.3; ko-kr; LG-L160L Build/IML74K) AppleWebkit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        sasParams.setSource("APP");
        sasParams.setOsId(SASRequestParameters.HandSetOS.iOS.getValue());
        final String externalKey = "918a1f78-811c-4145-912e-c1a45f7705a0";

        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(marimediaAdvId, "adgroupid",
                        null, null, 0, null, null, true, true, externalKey, null, null, null, new Long[] {1L}, true,
                        null, null, 0, null, false, false, false, false, false, false, false, false, false, false,
                        null, new ArrayList<Integer>(), 0.0d, null, null, 0, new Integer[] {0}));

        final String beaconUrl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0"
                        + "/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1"
                        + "/9cddca11?beacon=true";

        final String response =
                "{\"adyxAdRequestGuid\": \"3b327792-f364-41b4-9f34-501f1acc3109\", \"adType\": \"banner\", \"imageUrl\": \"http://media.go2speed.org/brand/files/taptica/4278/Bingo_2tixanimated_Banner_320x50.gif\", \"imageAltText\": null, \"adUrl\": \"http://tracking.taptica.com/aff_ic?tt_cid=3b327792f36441b49f34501f1acc3109&el=GbtixRMFpAr%2boJEVHfprXTgVAQ3Ld8jjq6x3iEWyaxyyAB9XQaJ2hY1C%2bRbXbyCqSUxUNfry7uYwwqJoGMRB4DXDBgQXPm3cW0TjdSjP3GwU4L4c80snbxBqkCNTlGzYfeDOTGlIBc72p%2bKawW1NjSO6o5imY%2fv08FTGqOkRkhtpsdYOVHKXY6dYrgs57S2lAlmQgY62z3717Io%2f82fSHB4vgiY9rovVowp65VpiBKbt3a6VLqOpERRikOpS6NDRe4qtJFdK326mBMsVETyPW1Egv4eZAnn1baZ5OLwYf9TWP3x1OsgHBD9YV5V2hA%2btjAExs9A%2brj%2b5Tk2lGAGFpK%2bM%2bd3ttbG%2f%2brE2WlbRVHfE3EtatWKJ0g2kh8p2uccF7N5FsRn9PvvRTwgM0pgmw9h5xjcFcG9PfLRrmZ6QXCq5hytHd4B3y7WU1pQUmjtQO8YHaj0wTdZ2NJJtyfuIL78qSwgTqfYiAkg7%2bMDVjs%3d\",    \"impUrl\": \"http://ad.taptica.com/aff_i?tt_cid=3b327792f36441b49f34501f1acc3109&el=GbtixRMFpAr%2boJEVHfprXTgVAQ3Ld8jjq6x3iEWyaxyyAB9XQaJ2hY1C%2bRbXbyCqSUxUNfry7uYwwqJoGMRB4DXDBgQXPm3cW0TjdSjP3GwU4L4c80snbxBqkCNTlGzYfeDOTGlIBc72p%2bKawW1NjSO6o5imY%2fv08FTGqOkRkhtpsdYOVHKXY6dYrgs57S2lAlmQgY62z3717Io%2f82fSHB4vgiY9rovVowp65VpiBKbt3a6VLqOpERRikOpS6NDRe4qtJFdK326mBMsVETyPW1Egv4eZAnn1baZ5OLwYf9UTWP3x1OsgHBD9YV5V2hA%2btjAExs9A%2brj%2b5Tk2lGAGFpK%2bM%2bd3ttbG%2f%2brE2WlbRVHfE3EtatWKJ0g2kh8p2uccF7N5FsRn9PvvRTwgM0pgmw9h5xjcFcG9PfLRrmZ6QXCq5hytHd4B3y7WU1pQUmjtQO8YHaj0wTdZ2NJJtyfuIL78qSwgTqfYiAkg7%2bMDVjs%3d\", \"urlType\": \"web\", \"beacon\": null, \"adInterval\": \"10\", \"advAppId\": \"542781249\", \"storeUrl\": null, \"payout\": \"1.05000\"}";

        dcpMarimediaAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl, (short) 3, repositoryHelper);
        dcpMarimediaAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpMarimediaAdNetwork.getHttpResponseStatusCode(), 200);

        final String actualResponse = dcpMarimediaAdNetwork.getResponseContent();
        final String expectedResponse =
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><a href='http://tracking.taptica.com/aff_ic?tt_cid=3b327792f36441b49f34501f1acc3109&el=GbtixRMFpAr%2boJEVHfprXTgVAQ3Ld8jjq6x3iEWyaxyyAB9XQaJ2hY1C%2bRbXbyCqSUxUNfry7uYwwqJoGMRB4DXDBgQXPm3cW0TjdSjP3GwU4L4c80snbxBqkCNTlGzYfeDOTGlIBc72p%2bKawW1NjSO6o5imY%2fv08FTGqOkRkhtpsdYOVHKXY6dYrgs57S2lAlmQgY62z3717Io%2f82fSHB4vgiY9rovVowp65VpiBKbt3a6VLqOpERRikOpS6NDRe4qtJFdK326mBMsVETyPW1Egv4eZAnn1baZ5OLwYf9TWP3x1OsgHBD9YV5V2hA%2btjAExs9A%2brj%2b5Tk2lGAGFpK%2bM%2bd3ttbG%2f%2brE2WlbRVHfE3EtatWKJ0g2kh8p2uccF7N5FsRn9PvvRTwgM0pgmw9h5xjcFcG9PfLRrmZ6QXCq5hytHd4B3y7WU1pQUmjtQO8YHaj0wTdZ2NJJtyfuIL78qSwgTqfYiAkg7%2bMDVjs%3d' onclick=\"document.getElementById('click').src='$IMClickUrl';\" target=\"_blank\" style=\"text-decoration: none\"><img src='http://media.go2speed.org/brand/files/taptica/4278/Bingo_2tixanimated_Banner_320x50.gif'  /></a><img src='http://ad.taptica.com/aff_i?tt_cid=3b327792f36441b49f34501f1acc3109&el=GbtixRMFpAr%2boJEVHfprXTgVAQ3Ld8jjq6x3iEWyaxyyAB9XQaJ2hY1C%2bRbXbyCqSUxUNfry7uYwwqJoGMRB4DXDBgQXPm3cW0TjdSjP3GwU4L4c80snbxBqkCNTlGzYfeDOTGlIBc72p%2bKawW1NjSO6o5imY%2fv08FTGqOkRkhtpsdYOVHKXY6dYrgs57S2lAlmQgY62z3717Io%2f82fSHB4vgiY9rovVowp65VpiBKbt3a6VLqOpERRikOpS6NDRe4qtJFdK326mBMsVETyPW1Egv4eZAnn1baZ5OLwYf9UTWP3x1OsgHBD9YV5V2hA%2btjAExs9A%2brj%2b5Tk2lGAGFpK%2bM%2bd3ttbG%2f%2brE2WlbRVHfE3EtatWKJ0g2kh8p2uccF7N5FsRn9PvvRTwgM0pgmw9h5xjcFcG9PfLRrmZ6QXCq5hytHd4B3y7WU1pQUmjtQO8YHaj0wTdZ2NJJtyfuIL78qSwgTqfYiAkg7%2bMDVjs%3d' height=1 width=1 border=0 style=\"display:none;\"/><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/><img id=\"click\" width=\"1\" height=\"1\" style=\"display:none;\"/></body></html>";

        assertEquals(actualResponse, expectedResponse);
    }

    @Test
    public void testDCPMarimediaParseResponseEmpty() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();

        // casInternalRequestParameters.blockedCategories = new ArrayList<Long>(Arrays.asList(new Long[]{50l, 51l}));
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        casInternalRequestParameters.setUidIFA("23e2ewq445545");
        casInternalRequestParameters.setUidADT("0");
        casInternalRequestParameters.setUidIDUS1("202cb962ac59075b964b07152d234b70");

        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla%2F5.0+%28compatible%3B+MSIE+9.0%3B+Windows+NT+6.1%3B+Trident%2F5.0%29");
        sasParams.setSource("APP");
        sasParams.setOsId(SASRequestParameters.HandSetOS.iOS.getValue());
        final String externalKey = "918a1f78-811c-4145-912e-c1a45f7705a0";

        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(marimediaAdvId, "adgroupid",
                        null, null, 0, null, null, true, true, externalKey, null, null, null, new Long[] {1L}, true,
                        null, null, 0, null, false, false, false, false, false, false, false, false, false, false,
                        null, new ArrayList<Integer>(), 0.0d, null, null, 0, new Integer[] {0}));

        final String beaconUrl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0"
                        + "/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1"
                        + "/9cddca11?beacon=true";

        final String response = "{\"code\":0,\"adType\":\"empty\",\"description\":\"\"}";

        dcpMarimediaAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl, (short) 21, repositoryHelper);
        dcpMarimediaAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpMarimediaAdNetwork.getHttpResponseStatusCode(), 200);

        final String actualResponse = dcpMarimediaAdNetwork.getResponseContent();
        final String expectedResponse = null;

        assertEquals(actualResponse, expectedResponse);
    }
}
