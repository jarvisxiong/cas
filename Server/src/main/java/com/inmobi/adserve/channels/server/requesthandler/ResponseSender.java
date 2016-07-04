package com.inmobi.adserve.channels.server.requesthandler;

import static com.inmobi.adserve.channels.server.requesthandler.AdPoolResponseCreator.createAdPoolResponse;
import static com.inmobi.adserve.channels.server.requesthandler.AdResponseTemplate.AD_IMAI_START_TAG;
import static com.inmobi.adserve.channels.server.requesthandler.AdResponseTemplate.DCP_NATIVE_WRAPPING_AD_JSON;
import static com.inmobi.adserve.channels.server.requesthandler.AdResponseTemplate.END_TAG;
import static com.inmobi.adserve.channels.server.requesthandler.AdResponseTemplate.NO_AD_HTML;
import static com.inmobi.adserve.channels.server.requesthandler.AdResponseTemplate.NO_AD_IMAI;
import static com.inmobi.adserve.channels.server.requesthandler.AdResponseTemplate.NO_AD_JS_ADCODE;
import static com.inmobi.adserve.channels.server.requesthandler.AdResponseTemplate.NO_AD_VAST;
import static com.inmobi.adserve.channels.server.requesthandler.AdResponseTemplate.NO_AD_XHTML;
import static com.inmobi.adserve.channels.server.requesthandler.AdResponseTemplate.SDK_500_DCP_WRAPPING_AD_JSON;
import static com.inmobi.adserve.channels.server.requesthandler.AdResponseTemplate.START_TAG;
import static com.inmobi.adserve.channels.server.requesthandler.AdResponseTemplate.SUPPORTED_RESPONSE_FORMATS;
import static com.inmobi.adserve.channels.util.Utils.ExceptionBlock.getStackTrace;
import static com.inmobi.casthrift.DemandSourceType.DCP;
import static com.inmobi.casthrift.DemandSourceType.IX;
import static com.inmobi.casthrift.DemandSourceType.RTBD;

import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import javax.inject.Inject;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.inject.Provider;
import com.inmobi.adserve.adpool.AdPoolResponse;
import com.inmobi.adserve.adpool.EncryptionKeys;
import com.inmobi.adserve.adpool.RequestedAdType;
import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse.ResponseStatus;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.server.auction.AuctionEngine;
import com.inmobi.adserve.channels.server.kafkalogging.PhotonCasActivityWriter;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.config.GlobalConstant;
import com.inmobi.adserve.contracts.ump.NativeAd;
import com.inmobi.casthrift.DemandSourceType;
import com.inmobi.commons.security.api.InmobiSession;
import com.inmobi.commons.security.impl.InmobiSecurityImpl;
import com.inmobi.commons.security.util.exception.InmobiSecureException;
import com.inmobi.commons.security.util.exception.InvalidMessageException;
import com.inmobi.user.photon.datatypes.activity.NestedActivityRecord;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import lombok.Getter;
import lombok.Setter;

public class ResponseSender extends HttpRequestHandlerBase {

    private static final int ENCRYPTED_SDK_BASE_VERSION = 430;

    private final static Logger LOG = LoggerFactory.getLogger(ResponseSender.class);
    private static final int STORYBOARD_SDK_BASE_VERSION = 530;
    private static final Gson gson = new Gson();

    @Setter
    @Getter
    private SASRequestParameters sasParams;
    public CasInternalRequestParameters casInternalRequestParameters;

    private long totalTime;
    private List<ChannelSegment> rankList;
    private ThirdPartyAdResponse adResponse;
    private boolean responseSent;
    private int rankIndexToProcess;
    private int selectedAdIndex;
    private boolean requestCleaned;
    @Getter
    private final AuctionEngine auctionEngine;
    private final Object lock = new Object();
    private String terminationReason;
    private final long initialTime;
    private Marker traceMarker;

    @Setter
    @Inject
    private static Provider<Marker> traceMarkerProvider;

    private PhotonCasActivityWriter photonCasActivityWriter;


    public String getTerminationReason() {
        return terminationReason;
    }

    public void setTerminationReason(final String terminationReason) {
        this.terminationReason = terminationReason;
    }

    public List<ChannelSegment> getRankList() {
        return rankList;
    }

    public void setRankList(final List<ChannelSegment> rankList) {
        this.rankList = rankList;
    }

    private void setRankIndexToProcess(final int rankIndexToProcess) {
        this.rankIndexToProcess = rankIndexToProcess;
    }

    public ThirdPartyAdResponse getAdResponse() {
        return adResponse;
    }

    private ChannelSegment getRtbResponse() {
        return auctionEngine.getAuctionResponse();
    }

    public int getSelectedAdIndex() {
        return selectedAdIndex;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public long getTimeElapsed() {
        return System.currentTimeMillis() - initialTime;
    }

    public String getDST() {
        return sasParams.getDst() == 0
                ? StringUtils.EMPTY
                : DemandSourceType.findByValue(sasParams.getDst()).toString();
    }

    @Inject
    public ResponseSender() {
        initialTime = System.currentTimeMillis();
        totalTime = 0;
        rankList = null;
        adResponse = null;
        responseSent = false;
        sasParams = null;
        rankIndexToProcess = 0;
        selectedAdIndex = 0;
        requestCleaned = false;
        auctionEngine = new AuctionEngine();
        if (null != traceMarkerProvider) {
            traceMarker = traceMarkerProvider.get();
        }
        photonCasActivityWriter = PhotonCasActivityWriter.getInstance();
    }

    @Override
    public void sendAdResponse(final AdNetworkInterface selectedAdNetwork, final Channel serverChannel) {
        adResponse = selectedAdNetwork.getResponseAd();
        selectedAdIndex = getRankIndex(selectedAdNetwork);
        sendAdResponse(adResponse, serverChannel, selectedAdNetwork.getSelectedSlotId(),
                selectedAdNetwork.getRepositoryHelper());
    }

    // send Ad Response
    private void sendAdResponse(final ThirdPartyAdResponse adResponse, final Channel serverChannel,
            final Short selectedSlotId, final RepositoryHelper repositoryHelper) {
        // Making sure response is sent only once
        if (checkResponseSent()) {
            return;
        }

        LOG.debug("ad received so trying to send ad response");
        String finalResponse = adResponse.getResponse();
        final SlotSizeMapEntity slotSizeMapEntity = repositoryHelper.querySlotSizeMapRepository(selectedSlotId);
        LOG.debug("slot served is {}", selectedSlotId);
        final ResponseFormat rFormat = getResponseFormat();
        if (rFormat == ResponseFormat.XHTML) {
            final Dimension dim = slotSizeMapEntity.getDimension();
            final String startElement = String.format(START_TAG, (int) dim.getWidth(), (int) dim.getHeight());
            finalResponse = startElement + finalResponse + END_TAG;
        } else if ((rFormat == ResponseFormat.IMAI || rFormat == ResponseFormat.JSON)
                && RequestedAdType.NATIVE != sasParams.getRequestedAdType()) {
            finalResponse = AD_IMAI_START_TAG + finalResponse;
        }

        if (sasParams.getDst() == DCP.getValue()) {
            sendResponse(HttpResponseStatus.OK, finalResponse, adResponse.getResponseHeaders(), serverChannel);
            incrementStatsForFills(sasParams.getDst());
        } else {
            final AdPoolResponse adPoolResponse = createAdPoolResponse(auctionEngine.getAuctionResponse(),
                    adResponse.getResponse(), auctionEngine.getHighestBid());

            LOG.debug("{} response json to UMP: {}", sasParams.getDemandSourceType(), adPoolResponse);
            if (null == adPoolResponse || !SUPPORTED_RESPONSE_FORMATS.contains(sasParams.getRFormat())) {
                sendNoAdResponse(serverChannel);
            } else {
                try {
                    final TSerializer serializer = new TSerializer(new TBinaryProtocol.Factory());
                    final byte[] serializedResponse = serializer.serialize(adPoolResponse);
                    sendResponse(HttpResponseStatus.OK, serializedResponse, adResponse.getResponseHeaders(),
                            serverChannel);
                    incrementStatsForFills(sasParams.getDst());
                } catch (final TException e) {
                    LOG.error("Error in serializing the adPool response ", e);
                    sendNoAdResponse(serverChannel);
                }
            }
        }
    }

    private void incrementStatsForFills(final int dst) {
        if (dst == DCP.getValue()) {
            InspectorStats.incrementStatCount(InspectorStrings.DCP_FILLS);
        } else if (dst == RTBD.getValue()) {
            InspectorStats.incrementStatCount(InspectorStrings.RULE_ENGINE_FILLS);
        } else if (dst == IX.getValue()) {
            InspectorStats.incrementStatCount(InspectorStrings.IX_FILLS);
        }

        InspectorStats.incrementStatCount(InspectorStrings.TOTAL_FILLS);
    }

    private boolean checkResponseSent() {
        if (responseSent) {
            return true;
        }

        synchronized (lock) {
            if (!responseSent) {
                responseSent = true;
                return false;
            } else {
                return true;
            }
        }
    }

    // send response to the caller
    @SuppressWarnings("rawtypes")
    private void sendResponse(final HttpResponseStatus status, final String responseString, final Map responseHeaders,
            final Channel serverChannel) {
        final byte[] bytes = DCP.getValue() == sasParams.getDst()
                ? processDCPResponse(responseString)
                : responseString.getBytes(Charsets.UTF_8);

        if (bytes == null) {
            sendNoAdResponse(serverChannel);
        }
        sendResponse(status, bytes, responseHeaders, serverChannel);
    }

    private byte[] processDCPResponse(String responseString) {
        final boolean storyBoardSdkSupport =
                Formatter.isRequestFromSdkVersionOnwards(sasParams, STORYBOARD_SDK_BASE_VERSION);
        final boolean encryptedSdkBaseSupport =
                Formatter.isRequestFromSdkVersionOnwards(sasParams, ENCRYPTED_SDK_BASE_VERSION);

        // for native we have already base64encoded response string so we are reverting in case of sdk greater than 530
        if (RequestedAdType.NATIVE == sasParams.getRequestedAdType()) {
            responseString = String.format(DCP_NATIVE_WRAPPING_AD_JSON, sasParams.getRequestGuid(),
                    storyBoardSdkSupport ? getJSEscapeWithoutBase64(responseString) : responseString);
            LOG.debug("Rewrapping native JSON for DCP traffic. Wrapped Response is: {}", responseString);
        } else if (Formatter.isRequestFromSdkVersionOnwards(sasParams, 500)) {
            responseString = String.format(SDK_500_DCP_WRAPPING_AD_JSON, sasParams.getRequestGuid(),
                    storyBoardSdkSupport
                            ? StringEscapeUtils.escapeJavaScript(responseString)
                            : new String(Base64.encodeBase64(responseString.getBytes(CharsetUtil.UTF_8))));
            LOG.debug("Wrapping in JSON for SDK > 500. Wrapped Response is: {}", responseString);
        }
        try {
            return encryptResponseIfRequired(responseString.getBytes(Charsets.UTF_8), encryptedSdkBaseSupport,
                    storyBoardSdkSupport);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private byte[] encryptResponseIfRequired(byte[] responseBytes, final boolean encryptedSdkBaseSupport,
            final boolean storyBoardSdkSupport) {
        if (sasParams.getEncryptionKey() != null && responseBytes.length > 0 && encryptedSdkBaseSupport) {
            LOG.debug("Encrypting the response as request is from SDK: {}", sasParams.getSdkVersion());
            final EncryptionKeys encryptionKey = sasParams.getEncryptionKey();
            final InmobiSession inmobiSession = new InmobiSecurityImpl(null).newSession(null);
            responseBytes = storyBoardSdkSupport ? compressResponseBytes(responseBytes) : responseBytes;
            try {
                responseBytes = inmobiSession.write(responseBytes, encryptionKey.getAesKey(),
                        encryptionKey.getInitializationVector());
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Encyption Details:  EncryptionKey: {}  IVBytes: {}  Response: {}",
                            new String(encryptionKey.getAesKey(), CharsetUtil.UTF_8),
                            new String(encryptionKey.getInitializationVector(), CharsetUtil.UTF_8),
                            new String(responseBytes, CharsetUtil.UTF_8));
                }
            } catch (InmobiSecureException | InvalidMessageException e) {
                LOG.info("Exception while encrypting response from {}", e);
                throw new RuntimeException(e);
            }
        }
        return responseBytes;
    }

    /**
     * @param responseBytes bytes to compress
     * @return compressed Bytes
     */
    public byte[] compressResponseBytes(final byte[] responseBytes) {
        byte[] compressedBytes;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream)) {
            gzipOutputStream.write(responseBytes);
            gzipOutputStream.close();
            compressedBytes = outputStream.toByteArray();
            outputStream.close();
        } catch (final IOException ie) {
            LOG.error("Exception thrown while compressing response.", ie);
            throw new RuntimeException(ie);
        }
        return compressedBytes;
    }

    private String getJSEscapeWithoutBase64(final String nativeResponse) {
        final NativeAd nativeAd = gson.fromJson(nativeResponse, NativeAd.class);
        final String jsEscapePubContentWithoutBase64 =
                StringEscapeUtils.escapeJavaScript(new String(Base64.decodeBase64(nativeAd.getPubContent())));
        final NativeAd nativeAdWithoutbase64 = new NativeAd(jsEscapePubContentWithoutBase64, nativeAd.getContextCode(),
                nativeAd.getNamespace(), nativeAd.getLandingPage(), nativeAd.getEventTracking());
        return gson.toJson(nativeAdWithoutbase64);
    }

    // send response to the caller
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void sendResponse(final HttpResponseStatus status, byte[] responseBytes, final Map responseHeaders,
            final Channel serverChannel) {
        LOG.debug("Inside send Response");
        final FullHttpResponse response =
                new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.wrappedBuffer(responseBytes), false);
        if (null != responseHeaders) {
            for (final Map.Entry entry : (Set<Map.Entry>) responseHeaders.entrySet()) {
                response.headers().add(entry.getKey().toString(), responseHeaders.get(entry.getValue()));
            }
        }

        response.headers().add(HttpHeaders.Names.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        // TODO: to fix keep alive, we need to fix whole flow
        response.headers().add(HttpHeaders.Names.CONTENT_LENGTH, responseBytes.length);
        response.headers().add(HttpHeaders.Names.EXPIRES, "-1");
        response.headers().add(HttpHeaders.Names.PRAGMA, "no-cache");
        response.headers().add(HttpHeaders.Names.CONTENT_ENCODING, GlobalConstant.UTF_8);
        HttpHeaders.setKeepAlive(response, sasParams.isKeepAlive());
        System.getProperties().setProperty("http.keepAlive", String.valueOf(sasParams.isKeepAlive()));

        if (serverChannel.isOpen()) {
            if (sasParams.isKeepAlive()) {
                serverChannel.writeAndFlush(response);
            } else {
                serverChannel.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            }
        }

        totalTime = System.currentTimeMillis() - initialTime;
        LOG.debug("successfully sent response");

        // dst != 0 is true only for servlets rtbdFill, backFill, ixFill
        if (null != sasParams && 0 != sasParams.getDst()) {
            cleanUp();
            LOG.debug("successfully called cleanUp()");
        }
    }

    // send response to the caller
    public void sendResponse(final String responseString, final Channel serverChannel) {
        sendResponse(HttpResponseStatus.OK, responseString, null, serverChannel);
    }

    @Override
    public void sendNoAdResponse(final Channel serverChannel) {
        // Making sure response is sent only once
        if (checkResponseSent()) {
            return;
        }
        LOG.debug("Sending No ads");
        responseSent = true;
        // dst != 0 is true only for servlets rtbdFill, backFill, ixFill
        // This check has been added to prevent totalNoFills from being updated, when any servlet other than the ones
        // mentioned above throws an exception in HttpRequestHandler.
        if (null != sasParams && 0 != sasParams.getDst()) {
            InspectorStats.incrementStatCount(InspectorStrings.TOTAL_NO_FILLS);
        }

        HttpResponseStatus httpResponseStatus = HttpResponseStatus.NO_CONTENT;
        String defaultContent = NO_AD_IMAI;
        final ResponseFormat rFormat = getResponseFormat();
        if (rFormat != null) {
            switch (rFormat) {
                case IMAI:
                    // status code 204 whenever format=imai
                    httpResponseStatus = HttpResponseStatus.NO_CONTENT;
                    defaultContent = NO_AD_IMAI;
                    break;
                case JSON:
                    httpResponseStatus = HttpResponseStatus.NO_CONTENT;
                    defaultContent = NO_AD_IMAI;

                    if (DCP.getValue() == sasParams.getDst()) {
                        defaultContent = String.format(DCP_NATIVE_WRAPPING_AD_JSON, sasParams.getRequestGuid(),
                                StringUtils.EMPTY);
                        LOG.debug("Wrapping in JSON for DCP. Wrapped Response is: {}", defaultContent);
                    }

                    break;
                case NATIVE:
                    // status code 200 and empty ad content( i.e. ads:[]) for format = native
                    httpResponseStatus = HttpResponseStatus.OK;
                    defaultContent = StringUtils.EMPTY;

                    // Native on dcp
                    if (DCP.getValue() == sasParams.getDst()
                            && Formatter.isRequestFromSdkVersionOnwards(sasParams, 500)) {
                        defaultContent = String.format(DCP_NATIVE_WRAPPING_AD_JSON, sasParams.getRequestGuid(),
                                StringUtils.EMPTY);
                        LOG.debug("Wrapping in JSON for SDK > 500 & DCP. Wrapped Response is: {}", defaultContent);
                    }
                    break;
                case XHTML:
                    // status code 200 & empty ad content (i.e. adUnit missing) for format=xml
                    httpResponseStatus = HttpResponseStatus.OK;
                    defaultContent = NO_AD_XHTML;
                    break;
                case HTML:
                    httpResponseStatus = HttpResponseStatus.OK;
                    defaultContent = NO_AD_HTML;
                    break;
                case JS_AD_CODE:
                    httpResponseStatus = HttpResponseStatus.OK;
                    defaultContent = String.format(NO_AD_JS_ADCODE, sasParams.getRqIframe());
                    break;
                case VAST:
                    httpResponseStatus = HttpResponseStatus.OK;
                    defaultContent = NO_AD_VAST;
                    break;
            }
        }
        sendResponse(getResponseStatus(sasParams.getDst(), httpResponseStatus),
                getResponseBytes(sasParams.getDst(), defaultContent), new HashMap<String, String>(), serverChannel);
    }

    private HttpResponseStatus getResponseStatus(final int dstType, final HttpResponseStatus httpResponseStatus) {
        if (dstType == RTBD.getValue() || dstType == IX.getValue()) {
            return HttpResponseStatus.OK;
        }
        return httpResponseStatus;
    }

    private byte[] getResponseBytes(final int dstType, final String defaultResponse) {
        if (dstType == RTBD.getValue() || dstType == IX.getValue()) {
            final AdPoolResponse rtbdResponse = new AdPoolResponse();
            try {
                final TSerializer serializer = new TSerializer(new TBinaryProtocol.Factory());
                return serializer.serialize(rtbdResponse);
            } catch (final TException e) {
                LOG.error("Error in serializing the adPool response ", e);
                return StringUtils.EMPTY.getBytes(Charsets.UTF_8);
            }
        }
        return defaultResponse.getBytes(Charsets.UTF_8);
    }

    /**
     * @return true if request contains Iframe Id and is a request from js adcode.
     */
    private boolean isJsAdRequest() {
        if (null == sasParams) {
            return false;
        }
        final String adCode = sasParams.getAdcode();
        final String rqIframe = sasParams.getRqIframe();
        return adCode != null && rqIframe != null && "JS".equalsIgnoreCase(adCode);
    }

    @Override
    public Boolean isEligibleForProcess(final AdNetworkInterface adNetwork) {
        if (null == rankList) {
            return false;
        }
        final int index = getRankIndex(adNetwork);
        LOG.debug("inside isEligibleForProcess for {} and index is {}", adNetwork.getName(), index);
        return index == 0 || index == rankIndexToProcess;
    }

    @Override
    public Boolean isLastEntry(final AdNetworkInterface adNetwork) {
        final int index = getRankIndex(adNetwork);
        LOG.debug("inside isLastEntry for {} and index is {}", adNetwork.getName(), index);
        return index == rankList.size() - 1;
    }

    // Iterates over the complete rank list and set the new value for
    // rankIndexToProcess.
    @Override
    public void reassignRanks(final AdNetworkInterface adNetworkCaller, final Channel serverChannel) {
        int index = getRankIndex(adNetworkCaller);
        LOG.debug("reassignRanks called for {} and index is {}", adNetworkCaller.getName(), index);

        while (index < rankList.size()) {
            final ChannelSegment channelSegment = rankList.get(index);
            final AdNetworkInterface adNetwork = channelSegment.getAdNetworkInterface();

            LOG.debug("reassignRanks iterating for {} and index is {}", adNetwork.getName(), index);

            if (adNetwork.isRequestCompleted()) {
                if (adNetwork.getResponseAd().getResponseStatus() == ThirdPartyAdResponse.ResponseStatus.SUCCESS) {
                    // Sends the response if request is completed for the
                    // specific adapter.
                    sendAdResponse(adNetwork, serverChannel);
                    break;
                } else {
                    // Iterates to the next adapter.
                    index++;
                }
            } else {
                // Updates the value of rankIndexToProcess which is the next
                // index to be processed.
                rankIndexToProcess = index;
                break;
            }
        }
        // Sends no ad if reached to the end of the rank list.
        if (index == rankList.size()) {
            sendNoAdResponse(serverChannel);
        }
    }

    @Override
    public void cleanUp() {
        // Making sure cleanup is called only once
        if (requestCleaned) {
            return;
        }
        requestCleaned = true;
        LOG.debug("trying to close open channels");

        // closing unclosed dcp channels
        for (int index = 0; rankList != null && index < rankList.size(); index++) {
            LOG.debug("calling clean up for channel {}", rankList.get(index).getAdNetworkInterface());
            try {
                rankList.get(index).getAdNetworkInterface().cleanUp();
            } catch (final Exception exception) {
                LOG.debug("Error in closing channel for index: {} Name: {} Exception: {}", index,
                        rankList.get(index).getAdNetworkInterface(), exception);
            }
        }

        // Closing RTB channels
        final List<ChannelSegment> rtbList = auctionEngine.getUnfilteredChannelSegmentList();

        for (int index = 0; rtbList != null && index < rtbList.size(); index++) {
            LOG.debug("calling clean up for channel {}", rtbList.get(index).getAdNetworkInterface().getId());
            try {
                rtbList.get(index).getAdNetworkInterface().cleanUp();
            } catch (final Exception exception) {
                LOG.debug("Error in closing channel for index: {}  Name: {} Exception: {}", index,
                        rtbList.get(index).getAdNetworkInterface(), exception);
            }
        }

        LOG.debug("done with closing channels");
        writeLogs();
    }

    public void writeLogs() {
        if (null == sasParams) {
            InspectorStats.incrementStatCount(InspectorStrings.NON_AD_REQUESTS);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Done with logging as either sasParam is null or this is not an ad request");
            }
            return;
        }

        final List<ChannelSegment> dcpList = rankList;
        final List<ChannelSegment> rtbList = auctionEngine.getUnfilteredChannelSegmentList(); // Also acts as the ixList
        final List<ChannelSegment> list = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(dcpList) && CollectionUtils.isNotEmpty(rtbList)) {
            LOG.debug("Both DCP and RTBD/IX channel segment lists cannot be populated at the same time. "
                    + "Aborting Logging");
            return;
        } else if (CollectionUtils.isNotEmpty(dcpList)) {
            if (DCP.getValue() != sasParams.getDst()) {
                LOG.debug("DCP channel segment list cannot be populated when dst is not DCP. Aborting Logging");
                return;
            }
            list.addAll(dcpList);
        } else if (CollectionUtils.isNotEmpty(rtbList)) {
            if (DCP.getValue() == sasParams.getDst()) {
                LOG.debug("RTBD/IX channel segment list cannot be populated when dst is DCP. Aborting Logging");
                return;
            }
            list.addAll(rtbList);
        }

        long totalTime = getTotalTime();
        totalTime = totalTime > 2000 ? 0 : totalTime;
        try {
            ChannelSegment adResponseChannelSegment = null;
            if (null != getRtbResponse()) {
                adResponseChannelSegment = getRtbResponse();
            } else if (null != getAdResponse()) {
                adResponseChannelSegment = rankList.get(getSelectedAdIndex());
            }

            Logging.rrLogging(traceMarker, adResponseChannelSegment, list, sasParams, casInternalRequestParameters,
                    terminationReason, totalTime);
            Logging.advertiserLogging(list, CasConfigUtil.getLoggerConfig());
            Logging.sampledAdvertiserLogging(list, CasConfigUtil.getLoggerConfig());
            Logging.creativeLogging(list, sasParams);
            final NestedActivityRecord nestedActivityRecord =
                    photonCasActivityWriter.getNestedActivityRecord(adResponseChannelSegment, sasParams);
            photonCasActivityWriter.publish(nestedActivityRecord);
        } catch (final JSONException exception) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Exception raised in ResponseSender {}", exception);
                LOG.debug(getStackTrace(exception));
            }
        } catch (final TException exception) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Exception raised in ResponseSender {}", exception);
                LOG.debug(getStackTrace(exception));
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Done with logging");
        }
    }

    private int getRankIndex(final AdNetworkInterface adNetwork) {
        int index;
        for (index = 0; index < rankList.size(); index++) {
            if (rankList.get(index).getAdNetworkInterface().getImpressionId().equals(adNetwork.getImpressionId())) {
                break;
            }
        }
        return index;
    }

    // return the response format
    public ResponseFormat getResponseFormat() {
        if (sasParams == null) {
            return ResponseFormat.HTML;
        }

        final String responseFormat = sasParams.getRFormat();
        if (isJsAdRequest()) {
            return ResponseFormat.JS_AD_CODE;
        } else if (null == responseFormat) {
            return ResponseFormat.HTML;
        }
        return ResponseFormat.getValue(responseFormat);
    }

    @Override
    public void processDcpList(final Channel channel) {
        // There would always be rtb partner before going to dcp list
        // So will iterate over the dcp list once.
        if (getRankList().isEmpty()) {
            LOG.debug("dcp list is empty so sending NoAd");
            sendNoAdResponse(channel);
            return;
        }
        int rankIndex = rankIndexToProcess;
        if (rankList.size() <= rankIndex) {
            return;
        }
        ChannelSegment segment = getRankList().get(rankIndex);
        while (segment.getAdNetworkInterface().isRequestCompleted()) {
            if (segment.getAdNetworkInterface().getResponseAd().getResponseStatus() == ResponseStatus.SUCCESS) {
                this.sendAdResponse(segment.getAdNetworkInterface(), channel);
                break;
            }
            rankIndex++;
            if (rankIndex >= getRankList().size()) {
                sendNoAdResponse(channel);
                break;
            }
            segment = getRankList().get(rankIndex);
        }
        setRankIndexToProcess(rankIndex);
    }

    @Override
    public void processDcpPartner(final Channel channel, final AdNetworkInterface adNetworkInterface) {
        if (!isEligibleForProcess(adNetworkInterface)) {
            LOG.debug("{} is not eligible for processing", adNetworkInterface.getName());
            return;
        }
        LOG.debug("the channel is eligible for processing");
        if (adNetworkInterface.getResponseAd().getResponseStatus() == ThirdPartyAdResponse.ResponseStatus.SUCCESS) {
            sendAdResponse(adNetworkInterface, channel);
            cleanUp();
        } else if (isLastEntry(adNetworkInterface)) {
            sendNoAdResponse(channel);
            cleanUp();
        } else {
            reassignRanks(adNetworkInterface, channel);
        }
    }
}
