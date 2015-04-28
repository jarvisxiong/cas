package com.inmobi.adserve.channels.server.requesthandler;

import static com.inmobi.adserve.channels.util.Utils.ExceptionBlock.getStackTrace;
import static com.inmobi.casthrift.DemandSourceType.DCP;
import static com.inmobi.casthrift.DemandSourceType.IX;
import static com.inmobi.casthrift.DemandSourceType.RTBD;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.thirdparty.guava.common.collect.Sets;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.inject.Provider;
import com.googlecode.cqengine.resultset.common.NoSuchObjectException;
import com.googlecode.cqengine.resultset.common.NonUniqueObjectException;
import com.inmobi.adserve.adpool.AdInfo;
import com.inmobi.adserve.adpool.AdPoolResponse;
import com.inmobi.adserve.adpool.AuctionType;
import com.inmobi.adserve.adpool.Creative;
import com.inmobi.adserve.adpool.EncryptionKeys;
import com.inmobi.adserve.channels.adnetworks.ix.IXAdNetwork;
import com.inmobi.adserve.channels.adnetworks.mvp.HostedAdNetwork;
import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse.ResponseStatus;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.IXPackageEntity;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.server.auction.AuctionEngine;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.config.GlobalConstant;
import com.inmobi.casthrift.ADCreativeType;
import com.inmobi.casthrift.DemandSourceType;
import com.inmobi.casthrift.umprr.Csids;
import com.inmobi.commons.security.api.InmobiSession;
import com.inmobi.commons.security.impl.InmobiSecurityImpl;
import com.inmobi.commons.security.util.exception.InmobiSecureException;
import com.inmobi.commons.security.util.exception.InvalidMessageException;
import com.inmobi.types.AdIdChain;
import com.inmobi.types.GUID;
import com.inmobi.types.PricingModel;

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

    private static final String START_TAG =
            "<AdResponse><Ads number=\"1\"><Ad type=\"rm\" width=\"%s\" height=\"%s\"><![CDATA[";
    private static final String END_TAG = " ]]></Ad></Ads></AdResponse>";
    private static final String AD_IMAI_START_TAG = "<!DOCTYPE html>";
    private static final String NO_AD_IMAI = StringUtils.EMPTY;
    private static final String NO_AD_XHTML = "<AdResponse><Ads></Ads></AdResponse>";
    private static final String NO_AD_HTML = "<!-- mKhoj: No advt for this position -->";
    private static final String NO_AD_JS_ADCODE = "<html><head><title></title><style type=\"text/css\">"
            + " body {margin: 0; overflow: hidden; background-color: transparent}"
            + " </style></head><body class=\"nofill\"><!-- NO FILL -->"
            + "<script type=\"text/javascript\" charset=\"utf-8\">"
            + "parent.postMessage('{\"topic\":\"nfr\",\"container\" : \"%s\"}', '*');</script></body></html>";
    private static Set<String> SUPPORTED_RESPONSE_FORMATS = Sets.newHashSet("html", "xhtml", "axml", "imai", "native");

    /**
     * At IX-Rubicon, bid will be taken by DSP's who have deal with the publisher, if the bid is absent, then the return
     * ad from IX will alse be absent.
     */
    protected static final String PRIVATE_AUCTION_DEAL = "PRIVATE_AUCTION_DEAL";

    /**
     * At IX-Rubicon, at first, bid will be taken by DSP's who have deal with the publisher, if the bid is absent, then
     * the bid is considered from the remaining DSP's.
     */
    protected static final String RIGHT_TO_FIRST_REFUSAL_DEAL = "RIGHT_TO_FIRST_REFUSAL_DEAL";

    /**
     * At IX-Rubicon, bid will be taken by open auction from all DSP's, but the with DSP who have this deal, they will
     * get additional info like geo, bidLandScaping etc
     */
    protected static final String PREFERRED_DEAL = "PREFERRED_DEAL";

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
    private final AuctionEngine auctionEngine;
    private final Object lock = new Object();
    private String terminationReason;
    private final long initialTime;
    private Marker traceMarker;

    @Setter
    @Inject
    private static Provider<Marker> traceMarkerProvider;

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
    
    public long getTimeElapsed(){
        return System.currentTimeMillis() - initialTime;
    }
    
    public String getDST(){
        if(sasParams.getDst() == 0){
            return StringUtils.EMPTY;
        }
        return DemandSourceType.findByValue(sasParams.getDst()).toString();
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
    }

    @Override
    public void sendAdResponse(final AdNetworkInterface selectedAdNetwork, final Channel serverChannel) {

        /*
         * Updating rankList for the selected DSP ChannelSegment.
         * This is being done so that all the logging happens on the selected DSP parameters, not on the RP parameters.
         * NOTE: In case of No Ad, the logging will happen on RP parameters only.
         */
        if (selectedAdNetwork instanceof IXAdNetwork) {
            getAuctionEngine().setUnfilteredChannelSegmentList(Arrays.asList(getAuctionEngine().getAuctionResponse()));
        }

        adResponse = selectedAdNetwork.getResponseAd();
        selectedAdIndex = getRankIndex(selectedAdNetwork);

        Boolean isHASAdResponse = false;
        if (selectedAdNetwork instanceof HostedAdNetwork) {
            isHASAdResponse = true;
        }

        sendAdResponse(adResponse, serverChannel, selectedAdNetwork.getSelectedSlotId(),
                selectedAdNetwork.getRepositoryHelper(), isHASAdResponse);
    }

    // send Ad Response

    private void sendAdResponse(final ThirdPartyAdResponse adResponse, final Channel serverChannel,
            final Short selectedSlotId, final RepositoryHelper repositoryHelper, final Boolean isHASAdResponse) {
        // Making sure response is sent only once
        if (checkResponseSent()) {
            return;
        }

        LOG.debug("ad received so trying to send ad response");
        String finalResponse = adResponse.getResponse();
        final SlotSizeMapEntity slotSizeMapEntity = repositoryHelper.querySlotSizeMapRepository(selectedSlotId);
        if (slotSizeMapEntity != null) {
            LOG.debug("slot served is {}", selectedSlotId);

            if (getResponseFormat() == ResponseFormat.XHTML) {
                final Dimension dim = slotSizeMapEntity.getDimension();
                final String startElement = String.format(START_TAG, (int) dim.getWidth(), (int) dim.getHeight());
                finalResponse = startElement + finalResponse + END_TAG;
            } else if (getResponseFormat() == ResponseFormat.IMAI) {
                finalResponse = AD_IMAI_START_TAG + finalResponse;
            }
        } else {
            LOG.info("invalid slot, so not returning response, even though we got an ad");
            InspectorStats.incrementStatCount(InspectorStrings.TOTAL_NO_FILLS);
            if (getResponseFormat() == ResponseFormat.XHTML) {
                finalResponse = NO_AD_XHTML;
            }
            sendResponse(HttpResponseStatus.OK, finalResponse, adResponse.getResponseHeaders(), serverChannel);
            return;
        }

        if (sasParams.getDst() == DCP.getValue()) {
            sendResponse(HttpResponseStatus.OK, finalResponse, adResponse.getResponseHeaders(), serverChannel);
            incrementStatsForFills(sasParams.getDst(), isHASAdResponse);
        } else {
            final String dstName = DemandSourceType.findByValue(sasParams.getDst()).toString();
            final AdPoolResponse rtbdOrIxResponse = createThriftResponse(adResponse.getResponse(), repositoryHelper);
            LOG.debug("{} response json to RE is {}", dstName, rtbdOrIxResponse);
            if (null == rtbdOrIxResponse || !SUPPORTED_RESPONSE_FORMATS.contains(sasParams.getRFormat())) {
                sendNoAdResponse(serverChannel);
            } else {
                try {
                    final TSerializer serializer = new TSerializer(new TBinaryProtocol.Factory());
                    final byte[] serializedResponse = serializer.serialize(rtbdOrIxResponse);
                    sendResponse(HttpResponseStatus.OK, serializedResponse, adResponse.getResponseHeaders(),
                            serverChannel);
                    incrementStatsForFills(sasParams.getDst(), isHASAdResponse);
                } catch (final TException e) {
                    LOG.error("Error in serializing the adPool response ", e);
                    sendNoAdResponse(serverChannel);
                }
            }
        }
    }

    private void incrementStatsForFills(final int dst, final Boolean isHASAdResponse) {
        if (dst == DemandSourceType.DCP.getValue()) {
            InspectorStats.incrementStatCount(InspectorStrings.DCP_FILLS);
        } else if (dst == DemandSourceType.RTBD.getValue()) {
            if (isHASAdResponse) {
                InspectorStats.incrementStatCount(InspectorStrings.HOSTED_FILLS);
            }
            InspectorStats.incrementStatCount(InspectorStrings.RULE_ENGINE_FILLS);
        } else if (dst == DemandSourceType.IX.getValue()) {
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

    protected AdPoolResponse createThriftResponse(final String finalResponse, final RepositoryHelper repositoryHelper) {
        final AdPoolResponse adPoolResponse = new AdPoolResponse();
        final AdInfo rtbdAd = new AdInfo();
        final AdIdChain adIdChain = new AdIdChain();
        final Csids csids = new Csids();

        final ChannelSegmentEntity channelSegmentEntity = getRtbResponse().getChannelSegmentEntity();
        final ADCreativeType responseCreativeType = getRtbResponse().getAdNetworkInterface().getCreativeType();

        adIdChain.setAdgroup_guid(channelSegmentEntity.getAdgroupId());
        adIdChain.setAd_guid(channelSegmentEntity.getAdId(responseCreativeType));
        adIdChain.setCampaign_guid(channelSegmentEntity.getCampaignId());
        adIdChain.setAd(channelSegmentEntity.getIncId(responseCreativeType));
        adIdChain.setGroup(channelSegmentEntity.getAdgroupIncId());
        adIdChain.setCampaign(channelSegmentEntity.getCampaignIncId());
        adIdChain.setAdvertiser_guid(channelSegmentEntity.getAdvertiserId());

        rtbdAd.setPricingModel(PricingModel.CPM);

        // TODO: Create method and write UT
        switch (getRtbResponse().getAdNetworkInterface().getDst()) {
            case IX: // If IX,

                // Set IX specific parameters
                if (getRtbResponse().getAdNetworkInterface() instanceof IXAdNetwork) {
                    final IXAdNetwork ixAdNetwork = (IXAdNetwork) getRtbResponse().getAdNetworkInterface();
                    final String dealId = ixAdNetwork.returnDealId();
                    final long highestBid = (long) (ixAdNetwork.returnAdjustBid() * Math.pow(10, 6));

                    IXPackageEntity dealIXPackageEntity = null;
                    // Checking whether a dealId was provided in the bid response
                    if (dealId != null) {
                        try {
                            dealIXPackageEntity = repositoryHelper.queryIxPackageByDeal(dealId);
                        } catch (final NoSuchObjectException exception) {
                            LOG.error("For the dealId, we dont have entry in our system {}", dealId);
                            InspectorStats.incrementStatCount(InspectorStrings.IX_DEAL_NON_EXISTING);
                        } catch (final NonUniqueObjectException exception) {
                            LOG.error("For the dealId, we dont have entry in our system {}", dealId);
                            InspectorStats.incrementStatCount(InspectorStrings.IX_DEAL_NON_EXISTING);
                        }
                    }


                    if (null != dealIXPackageEntity) {

                        if (ixAdNetwork.isExternalPersonaDeal()) {
                            csids.setMatchedCsids(ixAdNetwork.returnUsedCsids());
                            TSerializer serializer = new TSerializer(new TBinaryProtocol.Factory());
                            try {
                                adPoolResponse.setRequestPoolSpecificInfo(serializer.serialize(csids));
                            } catch (TException exc) {
                                LOG.error("Could not send csId to UMP, thrift exception {}", exc);

                            }
                        }

                        final int indexOfDealId = dealIXPackageEntity.getDealIds().indexOf(dealId);

                        final String dealType =
                                dealIXPackageEntity.getAccessTypes().size() > indexOfDealId ? dealIXPackageEntity
                                        .getAccessTypes().get(indexOfDealId) : "RIGHT_TO_FIRST_REFUSAL_DEAL";
                        rtbdAd.setDealId(dealId);
                        rtbdAd.setHighestBid(highestBid);
                        rtbdAd.setAuctionType(AuctionType.FIRST_PRICE);

                        if (RIGHT_TO_FIRST_REFUSAL_DEAL.contentEquals(dealType)) {
                            // At IX-Rubicon, at first, bid will be taken by DSP's who have deal with the publisher, if
                            // the bid is absent, then
                            // get additional info like geo, bidLandScaping etc
                            rtbdAd.setAuctionType(AuctionType.TRUMP);
                        }
                    } else {
                        // otherwise auction type is set to FIRST_PRICE
                        rtbdAd.setAuctionType(AuctionType.FIRST_PRICE);
                    }
                }
                break;

            case RTBD:
                // If Hosted Ad Server response then Auction Type is set to PREFERRED_DEAL
                if (getRtbResponse().getAdNetworkInterface() instanceof HostedAdNetwork) {
                    rtbdAd.setPricingModel(PricingModel.CPC);
                    rtbdAd.setAuctionType(AuctionType.TRUMP);
                } else {
                    // For normal RTBD responses, auction type is set to SECOND_PRICE
                    rtbdAd.setAuctionType(AuctionType.SECOND_PRICE);
                }
                break;
            default: // For DCP, auction type is set to SECOND_PRICE
                rtbdAd.setAuctionType(AuctionType.SECOND_PRICE);
                break;
        }

        final List<AdIdChain> adIdChains = new ArrayList<AdIdChain>();
        adIdChains.add(adIdChain);
        rtbdAd.setAdIds(adIdChains);

        final long bid = (long) (getRtbResponse().getAdNetworkInterface().getBidPriceInUsd() * Math.pow(10, 6));
        rtbdAd.setPrice(bid);
        rtbdAd.setBid(bid);
        final UUID uuid = UUID.fromString(getRtbResponse().getAdNetworkInterface().getImpressionId());
        rtbdAd.setImpressionId(new GUID(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits()));
        rtbdAd.setSlotServed(getRtbResponse().getAdNetworkInterface().getSelectedSlotId());
        final Creative rtbdCreative = new Creative();
        rtbdCreative.setValue(finalResponse);
        rtbdAd.setCreative(rtbdCreative);
        adPoolResponse.setAds(Arrays.asList(rtbdAd));
        adPoolResponse
                .setMinChargedValue((long) (getRtbResponse().getAdNetworkInterface().getSecondBidPriceInUsd() * Math
                        .pow(10, 6)));
        if (!GlobalConstant.USD.equalsIgnoreCase(getRtbResponse().getAdNetworkInterface().getCurrency())) {
            rtbdAd.setOriginalCurrencyName(getRtbResponse().getAdNetworkInterface().getCurrency());
            rtbdAd.setBidInOriginalCurrency((long) (getRtbResponse().getAdNetworkInterface().getBidPriceInLocal() * Math
                    .pow(10, 6)));
        }
        return adPoolResponse;
    }

    // send response to the caller
    @SuppressWarnings("rawtypes")
    private void sendResponse(final HttpResponseStatus status, final String responseString, final Map responseHeaders,
            final Channel serverChannel) {
        final byte[] bytes = responseString.getBytes(Charsets.UTF_8);
        sendResponse(status, bytes, responseHeaders, serverChannel);
    }

    // send response to the caller
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void sendResponse(final HttpResponseStatus status, byte[] responseBytes, final Map responseHeaders,
            final Channel serverChannel) {
        LOG.debug("Inside send Response");
        if (responseBytes.length > 0) {
            responseBytes = encryptResponseIfRequired(responseBytes);
        }
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
        HttpHeaders.setKeepAlive(response, sasParams.isKeepAlive());
        System.getProperties().setProperty("http.keepAlive", String.valueOf(sasParams.isKeepAlive()));
       
        if(serverChannel.isOpen()){
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

    /**
     * @param responseBytes
     * @return
     */
    private byte[] encryptResponseIfRequired(byte[] responseBytes) {
        if (sasParams.getSdkVersion() != null
                && Integer.parseInt(sasParams.getSdkVersion().substring(1)) >= ENCRYPTED_SDK_BASE_VERSION
                && sasParams.getDst() == 2
                && sasParams.getEncryptionKey() != null) {
            LOG.debug("Encrypting the response as request is from SDK: {}", sasParams.getSdkVersion());
            final EncryptionKeys encryptionKey = sasParams.getEncryptionKey();
            final InmobiSession inmobiSession = new InmobiSecurityImpl(null).newSession(null);
            try {
                responseBytes =
                        inmobiSession.write(responseBytes, encryptionKey.getAesKey(),
                                encryptionKey.getInitializationVector());
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Encyption Details:  EncryptionKey: {}  IVBytes: {}  Response: {}", new String(
                            encryptionKey.getAesKey(), CharsetUtil.UTF_8),
                            new String(encryptionKey.getInitializationVector(), CharsetUtil.UTF_8), new String(
                                    responseBytes, CharsetUtil.UTF_8));
                }
            } catch (InmobiSecureException | InvalidMessageException e) {
                LOG.info("Exception while encrypting response from {}", e);
                throw new RuntimeException(e);
            }
        }
        return responseBytes;
    }

    // send response to the caller
    public void sendResponse(final String responseString, final Channel serverChannel) {
        sendResponse(HttpResponseStatus.OK, responseString, null, serverChannel);
    }

    @Override
    public AuctionEngine getAuctionEngine() {
        return auctionEngine;
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

        HttpResponseStatus httpResponseStatus;
        String defaultContent;
        switch (getResponseFormat()) {
            case IMAI:
                // status code 204 whenever format=imai
                httpResponseStatus = HttpResponseStatus.NO_CONTENT;
                defaultContent = NO_AD_IMAI;
                break;
            case NATIVE:
                // status code 200 and empty ad content( i.e. ads:[]) for format =
                // native
                httpResponseStatus = HttpResponseStatus.OK;
                defaultContent = StringUtils.EMPTY;
                break;
            case XHTML:
                // status code 200 & empty ad content (i.e. adUnit missing) for
                // format=xml
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
            default:
                httpResponseStatus = HttpResponseStatus.OK;
                defaultContent = NO_AD_HTML;
                break;
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

    // Return true if request contains Iframe Id and is a request from js
    // adcode.
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
                LOG.debug("Error in closing channel for index: {} Name: {} Exception: {}", index, rankList.get(index)
                        .getAdNetworkInterface(), exception);
            }
        }

        // Closing RTB channels
        final List<ChannelSegment> rtbList = auctionEngine.getUnfilteredChannelSegmentList();

        for (int index = 0; rtbList != null && index < rtbList.size(); index++) {
            LOG.debug("calling clean up for channel {}", rtbList.get(index).getAdNetworkInterface().getId());
            try {
                rtbList.get(index).getAdNetworkInterface().cleanUp();
            } catch (final Exception exception) {
                LOG.debug("Error in closing channel for index: {}  Name: {} Exception: {}", index, rtbList.get(index)
                        .getAdNetworkInterface(), exception);
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

        List<ChannelSegment> dcpList = rankList;
        List<ChannelSegment> rtbList = auctionEngine.getUnfilteredChannelSegmentList(); // Also acts as the ixList
        final List<ChannelSegment> list = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(dcpList) && CollectionUtils.isNotEmpty(rtbList)) {
            LOG.debug("Both DCP and RTBD/IX channel segment lists cannot be populated at the same time. "
                    + "Aborting Logging");
            return;
        } else if (CollectionUtils.isNotEmpty(dcpList)) {
            if (DemandSourceType.DCP.getValue() != sasParams.getDst()) {
                LOG.debug("DCP channel segment list cannot be populated when dst is not DCP. Aborting Logging");
                return;
            }
            list.addAll(dcpList);
        } else if (CollectionUtils.isNotEmpty(rtbList)) {
            if (DemandSourceType.DCP.getValue() == sasParams.getDst()) {
                LOG.debug("RTBD/IX channel segment list cannot be populated when dst is DCP. Aborting Logging");
                return;
            }
            list.addAll(rtbList);
        }

        long totalTime = getTotalTime();
        if (totalTime > 2000) {
            totalTime = 0;
        }
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
        if(LOG.isDebugEnabled()) {
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

    public enum ResponseFormat {
        XHTML("axml", "xhtml"), HTML("html"), IMAI("imai"), NATIVE("native"), JS_AD_CODE("jsAdCode");

        private String[] formats;

        private static final Map<String, ResponseFormat> STRING_TO_FORMAT_MAP = Maps.newHashMap();

        static {
            for (final ResponseFormat responseFormat : ResponseFormat.values()) {
                for (final String format : responseFormat.formats) {
                    STRING_TO_FORMAT_MAP.put(format.toLowerCase(), responseFormat);
                }
            }
        }

        private ResponseFormat(final String... formats) {
            this.formats = formats;
        }

        public static ResponseFormat getValue(final String format) {
            return STRING_TO_FORMAT_MAP.get(format.toLowerCase());
        }

    }
}
