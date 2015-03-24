package com.inmobi.adserve.channels.adnetworks.baidu;


import com.baidu.BaiduBidRequest.AdSlot;
import com.baidu.BaiduBidRequest.App;
import com.baidu.BaiduBidRequest.BidRequest;
import com.baidu.BaiduBidRequest.Device;
import com.baidu.BaiduBidRequest.Gps;
import com.baidu.BaiduBidRequest.Network;
import com.baidu.BaiduBidRequest.Size;
import com.baidu.BaiduBidRequest.UdId;
import com.baidu.BaiduBidRequest.Version;
import com.baidu.BaiduBidResponse;
import com.baidu.BaiduBidResponse.BidResponse;
import com.baidu.BaiduBidResponse.MaterialMeta;
import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import com.ning.http.client.RequestBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.velocity.VelocityContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Dimension;
import java.net.URI;
import java.net.URISyntaxException;

public class DCPBaiduAdNetwork extends AbstractDCPAdNetworkImpl {

	private static final Logger LOG = LoggerFactory
			.getLogger(DCPBaiduAdNetwork.class);

	private int width;
	private int height;
	private String size;
	private double latitude;
	private double longitude;
	private String adSlotId=null;
	private static final String BLANK_VAL ="";
	private static final String AD_TRACKING_ENABLED="1";
	private static final String AD_SLOTID_DB_KEY="slot";

	public DCPBaiduAdNetwork(final Configuration config,
			final Bootstrap clientBootstrap,
			final HttpRequestHandlerBase baseRequestHandler,
			final Channel serverChannel) {
		super(config, clientBootstrap, baseRequestHandler, serverChannel);

	}

	@Override
	public boolean configureParameters() {
		if (StringUtils.isBlank(sasParams.getRemoteHostIp())
				|| StringUtils.isBlank(sasParams.getUserAgent())
				|| StringUtils.isBlank(externalSiteId)) {
			LOG.debug("mandatory parameters missing for baidu so exiting adapter");
			return false;
		}
		host = config.getString("baidu.host");
        isByteResponseSupported =true;

		final SlotSizeMapEntity slotSizeMapEntity = repositoryHelper
				.querySlotSizeMapRepository(selectedSlotId);
		if (null != slotSizeMapEntity) {
			final Dimension dim = slotSizeMapEntity.getDimension();
			width = (int) Math.ceil(dim.getWidth());
			height = (int) Math.ceil(dim.getHeight());
		} else {
			LOG.debug("mandate parameters missing for Baidu, so returning from adapter");
			LOG.info("Configure parameters inside Baidu returned false");
			return false;
		}

		if (casInternalRequestParameters.getLatLong() != null
				&& StringUtils.countMatches(
						casInternalRequestParameters.getLatLong(), ",") > 0) {
			String[] latlong = casInternalRequestParameters.getLatLong().split(
					",");
			latitude = Double.valueOf(latlong[0]);
			longitude = Double.valueOf(latlong[1]);

		}
		String udid = getUid();
		if (null == udid) {
			LOG.debug("mandate parameters missing for Baidu, so returning from adapter");
			LOG.info("Configure parameters inside Baidu returned false");
			return false;
		}

		JSONObject additionalParams = entity.getAdditionalParams();

		try {
			// ad slot id is configured as the additional param in the
			// segment table
			adSlotId = entity.getAdditionalParams().getString(AD_SLOTID_DB_KEY);
		} catch (final JSONException e) {
			LOG.debug("slot is not configured for the segment:{} {}, exception raised {}", entity.getExternalSiteKey(),
					getName(), e);
			LOG.info("Configure parameters inside Baidu returned false");
			return false;
		}

		if (sasParams.getOsId() != com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS.Android.getValue() &&
				sasParams.getOsId() != com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS.iOS.getValue()) {
			LOG.debug("Not Android/ios request {}",
					getName());
			LOG.info("Configure parameters inside Baidu returned false");
			return false;
		}
		LOG.info("Configure parameters inside baidu returned true");
		return true;
	}

	@Override
	public String getName() {
		return "baiduDCP";
	}

	@Override
	public URI getRequestUri() throws Exception {
		try {
			return (new URI(host));
		} catch (URISyntaxException exception) {
			errorStatus = ThirdPartyAdResponse.ResponseStatus.MALFORMED_URL;
			LOG.info("{}", exception);
		}
		return null;
	}

	private com.baidu.BaiduBidRequest.BidRequest getRequest(){
		BidRequest.Builder builder = BidRequest.newBuilder();
		AdSlot.Builder adSlotBuilder = AdSlot.newBuilder();
		Size.Builder sizeBuilder = Size.newBuilder();
		sizeBuilder.setHeight(height);
		sizeBuilder.setWidth(width);
		adSlotBuilder.setSize(sizeBuilder);
		adSlotBuilder.setId(adSlotId);

		Version.Builder versionBuilder = Version.newBuilder();
		versionBuilder.setMinor(0);
		versionBuilder.setMajor(4);
		builder.setApiVersion(versionBuilder);
		App.Builder appBuilder = App.newBuilder();
		appBuilder.setId(externalSiteId);
		builder.setApp(appBuilder);

		Device.Builder deviceBuilder = Device.newBuilder();
		//Device device = new Device();
		UdId.Builder udidBuilder = UdId.newBuilder();
		if (StringUtils.isNotBlank(casInternalRequestParameters.getUidIFA())
				&& AD_TRACKING_ENABLED.equals(casInternalRequestParameters.getUidADT()))
		{
			udidBuilder.setIdfa(casInternalRequestParameters.getUidIFA());
		}
		deviceBuilder.setUdid(udidBuilder);
		deviceBuilder.setType(com.baidu.BaiduBidRequest.Device.Type.PHONE);
		deviceBuilder.setVendor(BLANK_VAL);
		deviceBuilder.setModel(BLANK_VAL);

		if (sasParams.getOsId() == HandSetOS.Android.getValue()) { // android
			deviceBuilder.setOs(com.baidu.BaiduBidRequest.Device.Os.ANDROID);
		} else if (sasParams.getOsId() == HandSetOS.iOS.getValue()) { // iPhone
			deviceBuilder.setOs(com.baidu.BaiduBidRequest.Device.Os.IOS);
		}

		Network.Builder networkBuilder = Network.newBuilder();
		networkBuilder.setIpv4(sasParams.getRemoteHostIp());
		if (com.inmobi.adserve.adpool.NetworkType.WIFI == sasParams.getNetworkType()) {
			networkBuilder.setType(Network.Type.WIFI);
		} else {
			networkBuilder.setType(Network.Type.NEW_TYPE);
		}
		builder.setNetwork(networkBuilder);

		Gps.Builder gpsBuilder = Gps.newBuilder();
		if(latitude != 0){
			gpsBuilder.setLatitude(latitude);
			gpsBuilder.setLongitude(longitude);
			gpsBuilder.setType(Gps.Type.WGS84);
			gpsBuilder.setTimestamp((int)System.currentTimeMillis());
		}

		if (StringUtils.isNotEmpty(sasParams.getOsMajorVersion())) {
            Version.Builder osVersionBuilder = com.baidu.BaiduBidRequest.Version.newBuilder();
			String[] osList = sasParams.getOsMajorVersion().split("\\.");
			if(osList.length > 1) {
				osVersionBuilder.setMajor(Integer.parseInt(osList[0]));
				osVersionBuilder.setMinor(Integer.parseInt(osList[1]));
				deviceBuilder.setOsVersion(osVersionBuilder);
			}
		}
		builder.setDevice(deviceBuilder);
		builder.addAdslots(adSlotBuilder);
		builder.setRequestId(impressionId);
		BidRequest request = builder.build();
		LOG.debug(request.toString());
		return request;
	}

	@Override
	public RequestBuilder getNingRequestBuilder() throws Exception {
		URI uri = getRequestUri();
		if (uri.getPort() == -1) {
			uri = new URIBuilder(uri).setPort(80).build();
		}

		RequestBuilder ningRequestBuilder = new RequestBuilder("POST")
				.setUrl(uri.toString())
				.setHeader(HttpHeaders.Names.USER_AGENT,
						sasParams.getUserAgent())
				.setHeader(HttpHeaders.Names.ACCEPT_LANGUAGE, "en-us")
				.setHeader(HttpHeaders.Names.ACCEPT_ENCODING,
						HttpHeaders.Values.BYTES)
				.setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/protobuf")
				.setHeader("X-Forwarded-For", sasParams.getRemoteHostIp())
				.setHeader(HttpHeaders.Names.HOST, uri.getHost())
				.setBody(getRequest().toByteArray());

		LOG.debug("Baidu request: {}", ningRequestBuilder);
		return ningRequestBuilder;
	}

	@Override
	public String getId() {
		return (config.getString("baidu.advertiserId"));
	}

	@Override
	public boolean isClickUrlRequired() {
		return true;
	}

    @Override
    public void parseResponse(final byte[] responseByte, final HttpResponseStatus status) {
        try {
            adStatus = "NO_AD";
            statusCode = 500;
            BidResponse responses = BidResponse.parseFrom(responseByte);
            LOG.debug("Response from baidu {}", responses);
            int adCount = responses.getAdsCount();
            final VelocityContext context;
            if(adCount > 0){
                context = new VelocityContext();
                TemplateType t=TemplateType.HTML;
                MaterialMeta responseMeta = responses.getAds(0).getMaterialMeta();
                BaiduBidResponse.Ad ad= responses.getAds(0);
                context.put(VelocityTemplateFieldConstants.IM_CLICK_URL, clickUrl);
                if(null != responseMeta.getClickUrl()){
                    context.put(VelocityTemplateFieldConstants.PARTNER_CLICK_URL,responseMeta.getClickUrl());
                }
                if(responseMeta.getWinNoticeUrlCount() > 1){
                    context.put(VelocityTemplateFieldConstants.PARTNER_BEACON_URL,responseMeta.getWinNoticeUrl(0));
                    context.put(VelocityTemplateFieldConstants.PARTNER_BEACON_URL_1,responseMeta.getWinNoticeUrl(1));
                }else if(responseMeta.getWinNoticeUrlCount() == 1){
                    context.put(VelocityTemplateFieldConstants.PARTNER_BEACON_URL,responseMeta.getWinNoticeUrl(0));
                }
                if(responseMeta.getCreativeType() == MaterialMeta.CreativeType.HTML) {

                    context.put(VelocityTemplateFieldConstants.PARTNER_HTML_CODE,
                            ad.getHtmlSnippet());
                }else if(responseMeta.getCreativeType() == MaterialMeta.CreativeType.IMAGE) {

                    context.put(VelocityTemplateFieldConstants.PARTNER_IMG_URL,responseMeta.getMediaUrl());
                    t = TemplateType.IMAGE;
                }else if(responseMeta.getCreativeType() == MaterialMeta.CreativeType.TEXT || responseMeta.getCreativeType() == MaterialMeta.CreativeType.TEXT_ICON) {

                    context.put(VelocityTemplateFieldConstants.AD_TEXT,responseMeta.getTitle());
                    if(responseMeta.getDescription1() != null) {
                        context.put(VelocityTemplateFieldConstants.DESCRIPTION, responseMeta.getDescription1());
                    }
                    if(responseMeta.getIconUrl() != null){
                        context.put(VelocityTemplateFieldConstants.PARTNER_IMG_URL, responseMeta.getDescription1());
                    }
                    final String vmTemplate = Formatter.getRichTextTemplateForSlot(selectedSlotId.toString());
                    if (!StringUtils.isEmpty(vmTemplate)) {
                        context.put(VelocityTemplateFieldConstants.TEMPLATE, vmTemplate);
                        t = TemplateType.RICH;
                    } else {
                        t = TemplateType.PLAIN;
                    }
                }else {
                    return;
                }
                responseContent = Formatter.getResponseFromTemplate(
                        t, context, sasParams, beaconUrl);
                LOG.debug("Baidu AD" + responseContent);
                adStatus = "AD";
                statusCode = 200;
            } else {
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
