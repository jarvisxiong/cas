package com.inmobi.adserve.channels.adnetworks;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.File;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.apache.commons.configuration.Configuration;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.Test;

import com.inmobi.adserve.channels.adnetworks.ifc.IFCAdNetwork;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.phoenix.logging.DebugLogger;


public class IFCAdNetworkTest extends TestCase {
	private IFCAdNetwork ifcAdNetwork;
	private Configuration mockConfig = null;
	private final String ifcHostus = "http://10.14.118.91:8083/IFCPlatform/";
	private final String ifcAdvertiserId = "1234";
	private final String ifcResponseFormat = "json";
	private final String isTest = "0";
	private final String filter = "clean";
	private final String debug = "debug";
	private final String loggerConf = "/tmp/channel-server.properties";
	private DebugLogger logger;

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
		SlotSizeMapping.init();
		ifcAdNetwork = new IFCAdNetwork(mockConfig, null, base, serverChannel);
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
		sasParams.setSlot(Short.valueOf("11"));
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
		assertEquals(ifcAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, "", ""), true);
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
		sasParams.setSlot(Short.valueOf("1"));
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
		assertEquals(ifcAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, "", ""), true);
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
		sasParams.setSlot(Short.valueOf("1"));
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
		assertEquals(ifcAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, "", ""), false);
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
		sasParams.setSlot(Short.valueOf("1"));
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
		ifcAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, "", "");
		try {
			ifcAdNetwork.parseResponse("", HttpResponseStatus.OK);
			ifcAdNetwork.parseResponse(null, HttpResponseStatus.OK);
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
		sasParams.setSlot(Short.valueOf("1"));
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
		assertEquals(ifcAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, "", ""), true);
	}
}
