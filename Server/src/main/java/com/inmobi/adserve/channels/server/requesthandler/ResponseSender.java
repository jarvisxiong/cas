package com.inmobi.adserve.channels.server.requesthandler;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.inject.Provider;
import com.inmobi.adserve.adpool.*;
import com.inmobi.adserve.channels.adnetworks.ix.IXAdNetwork;
import com.inmobi.adserve.channels.api.*;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse.ResponseStatus;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.server.ChannelServer;
import com.inmobi.adserve.channels.server.auction.AuctionEngine;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.casthrift.ADCreativeType;
import com.inmobi.casthrift.DemandSourceType;
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
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import org.apache.hadoop.thirdparty.guava.common.collect.Sets;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import javax.inject.Inject;

import java.awt.*;
import java.util.*;
import java.util.List;

public class ResponseSender extends HttpRequestHandlerBase {

	private static final int ENCRYPTED_SDK_BASE_VERSION = 430;

	private final static Logger LOG = LoggerFactory.getLogger(ResponseSender.class);

	private static final String START_TAG = "<AdResponse><Ads number=\"1\"><Ad type=\"rm\" width=\"%s\" height=\"%s\"><![CDATA[";
	private static final String END_TAG = " ]]></Ad></Ads></AdResponse>";
	private static final String AD_IMAI_START_TAG = "<!DOCTYPE html>";
	private static final String NO_AD_IMAI = "";
	private static final String NO_AD_XHTML = "<AdResponse><Ads></Ads></AdResponse>";
	private static final String NO_AD_HTML = "<!-- mKhoj: No advt for this position -->";
	private static final String NO_AD_JS_ADCODE = "<html><head><title></title><style type=\"text/css\">"
			+ " body {margin: 0; overflow: hidden; background-color: transparent}" + " </style></head><body class=\"nofill\"><!-- NO FILL -->"
			+ "<script type=\"text/javascript\" charset=\"utf-8\">"
			+ "parent.postMessage('{\"topic\":\"nfr\",\"container\" : \"%s\"}', '*');</script></body></html>";
	private static Set<String> SUPPORTED_RESPONSE_FORMATS = Sets.newHashSet("html", "xhtml", "axml", "imai","native");

	private long totalTime;
	private List<ChannelSegment> rankList;
	private ThirdPartyAdResponse adResponse;
	private boolean responseSent;
	public SASRequestParameters sasParams;
	private int rankIndexToProcess;
	private int selectedAdIndex;
	private boolean requestCleaned;
	public CasInternalRequestParameters casInternalRequestParameters;
	private final AuctionEngine auctionEngine;
	private final Object lock = new Object();
	private String terminationReason;

	private final long initialTime;
    private Marker  traceMarker;

    public String getTerminationReason() {
		return terminationReason;
	}

	public void setTerminationReason(final String terminationReason) {
		this.terminationReason = terminationReason;
	}

	public List<ChannelSegment> getRankList() {
		return this.rankList;
	}

	public void setRankList(final List<ChannelSegment> rankList) {
		this.rankList = rankList;
	}

	private void setRankIndexToProcess(final int rankIndexToProcess) {
		this.rankIndexToProcess = rankIndexToProcess;
	}

	public ThirdPartyAdResponse getAdResponse() {
		return this.adResponse;
	}

	public ChannelSegment getRtbResponse() {
		return auctionEngine.getAuctionResponse();
	}

	public int getSelectedAdIndex() {
		return this.selectedAdIndex;
	}

	public long getTotalTime() {
		return this.totalTime;
	}

	@Inject
	public ResponseSender(final Provider<Marker> traceMarkerProvider) {
		this.initialTime = System.currentTimeMillis();
		this.totalTime = 0;
		this.rankList = null;
		this.adResponse = null;
		this.responseSent = false;
		this.sasParams = null;
		this.rankIndexToProcess = 0;
		this.selectedAdIndex = 0;
		this.requestCleaned = false;
		this.auctionEngine = new AuctionEngine();
        if (null != traceMarkerProvider) {
            this.traceMarker = traceMarkerProvider.get();
        }
    }

    @Override
	public void sendAdResponse(final AdNetworkInterface selectedAdNetwork, final Channel serverChannel) {
		adResponse = selectedAdNetwork.getResponseAd();
		selectedAdIndex = getRankIndex(selectedAdNetwork);
		sendAdResponse(adResponse, serverChannel);
	}

	// send Ad Response
	private void sendAdResponse(final ThirdPartyAdResponse adResponse, final Channel serverChannel) throws NullPointerException {

		// Making sure response is sent only once
		if (checkResponseSent()) {
			return;
		}

		LOG.debug("ad received so trying to send ad response");
		String finalReponse = adResponse.response;
		if (sasParams.getSlot() != null && SlotSizeMapping.getDimension(Long.valueOf(sasParams.getSlot())) != null) {
			LOG.debug("slot served is {}", sasParams.getSlot());
			InspectorStats.incrementStatCount(InspectorStrings.totalFills);
			if (getResponseFormat() == ResponseFormat.XHTML) {
				Dimension dim = SlotSizeMapping.getDimension(Long.valueOf(sasParams.getSlot()));
				String startElement = String.format(START_TAG, (int) dim.getWidth(), (int) dim.getHeight());
				finalReponse = startElement + finalReponse + END_TAG;
			} else if (getResponseFormat() == ResponseFormat.IMAI) {
				finalReponse = AD_IMAI_START_TAG + finalReponse;
			}
		} else {
			LOG.info("invalid slot, so not returning response, even though we got an ad");
			InspectorStats.incrementStatCount(InspectorStrings.totalNoFills);
			if (getResponseFormat() == ResponseFormat.XHTML) {
				finalReponse = NO_AD_XHTML;
			}
			sendResponse(HttpResponseStatus.OK, finalReponse, adResponse.responseHeaders, serverChannel);
			return;
		}
		
		if (sasParams.getDst() == DemandSourceType.DCP.getValue()) {
			sendResponse(HttpResponseStatus.OK, finalReponse, adResponse.responseHeaders, serverChannel);
		} else {
			String dstName = DemandSourceType.findByValue(sasParams.getDst()).name();
			System.out.println(dstName);
			AdPoolResponse rtbdOrIxResponse = createThriftResponse(adResponse.response);
			LOG.debug("{} response json to RE is {}", dstName, rtbdOrIxResponse);
			if (null == rtbdOrIxResponse || !SUPPORTED_RESPONSE_FORMATS.contains(sasParams.getRFormat())) {
				sendNoAdResponse(serverChannel);
			} else {
				try {
					TSerializer serializer = new TSerializer(new TBinaryProtocol.Factory());
					byte[] serializedResponse = serializer.serialize(rtbdOrIxResponse);
					sendResponse(HttpResponseStatus.OK, serializedResponse, adResponse.responseHeaders, serverChannel);
					InspectorStats.incrementStatCount(InspectorStrings.ruleEngineFills);
				} catch (TException e) {
					LOG.error("Error in serializing the adPool response ", e);
					sendNoAdResponse(serverChannel);
				}
			}
		}
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

	protected AdPoolResponse createThriftResponse(final String finalResponse) {
		AdPoolResponse adPoolResponse = new AdPoolResponse();
		AdInfo rtbdAd = new AdInfo();
		AdIdChain adIdChain = new AdIdChain();

		ChannelSegmentEntity channelSegmentEntity = getRtbResponse().getChannelSegmentEntity();
		ADCreativeType responseCreativeType =  getRtbResponse().getAdNetworkInterface().getCreativeType();

		adIdChain.setAdgroup_guid(channelSegmentEntity.getAdgroupId());
		adIdChain.setAd_guid(channelSegmentEntity.getAdId(responseCreativeType));
		adIdChain.setAdvertiser_guid(channelSegmentEntity.getAdvertiserId());
		adIdChain.setCampaign_guid(channelSegmentEntity.getCampaignId());
		adIdChain.setAd(channelSegmentEntity.getIncId(responseCreativeType));
		adIdChain.setGroup(channelSegmentEntity.getAdgroupIncId());
		adIdChain.setCampaign(channelSegmentEntity.getCampaignIncId());
		

        switch(getRtbResponse().getAdNetworkInterface().getDst()) {
            case 8: // If IX,

                // Set IX specific parameters
                if (getRtbResponse().getAdNetworkInterface() instanceof IXAdNetwork) {
                    IXAdNetwork ixAdNetwork = (IXAdNetwork) getRtbResponse().getAdNetworkInterface();
                    String dealId = ixAdNetwork.returnDealId();
                    long highestBid = (long)(ixAdNetwork.returnAdjustBid() * Math.pow(10, 6));
		            int pmptier = ixAdNetwork.returnPmptier();

                    // Advertiser GUID is set from the Buyer field in the BID thrift object in the case of IX
                    adIdChain.setAdvertiser_guid(ixAdNetwork.returnBuyer());
                    
                    // Checking whether a dealId was provided in the bid response
                    if (null != dealId) {
                        // If dealId is present, then auction type is set to PREFERRED_DEAL
                        // and dealId is set
                        rtbdAd.setDealId(dealId);
                        rtbdAd.setHighestBid(highestBid);
                        if (pmptier == 3) {
                            // If private marketplace tier is 3 then the deal is a private auction
                            rtbdAd.setAuctionType(AuctionType.PRIVATE_AUCTION);
                        } else if (ixAdNetwork.returnPmptier() == 4){
                            // If private marketplace tier is 4 then the deal is a preferred deal
                            rtbdAd.setAuctionType(AuctionType.PREFERRED_DEAL);
                        } else {
                            // When pmptier is 0 (default value), auction is an open auction
                            // Other values are reserved for future use
                            rtbdAd.setAuctionType(AuctionType.PREFERRED_DEAL);
                        }
                    } else {
                        // otherwise auction type is set to FIRST_PRICE
                        rtbdAd.setAuctionType(AuctionType.FIRST_PRICE);
                    }
                }
                break;

            default:// For RTBD/DCP, auction type is set to SECOND_PRICE
                adIdChain.setAdvertiser_guid(channelSegmentEntity.getAdvertiserId());
                rtbdAd.setAuctionType(AuctionType.SECOND_PRICE);
                break;
        }

		List<AdIdChain> adIdChains = new ArrayList<AdIdChain>();
		adIdChains.add(adIdChain);
		rtbdAd.setAdIds(adIdChains);

		rtbdAd.setPricingModel(PricingModel.CPM);
		long bid = (long) (getRtbResponse().getAdNetworkInterface().getBidPriceInUsd() * Math.pow(10, 6));
		rtbdAd.setPrice(bid);
		rtbdAd.setBid(bid);
		UUID uuid = UUID.fromString(getRtbResponse().getAdNetworkInterface().getImpressionId());
		rtbdAd.setImpressionId(new GUID(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits()));
		rtbdAd.setSlotServed(sasParams.getSlot());
		Creative rtbdCreative = new Creative();
		rtbdCreative.setValue(finalResponse);
		rtbdAd.setCreative(rtbdCreative);
		adPoolResponse.setAds(Arrays.asList(rtbdAd));
		adPoolResponse.setMinChargedValue((long) (getRtbResponse().getAdNetworkInterface().getSecondBidPriceInUsd() * Math.pow(10, 6)));
		if (!"USD".equalsIgnoreCase(getRtbResponse().getAdNetworkInterface().getCurrency())) {
			rtbdAd.setOriginalCurrencyName(getRtbResponse().getAdNetworkInterface().getCurrency());
			rtbdAd.setBidInOriginalCurrency((long) (getRtbResponse().getAdNetworkInterface().getBidPriceInLocal() * Math.pow(10, 6)));
		}
		return adPoolResponse;
	}

	// send response to the caller
	private void sendResponse(final HttpResponseStatus status, final String responseString, final Map responseHeaders, final Channel serverChannel)
			throws NullPointerException {

		byte[] bytes = responseString.getBytes(Charsets.UTF_8);
		sendResponse(status, bytes, responseHeaders, serverChannel);
	}

	// send response to the caller
	private void sendResponse(final HttpResponseStatus status, byte[] responseBytes, final Map responseHeaders, final Channel serverChannel)
			throws NullPointerException {
		LOG.debug("Inside send Response");

		responseBytes = encryptResponseIfRequired(responseBytes);

		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.wrappedBuffer(responseBytes), false);

		if (null != responseHeaders) {
			for (Map.Entry entry : (Set<Map.Entry>) responseHeaders.entrySet()) {
				response.headers().add(entry.getKey().toString(), responseHeaders.get(entry.getValue()));
			}
		}

		response.headers().add(HttpHeaders.Names.CACHE_CONTROL, "no-cache, no-store, must-revalidate");

		// TODO: to fix keep alive, we need to fix whole flow
		// HttpHeaders.setKeepAlive(response, serverChannel.isKeepAlive());
		response.headers().add(HttpHeaders.Names.CONTENT_LENGTH, responseBytes.length);

		response.headers().add(HttpHeaders.Names.EXPIRES, "-1");
		response.headers().add(HttpHeaders.Names.PRAGMA, "no-cache");
		HttpHeaders.setKeepAlive(response, sasParams.isKeepAlive());

		if (sasParams.isKeepAlive()) {
			serverChannel.writeAndFlush(response);
		} else {
			serverChannel.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
		}

		totalTime = System.currentTimeMillis() - initialTime;
		LOG.debug("successfully sent response");
		if (null != sasParams) {
			cleanUp();
			LOG.debug("successfully called cleanUp()");
		}
	}

	/**
	 * @param responseBytes
	 * @return
	 */
	private byte[] encryptResponseIfRequired(byte[] responseBytes) {
		if (sasParams.getSdkVersion() != null && Integer.parseInt(sasParams.getSdkVersion().substring(1)) >= ENCRYPTED_SDK_BASE_VERSION
				&& sasParams.getDst() == 2) {

			LOG.debug("Encrypting the response as request is from SDK: {}", sasParams.getSdkVersion());
			EncryptionKeys encryptionKey = sasParams.getEncryptionKey();
			InmobiSession inmobiSession = new InmobiSecurityImpl(null).newSession(null);

			try {

				responseBytes = inmobiSession.write(responseBytes, encryptionKey.getAesKey(), encryptionKey.getInitializationVector());

				if (LOG.isDebugEnabled()) {
					LOG.debug("Encyption Details:  EncryptionKey: {}  IVBytes: {}  Response: {}", new String(encryptionKey.getAesKey(), CharsetUtil.UTF_8),
							new String(encryptionKey.getInitializationVector(), CharsetUtil.UTF_8), new String(responseBytes, CharsetUtil.UTF_8));
				}

			} catch (InmobiSecureException | InvalidMessageException e) {
				LOG.info("Exception while encrypting response from {}", e);
				throw new RuntimeException(e);
			}

		}
		return responseBytes;
	}

	// send response to the caller
	public void sendResponse(final String responseString, final Channel serverChannel) throws NullPointerException {
		sendResponse(HttpResponseStatus.OK, responseString, null, serverChannel);
	}

	@Override
	public AuctionEngine getAuctionEngine() {
		return auctionEngine;
	}

	// TODO: does it need to be synchronized?
	@Override
	public void sendNoAdResponse(final Channel serverChannel) throws NullPointerException {
		// Making sure response is sent only once
		if (checkResponseSent()) {
			return;
		}
		LOG.debug("Sending No ads");

		responseSent = true;

		InspectorStats.incrementStatCount(InspectorStrings.totalNoFills);

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
			defaultContent = "";
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
		default:
			httpResponseStatus = HttpResponseStatus.OK;
			defaultContent = NO_AD_HTML;
			break;
		}

		sendResponse(getResponseStatus(sasParams.getDst(), httpResponseStatus),
				getResponseBytes(sasParams.getDst(), defaultContent),
				new HashMap<String, String>(), serverChannel);
	}

	private HttpResponseStatus getResponseStatus(final int dstType, final HttpResponseStatus httpResponseStatus) {
		if (dstType == DemandSourceType.RTBD.getValue() || dstType == DemandSourceType.IX.getValue()) {
			return HttpResponseStatus.OK;
		}
		return httpResponseStatus;
	}

	private byte[] getResponseBytes(final int dstType, final String defaultResponse) {
		if(dstType == DemandSourceType.RTBD.getValue() || dstType == DemandSourceType.IX.getValue()) {
			AdPoolResponse rtbdResponse = new AdPoolResponse();
			try {
				TSerializer serializer = new TSerializer(new TBinaryProtocol.Factory());
				return serializer.serialize(rtbdResponse);
			} catch (TException e) {
				LOG.error("Error in serializing the adPool response ", e);
				return "".getBytes(Charsets.UTF_8);
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
		String adCode = sasParams.getAdcode();
		String rqIframe = sasParams.getRqIframe();
		return adCode != null && rqIframe != null && adCode.equalsIgnoreCase("JS");
	}

	@Override
	public Boolean isEligibleForProcess(final AdNetworkInterface adNetwork) {
		if (null == rankList) {
			return false;
		}
		int index = getRankIndex(adNetwork);
		LOG.debug("inside isEligibleForProcess for {} and index is {}", adNetwork.getName(), index);
		return index == 0 || index == rankIndexToProcess;
	}

	@Override
	public Boolean isLastEntry(final AdNetworkInterface adNetwork) {
		int index = getRankIndex(adNetwork);
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
			ChannelSegment channelSegment = rankList.get(index);
			AdNetworkInterface adNetwork = channelSegment.getAdNetworkInterface();

			LOG.debug("reassignRanks iterating for {} and index is {}", adNetwork.getName(), index);

			if (adNetwork.isRequestCompleted()) {
				if (adNetwork.getResponseAd().responseStatus == ThirdPartyAdResponse.ResponseStatus.SUCCESS) {
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
			} catch (Exception exception) {
				LOG.debug("Error in closing channel for index: {} Name: {} Exception: {}", index, rankList.get(index).getAdNetworkInterface(), exception);
			}
		}

		// Closing RTB channels
		List<ChannelSegment> rtbList = auctionEngine.getUnfilteredChannelSegmentList();

		for (int index = 0; rtbList != null && index < rtbList.size(); index++) {
			LOG.debug("calling clean up for channel {}", rtbList.get(index).getAdNetworkInterface().getId());
			try {
				rtbList.get(index).getAdNetworkInterface().cleanUp();
			} catch (Exception exception) {
				LOG.debug("Error in closing channel for index: {}  Name: {} Exception: {}", index, rtbList.get(index).getAdNetworkInterface(), exception);
			}
		}

		LOG.debug("done with closing channels");
		writeLogs();
	}

	public void writeLogs() {
        if (null == sasParams) {
            InspectorStats.incrementStatCount(InspectorStrings.NON_AD_REQUESTS);
            LOG.debug("Not logging anything, either sasParam is null or this is not an ad request");
            LOG.debug("done with logging");
            return;
        }
		List<ChannelSegment> list = new ArrayList<ChannelSegment>();
		if (null != getRankList()) {
			list.addAll(getRankList());
		}
		if (null != getAuctionEngine().getUnfilteredChannelSegmentList()) {
			list.addAll(getAuctionEngine().getUnfilteredChannelSegmentList());
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
				adResponseChannelSegment = getRankList().get(getSelectedAdIndex());
			}
			Logging.rrLogging(traceMarker, adResponseChannelSegment, list, sasParams, terminationReason, totalTime);
			Logging.advertiserLogging(list, CasConfigUtil.getLoggerConfig());
			Logging.sampledAdvertiserLogging(list, CasConfigUtil.getLoggerConfig());
			Logging.creativeLogging(list, sasParams);
		} catch (JSONException exception) {
			LOG.debug(ChannelServer.getMyStackTrace(exception));
		} catch (TException exception) {
			LOG.debug(ChannelServer.getMyStackTrace(exception));
		}
		LOG.debug("done with logging");
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

		String responseFormat = sasParams.getRFormat();
		if (isJsAdRequest()) {
			return ResponseFormat.JS_AD_CODE;
		} else if (null == responseFormat) {
			return ResponseFormat.HTML;
		} else if ("axml".equalsIgnoreCase(responseFormat)) {
			return ResponseFormat.XHTML;
		}else if("native".equalsIgnoreCase(responseFormat)){
			return ResponseFormat.NATIVE;
        }
		return ResponseFormat.getValue(responseFormat);
	}

	@Override
	public void processDcpList(final Channel channel) {
		// There would always be rtb partner before going to dcp list
		// So will iterate over the dcp list once.
		if (this.getRankList().isEmpty()) {
			LOG.debug("dcp list is empty so sending NoAd");
			this.sendNoAdResponse(channel);
			return;
		}
		int rankIndex = rankIndexToProcess;
		if (rankList.size() <= rankIndex) {
			return;
		}
		ChannelSegment segment = this.getRankList().get(rankIndex);
		while (segment.getAdNetworkInterface().isRequestCompleted()) {
			if (segment.getAdNetworkInterface().getResponseAd().responseStatus == ResponseStatus.SUCCESS) {
				this.sendAdResponse(segment.getAdNetworkInterface(), channel);
				break;
			}
			rankIndex++;
			if (rankIndex >= this.getRankList().size()) {
				this.sendNoAdResponse(channel);
				break;
			}
			segment = getRankList().get(rankIndex);
		}
		this.setRankIndexToProcess(rankIndex);
	}

	@Override
	public void processDcpPartner(final Channel channel, final AdNetworkInterface adNetworkInterface) {
		if (!this.isEligibleForProcess(adNetworkInterface)) {
			LOG.debug("{} is not eligible for processing", adNetworkInterface.getName());
			return;
		}
		LOG.debug("the channel is eligible for processing");
		if (adNetworkInterface.getResponseAd().responseStatus == ThirdPartyAdResponse.ResponseStatus.SUCCESS) {
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

		private static final Map<String, ResponseFormat> stringToFormatMap = Maps.newHashMap();

		static {
			for (ResponseFormat responseFormat : ResponseFormat.values()) {
				for (String format : responseFormat.formats) {
					stringToFormatMap.put(format, responseFormat);
				}
			}

		}

		private ResponseFormat(final String... formats) {
			this.formats = formats;
		}

		public static ResponseFormat getValue(final String format) {
			return stringToFormatMap.get(format.toLowerCase());
		}

	}

}
