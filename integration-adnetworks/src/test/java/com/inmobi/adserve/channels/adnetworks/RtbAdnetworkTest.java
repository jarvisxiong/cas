package com.inmobi.adserve.channels.adnetworks;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

import junit.framework.TestCase;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TSimpleJSONProtocol;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.inmobi.adserve.channels.adnetworks.rtb.ImpressionCallbackHelper;
import com.inmobi.adserve.channels.adnetworks.rtb.RtbAdNetwork;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.config.ServerConfig;
import com.inmobi.adserve.channels.api.provider.AsyncHttpClientProvider;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.CurrencyConversionEntity;
import com.inmobi.adserve.channels.entity.WapSiteUACEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.util.IABCategoriesInterface;
import com.inmobi.adserve.channels.util.IABCategoriesMap;
import com.inmobi.adserve.channels.util.Utils.ClickUrlsRegenerator;
import com.inmobi.adserve.channels.util.Utils.TestUtils;
import com.inmobi.casthrift.rtb.Bid;
import com.inmobi.casthrift.rtb.BidResponse;
import com.inmobi.casthrift.rtb.SeatBid;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Request;


public class RtbAdnetworkTest extends TestCase {

  private Configuration mockConfig = null;
  private final String debug = "debug";
  private final String loggerConf = "/tmp/channel-server.properties";
  private RtbAdNetwork rtbAdNetwork;
  private final SASRequestParameters sasParams = new SASRequestParameters();
  private final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
  private final String rtbAdvId = "id";
  BidResponse bidResponse;

  public void prepareMockConfig() {
    mockConfig = createMock(Configuration.class);
    expect(mockConfig.getString("debug")).andReturn(debug).anyTimes();
    expect(mockConfig.getString("slf4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/logger.xml");
    expect(mockConfig.getString("log4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties");
    final String advertiserName = "rtb";
    expect(mockConfig.getString(advertiserName + ".advertiserId")).andReturn("").anyTimes();
    expect(mockConfig.getString(advertiserName + ".urlArg")).andReturn("").anyTimes();
    expect(mockConfig.getString(advertiserName + ".rtbVer", "2.0")).andReturn("2.0").anyTimes();
    expect(mockConfig.getString(advertiserName + ".wnUrlback")).andReturn("").anyTimes();
    expect(mockConfig.getString(advertiserName + ".rtbMethod")).andReturn("").anyTimes();
    expect(mockConfig.getBoolean(advertiserName + ".isWnRequired")).andReturn(true).anyTimes();
    expect(mockConfig.getBoolean(advertiserName + ".isWinFromClient")).andReturn(true).anyTimes();
    expect(mockConfig.getBoolean(advertiserName + ".siteBlinded")).andReturn(true).anyTimes();
    expect(mockConfig.getBoolean(advertiserName + ".htmlSupported", true)).andReturn(true).anyTimes();
    expect(mockConfig.getBoolean(advertiserName + ".nativeSupported", false)).andReturn(false).anyTimes();
    expect(mockConfig.getBoolean(advertiserName + ".bannerVideoSupported", false)).andReturn(true).once();
    expect(mockConfig.getStringArray("rtb.blockedAdvertisers")).andReturn(
        new String[] {"king.com", "supercell.net", "paps.com", "fhs.com", "china.supercell.com", "supercell.com"})
        .anyTimes();
    expect(mockConfig.getString("key.1.value")).andReturn("clickmaker.key.1.value").anyTimes();
    expect(mockConfig.getString("key.2.value")).andReturn("clickmaker.key.2.value").anyTimes();
    expect(mockConfig.getString("beaconURLPrefix")).andReturn("clickmaker.beaconURLPrefix").anyTimes();
    expect(mockConfig.getString("clickURLPrefix")).andReturn("clickmaker.clickURLPrefix").anyTimes();
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
    sasParams.setSource("app");
    sasParams.setDst(2);
    final String urlBase = "";
    final CurrencyConversionEntity currencyConversionEntity = EasyMock.createMock(CurrencyConversionEntity.class);
    EasyMock.expect(currencyConversionEntity.getConversionRate()).andReturn(10.0).anyTimes();
    EasyMock.replay(currencyConversionEntity);
    final RepositoryHelper repositoryHelper = EasyMock.createMock(RepositoryHelper.class);
    EasyMock.expect(repositoryHelper.queryCurrencyConversionRepository(EasyMock.isA(String.class)))
        .andReturn(currencyConversionEntity).anyTimes();
    EasyMock.replay(repositoryHelper);

    rtbAdNetwork = new RtbAdNetwork(mockConfig, null, base, serverChannel, urlBase, "rtb", 200, repositoryHelper, true);

    final Field asyncHttpClientProviderField = RtbAdNetwork.class.getDeclaredField("asyncHttpClientProvider");
    asyncHttpClientProviderField.setAccessible(true);
    final ServerConfig serverConfig = createMock(ServerConfig.class);
    expect(serverConfig.getDcpRequestTimeoutInMillis()).andReturn(800).anyTimes();
    expect(serverConfig.getRtbRequestTimeoutInMillis()).andReturn(200).anyTimes();
    expect(serverConfig.getMaxDcpOutGoingConnections()).andReturn(200).anyTimes();
    expect(serverConfig.getMaxRtbOutGoingConnections()).andReturn(200).anyTimes();
    replay(serverConfig);
    final AsyncHttpClientProvider asyncHttpClientProvider =
        new AsyncHttpClientProvider(serverConfig, Executors.newCachedThreadPool());
    asyncHttpClientProvider.setup();
    asyncHttpClientProviderField.set(null, asyncHttpClientProvider);

    final Bid bid2 = new Bid();
    bid2.id = "ab73dd4868a0bbadf8fd7527d95136b4";
    bid2.adid = "1335571993285";
    bid2.price = 0.2;
    bid2.cid = "cid";
    bid2.crid = "crid";
    bid2.adm =
        "<style type='text/css'>body { margin:0;padding:0 }  </style> <p align='center'><a href='http://www.inmobi.com/' target='_blank'><img src='http://www.digitalmarket.asia/wp-content/uploads/2012/04/7a4cb5ba9e52331ae91aeee709cd3fe3.jpg' border='0'/></a></p>";
    bid2.impid = "impressionId";
    final List<Bid> bidList = new ArrayList<Bid>();
    bidList.add(bid2);
    final SeatBid seatBid = new SeatBid();
    seatBid.seat = "TO-BE-DETERMINED";
    seatBid.bid = bidList;
    final List<SeatBid> seatBidList = new ArrayList<SeatBid>();
    seatBidList.add(seatBid);
    bidResponse = new BidResponse();
    bidResponse.setSeatbid(seatBidList);
    bidResponse.id = "SGu1Jpq1IO";
    bidResponse.bidid = "ac1a2c944cff0a176643079625b0cad4a1bbe4a3";
    bidResponse.cur = "USD";
    rtbAdNetwork.setBidResponse(bidResponse);
  }

  @Test
  public void testImpressionCallback() {
    final String clickUrl =
        "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
    final String beaconUrl = "";
    final String externalSiteKey = "f6wqjq1r5v";
    final ChannelSegmentEntity entity =
        new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId, null, null, null, 0, null,
            null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null, 0, null, false,
            false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d, null,
            null, 32, new Integer[] {0}));
    rtbAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl);
    RtbAdNetwork.impressionCallbackHelper = createMock(ImpressionCallbackHelper.class);
    expect(
        RtbAdNetwork.impressionCallbackHelper.writeResponse(isA(URI.class), isA(Request.class),
            isA(AsyncHttpClient.class))).andReturn(true).anyTimes();
    replay(RtbAdNetwork.impressionCallbackHelper);
    rtbAdNetwork.setBidResponse(bidResponse);
    rtbAdNetwork.impressionCallback();
  }

  @Test
  public void testGetRequestUri() throws URISyntaxException {
    final URI uri = new URI("urlBase");
    rtbAdNetwork.setUrlArg("urlArg");
    rtbAdNetwork.setUrlBase("urlBase");
    assertEquals(uri, rtbAdNetwork.getRequestUri());
  }

  /*
   * @Test public void testGetHttpRequestBidRequestNull() throws Exception { URI uri = new
   * URI("http://localhost:8800?urlArg="); HttpRequest httpRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1,
   * HttpMethod.POST, uri.toASCIIString()); httpRequest.setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/json");
   * httpRequest.setHeader("x_openrtb_version", "2"); rtbAdNetwork.setUrlArg("urlArg");
   * rtbAdNetwork.setUrlBase("http://localhost:8800"); assertEquals(null, rtbAdNetwork.getHttpRequest()); }
   */
  /*
   * @Test public void testGetHttpRequestBidRequestNotNull() throws Exception { URI uri = new
   * URI("http://localhost:8800"); HttpRequest httpRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1,
   * HttpMethod.POST, uri.toASCIIString()); httpRequest.setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/json");
   * httpRequest.setHeader("x-openrtb-version", "2.0"); httpRequest.setHeader(HttpHeaders.Names.CONNECTION,
   * HttpHeaders.Values.CLOSE); httpRequest.setHeader(HttpHeaders.Names.HOST, uri.getHost());
   * httpRequest.setHeader(HttpHeaders.Names.CONTENT_LENGTH, "0"); rtbAdNetwork.setUrlArg("urlArg"); StringBuilder str
   * = new StringBuilder(); str .append(
   * "{\"id\":\"4f8d98e2-4bbd-40bc-8795-22da170700f9\",\"imp\":[{\"id\":\"4f8d98e2-4bbd-40bc-8795-22da170700f9\",\"banner\":{\"w\":120,\"h\":20,\"id\":\"4f8d98e2-4bbd-40bc-8795-22da170700f9\"},\"bidfloorcur\":\"USD\",\"iframebuster\":[\"None\"]}],\"app\":{\"id\":\"0000000000\",\"cat\":[\"IAB1-1\",\"IAB24\",\"IAB5\"]},\"device\":{\"ua\":\"Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334\",\"ip\":\"206.29.182.240\",\"geo\":{\"lat\":37.442901611328125,\"lon\":-122.15139770507812,\"type\":2},\"connectiontype\":2},\"user\":{\"id\":\"1234\",\"buyerid\":\"1234\",\"yob\":1987,\"gender\":\"Male\"},\"at\":2,\"tmax\":200,\"cur\":[\"USD\"]}"
   * ); StringBuilder responseAdm = new StringBuilder();
   * responseAdm.append("<html><body style=\"margin:0;padding:0;\">"); responseAdm .append(
   * "<script src=\"mraid.js\" ></script><style type=\'text/css\'>body { margin:0;padding:0 }  </style> <p align='center'><a href=\'http://www.inmobi.com/\' target='_blank'><img src='http://www.digitalmarket.asia/wp-content/uploads/2012/04/7a4cb5ba9e52331ae91aeee709cd3fe3.jpg' border='0'/></a></p>"
   * ); responseAdm.append("<img src=\'\' height=1 width=1 border=0 /></body></html>"); String clickUrl =
   * "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1"
   * ; String beaconUrl = ""; String externalSiteKey = "f6wqjq1r5v"; ChannelSegmentEntity entity = new
   * ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId, null, null, null, 0, null, null,
   * true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null, 0, null, false, false, false, false, false,
   * false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
   * rtbAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl);
   * TSerializer serializer = new TSerializer(new TSimpleJSONProtocol.Factory());
   * rtbAdNetwork.parseResponse(serializer.toString(bidResponse), HttpResponseStatus.OK);
   * assertEquals(responseAdm.toString(), rtbAdNetwork.responseContent); rtbAdNetwork.setSecondBidPrice(0.23);
   * rtbAdNetwork.setEncryptedBid("abc"); String afterMacros = rtbAdNetwork.replaceRTBMacros(responseAdm.toString());
   * assertEquals(afterMacros, rtbAdNetwork.responseContent); rtbAdNetwork.parseResponse(str.toString(),
   * HttpResponseStatus.NOT_FOUND); assertEquals("", rtbAdNetwork.responseContent);
   * 
   * rtbAdNetwork.setBidRequest(new BidRequest()); rtbAdNetwork.setUrlBase("http://localhost:8800");
   * assertEquals(httpRequest.toString(), rtbAdNetwork.getHttpRequest().toString()); }
   */

  @Test
  public void testConfigureParameters() {
    final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
    sasParams.setRemoteHostIp("206.29.182.240");
    sasParams.setSource("wap");
    sasParams
        .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
    casInternalRequestParameters.setLatLong("37.4429,-122.1514");
    casInternalRequestParameters.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
    final String clickUrl =
        "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
    final String externalSiteKey = "f6wqjq1r5v";
    final String beaconUrl = "";
    final ChannelSegmentEntity entity =
        new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId, null, null, null, 0, null,
            null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null, 0, null, false,
            false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d, null,
            null, 32, new Integer[] {0}));
    assertEquals(
        rtbAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl), false);
  }

  @Test
  public void testShouldTestCategorySetForSiteNameOrAppName() {
    final String externalSiteKey = "f6wqjq1r5v";
    final ChannelSegmentEntity entity =
        new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId, null, null, null, 0, null,
            null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null, 0, null, false,
            false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d, null,
            null, 32, new Integer[] {0}));
    final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
    sasParams.setRemoteHostIp("206.29.182.240");
    sasParams.setSiteId("some_site_id");
    sasParams.setSource("wap");
    WapSiteUACEntity.Builder builder = WapSiteUACEntity.newBuilder();
    builder.setAppType("Games");
    sasParams.setWapSiteUACEntity(new WapSiteUACEntity(builder));
    sasParams
        .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
    casInternalRequestParameters.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
    rtbAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, "", "");

    // First UAC Entity Category should be present as Site Name.
    assertEquals("Games", rtbAdNetwork.getBidRequest().getSite().getName());

    // For App, First UAC Entity Category should be present as App Name.
    sasParams.setSource("app");
    rtbAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, "", "");
    assertEquals("Games", rtbAdNetwork.getBidRequest().getApp().getName());

    // If WapSiteUACEntity is null, then it should fallback to InMobi categories.
    sasParams.setSource("app");
    sasParams.setWapSiteUACEntity(null);
    sasParams.setCategories(Lists.newArrayList(15L, 12L, 11L));
    rtbAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, "", "");
    // 15 mean board games. Refer to CategoryList
    assertEquals("Board", rtbAdNetwork.getBidRequest().getApp().getName());

    // If WapSiteUACEntity is not null, then it should set primary category name from uac.
    sasParams.setSource("app");
    builder = WapSiteUACEntity.newBuilder();
    builder.setAppType("Social");
    sasParams.setWapSiteUACEntity(new WapSiteUACEntity(builder));
    rtbAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, "", "");
    // Setting primary category name from uac.
    assertEquals("Social", rtbAdNetwork.getBidRequest().getApp().getName());

    // If WapSiteUACEntity is null, then it should fallback to InMobi categories.
    sasParams.setSource("wap");
    sasParams.setWapSiteUACEntity(null);
    sasParams.setCategories(Lists.newArrayList(11L, 12L, 15L));
    rtbAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, "", "");
    // 11 mean Games. Refer to CategoryList
    assertEquals("Games", rtbAdNetwork.getBidRequest().getSite().getName());

    // If WapSiteUACEntity and InMobi categories are null.
    sasParams.setSource("wap");
    sasParams.setWapSiteUACEntity(null);
    final ArrayList<Long> list = new ArrayList<Long>();
    sasParams.setCategories(list);
    rtbAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, "", "");

    assertEquals("miscellenous", rtbAdNetwork.getBidRequest().getSite().getName());
  }


  @Test
  public void testShouldHaveFixedBlockedAdvertisers() {
    final String externalSiteKey = "f6wqjq1r5v";
    final ChannelSegmentEntity entity =
        new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId, null, null, null, 0, null,
            null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null, 0, null, false,
            false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d, null,
            null, 32, new Integer[] {0}));
    final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
    sasParams.setRemoteHostIp("206.29.182.240");
    sasParams.setSource("wap");
    sasParams
        .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
    casInternalRequestParameters.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
    rtbAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, "", "");

    // Expected Blocked Advertisers
    final ArrayList<String> expectedBlockedAdvertisers =
        Lists.newArrayList("king.com", "supercell.net", "paps.com", "fhs.com", "china.supercell.com", "supercell.com");
    assertNull(casInternalRequestParameters.getBlockedAdvertisers());
    assertEquals(6, rtbAdNetwork.getBidRequest().getBadv().size());
    assertTrue(rtbAdNetwork.getBidRequest().getBadv().containsAll(expectedBlockedAdvertisers));
  }

  @Test
  public void testShouldAddFixedBlockedAdvertisersForExistingBlockedList() {
    final String externalSiteKey = "f6wqjq1r5v";
    final ChannelSegmentEntity entity =
        new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId, null, null, null, 0, null,
            null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null, 0, null, false,
            false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d, null,
            null, 32, new Integer[] {0}));
    final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
    casInternalRequestParameters.setBlockedAdvertisers(Lists.newArrayList("abcd.com"));
    sasParams.setRemoteHostIp("206.29.182.240");
    sasParams.setSource("wap");
    sasParams
        .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
    casInternalRequestParameters.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
    rtbAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, "", "");

    // Expected Blocked Advertisers
    final ArrayList<String> expectedBlockedAdvertisers =
        Lists.newArrayList("abcd.com", "king.com", "supercell.net", "paps.com", "fhs.com", "china.supercell.com",
            "supercell.com");
    assertEquals(1, casInternalRequestParameters.getBlockedAdvertisers().size());
    assertEquals(7, rtbAdNetwork.getBidRequest().getBadv().size());
    assertTrue(rtbAdNetwork.getBidRequest().getBadv().containsAll(expectedBlockedAdvertisers));
  }

  @Test
  public void testShouldHaveBlockedCategories() {
    final String externalSiteKey = "f6wqjq1r5v";
    final ChannelSegmentEntity entity =
        new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId, null, null, null, 0, null,
            null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null, 0, null, false,
            false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d, null,
            null, 32, new Integer[] {0}));
    final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
    sasParams.setRemoteHostIp("206.29.182.240");
    sasParams.setSource("wap");
    sasParams
        .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
    casInternalRequestParameters.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
    casInternalRequestParameters.setBlockedIabCategories(Lists.newArrayList("IAB-1", "IAB-2", "IAB-3"));
    rtbAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, "", "");

    // Expected Blocked Categories
    final List<String> expectedBlockedCategories = Lists.newArrayList("IAB-1", "IAB-2", "IAB-3");

    // Add family safe blocked categories to the expected list
    final IABCategoriesInterface iabCategoriesMap = new IABCategoriesMap();
    final List<String> familySafeBlockedCategories =
        iabCategoriesMap.getIABCategories(IABCategoriesMap.FAMILY_SAFE_BLOCK_CATEGORIES);
    expectedBlockedCategories.addAll(familySafeBlockedCategories);

    assertEquals(expectedBlockedCategories.size(), rtbAdNetwork.getBidRequest().getBcat().size());
    assertTrue(rtbAdNetwork.getBidRequest().getBcat().containsAll(expectedBlockedCategories));
  }


  @Test
  public void testConfigureParametersWithAllsasparams() {
    final SASRequestParameters sasParams = new SASRequestParameters();
    final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
    sasParams.setSiteId("123");
    sasParams.setSource("app");
    sasParams.setSlot((short) 1);
    final Long[] catLong = new Long[2];
    catLong[0] = (long) 1;
    catLong[1] = (long) 2;
    sasParams.setCategories(Arrays.asList(catLong));
    sasParams.setLocSrc("wifi");
    sasParams.setGender("Male");
    casInternalRequestParameters.setUid("1234");
    sasParams.setAge((short) 26);
    sasParams.setRemoteHostIp("206.29.182.240");
    sasParams
        .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
    casInternalRequestParameters.setLatLong("37.4429,-122.1514");
    casInternalRequestParameters.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
    casInternalRequestParameters.setAuctionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
    final String clickUrl =
        "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
    final String externalSiteKey = "f6wqjq1r5v";
    final String beaconUrl = "";
    final ChannelSegmentEntity entity =
        new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId, null, null, null, 0, null,
            null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null, 0, null, false,
            false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d, null,
            null, 32, new Integer[] {0}));
    assertEquals(
        rtbAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl), true);
  }

  @Test
  public void testRtbGetName() throws Exception {
    assertEquals(rtbAdNetwork.getName(), "rtb");
  }

  @Test
  public void testReplaceMacros() {
    final String clickUrl =
        "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
    final String externalSiteKey = "f6wqjq1r5v";
    final String beaconUrl = "";
    final ChannelSegmentEntity entity =
        new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId, null, null, null, 0, null,
            null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null, 0, null, false,
            false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d, null,
            null, 32, new Integer[] {0}));
    rtbAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl);
    rtbAdNetwork.setCallbackUrl("http://rtb:8970/${AUCTION_ID}/${AUCTION_BID_ID}");
    rtbAdNetwork.setCallbackUrl(rtbAdNetwork.replaceRTBMacros(rtbAdNetwork.getCallbackUrl()));
    assertEquals("http://rtb:8970/SGu1Jpq1IO/ac1a2c944cff0a176643079625b0cad4a1bbe4a3", rtbAdNetwork.getCallbackUrl());
  }

  @Test
  public void testReplaceMacrosAllPosibilities() {
    final String clickUrl =
        "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
    final String externalSiteKey = "f6wqjq1r5v";
    final String beaconUrl = "";
    sasParams.setSource("app");
    sasParams.setRemoteHostIp("206.29.182.240");
    sasParams
        .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
    casInternalRequestParameters.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
    final ChannelSegmentEntity entity =
        new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId, null, null, null, 0, null,
            null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null, 0, null, false,
            false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d, null,
            null, 32, new Integer[] {0}));
    rtbAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl);
    rtbAdNetwork
        .setCallbackUrl("http://rtb:8970/${AUCTION_ID}/${AUCTION_BID_ID}/${AUCTION_IMP_ID}/${AUCTION_SEAT_ID}/${AUCTION_AD_ID}/${AUCTION_PRICE}/${AUCTION_CURRENCY}");
    rtbAdNetwork.setEncryptedBid("abc");
    rtbAdNetwork.setCallbackUrl(rtbAdNetwork.replaceRTBMacros(rtbAdNetwork.getCallbackUrl()));
    assertEquals(
        "http://rtb:8970/SGu1Jpq1IO/ac1a2c944cff0a176643079625b0cad4a1bbe4a3/4f8d98e2-4bbd-40bc-8795-22da170700f9/TO-BE-DETERMINED/1335571993285/0.0/USD",
        rtbAdNetwork.getCallbackUrl());
  }

  @Test
  public void testParseResponse() throws TException {
    final StringBuilder str = new StringBuilder();
    // Temporarily using ixResponseJSON instead of rtbdResponseJSON
    str.append(TestUtils.SampleStrings.ixResponseJson);
    final StringBuilder responseAdm = new StringBuilder();
    responseAdm.append("<html><body style=\"margin:0;padding:0;\">");
    responseAdm
        .append("<script src=\"mraid.js\" ></script><style type=\'text/css\'>body { margin:0;padding:0 }  </style> <p align='center'><a href=\'http://www.inmobi.com/\' target='_blank'><img src='http://www.digitalmarket.asia/wp-content/uploads/2012/04/7a4cb5ba9e52331ae91aeee709cd3fe3.jpg' border='0'/></a></p>");
    responseAdm.append("<img src=\'beacon?b=${WIN_BID}\' height=1 width=1 border=0 />");
    responseAdm.append("</body></html>");
    final String clickUrl =
        "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
    final String beaconUrl = "beacon";
    final String externalSiteKey = "f6wqjq1r5v";
    final ChannelSegmentEntity entity =
        new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId, null, null, null, 0, null,
            null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null, 0, null, false,
            false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d, null,
            null, 32, new Integer[] {0}));
    rtbAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl);
    final TSerializer serializer = new TSerializer(new TSimpleJSONProtocol.Factory());
    rtbAdNetwork.parseResponse(serializer.toString(bidResponse), HttpResponseStatus.OK);
    assertEquals(responseAdm.toString(), rtbAdNetwork.getResponseContent());
    rtbAdNetwork.setEncryptedBid("0.23");
    rtbAdNetwork.setSecondBidPrice(0.23);
    final String afterMacros = rtbAdNetwork.replaceRTBMacros(responseAdm.toString());
    assertEquals(afterMacros, rtbAdNetwork.getResponseContent());
    rtbAdNetwork.parseResponse(str.toString(), HttpResponseStatus.NOT_FOUND);
    assertEquals("", rtbAdNetwork.getResponseContent());
  }

  @Test
  public void testParseResponseWithRMD() throws TException {
    bidResponse.setCur("RMD");
    bidResponse.getSeatbid().get(0).getBidIterator().next().setNurl("${AUCTION_PRICE}${AUCTION_CURRENCY}");
    final StringBuilder responseAdm = new StringBuilder();
    responseAdm.append("<html><body style=\"margin:0;padding:0;\">");
    responseAdm
        .append("<script src=\"mraid.js\" ></script><style type=\'text/css\'>body { margin:0;padding:0 }  </style> <p align='center'><a href=\'http://www.inmobi.com/\' target='_blank'><img src='http://www.digitalmarket.asia/wp-content/uploads/2012/04/7a4cb5ba9e52331ae91aeee709cd3fe3.jpg' border='0'/></a></p>");
    responseAdm
        .append("<img src=\'?b=${WIN_BID}\' height=1 width=1 border=0 /><img src=\'${AUCTION_PRICE}${AUCTION_CURRENCY}\' height=1 width=1 border=0 />");
    responseAdm.append("</body></html>");
    final String clickUrl =
        "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
    final String beaconUrl = "";
    final String externalSiteKey = "f6wqjq1r5v";
    final ChannelSegmentEntity entity =
        new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId, null, null, null, 0, null,
            null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null, 0, null, false,
            false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d, null,
            null, 32, new Integer[] {0}));
    sasParams.setDst(2);
    rtbAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl);
    final TSerializer serializer = new TSerializer(new TSimpleJSONProtocol.Factory());
    rtbAdNetwork.parseResponse(serializer.toString(bidResponse), HttpResponseStatus.OK);
    assertEquals(responseAdm.toString(), rtbAdNetwork.getResponseContent());
    rtbAdNetwork.setEncryptedBid("0.23");
    rtbAdNetwork.setSecondBidPrice(0.23);
    final String afterMacros = rtbAdNetwork.replaceRTBMacros(responseAdm.toString());
    assertEquals(afterMacros, rtbAdNetwork.getResponseContent());
  }

  @Test
  public void testParseResponseWithVideo() throws TException {

    final String oldImpressionId = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
    final String newImpressionId = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee";
    final StringBuilder str = new StringBuilder();
    str.append("{\"id\":\"fc30d281-0147-1000-d51b-000402530000\",\"seatbid\":[{\"bid\":[{\"id\":\"ab73dd4868a0bbadf8fd7527d95136b4\",\"impid\":\"fc30d283-0147-1000-fd85-000534960000\",\"price\":2.0759854316711426,\"adid\":\"1335571993285\",\"nurl\":\"http://partner-wn.dummy-bidder.com/callback/${AUCTION_ID}/${AUCTION_BID_ID}/${AUCTION_PRICE}\",\"adm\":\"http://demo.tremorvideo.com/proddev/vast/vast2RegularLinear.xml\",\"adomain\":[\"mkhoj.com\"],\"iurl\":\"http://www.inmobi.com\",\"cid\":\"cid\",\"crid\":\"crid\",\"attr\":[1],\"ext\":{\"video\":{\"linearity\":1,\"duration\":30,\"type\":\"VAST 2.0\"}}}],\"seat\":\"f55c9d46d7704f8789015a64153a7015\"}],\"bidid\":\"fc30d281-0147-1000-d51b-000402530000\",\"cur\":\"USD\"}");

    final String clickUrl =
        "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/" + oldImpressionId
            + "/-1/1/9cddca11?ds=1";
    final String beaconUrl =
        "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/" + oldImpressionId
            + "/-1/0/9cddca11?ds=1";
    final String externalSiteKey = "f6wqjq1r5v";
    final ChannelSegmentEntity entity =
        new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId, null, null, null, 0, null,
            null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null, 0, null, false,
            false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d, null,
            null, 32, new Integer[] {0}));

    sasParams.setSlot((short) 14);
    sasParams.setBannerVideoSupported(true);
    sasParams.setImpressionId(oldImpressionId);

    // Set the video specific impression Id.
    casInternalRequestParameters.setImpressionIdForVideo(newImpressionId);

    ClickUrlsRegenerator.init(mockConfig);

    rtbAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl);
    final boolean deserializeStatus = rtbAdNetwork.deserializeResponse(str.toString());

    assertEquals(true, deserializeStatus);

    // Verify that impression id is replaced correctly.
    assertEquals(newImpressionId, rtbAdNetwork.getImpressionId());

    rtbAdNetwork.parseResponse(str.toString(), HttpResponseStatus.OK);

    // Verify that new impression id is used in the generate response.
    // 5 times = 1 impression url + 2 beacon url + 2 click url
    assertEquals(5, StringUtils.countMatches(rtbAdNetwork.getResponseContent(), newImpressionId));

    // Old impression id should not be used.
    assertEquals(0, StringUtils.countMatches(rtbAdNetwork.getResponseContent(), oldImpressionId));
  }
}
