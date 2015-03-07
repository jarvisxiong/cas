package com.inmobi.adserve.channels.api;

import static com.github.dreamhead.moco.Moco.by;
import static com.github.dreamhead.moco.Moco.httpserver;
import static com.github.dreamhead.moco.Moco.status;
import static com.github.dreamhead.moco.Moco.uri;
import static com.github.dreamhead.moco.Runner.runner;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.createPartialMock;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.mockStaticNice;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import lombok.Getter;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.support.membermodification.MemberMatcher;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.github.dreamhead.moco.HttpServer;
import com.github.dreamhead.moco.Runner;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.scope.NettyRequestScope;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.casthrift.ADCreativeType;
import com.inmobi.casthrift.DemandSourceType;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Request;
import com.ning.http.client.Response;

@RunWith(PowerMockRunner.class)
@PrepareForTest(InspectorStats.class)
public class BaseAdNetworkImplTest {

    private static Runner runner;
    private static String serverUrl;

    @BeforeClass
    public static void setUpBeforeClass() {
        // Start a WebServer on localhost for testing.
        final HttpServer server = httpserver();
        server.request(by(uri("/get"))).response("Response from /get");
        server.request(by(uri("/emptyResponse"))).response(status(200));
        server.request(by(uri("/http503"))).response(status(503));
        server.response("GENERIC RESPONSE");

        runner = runner(server);
        runner.start();

        serverUrl = "http://localhost:" + server.port();
    }

    @AfterClass
    public static void tearDownAfterClass() {
        runner.stop();
    }

    @Test
    public void testProcessResponseWithAuction() {
        final HttpRequestHandlerBase mockHttpRequestHandlerBase = createMock(HttpRequestHandlerBase.class);
        final Channel mockChannel = createMock(Channel.class);
        final AuctionEngineInterface mockAuctionEngine = createMock(AuctionEngineInterface.class);
        final AdNetworkInterface mockAdNetwork = createMock(AdNetworkInterface.class);

        expect(mockHttpRequestHandlerBase.getAuctionEngine()).andReturn(mockAuctionEngine).anyTimes();
        expect(mockAuctionEngine.areAllChannelSegmentRequestsComplete()).andReturn(true);
        expect(mockAuctionEngine.isAuctionComplete()).andReturn(false);
        expect(mockAuctionEngine.runAuctionEngine()).andReturn(mockAdNetwork);
        expect(mockAdNetwork.getName()).andReturn("MockAdNetwork");
        mockHttpRequestHandlerBase.sendAdResponse(mockAdNetwork, mockChannel);
        expectLastCall().once();
        replayAll();

        final BaseAdNetworkImpl baseAdNetwork = new BaseAdNetworkImpl(mockHttpRequestHandlerBase, mockChannel) {
            @Override
            public URI getRequestUri() throws Exception {
                return new URI(serverUrl);
            }
        };

        baseAdNetwork.processResponse();
        assertTrue(baseAdNetwork.isRequestComplete);
        verifyAll();
    }

    @Test
    public void testProcessResponseWithAuctionAlreadyCompleted() {
        final HttpRequestHandlerBase mockHttpRequestHandlerBase = createMock(HttpRequestHandlerBase.class);
        final Channel mockChannel = createMock(Channel.class);
        final AuctionEngineInterface mockAuctionEngine = createMock(AuctionEngineInterface.class);

        expect(mockHttpRequestHandlerBase.getAuctionEngine()).andReturn(mockAuctionEngine).anyTimes();
        expect(mockAuctionEngine.areAllChannelSegmentRequestsComplete()).andReturn(true);
        expect(mockAuctionEngine.isAuctionComplete()).andReturn(true);
        expect(mockAuctionEngine.isAuctionResponseNull()).andReturn(false);

        replayAll();
        final BaseAdNetworkImpl baseAdNetwork = new BaseAdNetworkImpl(mockHttpRequestHandlerBase, mockChannel) {
            @Override
            public URI getRequestUri() throws Exception {
                return new URI(serverUrl);
            }
        };

        baseAdNetwork.processResponse();
        assertTrue(baseAdNetwork.isRequestComplete);
        verifyAll();
    }

    @Test
    public void testMakeAsyncRequestValidResponse() throws Exception {
        final BaseAdNetworkImpl baseAdNetwork = getBaseAdNetworkForTest();
        final AsyncHttpClient asyncHttpClientForTest = new AsyncHttpClientForTest();

        expect(baseAdNetwork.getAsyncHttpClient()).andReturn(asyncHttpClientForTest);
        expect(baseAdNetwork.getRequestUri()).andReturn(new URI(serverUrl + "/get")).times(2);
        expect(baseAdNetwork.getName()).andReturn("testAdapterName").anyTimes();
        replay(baseAdNetwork);

        MemberMatcher.field(BaseAdNetworkImpl.class, "scope").set(baseAdNetwork, new NettyRequestScope());
        baseAdNetwork.makeAsyncRequest();

        assertTrue(baseAdNetwork.isRequestCompleted());
        assertEquals("AD", baseAdNetwork.adStatus);
        assertEquals(baseAdNetwork.statusCode, 200);
        assertEquals(baseAdNetwork.responseContent, "<html><body>Response from /get</body></html>");
        verifyAll();
    }

    @Test
    public void testMakeAsyncRequestEmptyResponse() throws Exception {
        final BaseAdNetworkImpl baseAdNetwork = getBaseAdNetworkForTest();
        final AsyncHttpClient asyncHttpClientForTest = new AsyncHttpClientForTest();

        expect(baseAdNetwork.getAsyncHttpClient()).andReturn(asyncHttpClientForTest);
        expect(baseAdNetwork.getRequestUri()).andReturn(new URI(serverUrl + "/emptyResponse")).times(2);
        expect(baseAdNetwork.getName()).andReturn("testAdapterName").anyTimes();
        replay(baseAdNetwork);

        MemberMatcher.field(BaseAdNetworkImpl.class, "scope").set(baseAdNetwork, new NettyRequestScope());
        baseAdNetwork.makeAsyncRequest();

        assertTrue(baseAdNetwork.isRequestCompleted());
        assertEquals(baseAdNetwork.adStatus, "NO_AD");
        assertEquals(baseAdNetwork.statusCode, 500);
        assertEquals(baseAdNetwork.responseContent, "");
        verifyAll();
    }

    @Test
    public void testMakeAsyncRequestWithHttp503() throws Exception {
        final BaseAdNetworkImpl baseAdNetwork = getBaseAdNetworkForTest();
        final AsyncHttpClient asyncHttpClientForTest = new AsyncHttpClientForTest();

        expect(baseAdNetwork.getAsyncHttpClient()).andReturn(asyncHttpClientForTest);
        expect(baseAdNetwork.getRequestUri()).andReturn(new URI(serverUrl + "/http503")).times(2);
        expect(baseAdNetwork.getName()).andReturn("testAdapterName").anyTimes();
        replay(baseAdNetwork);

        MemberMatcher.field(BaseAdNetworkImpl.class, "scope").set(baseAdNetwork, new NettyRequestScope());
        baseAdNetwork.makeAsyncRequest();

        assertTrue(baseAdNetwork.isRequestCompleted());
        assertEquals(baseAdNetwork.adStatus, "NO_AD");
        assertEquals(baseAdNetwork.statusCode, 503);
        assertEquals(baseAdNetwork.responseContent, "");
        verifyAll();
    }

    @Test
    public void testMakeAsyncRequestWithInvalidURL() throws Exception {
        final BaseAdNetworkImpl baseAdNetwork = getBaseAdNetworkForTest();
        final AsyncHttpClient asyncHttpClientForTest = new AsyncHttpClientForTest();

        mockStaticNice(InspectorStats.class);
        expect(baseAdNetwork.getAsyncHttpClient()).andReturn(asyncHttpClientForTest);
        expect(baseAdNetwork.getRequestUri()).andReturn(new URI("http://localhos-invalidURL")).times(2);
        expect(baseAdNetwork.getName()).andReturn("testAdapterName").anyTimes();
        replayAll();

        MemberMatcher.field(BaseAdNetworkImpl.class, "scope").set(baseAdNetwork, new NettyRequestScope());
        baseAdNetwork.makeAsyncRequest();

        assertTrue(baseAdNetwork.isRequestCompleted());
        assertEquals(baseAdNetwork.adStatus, "TIME_OUT");
        assertEquals(baseAdNetwork.statusCode, 0);
        assertEquals(baseAdNetwork.responseContent, null);
        verifyAll();
    }

    /**
     * This method returns an object of BaseAdNetworkImpl which is used for testing various cases of makeAsyncRequest().
     */
    private BaseAdNetworkImpl getBaseAdNetworkForTest() throws Exception {
        final HttpRequestHandlerBase mockHttpRequestHandlerBase = createMock(HttpRequestHandlerBase.class);
        final Channel mockChannel = createMock(Channel.class);
        final AuctionEngineInterface mockAuctionEngine = createMock(AuctionEngineInterface.class);
        final AdNetworkInterface mockAdNetwork = createMock(AdNetworkInterface.class);
        final SASRequestParameters mockSasParam = createMock(SASRequestParameters.class);
        final ChannelSegmentEntity mockEntity = createMock(ChannelSegmentEntity.class);
        final CasInternalRequestParameters mockCasInternal = createMock(CasInternalRequestParameters.class);

        expect(mockHttpRequestHandlerBase.getAuctionEngine()).andReturn(mockAuctionEngine).anyTimes();
        mockHttpRequestHandlerBase.sendAdResponse(isA(AdNetworkInterface.class), isA(Channel.class));
        expectLastCall().once();
        expect(mockAuctionEngine.areAllChannelSegmentRequestsComplete()).andReturn(true);
        expect(mockAuctionEngine.isAuctionComplete()).andReturn(false);
        expect(mockAuctionEngine.runAuctionEngine()).andReturn(mockAdNetwork);
        expect(mockAdNetwork.getName()).andReturn("MockAdNetwork");
        expect(mockEntity.getExternalSiteKey()).andReturn("test-external-site-key");
        expect(mockEntity.getAdgroupIncId()).andReturn(5L);
        expect(mockSasParam.getSiteIncId()).andReturn(10L);
        expect(mockSasParam.getImpressionId()).andReturn("AAAAAAAAAABBBBBBBBBCCCCCCCCCCCC");
        expect(mockSasParam.getUserAgent()).andReturn("test-user-agent");
        expect(mockSasParam.getRemoteHostIp()).andReturn("9.9.9.9");
        expect(mockCasInternal.isTraceEnabled()).andReturn(true);
        expect(mockChannel.isOpen()).andReturn(true).times(1);



        replayAll();

        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        final IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);

        final String host = serverUrl + "/get";
        final String[] mockedMethods = {"getAsyncHttpClient", "getName"};
        final Object[] constructorArgs = new Object[] {mockHttpRequestHandlerBase, mockChannel};
        final BaseAdNetworkImpl baseAdNetwork =
                createPartialMock(BaseAdNetworkImpl.class, mockedMethods, constructorArgs);
        baseAdNetwork.setHost(host);
        baseAdNetwork.configureParameters(mockSasParam, mockCasInternal, mockEntity, "", "", 14L, null);
        return baseAdNetwork;
    }

    @Test
    public void testMakeAsyncRequestJSAdTag() throws Exception {
        final HttpRequestHandlerBase mockHttpRequestHandlerBase = createMock(HttpRequestHandlerBase.class);
        final Channel mockChannel = createMock(Channel.class);
        final AuctionEngineInterface mockAuctionEngine = createMock(AuctionEngineInterface.class);
        final AdNetworkInterface mockAdNetwork = createMock(AdNetworkInterface.class);
        final SASRequestParameters mockSasParam = createMock(SASRequestParameters.class);
        final ChannelSegmentEntity mockEntity = createMock(ChannelSegmentEntity.class);
        final CasInternalRequestParameters mockCasInternal = createMock(CasInternalRequestParameters.class);

        expect(mockHttpRequestHandlerBase.getAuctionEngine()).andReturn(mockAuctionEngine).anyTimes();
        mockHttpRequestHandlerBase.sendAdResponse(isA(AdNetworkInterface.class), isA(Channel.class));
        expectLastCall().once();
        expect(mockAuctionEngine.areAllChannelSegmentRequestsComplete()).andReturn(true);
        expect(mockAuctionEngine.isAuctionComplete()).andReturn(false);
        expect(mockAuctionEngine.runAuctionEngine()).andReturn(mockAdNetwork);
        expect(mockAdNetwork.getName()).andReturn("MockAdNetwork");
        expect(mockEntity.getExternalSiteKey()).andReturn("test-external-site-key");
        expect(mockEntity.getAdgroupIncId()).andReturn(5L);
        expect(mockSasParam.getSiteIncId()).andReturn(10L);
        expect(mockSasParam.getImpressionId()).andReturn("AAAAAAAAAABBBBBBBBBCCCCCCCCCCCC");

        replayAll();
        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        final IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);

        final String[] mockedMethods = {"getAsyncHttpClient", "getName", "useJsAdTag"};
        final Object[] constructorArgs = new Object[] {mockHttpRequestHandlerBase, mockChannel};
        final BaseAdNetworkImpl baseAdNetwork =
                createPartialMock(BaseAdNetworkImpl.class, mockedMethods, constructorArgs);
        final String host = serverUrl + "/get";
        baseAdNetwork.setHost(host);
        baseAdNetwork.configureParameters(mockSasParam, mockCasInternal, mockEntity, "", "", 14L, null);


        expect(baseAdNetwork.useJsAdTag()).andReturn(true);
        expect(baseAdNetwork.getName()).andReturn("testAdapterName").anyTimes();
        replay(baseAdNetwork);

        baseAdNetwork.makeAsyncRequest();
        assertTrue(baseAdNetwork.isRequestCompleted());
        verifyAll();
    }

    @Test
    public void testGetCategories() throws Exception {
        final SASRequestParameters mockSasParam = createMock(SASRequestParameters.class);
        final ChannelSegmentEntity mockEntity = createMock(ChannelSegmentEntity.class);

        expect(mockSasParam.getCategories()).andReturn(Arrays.asList(31L, 32L, 39L)).anyTimes();
        expect(mockSasParam.getImpressionId()).andReturn("AAAAAAAAAABBBBBBBBBCCCCCCCCCCCC");
        expect(mockSasParam.getSiteIncId()).andReturn(10L);
        expect(mockEntity.getAdgroupIncId()).andReturn(5L);
        expect(mockEntity.getExternalSiteKey()).andReturn("test-external-site-key");
        expect(mockEntity.getCategoryTaxonomy()).andReturn(new Long[] {31L, 32L, 33L}).times(4);
        expect(mockEntity.isAllTags()).andReturn(true).times(1).andReturn(false).times(3);
        replayAll();

        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        final IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);

        final BaseAdNetworkImpl baseAdNetwork = new BaseAdNetworkImpl(null, null) {
            @Override
            public URI getRequestUri() throws Exception {
                return null;
            }
        };
        final String host = serverUrl + "/get";
        baseAdNetwork.setHost(host);
        baseAdNetwork.configureParameters(mockSasParam, null, mockEntity, "", "", 14L, null);

        // Test to get all categories
        String categories = baseAdNetwork.getCategories(';');
        assertEquals("Health & Fitness;Lifestyle;Brides & Weddings", categories);

        // Test to get all IAB category with entity.isAllTags=false
        categories = baseAdNetwork.getCategories(';', true, true);
        assertEquals("IAB7;IAB18", categories);

        // Test to get one IAB category with entity.isAllTags=false
        categories = baseAdNetwork.getCategories(';', false, true);
        assertEquals("IAB7", categories);

        // Test to get one category with entity.isAllTags=false
        categories = baseAdNetwork.getCategories(';', false, false);
        assertEquals("Health & Fitness", categories);

        verifyAll();
    }

    @Test
    public void testIsValidResponse() {
        final BaseAdNetworkImpl baseAdNetwork = new BaseAdNetworkImpl(null, null) {
            @Override
            public URI getRequestUri() throws Exception {
                return null;
            }
        };

        // Valid Response
        boolean response = baseAdNetwork.isValidResponse("Valid Response", HttpResponseStatus.OK);
        assertTrue(response);

        // Empty Response
        response = baseAdNetwork.isValidResponse("", HttpResponseStatus.OK);
        assertEquals(baseAdNetwork.statusCode, 500);
        assertFalse(response);

        // Invalid Response
        response = baseAdNetwork.isValidResponse("ERROR: Bad Request", HttpResponseStatus.BAD_REQUEST);
        assertFalse(response);
    }

    @Test
    public void testIsInterstitial() throws Exception {
        final ChannelSegmentEntity mockEntity = createMock(ChannelSegmentEntity.class);
        final SASRequestParameters mockSasParam = createMock(SASRequestParameters.class);

        expect(mockEntity.getExternalSiteKey()).andReturn("test-external-site-key").times(2);
        expect(mockEntity.getAdgroupIncId()).andReturn(5L).times(2);
        expect(mockSasParam.getSiteIncId()).andReturn(10L).times(2);
        expect(mockSasParam.getImpressionId()).andReturn("AAAAAAAAAABBBBBBBBBCCCCCCCCCCCC").times(2);

        replayAll();

        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        final IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);

        final BaseAdNetworkImpl baseAdNetwork = new BaseAdNetworkImpl(null, null) {
            @Override
            public URI getRequestUri() throws Exception {
                return null;
            }
        };
        final String host = serverUrl + "/get";
        baseAdNetwork.setHost(host);

        baseAdNetwork.configureParameters(mockSasParam, null, mockEntity, "", "", 14L, null);
        boolean result = baseAdNetwork.isInterstitial();
        assertTrue(result);

        baseAdNetwork.configureParameters(mockSasParam, null, mockEntity, "", "", 13L, null);
        result = baseAdNetwork.isInterstitial();
        assertFalse(result);

        verifyAll();
    }

    /**
     * This method tests various small methods of BaseAdNetworkImpl.
     */
    @Test
    public void testMiscMethods() throws Exception {
        final ChannelSegmentEntity mockEntity = createMock(ChannelSegmentEntity.class);
        final SASRequestParameters mockSasParam = createMock(SASRequestParameters.class);

        expect(mockEntity.getExternalSiteKey()).andReturn("test-external-site-key").times(1);
        expect(mockEntity.getAdgroupIncId()).andReturn(5L).times(1);
        expect(mockSasParam.getSiteIncId()).andReturn(10L).times(1);
        expect(mockSasParam.getImpressionId()).andReturn("AAAAAAAAAABBBBBBBBBCCCCCCCCCCCC").times(1);
        expect(mockSasParam.getAge()).andReturn((short) 11).times(3);
        expect(mockSasParam.getDst()).andReturn(2).times(1).andReturn(6).times(1).andReturn(8).times(1);
        expect(mockSasParam.getOsId()).andReturn(3).times(1).andReturn(5).times(1);
        expect(mockSasParam.getSource()).andReturn(null).times(1).andReturn("WAP").times(2).andReturn("APP").times(2);
        replayAll();

        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        final IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);

        final String[] mockedMethods = {"isNativeRequest"};
        final BaseAdNetworkImpl baseAdNetwork = createPartialMock(BaseAdNetworkImpl.class, mockedMethods);
        final String host = serverUrl + "/get";
        baseAdNetwork.setHost(host);

        expect(baseAdNetwork.isNativeRequest()).andReturn(true).times(1).andReturn(false).times(2);

        baseAdNetwork.configureParameters(mockSasParam, null, mockEntity, "", "", 14L, null);
        replay(baseAdNetwork);

        // Test getCreativeType().
        assertEquals(baseAdNetwork.getCreativeType(), ADCreativeType.NATIVE);
        assertEquals(baseAdNetwork.getCreativeType(), ADCreativeType.BANNER);
        baseAdNetwork.isVideoRequest = true;
        assertEquals(baseAdNetwork.getCreativeType(), ADCreativeType.INTERSTITIAL_VIDEO);

        // Test getHashedValue()
        String res = baseAdNetwork.getHashedValue("TEST STRING", "MD5");
        assertEquals(res, "2d7d687432758a8eeeca7b7e5d518e7f");
        res = baseAdNetwork.getHashedValue("TEST STRING", "SHA-1");
        assertEquals(res, "d39d009c05797a93a79720952e99c7054a24e7c4");

        // Test cleanup()
        baseAdNetwork.cleanUp();
        assertEquals(baseAdNetwork.adStatus, "TERM");
        assertEquals(baseAdNetwork.getResponseStruct().getAdStatus(), "TERM");

        // Test getURLEncode()
        res = baseAdNetwork.getURLEncode("this is a sample sentence.", "UTF-8");
        assertEquals(res, "this+is+a+sample+sentence.");
        // Test getURLEncode()
        res = baseAdNetwork.getURLEncode("special chars: ? # @.", "UTF-8");
        assertEquals(res, "special+chars%3A+%3F+%23+%40.");

        // Test getYearofBirth()
        final String yearOfBirth = baseAdNetwork.getYearofBirth();
        assertEquals(yearOfBirth, Integer.toString(new GregorianCalendar().get(Calendar.YEAR) - 11));

        // Test getDst()
        assertEquals(baseAdNetwork.getDst(), DemandSourceType.DCP);
        assertEquals(baseAdNetwork.getDst(), DemandSourceType.RTBD);
        assertEquals(baseAdNetwork.getDst(), DemandSourceType.IX);

        // TEst getSelectedSlotId()
        assertEquals(baseAdNetwork.getSelectedSlotId(), new Short((short) 14));

        // Test isAndroid() and isIOS()
        assertEquals(baseAdNetwork.isAndroid(), true);
        assertEquals(baseAdNetwork.isIOS(), true);

        // Test isApp()
        assertEquals(baseAdNetwork.isApp(), false);
        assertEquals(baseAdNetwork.isApp(), false);
        assertEquals(baseAdNetwork.isApp(), true);


        verifyAll();
    }

    @Test
    public void testGetUid() throws Exception {
        final ChannelSegmentEntity mockEntity = createMock(ChannelSegmentEntity.class);
        final SASRequestParameters mockSasParam = createMock(SASRequestParameters.class);
        final CasInternalRequestParameters mockCasInternalRequestParameters =
                createMock(CasInternalRequestParameters.class);

        expect(mockEntity.getExternalSiteKey()).andReturn("test-external-site-key").times(1);
        expect(mockEntity.getAdgroupIncId()).andReturn(5L).times(1);
        expect(mockSasParam.getSiteIncId()).andReturn(10L).times(1);
        expect(mockSasParam.getImpressionId()).andReturn("AAAAAAAAAABBBBBBBBBCCCCCCCCCCCC").times(1);
        expect(mockCasInternalRequestParameters.getUidIFA()).andReturn(null).times(6)
                .andReturn("IFA0000000000000000000000000000").times(2);
        expect(mockCasInternalRequestParameters.getGpid()).andReturn(null).times(5)
                .andReturn("GPID0000000000000000000000000000").times(2);
        expect(mockCasInternalRequestParameters.getUidSO1()).andReturn(null).times(4)
                .andReturn("SO100000000000000000000000000000").times(2);
        expect(mockCasInternalRequestParameters.getUidMd5()).andReturn(null).times(3)
                .andReturn("MD500000000000000000000000000000").times(2);
        expect(mockCasInternalRequestParameters.getUidO1()).andReturn(null).times(2)
                .andReturn("O1000000000000000000000000000000").times(2);
        expect(mockCasInternalRequestParameters.getUidIDUS1()).andReturn(null).times(1)
                .andReturn("IDUS1000000000000000000000000000").times(2);
        expect(mockCasInternalRequestParameters.getUid()).andReturn("UID00000000000000000000000000000").times(2);
        expect(mockCasInternalRequestParameters.getUidADT()).andReturn("1").times(2);

        replayAll();


        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        final IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);

        final BaseAdNetworkImpl baseAdNetwork = new BaseAdNetworkImpl(null, null) {
            @Override
            public URI getRequestUri() throws Exception {
                return null;
            }
        };
        final String host = serverUrl + "/get";
        baseAdNetwork.setHost(host);
        baseAdNetwork
                .configureParameters(mockSasParam, mockCasInternalRequestParameters, mockEntity, "", "", 14L, null);

        assertEquals("UID00000000000000000000000000000", baseAdNetwork.getUid());
        assertEquals("IDUS1000000000000000000000000000", baseAdNetwork.getUid());
        assertEquals("O1000000000000000000000000000000", baseAdNetwork.getUid());
        assertEquals("MD500000000000000000000000000000", baseAdNetwork.getUid());
        assertEquals("SO100000000000000000000000000000", baseAdNetwork.getUid());
        assertEquals("GPID0000000000000000000000000000", baseAdNetwork.getUid());
        assertEquals("IFA0000000000000000000000000000", baseAdNetwork.getUid());

        verifyAll();
    }

    /**
     * This a sync version of Async HTTP Client for testing.
     */
    public class AsyncHttpClientForTest extends AsyncHttpClient {

        @Override
        public <T> ListenableFuture<T> executeRequest(final Request request, final AsyncHandler<T> handler)
                throws IOException {
            final AsyncHandlerWithSignal asyncHandlerWithSignal = new AsyncHandlerWithSignal(handler);
            final ListenableFuture lf = super.executeRequest(request, asyncHandlerWithSignal);

            // Wait till the callback is completed. Wait Timeout: 5 secs.
            try {
                asyncHandlerWithSignal.getSignal().await(5, TimeUnit.SECONDS);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
            return lf;
        }
    }


    /**
     * This extended class provides a signal to determine the request completion.
     */
    public class AsyncHandlerWithSignal extends AsyncCompletionHandler<Response> {

        @Getter
        private final CountDownLatch signal = new CountDownLatch(1);
        private final AsyncCompletionHandler reqHandlerFromBaseAdNetwork;

        AsyncHandlerWithSignal(final AsyncHandler reqHandlerFromBaseAdNetwork) {
            this.reqHandlerFromBaseAdNetwork = (AsyncCompletionHandler) reqHandlerFromBaseAdNetwork;
        }

        @Override
        public Response onCompleted(final Response response) throws Exception {
            reqHandlerFromBaseAdNetwork.onCompleted(response);
            signal.countDown();
            return response;
        }

        @Override
        public void onThrowable(final Throwable t) {
            reqHandlerFromBaseAdNetwork.onThrowable(t);
            signal.countDown();
        }
    }
}
