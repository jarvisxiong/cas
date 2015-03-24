package com.inmobi.adserve.channels.adnetworks;

import com.inmobi.adserve.channels.adnetworks.ifc.IFCAdNetwork;
import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.IPRepository;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;
import junit.framework.TestCase;
import org.apache.commons.configuration.Configuration;
import org.easymock.EasyMock;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.Test;

import java.awt.Dimension;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;


public class IFCAdNetworkTest extends TestCase {
    private IFCAdNetwork ifcAdNetwork;
    private Configuration mockConfig = null;
    private final String ifcHostus = "http://10.14.118.91:8083/IFCPlatform/";
    private final String ifcAdvertiserId = "1234";
    private final String ifcResponseFormat = "json";
    private final String isTest = "0";
    private final String filter = "clean";
    private final String debug = "debug";
    private RepositoryHelper repositoryHelper;

    public void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("ifc.advertiserId")).andReturn(ifcAdvertiserId).anyTimes();
        expect(mockConfig.getString("ifc.responseFormat")).andReturn(ifcResponseFormat).anyTimes();
        expect(mockConfig.getString("ifc.isTest")).andReturn(isTest).anyTimes();
        expect(mockConfig.getString("ifc.filter")).andReturn(filter).anyTimes();
        expect(mockConfig.getString("debug")).andReturn(debug).anyTimes();
        expect(mockConfig.getString("ifc.host")).andReturn(ifcHostus).anyTimes();
        expect(mockConfig.getString("slf4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/logger.xml");
        expect(mockConfig.getString("log4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties");
        expect(mockConfig.getInt("ifc.readtimeoutMillis")).andReturn(800).anyTimes();
        replay(mockConfig);
    }

    @Override
    public void setUp() throws Exception {
        File f;
        f = new File("/tmp/channel-server.properties");
        if (!f.exists()) {
            f.createNewFile();
        }
        final Channel serverChannel = createMock(Channel.class);
        final HttpRequestHandlerBase base = createMock(HttpRequestHandlerBase.class);
        prepareMockConfig();
        final SlotSizeMapEntity slotSizeMapEntityFor1 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor1.getDimension()).andReturn(new Dimension(120, 20)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor1);
        final SlotSizeMapEntity slotSizeMapEntityFor4 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor4.getDimension()).andReturn(new Dimension(300, 50)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor4);
        final SlotSizeMapEntity slotSizeMapEntityFor9 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor9.getDimension()).andReturn(new Dimension(320, 48)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor9);
        final SlotSizeMapEntity slotSizeMapEntityFor10 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor10.getDimension()).andReturn(new Dimension(300, 250)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor10);
        final SlotSizeMapEntity slotSizeMapEntityFor11 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor11.getDimension()).andReturn(new Dimension(728, 90)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor11);
        final SlotSizeMapEntity slotSizeMapEntityFor12 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor12.getDimension()).andReturn(new Dimension(468, 60)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor12);
        final SlotSizeMapEntity slotSizeMapEntityFor14 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor14.getDimension()).andReturn(new Dimension(320, 480)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor14);
        final SlotSizeMapEntity slotSizeMapEntityFor15 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor15.getDimension()).andReturn(new Dimension(320, 50)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor15);
        repositoryHelper = EasyMock.createMock(RepositoryHelper.class);
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)1))
                .andReturn(slotSizeMapEntityFor1).anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)4))
                .andReturn(slotSizeMapEntityFor4).anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)9))
                .andReturn(slotSizeMapEntityFor9).anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)11))
                .andReturn(slotSizeMapEntityFor11).anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)12))
                .andReturn(slotSizeMapEntityFor12).anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)14))
                .andReturn(slotSizeMapEntityFor14).anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)15))
                .andReturn(slotSizeMapEntityFor15).anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository(Short.MAX_VALUE))
                .andReturn(null).anyTimes();
        EasyMock.replay(repositoryHelper);
        ifcAdNetwork = new IFCAdNetwork(mockConfig, null, base, serverChannel);
        
        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);
        
        ifcAdNetwork.setHost(ifcHostus);
    }

    @Test
    public void testConfigureParameters() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        final JSONArray jsonArray = new JSONArray();
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("tid", "123");
        jsonObject.put("handset", jsonArray);
        jsonObject.put("carrier", jsonArray);
        jsonObject.put("pub-id", "13312321");
        jsonObject.put("site-allowBanner", false);
        jsonObject.put("deviceOs", "deviceOs");
        jsonObject.put("deviceOSVersion", "deviceOSVersion");
        jsonObject.put("adcode", "NON-JS");
        sasParams.setAllParametersJson(jsonObject.toString());
        sasParams.setSiteId("12");
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(ifcAdvertiserId, null, null,
                        null, 0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null,
                        null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(ifcAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, "", "", (short) 11, repositoryHelper), true);
    }

    @Test
    public void testConfigureParametersForSdkVersion() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        final JSONArray jsonArray = new JSONArray();
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("tid", "123");
        jsonObject.put("handset", jsonArray);
        jsonObject.put("carrier", jsonArray);
        jsonObject.put("pub-id", "13312321");
        jsonObject.put("site-allowBanner", false);
        jsonObject.put("deviceOs", "deviceOs");
        jsonObject.put("deviceOSVersion", "deviceOSVersion");
        jsonObject.put("adcode", "NON-JS");
        jsonObject.put("sdk-version", "i351");
        sasParams.setAllParametersJson(jsonObject.toString());
        sasParams.setSiteId("12");
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(ifcAdvertiserId, null, null,
                        null, 0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null,
                        null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(ifcAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, "", "", (short) 1, repositoryHelper), true);
    }

    @Test
    public void testConfigureParametersForSdkVersion300() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        final JSONArray jsonArray = new JSONArray();
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("tid", "123");
        jsonObject.put("handset", jsonArray);
        jsonObject.put("carrier", jsonArray);
        jsonObject.put("pub-id", "13312321");
        jsonObject.put("site-allowBanner", false);
        jsonObject.put("deviceOs", "deviceOs");
        jsonObject.put("deviceOSVersion", "deviceOSVersion");
        jsonObject.put("adcode", "NON-JS");
        jsonObject.put("sdk-version", "i301");
        sasParams.setAllParametersJson(jsonObject.toString());
        sasParams.setSiteId("12");
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(ifcAdvertiserId, null, null,
                        null, 0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null,
                        null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(ifcAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, "", "", (short) 1, repositoryHelper), false);
    }

    @Test
    public void testGetLogline() {
        assertNotNull(ifcAdNetwork.getLogline());
    }

    @Test
    public void testGetResponseAd() {
        assertNotNull(ifcAdNetwork.getResponseAd());
    }

    @Test
    public void testParseResponse() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        final JSONArray jsonArray = new JSONArray();
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("tid", "123");
        jsonObject.put("handset", jsonArray);
        jsonObject.put("carrier", jsonArray);
        jsonObject.put("pub-id", "13312321");
        jsonObject.put("site-allowBanner", false);
        jsonObject.put("deviceOs", "deviceOs");
        jsonObject.put("deviceOSVersion", "deviceOSVersion");
        jsonObject.put("adcode", "NON-JS");
        jsonObject.put("sdk-version", "i351");
        sasParams.setAllParametersJson(jsonObject.toString());
        sasParams.setSiteId("12");
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        sasParams.setImaiBaseUrl("abcd");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(ifcAdvertiserId, null, null,
                        null, 0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null,
                        null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        ifcAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, "", "", (short) 1, repositoryHelper);
        try {
            ifcAdNetwork.parseResponse("", HttpResponseStatus.OK);
            //ifcAdNetwork.parseResponse("", HttpResponseStatus.OK);
            ifcAdNetwork.parseResponse("fkjhsdfkjahfkjsa", HttpResponseStatus.OK);
        } catch (final Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testGetRequestBody() throws JSONException {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        final JSONArray jsonArray = new JSONArray();
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("tid", "123");
        jsonObject.put("handset", jsonArray);
        jsonObject.put("carrier", jsonArray);
        jsonObject.put("pub-id", "13312321");
        jsonObject.put("site-allowBanner", false);
        jsonObject.put("deviceOs", "deviceOs");
        jsonObject.put("deviceOSVersion", "deviceOSVersion");
        jsonObject.put("adcode", "JS");
        sasParams.setAllParametersJson(jsonObject.toString());
        sasParams.setSiteId("12");
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(ifcAdvertiserId, null, null,
                        null, 0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null,
                        null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(ifcAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, "", "", (short) 1, repositoryHelper), true);
    }
}
