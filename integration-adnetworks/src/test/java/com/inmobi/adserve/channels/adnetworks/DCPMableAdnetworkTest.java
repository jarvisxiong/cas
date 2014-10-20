package com.inmobi.adserve.channels.adnetworks;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.configuration.Configuration;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.Test;

import com.inmobi.adserve.channels.adnetworks.mable.DCPMableAdnetwork;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;


public class DCPMableAdnetworkTest extends TestCase {
	private Configuration mockConfig = null;
	private final String debug = "debug";
	private final String loggerConf = "/tmp/channel-server.properties";

	private DCPMableAdnetwork dcpMableAdNetwork;
	private final String mableHost = "http://ad.ipredictive.com/d/ads";
	private final String mableStatus = "on";
	private final String mableAdvId = "mableadv1";
	private final String mableTest = "1";
	private final String mableAuthKey = "335eaf2639079ffa40b5f7d69f3051fb";

	public void prepareMockConfig() {
		mockConfig = createMock(Configuration.class);
		expect(mockConfig.getString("mable.host")).andReturn(mableHost).anyTimes();
		expect(mockConfig.getString("mable.status")).andReturn(mableStatus).anyTimes();
		expect(mockConfig.getString("mable.test")).andReturn(mableTest).anyTimes();
		expect(mockConfig.getString("mable.advertiserId")).andReturn(mableAdvId).anyTimes();
		expect(mockConfig.getString("mable.authKey")).andReturn(mableAuthKey).anyTimes();
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
		SlotSizeMapping.init();
		Formatter.init();
		dcpMableAdNetwork = new DCPMableAdnetwork(mockConfig, null, base, serverChannel);
	}

	@Test
	public void testDCPMableConfigureParameters() throws JSONException {
		final SASRequestParameters sasParams = new SASRequestParameters();
		final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
		sasParams.setRemoteHostIp("206.29.182.240");
		sasParams.setSlot(Short.valueOf("11"));
		sasParams
				.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
		casInternalRequestParameters.setLatLong("37.4429,-122.1514");
		casInternalRequestParameters.setUid("23e2ewq445545");
		final String clurl =
				"http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
		sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
		final String externalKey = "f6wqjq1r5v";
		final ChannelSegmentEntity entity =
				new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(mableAdvId, null, null, null, 0,
						null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
						null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
								"{\"spot\":54235,\"pubId\":\"inmobi_1\"," + "\"site\":1234}"),
						new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
		assertEquals(true,
				dcpMableAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
	}

	@Test
	public void testDCPMableConfigureParametersBlankIP() {
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
				new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(mableAdvId, null, null, null, 0,
						null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
						null, false, false, false, false, false, false, false, false, false, false, null,
						new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
		assertEquals(false,
				dcpMableAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
	}

	@Test
	public void testDCPMableConfigureParametersAdditionalParams() {
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
				new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(mableAdvId, null, null, null, 0,
						null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
						null, false, false, false, false, false, false, false, false, false, false, null,
						new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
		assertEquals(false,
				dcpMableAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
	}

	@Test
	public void testDCPMableConfigureParametersBlankUA() {
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
				new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(mableAdvId, null, null, null, 0,
						null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
						null, false, false, false, false, false, false, false, false, false, false, null,
						new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
		assertEquals(false,
				dcpMableAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
	}

	@Test
	public void testDCPMableRequestUri() throws Exception {
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
		sasParams.setSlot(Short.valueOf("15"));
		final String externalKey = "0344343";
		SlotSizeMapping.init();
		final ChannelSegmentEntity entity =
				new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(mableAdvId, null, null, null, 0,
						null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
						null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
								"{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"),
						new ArrayList<Integer>(), 0.0d, null, null, 0, new Integer[] {0}));

		if (dcpMableAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, null)) {
			final String actualUrl = dcpMableAdNetwork.getRequestUri().toString();
			final String expectedUrl = "http://ad.ipredictive.com/d/ads";
			assertEquals(expectedUrl, actualUrl);
		}
	}

	@Test
	public void testDCPMableRequestUriBlankLatLong() throws Exception {
		final SASRequestParameters sasParams = new SASRequestParameters();
		final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
		sasParams.setRemoteHostIp("206.29.182.240");
		sasParams.setUserAgent("Mozilla");
		sasParams.setSource("WAP");
		casInternalRequestParameters.setBlockedIabCategories(Arrays.asList(new String[] {"IAB10", "IAB21", "IAB12"}));
		casInternalRequestParameters.setLatLong("38.5,-122.1514");
		sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
		casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
		sasParams.setSlot(Short.valueOf("15"));
		final List<Long> category = new ArrayList<Long>();
		category.add(3l);
		category.add(2l);
		sasParams.setCategories(category);

		final String externalKey = "01212121";
		SlotSizeMapping.init();
		final String clurl =
				"http://c2.w.inmobi.com/c"
						+ ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
						+ "-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
		final ChannelSegmentEntity entity =
				new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(mableAdvId, null, null, null, 0,
						null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
						null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
								"{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"),
						new ArrayList<Integer>(), 0.0d, null, null, 0, new Integer[] {0}));
		if (dcpMableAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null)) {
			final String actualUrl = dcpMableAdNetwork.getRequestUri().toString();
			System.out.println(actualUrl);
			final String expectedUrl = "http://ad.ipredictive.com/d/ads";
			assertEquals(expectedUrl, actualUrl);
		}
	}

	@Test
	public void testDCPMableParseAd() throws Exception {
		final SASRequestParameters sasParams = new SASRequestParameters();
		final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
		casInternalRequestParameters.setBlockedIabCategories(Arrays.asList(new String[] {"IAB10", "IAB21", "IAB12"}));
		sasParams.setRemoteHostIp("206.29.182.240");
		sasParams.setUserAgent("Mozilla");
		sasParams.setSlot(Short.valueOf("15"));
		final String externalKey = "19100";
		final String beaconUrl =
				"http://c2.w.inmobi.com/c"
						+ ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
						+ "-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";

		final ChannelSegmentEntity entity =
				new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(mableAdvId, null, null, null, 0,
						null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
						null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
								"{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"),
						new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
		dcpMableAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl);
		final String response =
				"<div style='margin:0px; padding:0px;'><a target=\"_top\" href=\"http://ad.ipredictive.com/d/track/click?zid=inmobi_1_0_1_testkey&sid=bde34925-a0da-11e2-851d-f112587bd4c2&crid=424&adid=8&oid=55&cid=50&spid=322&pubid=58&ez_p=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA&rd=http%3A%2F%2Fapp.appsflyer.com%2Fid381840917%3Fpid%3Dmable_int%26c%3DMable%26clickid%3Dbde34925-a0da-11e2-851d-f112587bd4c2%253A424%253A58%253A8%253A50%253A55%253Ainmobi_1_0_1_testkey\"><img src=\"http://ad.ipredictive.com/d/img/image?zid=inmobi_1_0_1_testkey&sid=bde34925-a0da-11e2-851d-f112587bd4c2&crid=424&adid=8&oid=55&cid=50&spid=322&pubid=58&ez_p=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA&rd=http%3A%2F%2Fd299n2tvhpuett.cloudfront.net%2Fimage%2Fmable%2Fmable5.png&rr=43207\" width=\"320\" height=\"50\"></img></a></div>";
		dcpMableAdNetwork.parseResponse(response, HttpResponseStatus.OK);
		assertEquals(200, dcpMableAdNetwork.getHttpResponseStatusCode());
		assertEquals(
				"<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><div style='margin:0px; padding:0px;'><a target=\"_top\" href=\"http://ad.ipredictive.com/d/track/click?zid=inmobi_1_0_1_testkey&sid=bde34925-a0da-11e2-851d-f112587bd4c2&crid=424&adid=8&oid=55&cid=50&spid=322&pubid=58&ez_p=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA&rd=http%3A%2F%2Fapp.appsflyer.com%2Fid381840917%3Fpid%3Dmable_int%26c%3DMable%26clickid%3Dbde34925-a0da-11e2-851d-f112587bd4c2%253A424%253A58%253A8%253A50%253A55%253Ainmobi_1_0_1_testkey\"><img src=\"http://ad.ipredictive.com/d/img/image?zid=inmobi_1_0_1_testkey&sid=bde34925-a0da-11e2-851d-f112587bd4c2&crid=424&adid=8&oid=55&cid=50&spid=322&pubid=58&ez_p=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA&rd=http%3A%2F%2Fd299n2tvhpuett.cloudfront.net%2Fimage%2Fmable%2Fmable5.png&rr=43207\" width=\"320\" height=\"50\"></img></a></div><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>",
				dcpMableAdNetwork.getHttpResponseContent());
	}

	@Test
	public void testDCPMableParseAppAd() throws Exception {
		final SASRequestParameters sasParams = new SASRequestParameters();
		final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
		casInternalRequestParameters.setBlockedIabCategories(Arrays.asList(new String[] {"IAB10", "IAB21", "IAB12"}));
		sasParams.setRemoteHostIp("206.29.182.240");
		sasParams.setUserAgent("Mozilla");
		sasParams.setSlot(Short.valueOf("15"));
		sasParams.setSource("APP");
		final String externalKey = "19100";
		final String beaconUrl =
				"http://c2.w.inmobi.com/c"
						+ ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
						+ "-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";

		final ChannelSegmentEntity entity =
				new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(mableAdvId, null, null, null, 0,
						null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
						null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
								"{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"),
						new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
		dcpMableAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl);
		final String response =
				"<div style='margin:0px; padding:0px;'><a target=\"_top\" href=\"http://ad.ipredictive.com/d/track/click?zid=inmobi_1_0_1_testkey&sid=bde34925-a0da-11e2-851d-f112587bd4c2&crid=424&adid=8&oid=55&cid=50&spid=322&pubid=58&ez_p=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA&rd=http%3A%2F%2Fapp.appsflyer.com%2Fid381840917%3Fpid%3Dmable_int%26c%3DMable%26clickid%3Dbde34925-a0da-11e2-851d-f112587bd4c2%253A424%253A58%253A8%253A50%253A55%253Ainmobi_1_0_1_testkey\"><img src=\"http://ad.ipredictive.com/d/img/image?zid=inmobi_1_0_1_testkey&sid=bde34925-a0da-11e2-851d-f112587bd4c2&crid=424&adid=8&oid=55&cid=50&spid=322&pubid=58&ez_p=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA&rd=http%3A%2F%2Fd299n2tvhpuett.cloudfront.net%2Fimage%2Fmable%2Fmable5.png&rr=43207\" width=\"320\" height=\"50\"></img></a></div>";
		dcpMableAdNetwork.parseResponse(response, HttpResponseStatus.OK);
		assertEquals(200, dcpMableAdNetwork.getHttpResponseStatusCode());
		assertEquals(
				"<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><div style='margin:0px; padding:0px;'><a target=\"_top\" href=\"http://ad.ipredictive.com/d/track/click?zid=inmobi_1_0_1_testkey&sid=bde34925-a0da-11e2-851d-f112587bd4c2&crid=424&adid=8&oid=55&cid=50&spid=322&pubid=58&ez_p=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA&rd=http%3A%2F%2Fapp.appsflyer.com%2Fid381840917%3Fpid%3Dmable_int%26c%3DMable%26clickid%3Dbde34925-a0da-11e2-851d-f112587bd4c2%253A424%253A58%253A8%253A50%253A55%253Ainmobi_1_0_1_testkey\"><img src=\"http://ad.ipredictive.com/d/img/image?zid=inmobi_1_0_1_testkey&sid=bde34925-a0da-11e2-851d-f112587bd4c2&crid=424&adid=8&oid=55&cid=50&spid=322&pubid=58&ez_p=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA&rd=http%3A%2F%2Fd299n2tvhpuett.cloudfront.net%2Fimage%2Fmable%2Fmable5.png&rr=43207\" width=\"320\" height=\"50\"></img></a></div><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>",
				dcpMableAdNetwork.getHttpResponseContent());
	}

	@Test
	public void testDCPMableParseNoAd() throws Exception {
		final String response = "";
		dcpMableAdNetwork.parseResponse(response, HttpResponseStatus.OK);
		assertEquals(500, dcpMableAdNetwork.getHttpResponseStatusCode());
	}

	@Test
	public void testDCPMableParseEmptyResponseCode() throws Exception {
		final String response = "";
		dcpMableAdNetwork.parseResponse(response, HttpResponseStatus.OK);
		assertEquals(500, dcpMableAdNetwork.getHttpResponseStatusCode());
		assertEquals("", dcpMableAdNetwork.getHttpResponseContent());
	}

	@Test
	public void testDCPMableGetId() throws Exception {
		assertEquals(mableAdvId, dcpMableAdNetwork.getId());
	}

	@Test
	public void testDCPMableGetImpressionId() throws Exception {
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
				new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(mableAdvId, null, null, null, 0,
						null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
						null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
								"{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"),
						new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
		dcpMableAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null);
		assertEquals("4f8d98e2-4bbd-40bc-8795-22da170700f9", dcpMableAdNetwork.getImpressionId());
	}

	@Test
	public void testDCPMableGetName() throws Exception {
		assertEquals("mable", dcpMableAdNetwork.getName());
	}

	@Test
	public void testDCPMableIsClickUrlReq() throws Exception {
		assertEquals(false, dcpMableAdNetwork.isClickUrlRequired());
	}
}
