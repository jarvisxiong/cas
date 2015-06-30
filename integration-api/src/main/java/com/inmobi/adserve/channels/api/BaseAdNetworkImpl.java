package com.inmobi.adserve.channels.api;


import static com.inmobi.adserve.channels.util.config.GlobalConstant.CPC;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;

import com.google.inject.Provider;
import com.inmobi.adserve.adpool.RequestedAdType;
import com.inmobi.adserve.channels.api.provider.AsyncHttpClientProvider;
import com.inmobi.adserve.channels.api.trackers.DefaultLazyInmobiAdTrackerBuilderFactory;
import com.inmobi.adserve.channels.api.trackers.InmobiAdTracker;
import com.inmobi.adserve.channels.api.trackers.InmobiAdTrackerBuilder;
import com.inmobi.adserve.channels.api.trackers.InmobiAdTrackerBuilderFactory;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.scope.NettyRequestScope;
import com.inmobi.adserve.channels.util.CategoryList;
import com.inmobi.adserve.channels.util.DocumentBuilderHelper;
import com.inmobi.adserve.channels.util.IABCategoriesMap;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.JaxbHelper;
import com.inmobi.adserve.channels.util.Utils.ExceptionBlock;
import com.inmobi.adserve.channels.util.config.GlobalConstant;
import com.inmobi.casthrift.ADCreativeType;
import com.inmobi.casthrift.DemandSourceType;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.Response;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.Getter;
import lombok.Setter;


// This abstract class have base functionality of TPAN adapters.
public abstract class BaseAdNetworkImpl implements AdNetworkInterface {
    private static final Logger LOG = LoggerFactory.getLogger(BaseAdNetworkImpl.class);

    protected Marker traceMarker;
    protected static final String USD = GlobalConstant.USD;
    protected static final String WAP = GlobalConstant.WAP;
    protected static final String APP = GlobalConstant.APP;
    protected static final String UA = "ua";
    protected static final String IP = "ip";
    protected static final String LAT = "lat";
    protected static final String LONG = "long";
    protected static final String ZIP = "zip";
    protected static final String COUNTRY = "country";
    protected static final String GENDER = "gender";
    protected static final String DEFAULT_EMPTY_STRING = StringUtils.EMPTY;
    protected static final String NO_AD = GlobalConstant.NO_AD;
    protected static final String NATIVE_STRING = GlobalConstant.NATIVE_STRING;
    protected static final String AD_STRING = "AD";
    protected static final String MRAID = "<script src=\"mraid.js\" ></script>";
    protected static final String CONTENT_TYPE_VALUE = "application/json; charset=utf-8";
    protected static final String TERM = "TERM";
    protected static final String LATLON = GlobalConstant.LATLON;
    protected static final String DERIVED_LAT_LON = "DERIVED_LAT_LON";
    protected static final String WIFI = "WIFI";
    protected static final String NON_WIFI = "NON_WIFI";
    protected static final String CCID = "CCID";
    protected static final String GET = "GET";
    protected static final String POST = "POST";

    @Inject
    protected static JaxbHelper jaxbHelper;

    @Inject
    protected static DocumentBuilderHelper documentBuilderHelper;

    @Inject
    private static AsyncHttpClientProvider asyncHttpClientProvider;

    @Inject
    private static NettyRequestScope scope;

    @Inject
    private static Provider<Marker> traceMarkerProvider;

    @DefaultLazyInmobiAdTrackerBuilderFactory
    @Inject
    private static InmobiAdTrackerBuilderFactory inmobiAdTrackerBuilderFactory;
    protected InmobiAdTracker inmobiAdTracker;

    private boolean isCpc;
    public volatile boolean isRequestComplete = false;
    protected ChannelFuture future;
    protected Bootstrap clientBootstrap;
    protected HttpRequest request;
    protected long startTime;
    protected int statusCode;
    @Getter
    protected String responseContent;
    @Getter
    protected String adStatus = NO_AD;
    protected ThirdPartyAdResponse.ResponseStatus errorStatus = ThirdPartyAdResponse.ResponseStatus.SUCCESS;
    protected boolean isHTMLResponseSupported = true;
    protected boolean isNativeResponseSupported = false;
    protected boolean isVideoRequest = false;
    protected boolean isNativeRequest = false;
    protected boolean isRtbPartner = false;
    protected boolean isIxPartner = false;
    protected SASRequestParameters sasParams;
    protected CasInternalRequestParameters casInternalRequestParameters;
    protected HttpRequestHandlerBase baseRequestHandler = null;
    protected String requestUrl = DEFAULT_EMPTY_STRING;
    protected ChannelSegmentEntity entity;
    protected String externalSiteId;
    @Getter
    @Setter
    protected String host;
    protected String impressionId;
    protected String source;
    protected String blindedSiteId;
    protected Short selectedSlotId;
    protected RepositoryHelper repositoryHelper;
    protected String format = GlobalConstant.UTF_8;
    protected final Channel serverChannel;

    private Map<?, ?> responseHeaders;
    private long latency;
    private long connectionLatency;
    private ThirdPartyAdResponse responseStruct;
    private String adapterName;

    protected Double forwardedBidFloor;
    protected Double forwardedBidGuidance;

    @Inject
    protected static IPRepository ipRepository;
    private boolean isIPResolutionDisabled = true;
    private String publicHostName;
    protected boolean isByteResponseSupported = false;

    public BaseAdNetworkImpl(final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        this.baseRequestHandler = baseRequestHandler;
        this.serverChannel = serverChannel;
        if (traceMarkerProvider != null) {
            traceMarker = traceMarkerProvider.get();
        }
    }

    @Override
    public void setName(final String adapterName) {
        this.adapterName = adapterName;
    }

    @Override
    public String getName() {
        return adapterName;
    }

    @Override
    public boolean isRtbPartner() {
        return isRtbPartner;
    }

    @Override
    public boolean isIxPartner() {
        return isIxPartner;
    }

    /**
     * This method builds the InmobiAdTracker object.<br>
     * <br>
     *
     * 1) This function can be called anytime after method configureParameters has been called and impressionId has been
     * generated. <br>
     * <br>
     * 2) The InmobiAdTracker object will be built only once, subsequent calls to this method will be ignored. <br>
     * <br>
     * 3) To override any field of the InmobiAdTracker Builder, please override overrideInmobiAdTracker() in the
     * specific adapter <br>
     * <br>
     * 4) To build a non default InmobiAdTracker: <br>
     * a) Create new implementations of InmobiAdTracker and InmobiAdTrackerBuilder <br>
     * b) Create a new InmobiAdTrackerBuilderFactory annotation <br>
     * c) Add a new entry in InmobiAdTrackerModule <br>
     * d) Redeclare InmobiAdTrackerBuilderFactory with the new annotation
     */
    protected final void buildInmobiAdTracker() {
        if (null != inmobiAdTracker) {
            return;
        }

        LOG.debug("Generating tracker urls for {} with impressionId: {}", this.getName(), this.impressionId);
        final InmobiAdTrackerBuilder builder =
                getInmobiAdTrackerBuilderFactory().getBuilder(sasParams, impressionId, isCpc);
        overrideInmobiAdTracker(builder);
        inmobiAdTracker = builder.buildInmobiAdTracker();
    }

    /**
     * This method is for overriding any fields of the InmobiAdTrackerBuilder
     * 
     * @param builder
     */
    protected void overrideInmobiAdTracker(final InmobiAdTrackerBuilder builder) {
        // Override this method in the specific adapter
    }

    /**
     * Returns the InmobiAdTrackerBuilderFactory instance
     * 
     * @return
     */
    protected InmobiAdTrackerBuilderFactory getInmobiAdTrackerBuilderFactory() {
        return inmobiAdTrackerBuilderFactory;
    }

    @Override
    public final String getClickUrl() {
        if (null == inmobiAdTracker) {
            InspectorStats.incrementStatCount(getName(), InspectorStrings.TRACKER_BEING_FETCHED_BEFORE_GENERATION);
            InspectorStats.incrementStatCount(InspectorStrings.TRACKER_BEING_FETCHED_BEFORE_GENERATION);
            return null;
        }
        return inmobiAdTracker.getClickUrl();
    }

    @Override
    public final String getBeaconUrl() {
        if (null == inmobiAdTracker) {
            InspectorStats.incrementStatCount(getName(), InspectorStrings.TRACKER_BEING_FETCHED_BEFORE_GENERATION);
            InspectorStats.incrementStatCount(InspectorStrings.TRACKER_BEING_FETCHED_BEFORE_GENERATION);
            return null;
        }
        return inmobiAdTracker.getBeaconUrl();
    }

    @Override
    public void processResponse() {
        LOG.debug("Inside process Response for the partner: {}", getName());
        if (isRequestComplete) {
            LOG.debug("Already cleanedup so returning from process response");
            return;
        }
        getResponseAd();
        isRequestComplete = true;
        if (baseRequestHandler.getAuctionEngine().areAllChannelSegmentRequestsComplete()) {
            LOG.debug("areAllChannelSegmentRequestsComplete is true");
            if (baseRequestHandler.getAuctionEngine().isAuctionComplete()) {
                LOG.debug("Auction has run already");
                if (baseRequestHandler.getAuctionEngine().isAuctionResponseNull()) {
                    LOG.debug("Auction has returned null so processing dcp list");
                    // Process dcp partner response.
                    baseRequestHandler.processDcpPartner(serverChannel, this);
                    return;
                }
                LOG.debug("Auction response is not null so sending auction response");
                return;
            } else {
                final AdNetworkInterface highestBid = baseRequestHandler.getAuctionEngine().runAuctionEngine();
                if (highestBid != null) {
                    LOG.debug("Sending auction response of {}", highestBid.getName());
                    baseRequestHandler.sendAdResponse(highestBid, serverChannel);
                    LOG.debug("sent auction response");
                    return;
                } else {
                    LOG.debug("rtb auction has returned null so processing dcp list");
                    baseRequestHandler.processDcpList(serverChannel);
                }
            }
        }
        LOG.debug("Auction has not run so waiting....");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public boolean makeAsyncRequest() {
        LOG.debug("In Adapter {}", this.getClass().getSimpleName());
        if (useJsAdTag()) {
            startTime = System.currentTimeMillis();
            buildInmobiAdTracker();
            generateJsAdResponse();
            latency = System.currentTimeMillis() - startTime;
            LOG.debug("{} operation complete latency {}", getName(), latency);
            processResponse();
            LOG.debug("sent jsadcode ... returning from make NingRequest");
            return true;
        }

        try {
            requestUrl = getRequestUri().toString();
            RequestBuilder ningRequestBuilder = getNingRequestBuilder();
            setVirtualHost(ningRequestBuilder);
            final Request ningRequest = ningRequestBuilder.build();
            LOG.debug("request : {}", ningRequest);
            startTime = System.currentTimeMillis();
            final boolean isTraceEnabled = casInternalRequestParameters.isTraceEnabled();
            getAsyncHttpClient().executeRequest(ningRequest, new AsyncCompletionHandler() {
                @Override
                public Response onCompleted(final Response response) throws Exception {
                    latency = System.currentTimeMillis() - startTime;
                    if (!serverChannel.isOpen()) {
                        InspectorStats.updateYammerTimerStats(getName(), latency, false);
                        return response;
                    }

                    MDC.put("requestId", String.format("0x%08x", serverChannel.hashCode()));
                    LOG.debug("isTraceEnabled {} scope : {}", isTraceEnabled, scope);

                    if (!isRequestCompleted()) {
                        InspectorStats.updateYammerTimerStats(getName(), latency, true);
                        LOG.debug("Operation complete for channel partner: {}", getName());
                        LOG.debug("{} operation complete latency {}", getName(), latency);

                        final String responseStr = response.getResponseBody(GlobalConstant.UTF_8);
                        final HttpResponseStatus httpResponseStatus =
                                HttpResponseStatus.valueOf(response.getStatusCode());

                        LOG.debug(traceMarker, "{} status code is {}", getName(), httpResponseStatus);
                        if (isByteResponseSupported) {
                            byte[] responseBytes = response.getResponseBodyAsBytes();
                            parseResponse(responseBytes, httpResponseStatus);
                        } else {
                            parseResponse(responseStr, httpResponseStatus);
                        }
                        processResponse();
                    }
                    return response;
                }

                @Override
                public void onThrowable(final Throwable t) {
                    latency = System.currentTimeMillis() - startTime;
                    InspectorStats.updateYammerTimerStats(getName(), latency, false);
                    if (t instanceof java.io.IOException) {
                        InspectorStats.incrementStatCount(InspectorStrings.IO_EXCEPTION);
                        InspectorStats.incrementStatCount(getName(), InspectorStrings.IO_EXCEPTION);
                    } else if (!(t instanceof java.util.concurrent.TimeoutException)) {
                        InspectorStats.incrementStatCount(InspectorStrings.UNCAUGHT_EXCEPTIONS, t.getClass()
                                .getSimpleName());
                        InspectorStats.incrementStatCount(getName(), t.getClass().getSimpleName());
                        if (LOG.isDebugEnabled()) {
                            final String message = "stack trace is -> " + ExceptionBlock.getCustomStackTrace(t);
                            LOG.debug(traceMarker, message);
                        }
                    }

                    if (isRequestCompleted() || !serverChannel.isOpen()) {
                        return;
                    }

                    MDC.put("requestId", String.format("0x%08x", serverChannel.hashCode()));
                    LOG.debug("onThrowable isTraceEnabled {} scope : {}", isTraceEnabled, scope);
                    LOG.debug("error while fetching response from: {} {}", getName(), t);

                    String dst;
                    if (isRtbPartner()) {
                        dst = "RTBD";
                    } else if (isIxPartner()) {
                        dst = "IX";
                    } else {
                        dst = "DCP";
                    }
                    InspectorStats.updateYammerTimerStats(dst, InspectorStrings.CLIENT_TIMER_LATENCY, latency);


                    if (t instanceof java.net.ConnectException) {
                        LOG.debug("{} connection timeout latency {}", getName(), latency);
                        adStatus = GlobalConstant.TIME_OUT;
                        InspectorStats.incrementStatCount(getName(), InspectorStrings.CONNECTION_TIMEOUT);
                        InspectorStats.incrementStatCount(InspectorStrings.CONNECTION_TIMEOUT);
                        processResponse();
                        return;
                    }

                    if (t instanceof java.util.concurrent.TimeoutException) {
                        LOG.debug("{} timeout latency {}", getName(), latency);
                        adStatus = GlobalConstant.TIME_OUT;
                        processResponse();
                        InspectorStats.incrementStatCount(InspectorStrings.TIMEOUT_EXCEPTION);
                        return;
                    }

                    LOG.debug("{} error latency {}", getName(), latency);
                    adStatus = TERM;
                    processResponse();
                }
            });
        } catch (final Exception e) {
            LOG.debug("Exception in {} makeAsyncRequest : {}", getName(), e);
        }
        LOG.debug("{} returning from make NingRequest", getName());
        return true;
    }

    protected AsyncHttpClient getAsyncHttpClient() {
        return asyncHttpClientProvider.getDcpAsyncHttpClient();
    }

    protected RequestBuilder getNingRequestBuilder() throws Exception {
        URI uri = getRequestUri();
        if (uri.getPort() == -1) {
            uri = new URIBuilder(uri).setPort(80).build();
        }

        return new RequestBuilder().setUrl(uri.toString())
                .setHeader(HttpHeaders.Names.USER_AGENT, sasParams.getUserAgent())
                .setHeader(HttpHeaders.Names.ACCEPT_LANGUAGE, "en-us")
                .setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.BYTES)
                .setHeader("X-Forwarded-For", sasParams.getRemoteHostIp())
                .setHeader(HttpHeaders.Names.HOST, uri.getHost());
    }

    /**
     * request url of each adapter for logging
     */
    @Override
    public String getRequestUrl() {
        return requestUrl;
    }

    /**
     * Returns the status code after the request is complete
     */
    @Override
    public int getHttpResponseStatusCode() {
        return statusCode;
    }

    @Override
    public Double getForwardedBidFloor() {
        return forwardedBidFloor;
    }

    @Override
    public Double getForwardedBidGuidance() {
        return forwardedBidGuidance;
    }

    // Returns the content of http response.
    @Override
    public String getHttpResponseContent() {
        return responseContent;
    }

    @Override
    public Map<?, ?> getResponseHeaders() {
        return responseHeaders;
    }

    // Returns true if request is completed.
    @Override
    public boolean isRequestCompleted() {
        return isRequestComplete;
    }

    @Override
    abstract public URI getRequestUri() throws Exception;

    @Override
    public String getId() {
        return DEFAULT_EMPTY_STRING;
    }

    @Override
    public void cleanUp() {
        if (!isRequestCompleted()) {
            isRequestComplete = true;

            LOG.debug("inside cleanup for channel {}", getId());
            adStatus = TERM;
            responseStruct = new ThirdPartyAdResponse();
            responseStruct.setLatency(latency);
            responseStruct.setStartTime(startTime);
            responseStruct.setAdStatus(adStatus);
        }
    }

    /**
     * returning the ThirdPartyAdResponse object to indicate status code, response message and latency
     **/
    @Override
    public ThirdPartyAdResponse getResponseAd() {
        if (responseStruct != null) {
            return responseStruct;
        }
        responseStruct = new ThirdPartyAdResponse();
        responseStruct.setResponseFormat(isNativeRequest()
                ? ThirdPartyAdResponse.ResponseFormat.JSON
                : ThirdPartyAdResponse.ResponseFormat.HTML);
        responseStruct.setResponse(getHttpResponseContent());
        responseStruct.setResponseHeaders(getResponseHeaders());
        if (statusCode >= 400) {
            responseStruct.setResponseStatus(ThirdPartyAdResponse.ResponseStatus.FAILURE_NETWORK_ERROR);
        } else if (statusCode >= 300) {
            responseStruct.setResponseStatus(ThirdPartyAdResponse.ResponseStatus.FAILURE_REQUEST_ERROR);
        } else if (statusCode == 200) {
            if (StringUtils.isBlank(responseContent) || !AD_STRING.equalsIgnoreCase(adStatus)) {
                adStatus = NO_AD;
                responseStruct.setResponseStatus(ThirdPartyAdResponse.ResponseStatus.FAILURE_NO_AD);
            } else {
                responseStruct.setResponseStatus(ThirdPartyAdResponse.ResponseStatus.SUCCESS);
                adStatus = AD_STRING;
            }
        } else if (statusCode >= 204) {
            responseStruct.setResponseStatus(ThirdPartyAdResponse.ResponseStatus.FAILURE_NO_AD);
        }
        responseStruct.setLatency(latency);
        responseStruct.setStartTime(startTime);
        responseStruct.setAdStatus(adStatus);
        LOG.debug("getting response ad for channel {}", getId());
        return responseStruct;
    }

    protected boolean configureParameters() {
        return false;
    }

    @Override
    public void setAdStatus(final String adStatus) {
        this.adStatus = adStatus;
    }

    public void parseResponse(final byte[] responseByte, final HttpResponseStatus status) {}

    @Override
    public boolean configureParameters(final SASRequestParameters param, final CasInternalRequestParameters casParams,
            final ChannelSegmentEntity entity, final long slotId, final RepositoryHelper repositoryHelper) {
        sasParams = param;
        this.casInternalRequestParameters = casParams;
        externalSiteId = entity.getExternalSiteKey();
        selectedSlotId = (short) slotId;
        this.repositoryHelper = repositoryHelper;
        impressionId = param.getImpressionId();
        // TODO: function is called again in createAppObject() in RtbAdNetwork with the same parameters
        blindedSiteId = getBlindedSiteId(param.getSiteIncId(), entity.getAdgroupIncId());
        this.entity = entity;
        isNativeRequest =
                APP.equalsIgnoreCase(sasParams.getSource())
                        && (NATIVE_STRING.equals(sasParams.getRFormat()) || RequestedAdType.NATIVE == sasParams
                                .getRequestedAdType());
        isCpc = getPricingModel(entity);
        final boolean isConfigured = configureParameters();
        if (isConfigured) {
            replaceHostWithIP();
        }
        return isConfigured;
    }

    private static final Boolean getPricingModel(ChannelSegmentEntity entity) {
        boolean isCpc = false;
        if (null != entity.getPricingModel() && CPC.equalsIgnoreCase(entity.getPricingModel())) {
            isCpc = true;
        }
        return isCpc;
    }

    @Override
    public double getBidPriceInUsd() {
        return -1;
    }

    @Override
    public double getBidPriceInLocal() {
        return -1;
    }

    @Override
    public boolean isInternal() {
        return false;
    }

    @Override
    public void impressionCallback() {
        // Do nothing
    }

    @Override
    public void noImpressionCallBack() {
        // Do Nothing
    }

    @Override
    public ThirdPartyAdResponse getResponseStruct() {
        return responseStruct;
    }

    // parsing the response message to get HTTP response code and httpresponse
    public void parseResponse(final String response, final HttpResponseStatus status) {
        LOG.debug("response is {} ", response);
        if (StringUtils.isBlank(response) || status.code() != 200 || response.startsWith("<!--")) {
            statusCode = status.code();
            if (200 == statusCode) {
                statusCode = 500;
            }
            responseContent = DEFAULT_EMPTY_STRING;
            return;
        } else {
            responseContent = response;
            statusCode = status.code();
            adStatus = AD_STRING;
            responseContent = "<html><body>".concat(responseContent).concat("</body></html>");
        }
        LOG.debug("response length is {}", responseContent.length());
    }

    /**
     * @param siteIncId
     * @param adGroupIncId
     * @return
     */
    protected static String getBlindedSiteId(final long siteIncId, final long adGroupIncId) {
        return new UUID(adGroupIncId, siteIncId).toString();
    }

    /**
     * Generates blinded site uuid from siteIncId. For a given site Id, the generated blinded SiteId will always be
     * same.
     * <p/>
     * NOTE: RTB uses a different logic where the blinded SiteId is a function of siteIncId+AdGroupIncId.
     */
    protected static String getBlindedSiteId(final long siteIncId) {
        final byte[] byteArr = ByteBuffer.allocate(8).putLong(siteIncId).array();
        return UUID.nameUUIDFromBytes(byteArr).toString();
    }

    protected String getCategories(final char seperator) {
        return getCategories(seperator, true);
    }

    protected String getCategories(final char seperator, final boolean isAllRequired) {
        return getCategories(seperator, isAllRequired, false);
    }

    protected String getCategories(final char seperator, final boolean isAllRequired, final boolean isIABCategory) {
        final StringBuilder sb = new StringBuilder();
        Long[] segmentCategories = null;
        boolean allTags = false;
        if (entity != null) {
            segmentCategories = entity.getCategoryTaxonomy();
            allTags = entity.isAllTags();
        }
        if (allTags) {
            if (isIABCategory) {
                return getValueFromListAsString(IABCategoriesMap.getIABCategories(sasParams.getCategories()), seperator);

            } else if (null != sasParams.getCategories()) {
                for (int index = 0; index < sasParams.getCategories().size(); index++) {
                    final String category = CategoryList.getCategory(sasParams.getCategories().get(index).intValue());
                    appendCategories(sb, category, seperator);
                    if (!isAllRequired) {
                        break;
                    }
                }
            }
        } else {
            for (int index = 0; index < sasParams.getCategories().size(); index++) {
                String category = null;
                final int cat = sasParams.getCategories().get(index).intValue();
                for (int i = 0; i < segmentCategories.length; i++) {
                    if (cat == segmentCategories[i]) {
                        if (isIABCategory) {
                            category =
                                    getValueFromListAsString(IABCategoriesMap.getIABCategories(segmentCategories[i]),
                                            seperator);
                        } else {
                            category = CategoryList.getCategory(cat);
                        }
                        appendCategories(sb, category, seperator);
                    }
                }
                if (!isAllRequired && null != category) {
                    break;
                }
            }
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
            return sb.toString();
        }
        if (isIABCategory) {
            return "IAB24";
        }
        return "miscellenous";
    }

    /**
     * function returns the unique device id
     * 
     * @param considerDnt - Should casInternalRequestParameters.isTrackingAllowed() taken into consideration
     * @return
     */
    protected String getUid(final boolean considerDnt) {
        final boolean trackIFA = considerDnt ? casInternalRequestParameters.isTrackingAllowed() : true;
        if (StringUtils.isNotEmpty(casInternalRequestParameters.getUidIFA()) && trackIFA) {
            return casInternalRequestParameters.getUidIFA();
        } else if (StringUtils.isNotEmpty(casInternalRequestParameters.getGpid()) && trackIFA) {
            return casInternalRequestParameters.getGpid();
        } else if (StringUtils.isNotEmpty(casInternalRequestParameters.getUidSO1())) {
            return casInternalRequestParameters.getUidSO1();
        } else if (StringUtils.isNotEmpty(casInternalRequestParameters.getUidMd5())) {
            return casInternalRequestParameters.getUidMd5();
        } else if (StringUtils.isNotEmpty(casInternalRequestParameters.getUidO1())) {
            return casInternalRequestParameters.getUidO1();
        } else if (StringUtils.isNotEmpty(casInternalRequestParameters.getUidIDUS1())) {
            return casInternalRequestParameters.getUidIDUS1();
        } else if (StringUtils.isNotEmpty(casInternalRequestParameters.getUid())) {
            return casInternalRequestParameters.getUid();
        }
        return null;
    }

    /**
     * @param sb
     * @param category
     */
    private void appendCategories(final StringBuilder sb, final String category, final char seperator) {
        LOG.debug("category is {}", category);
        if (category != null) {
            sb.append(category).append(seperator);
        }
    }

    @Override
    public long getLatency() {
        return latency;
    }

    @Override
    public String getImpressionId() {
        return impressionId;
    }

    // return year of birth
    protected int getYearofBirth() {
        try {
            if (sasParams.getAge() != null && sasParams.getAge().toString().matches("\\d+")) {
                final Calendar cal = new GregorianCalendar();
                return cal.get(Calendar.YEAR) - sasParams.getAge();
            }
        } catch (Exception e) {}
        return -1;
    }

    @Override
    public void setSecondBidPrice(final Double price) {
        return;
    }

    @Override
    public double getSecondBidPriceInUsd() {
        return -1;
    }


    @Override
    public long getConnectionLatency() {
        return connectionLatency;
    }

    protected String getURLEncode(final String param, final String format) {
        String encodedString = DEFAULT_EMPTY_STRING;
        String decoded = param;

        if (StringUtils.isNotBlank(param)) {
            try {
                String tobeEndoded = param;
                decoded = URLDecoder.decode(tobeEndoded, format);
                while (!tobeEndoded.equalsIgnoreCase(decoded)) {
                    tobeEndoded = decoded;
                    decoded = URLDecoder.decode(tobeEndoded, format);
                }
            } catch (final UnsupportedEncodingException uee) {
                LOG.debug("Error during decode in getURLEncode() for {} for string {}, exception raised {}", getName(),
                        param, uee);
            }
            try {
                encodedString = URLEncoder.encode(decoded.trim(), format);
            } catch (final UnsupportedEncodingException e) {
                LOG.debug("Error during encode in getURLEncode() for {} for string {}, exception raised {}", getName(),
                        param, e);
            }
        }
        return encodedString;
    }

    protected String getValueFromListAsString(final List<String> list) {
        return getValueFromListAsString(list, ',');
    }

    protected String getValueFromListAsString(final List<String> list, final char seperatar) {
        if (list.isEmpty()) {
            return DEFAULT_EMPTY_STRING;
        }
        final StringBuilder s = new StringBuilder(list.get(0));
        for (int i = 1; i < list.size(); i++) {
            s.append(seperatar).append(list.get(i));
        }
        return s.toString();
    }

    @Override
    public boolean useJsAdTag() {
        return false;
    }

    @Override
    public void generateJsAdResponse() {

    }

    @Override
    public void setEncryptedBid(final String encryptedBid) {

    }

    public boolean isNativeRequest() {
        return isNativeRequest;
    }

    @Override
    public ADCreativeType getCreativeType() {
        if (isNativeRequest()) {
            return ADCreativeType.NATIVE;
        } else if (isVideoRequest) {
            return ADCreativeType.INTERSTITIAL_VIDEO;
        } else {
            return ADCreativeType.BANNER;
        }
    }

    public static String getHashedValue(final String message, final String hashingType) {
        try {
            final MessageDigest md = MessageDigest.getInstance(hashingType);
            final byte[] array = md.digest(message.getBytes());
            final StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString(array[i] & 0xFF | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (final java.security.NoSuchAlgorithmException e) {
            LOG.debug("exception raised in BaseAdNetwork {}", e);
        }
        return null;
    }

    protected StringBuilder appendQueryParam(final StringBuilder builder, final String paramName, final int paramValue,
            final boolean isFirstParam) {
        return builder.append(isFirstParam ? '?' : '&').append(paramName).append('=').append(paramValue);
    }

    protected StringBuilder appendQueryParam(final StringBuilder builder, final String paramName,
            final String paramValue, final boolean isFirstParam) {
        return builder.append(isFirstParam ? '?' : '&').append(paramName).append('=').append(paramValue);
    }

    protected StringBuilder appendQueryParam(final StringBuilder builder, final String paramName,
            final double paramValue, final boolean isFirstParam) {
        return builder.append(isFirstParam ? '?' : '&').append(paramName).append('=').append(paramValue);
    }


    @Override
    public String getAuctionId() {
        return null;
    }

    @Override
    public String getRtbImpressionId() {
        return null;
    }

    @Override
    public String getSeatId() {
        return null;
    }

    @Override
    public String getCurrency() {
        return USD;
    }

    @Override
    public String getCreativeId() {
        return null;
    }

    @Override
    public String getIUrl() {
        return null;
    }

    @Override
    public List<Integer> getAttribute() {
        return null;
    }

    @Override
    public List<String> getADomain() {
        return null;
    }

    @Override
    public boolean isLogCreative() {
        return false;
    }

    @Override
    public void setLogCreative(final boolean logCreative) {
        //
    }

    @Override
    public String getAdMarkUp() {
        return null;
    }

    protected boolean isInterstitial() {
        if (10 == selectedSlotId // 300X250
                || 14 == selectedSlotId // 320X480
                || 16 == selectedSlotId // 768X1024
                || 17 == selectedSlotId /* 800x1280 */
                || 32 == selectedSlotId // 480x320
                || 33 == selectedSlotId // 1024x768
                || 34 == selectedSlotId) /* 1280x800 */{
            return true;
        }
        return false;
    }

    protected boolean isIOS() {
        return sasParams.getOsId() == SASRequestParameters.HandSetOS.iOS.getValue();
    }

    protected boolean isAndroid() {
        return sasParams.getOsId() == SASRequestParameters.HandSetOS.Android.getValue();
    }

    protected boolean isApp() {
        if (StringUtils.isBlank(sasParams.getSource())) {
            return false;
        } else {
            return APP.equalsIgnoreCase(sasParams.getSource());
        }
    }

    // Response is empty or null or status code other than 200.
    public boolean isValidResponse(final String response, final HttpResponseStatus status) {
        statusCode = status.code();
        if (null == response || response.trim().isEmpty() || statusCode != 200) {
            if (200 == statusCode) {
                statusCode = 500;
            }
            responseContent = DEFAULT_EMPTY_STRING;
            return false;
        } else {
            return true;
        }
    }

    /**
     * 
     * @param considerDnt -Should casInternalRequestParameters.isTrackingAllowed() taken into consideration
     * @return
     */
    protected String getGPID(final boolean considerDnt) {
        final boolean trackIFA = considerDnt ? casInternalRequestParameters.isTrackingAllowed() : true;
        return StringUtils.isNotBlank(casInternalRequestParameters.getGpid()) && trackIFA
                ? casInternalRequestParameters.getGpid()
                : null;
    }

    /**
     * 
     * @param considerDnt - Should casInternalRequestParameters.isTrackingAllowed() taken into consideration
     * @return
     */
    protected String getUidIFA(final boolean considerDnt) {
        final boolean trackIFA = considerDnt ? casInternalRequestParameters.isTrackingAllowed() : true;
        return StringUtils.isNotBlank(casInternalRequestParameters.getUidIFA()) && trackIFA
                ? casInternalRequestParameters.getUidIFA()
                : null;
    }

    @Override
    public DemandSourceType getDst() {
        return DemandSourceType.findByValue(sasParams.getDst());
    }

    @Override
    public Short getSelectedSlotId() {
        return selectedSlotId;
    }

    @Override
    public void disableIPResolution(boolean isIPResolutionDisabled) {
        this.isIPResolutionDisabled = isIPResolutionDisabled;
    }

    @Override
    public RepositoryHelper getRepositoryHelper() {
        return repositoryHelper;
    }

    private void replaceHostWithIP() {
        if (isIPResolutionDisabled || useJsAdTag()) {
            return;
        }
        if (host != null) {
            try {
                URI uri = new URI(host);
                publicHostName = uri.getHost();
                if (uri.getPort() != -1) {
                    publicHostName = publicHostName + ":" + uri.getPort();
                }
            } catch (URISyntaxException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(traceMarker, "URISyntaxException " + ExceptionBlock.getStackTrace(e), this.getClass()
                            .getSimpleName());
                }
            }
        }
        host = ipRepository.getIPAddress(getName(), host, traceMarker);
    }

    private void setVirtualHost(RequestBuilder ningRequestBuilder) {
        if (isIPResolutionDisabled) {
            return;
        }
        ningRequestBuilder.setVirtualHost(publicHostName);
    }
}
