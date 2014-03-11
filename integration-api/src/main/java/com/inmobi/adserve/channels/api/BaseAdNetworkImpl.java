package com.inmobi.adserve.channels.api;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.util.CategoryList;
import com.inmobi.adserve.channels.util.IABCategoriesInterface;
import com.inmobi.adserve.channels.util.IABCategoriesMap;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.Response;


// This abstract class have base functionality of TPAN adapters.
public abstract class BaseAdNetworkImpl implements AdNetworkInterface {

    private final static Logger                   LOG                     = LoggerFactory
                                                                                  .getLogger(BaseAdNetworkImpl.class);

    protected ChannelFuture                       future;
    protected ClientBootstrap                     clientBootstrap;
    protected Channel                             channel;
    protected HttpRequest                         request;
    protected long                                startTime;
    public volatile boolean                       isRequestComplete       = false;
    protected int                                 statusCode;
    public String                                 responseContent;
    public Map                                    responseHeaders;
    public long                                   latency;
    public long                                   connectionLatency;
    public String                                 adStatus                = "NO_AD";
    protected ThirdPartyAdResponse.ResponseStatus errorStatus             = ThirdPartyAdResponse.ResponseStatus.SUCCESS;

    protected SASRequestParameters                sasParams;
    protected CasInternalRequestParameters        casInternalRequestParameters;
    protected HttpRequestHandlerBase              baseRequestHandler      = null;
    protected final MessageEvent                  serverEvent;
    protected String                              requestUrl              = "";
    private ThirdPartyAdResponse                  responseStruct;
    private boolean                               isRtbPartner            = false;
    protected ChannelSegmentEntity                entity;

    protected String                              externalSiteId;
    protected String                              host;
    protected String                              impressionId;
    protected String                              clickUrl;
    protected String                              beaconUrl;
    protected String                              source;
    protected String                              blindedSiteId;
    protected String                              slot;
    private static final String                   DEFAULT_EMPTY_STRING    = "";
    protected String                              format                  = "UTF-8";
    private String                                adapterName;

    protected Request                             ningRequest;
    protected static String                       SITE_RATING_PERFORMANCE = "PERFORMANCE";
    protected static final String                 WAP                     = "WAP";
    private static final IABCategoriesInterface   iabCategoryMap          = new IABCategoriesMap();

    protected static final String                 UA                      = "ua";
    protected static final String                 IP                      = "ip";
    protected static final String                 LAT                     = "lat";
    protected static final String                 LONG                    = "long";
    protected static final String                 SIZE                    = "size";
    protected static final String                 ZIP                     = "zip";
    protected static final String                 COUNTRY                 = "country";
    protected static final String                 GENDER                  = "gender";

    public BaseAdNetworkImpl(final HttpRequestHandlerBase baseRequestHandler, final MessageEvent serverEvent) {
        this.baseRequestHandler = baseRequestHandler;
        this.serverEvent = serverEvent;
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

    public void setRtbPartner(final boolean isRtbPartner) {
        this.isRtbPartner = isRtbPartner;
    }

    @Override
    public Integer getChannelId() {
        if (null == channel) {
            return null;
        }
        return channel.getId();
    }

    public void processResponse() {
        LOG.debug("Inside process Response for the partner: {}", getName());
        if (isRequestComplete) {
            LOG.debug("Already cleanedup so returning from process response");
            return;
        }
        LOG.debug("Inside process Response for the partner: {}", getName());
        getResponseAd();
        isRequestComplete = true;
        if (baseRequestHandler.getAuctionEngine().isAllRtbComplete()) {
            LOG.debug("isAllRtbComplete is true");
            if (baseRequestHandler.getAuctionEngine().isAuctionComplete()) {
                LOG.debug("Rtb auction has run already");
                if (baseRequestHandler.getAuctionEngine().isRtbResponseNull()) {
                    LOG.debug("rtb auction has returned null so processing dcp list");
                    // Process dcp partner response.
                    baseRequestHandler.processDcpPartner(serverEvent, this);
                    return;
                }
                LOG.debug("rtb response is not null so sending rtb response");
                return;
            }
            else {
                AdNetworkInterface highestBid = baseRequestHandler.getAuctionEngine().runRtbSecondPriceAuctionEngine();
                if (highestBid != null) {
                    LOG.debug("Sending rtb response of {}", highestBid.getName());
                    baseRequestHandler.sendAdResponse(highestBid, serverEvent);
                    // highestBid.impressionCallback();
                    LOG.debug("sent rtb response");
                    return;
                }
                else {
                    LOG.debug("rtb auction has returned null so processing dcp list");
                    baseRequestHandler.processDcpList(serverEvent);
                }
            }
        }
        LOG.debug("rtb auction has not run so waiting....");
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public boolean makeAsyncRequest() {
        LOG.debug("In Adapter {}", this.getClass().getSimpleName());
        try {
            String uri = getRequestUri().toString();
            requestUrl = uri;
            setNingRequest(requestUrl);
            LOG.debug(" uri : {}", uri);
            startTime = System.currentTimeMillis();
            getAsyncHttpClient().executeRequest(ningRequest, new AsyncCompletionHandler() {
                @Override
                public Response onCompleted(final Response response) throws Exception {
                    MDC.put("requestId", serverEvent.getChannel().getId().toString());

                    if (!isRequestCompleted()) {
                        LOG.debug("Operation complete for channel partner: {}", getName());
                        latency = System.currentTimeMillis() - startTime;
                        LOG.debug("{} operation complete latency {}", getName(), latency);
                        String responseStr = response.getResponseBody();
                        HttpResponseStatus httpResponseStatus = HttpResponseStatus.valueOf(response.getStatusCode());
                        parseResponse(responseStr, httpResponseStatus);
                        processResponse();
                    }
                    return response;
                }

                @Override
                public void onThrowable(final Throwable t) {
                    MDC.put("requestId", serverEvent.getChannel().getId().toString());

                    if (isRequestComplete) {
                        return;
                    }

                    if (t instanceof java.util.concurrent.TimeoutException) {
                        latency = System.currentTimeMillis() - startTime;
                        LOG.debug("{} timeout latency {}", getName(), latency);
                        adStatus = "TIME_OUT";
                        processResponse();
                        return;
                    }

                    LOG.debug("{} error latency {}", getName(), latency);
                    adStatus = "TERM";
                    LOG.info("error while fetching response from: {} {}", getName(), t);
                    processResponse();
                    return;
                }
            });
        }
        catch (Exception e) {
            LOG.debug("Exception in {} makeAsyncRequest : {}", getName(), e.getMessage());
        }
        LOG.debug("{} returning from make NingRequest", getName());
        return true;
    }

    protected AsyncHttpClient getAsyncHttpClient() {
        return baseRequestHandler.getAsyncClient();
    }

    protected void setNingRequest(final String requestUrl) throws Exception {

        URI uri = getRequestUri();
        if (uri.getPort() == -1) {
            uri = new URIBuilder(uri).setPort(80).build();
        }

        ningRequest = new RequestBuilder().setURI(uri)
                .setHeader(HttpHeaders.Names.USER_AGENT, sasParams.getUserAgent())
                .setHeader(HttpHeaders.Names.ACCEPT_LANGUAGE, "en-us").setHeader(HttpHeaders.Names.REFERER, requestUrl)
                .setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.BYTES)
                .setHeader("X-Forwarded-For", sasParams.getRemoteHostIp())
                .setHeader(HttpHeaders.Names.HOST, getRequestUri().getHost()).build();
    }

    // request url of each adapter for logging
    @Override
    public String getRequestUrl() {
        return requestUrl;
    }

    // Returns the status code after the request is complete
    public int getHttpResponseStatusCode() {
        return statusCode;
    }

    // Returns the content of http response.
    @Override
    public String getHttpResponseContent() {
        return responseContent;
    }

    @Override
    public Map getResponseHeaders() {
        return responseHeaders;
    }

    // Returns true if request is completed.
    @Override
    public boolean isRequestCompleted() {
        return isRequestComplete;
    }

    public JSONObject getLogline() {
        return null;
    }

    @Override
    public URI getRequestUri() throws Exception {
        return null;
    }

    @Override
    public String getId() {
        return "";
    }

    @Override
    public void cleanUp() {
        if (!isRequestCompleted()) {
            isRequestComplete = true;
            if (channel == null) {
                responseStruct = new ThirdPartyAdResponse();
                latency = System.currentTimeMillis() - startTime;
                responseStruct.latency = latency;
                adStatus = "TERM";
                responseStruct.adStatus = adStatus;
                return;
            }
            else {
                ChannelsClientHandler.removeEntry(channel.getId());
                ChannelsClientHandler.statusMap.remove(channel.getId());
                channel.close();
            }
            LOG.debug("inside cleanup for channel {}", this.getId());
            latency = System.currentTimeMillis() - startTime;
            adStatus = "TERM";
            if (ChannelsClientHandler.adStatusMap.get(channel.getId()) != null
                    && ChannelsClientHandler.adStatusMap.get(channel.getId()).equals("TIME_OUT")) {
                adStatus = "TIME_OUT";
                ChannelsClientHandler.adStatusMap.remove(channel.getId());
                ChannelsClientHandler.statusMap.remove(channel.getId());
                ChannelsClientHandler.responseMap.remove(channel.getId());
            }
            responseStruct = new ThirdPartyAdResponse();
            responseStruct.latency = latency;
            responseStruct.adStatus = adStatus;
        }
        else {
            if (channel == null) {
                responseStruct = new ThirdPartyAdResponse();
                responseStruct.latency = latency;
                responseStruct.adStatus = adStatus;
                return;
            }
            if (ChannelsClientHandler.adStatusMap.get(channel.getId()) != null
                    && ChannelsClientHandler.adStatusMap.get(channel.getId()).equals("TIME_OUT")) {
                responseStruct = new ThirdPartyAdResponse();
                adStatus = "TIME_OUT";
                ChannelsClientHandler.adStatusMap.remove(channel.getId());
                ChannelsClientHandler.statusMap.remove(channel.getId());
                ChannelsClientHandler.responseMap.remove(channel.getId());
                responseStruct.adStatus = adStatus;
                responseStruct.latency = latency;
            }
        }
    }

    // returning the ThirdPartyAdResponse object to indicate status code,
    // response
    // message and latency
    @Override
    public ThirdPartyAdResponse getResponseAd() {
        if (responseStruct != null) {
            return responseStruct;
        }
        responseStruct = new ThirdPartyAdResponse();
        responseStruct.responseFormat = ThirdPartyAdResponse.ResponseFormat.HTML;
        responseStruct.response = getHttpResponseContent();
        responseStruct.responseHeaders = getResponseHeaders();
        if (statusCode >= 400) {
            responseStruct.responseStatus = ThirdPartyAdResponse.ResponseStatus.FAILURE_NETWORK_ERROR;
        }
        else if (statusCode >= 300) {
            responseStruct.responseStatus = ThirdPartyAdResponse.ResponseStatus.FAILURE_REQUEST_ERROR;
        }
        else if (statusCode == 200) {
            if (StringUtils.isBlank(responseContent) || !adStatus.equalsIgnoreCase("AD")) {
                adStatus = "NO_AD";
                responseStruct.responseStatus = ThirdPartyAdResponse.ResponseStatus.FAILURE_NO_AD;
            }
            else {
                responseStruct.responseStatus = ThirdPartyAdResponse.ResponseStatus.SUCCESS;
                adStatus = "AD";
            }
        }
        else if (statusCode >= 204) {
            responseStruct.responseStatus = ThirdPartyAdResponse.ResponseStatus.FAILURE_NO_AD;
        }
        responseStruct.latency = latency;
        LOG.debug("getting response ad for channel {}", this.getId());
        if (isClickUrlRequired()) {
            responseStruct.clickUrl = getClickUrl();
        }
        responseStruct.adStatus = adStatus;
        return responseStruct;
    }

    protected boolean configureParameters() {
        return false;
    }

    @Override
    public boolean configureParameters(final SASRequestParameters param,
            final CasInternalRequestParameters casInternalRequestParameters, final ChannelSegmentEntity entity,
            final String clickUrl, final String beaconUrl) {
        this.sasParams = param;
        this.casInternalRequestParameters = casInternalRequestParameters;
        this.externalSiteId = entity.getExternalSiteKey();
        this.slot = sasParams.getSlot();
        this.clickUrl = clickUrl;
        this.beaconUrl = beaconUrl;
        this.impressionId = param.getImpressionId();
        this.blindedSiteId = getBlindedSiteId(param.getSiteIncId(), entity.getAdgroupIncId());
        this.entity = entity;
        return configureParameters();
    }

    @Override
    public boolean isBeaconUrlRequired() {
        return true;
    }

    @Override
    public boolean isClickUrlRequired() {
        return false;
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

    @Override
    public String getClickUrl() {
        return clickUrl;
    }

    // parsing the response message to get HTTP response code and httpresponse
    public void parseResponse(final String response, final HttpResponseStatus status) {
        LOG.debug("response is {}", response);
        if (StringUtils.isBlank(response) || status.getCode() != 200 || response.startsWith("<!--")) {
            statusCode = status.getCode();
            if (200 == statusCode) {
                statusCode = 500;
            }
            responseContent = "";
            return;
        }
        else {
            responseContent = response;
            statusCode = status.getCode();
            adStatus = "AD";
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
        return (new UUID(adGroupIncId, siteIncId)).toString();
    }

    protected String getCategories(final char seperator) {
        return getCategories(seperator, true);

    }

    protected String getCategories(final char seperator, final boolean isAllRequired) {
        return getCategories(seperator, true, false);
    }

    protected String getCategories(final char seperator, final boolean isAllRequired, final boolean isIABCategory) {
        StringBuilder sb = new StringBuilder();
        Long[] segmentCategories = null;
        boolean allTags = false;
        if (entity != null) {
            segmentCategories = entity.getCategoryTaxonomy();
            allTags = entity.isAllTags();
        }
        if (allTags) {
            if (isIABCategory) {
                return getValueFromListAsString(iabCategoryMap.getIABCategories(sasParams.getCategories()), seperator);

            }
            else {
                for (int index = 0; index < sasParams.getCategories().size(); index++) {
                    String category = CategoryList.getCategory(sasParams.getCategories().get(index).intValue());
                    appendCategories(sb, category, seperator);
                    if (!isAllRequired) {
                        break;
                    }
                }
            }
        }
        else {
            for (int index = 0; index < sasParams.getCategories().size(); index++) {
                String category = null;
                int cat = sasParams.getCategories().get(index).intValue();
                for (int i = 0; i < segmentCategories.length; i++) {
                    if (cat == segmentCategories[i]) {
                        if (isIABCategory) {
                            return getValueFromListAsString(iabCategoryMap.getIABCategories(segmentCategories[i]),
                                    seperator);

                        }
                        category = CategoryList.getCategory(cat);
                        appendCategories(sb, category, seperator);
                        break;
                    }
                }
                if (!isAllRequired && null != category) {
                    break;
                }
            }
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
            return (sb.toString());
        }
        if (isIABCategory) {
            return "IAB24";
        }
        return "miscellenous";
    }

    /**
     * function returns the unique device id
     * 
     * @return
     */
    protected String getUid() {
        if (StringUtils.isNotEmpty(casInternalRequestParameters.uidIFA)) {
            return casInternalRequestParameters.uidIFA;
        }
        if (StringUtils.isNotEmpty(casInternalRequestParameters.uidSO1)) {
            return casInternalRequestParameters.uidSO1;
        }
        if (StringUtils.isNotEmpty(casInternalRequestParameters.uidMd5)) {
            return casInternalRequestParameters.uidMd5;
        }
        if (StringUtils.isNotEmpty(casInternalRequestParameters.uidO1)) {
            return casInternalRequestParameters.uidO1;
        }
        if (StringUtils.isNotEmpty(casInternalRequestParameters.uidIDUS1)) {
            return casInternalRequestParameters.uidIDUS1;
        }
        if (StringUtils.isNotEmpty(casInternalRequestParameters.uid)) {
            return casInternalRequestParameters.uid;
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
    protected String getYearofBirth() {
        if (sasParams.getAge() != null && sasParams.getAge().matches("\\d+")) {
            Calendar cal = new GregorianCalendar();
            return (Integer.toString(cal.get(Calendar.YEAR) - Integer.parseInt(sasParams.getAge())));
        }
        return null;
    }

    @Override
    public String getAdStatus() {
        return this.adStatus;
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
    public double getSecondBidPriceInLocal() {
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
            }
            catch (UnsupportedEncodingException uee) {
                LOG.debug("Error during decode in getURLEncode() for {} for string {}", getName(), param);
            }
            try {
                encodedString = URLEncoder.encode(decoded.trim(), format);
            }
            catch (UnsupportedEncodingException e) {
                LOG.debug("Error during encode in getURLEncode() for {} for string {}", getName(), param);
            }
        }
        return encodedString;
    }

    protected String getValueFromListAsString(final List<String> list) {
        return getValueFromListAsString(list, ',');
    }

    protected String getValueFromListAsString(final List<String> list, final char seperatar) {
        if (list.size() == 0) {
            return "";
        }
        StringBuilder s = new StringBuilder(list.get(0));
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

    protected String getHashedValue(final String message, final String hashingType) {
        try {
            MessageDigest md = MessageDigest.getInstance(hashingType);
            byte[] array = md.digest(message.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        }
        catch (java.security.NoSuchAlgorithmException e) {
        }
        return null;
    }

    protected StringBuilder appendQueryParam(final StringBuilder builder, final String paramName,
            final String paramValue, final boolean isFirstParam) {
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
        return "USD";
    }
}
