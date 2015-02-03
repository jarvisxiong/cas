package com.inmobi.adserve.channels.adnetworks.baidu;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.awt.Dimension;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.inmobi.adserve.adpool.NetworkType;
import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import com.inmobi.casthrift.rtb.BidResponse;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;

public class DCPBaiduAdNetwork extends AbstractDCPAdNetworkImpl {
	private static final Logger LOG = LoggerFactory
			.getLogger(DCPBaiduAdNetwork.class);

	private int width;
	private int height;
	private String size;
	private String latLong;

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

		final com.inmobi.adserve.channels.entity.SlotSizeMapEntity slotSizeMapEntity = repositoryHelper
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
			latLong = String.format("%s,%s", latlong[0], latlong[1]);

		}
		String udid = getUid();
		if (null == udid) {
			LOG.debug("mandate parameters missing for Baidu, so returning from adapter");
			LOG.info("Configure parameters inside Baidu returned false");
			return false;
		}

		LOG.info("Configure parameters inside baidu returned true");
		return true;
	}

	@Override
	public String getName() {
		return "baidu";
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

	private String getRequestParams() {
		Device device = new Device();

		if (StringUtils.isNotBlank(casInternalRequestParameters.getUidIFA())
				&& "1".equals(casInternalRequestParameters.getUidADT()))
			;
		{
			Udid udidVal = new Udid();
			udidVal.setIdfa(casInternalRequestParameters.getUidIFA());
			device.setUdid(udidVal);
		}

		App app = new App();
		app.setId(externalSiteId);
		app.setName(blindedSiteId);
		String category[] = getCategories(',', false, false).split(",");
		if (category.length > 0) {
			app.setCategory(category[0]);
		}

		AdSlot adslot = new AdSlot();
		Size adSize = new Size();
		adSize.setHeight(height);
		adSize.setWidth(width);
		adslot.setSize(adSize);

		BaiduRequest request = new BaiduRequest();
		request.setRequest_id(impressionId);
		Version version = new Version();
		version.setMajor(4);
		version.setMinor(0);
		request.setVersion(version);
		request.setApp(app);
		request.setDevice(device);
		request.setAd(adslot);

		Network network = new Network();
		network.setIpv6(sasParams.getRemoteHostIp());
		request.setNetwork(network);

		if (StringUtils.isNotEmpty(sasParams.getOsMajorVersion())) {
			Version osVersionVal = new Version();
			String[] osList = sasParams.getOsMajorVersion().split(".");
			osVersionVal.setMajor(Integer.parseInt(osList[0]));
			osVersionVal.setMinor(Integer.parseInt(osList[1]));
			device.setOs_version(osVersionVal);
		}

		if (NetworkType.WIFI == sasParams.getNetworkType()) {
			request.setCarrier(1);
		} else {
			request.setCarrier(0);
		}

		ObjectMapper mapper = new ObjectMapper();

		try {

			String requestBody = mapper.writeValueAsString(request);
			LOG.debug(requestBody);
			return requestBody;
		} catch (JsonProcessingException e) {
			LOG.error("{}", e);
		}
		return null;

	}

	@Override
	public Request getNingRequest() throws Exception {
		URI uri = getRequestUri();
		if (uri.getPort() == -1) {
			uri = new URIBuilder(uri).setPort(80).build();
		}

		String requestParams = getRequestParams();
		Request ningRequest = new RequestBuilder("POST")
				.setUrl(uri.toString())
				.setHeader(HttpHeaders.Names.USER_AGENT,
						sasParams.getUserAgent())
				.setHeader(HttpHeaders.Names.ACCEPT_LANGUAGE, "en-us")
				.setHeader(HttpHeaders.Names.ACCEPT_ENCODING,
						HttpHeaders.Values.BYTES)
				.setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/json")
				.setHeader("X-Forwarded-For", sasParams.getRemoteHostIp())
				.setHeader(HttpHeaders.Names.HOST, uri.getHost())
				.setBody(requestParams).build();
		LOG.debug("Baidu request: {}", ningRequest);
		LOG.debug("Baidu request Body: {}", requestParams);
		return ningRequest;
	}

	@Override
	public void parseResponse(final String response,
			final HttpResponseStatus status) {
		final Gson gson = new Gson();
		LOG.debug("response is {} and response length is {}", response,
				response.length());
		if (status.code() != 200 || StringUtils.isBlank(response)) {
			statusCode = status.code();
			if (200 == statusCode) {
				statusCode = 500;
			}
			responseContent = "";
			return;
		}
		try {
			final VelocityContext context = new VelocityContext();
			BaiduResponse adResponse = gson.fromJson(response,
					BaiduResponse.class);
			if (adResponse.getAds() != null
					&& StringUtils.isNotEmpty(adResponse.getAds()
							.getHtml_snippet())) {
				context.put(VelocityTemplateFieldConstants.PARTNER_HTML_CODE,
						adResponse.getAds().getHtml_snippet());
			} else {
				adStatus = "NO_AD";
				statusCode = 500;
				return;
			}
			responseContent = Formatter.getResponseFromTemplate(
					TemplateType.HTML, context, sasParams, beaconUrl);
			adStatus = "AD";
			statusCode = 200;
		} catch (Exception exception) {
			adStatus = "NO_AD";
			LOG.info("Error parsing response from Baidu : {}", exception);
			LOG.info("Response from Baidu: {}", response);
			return;
		}
		LOG.debug("response length is {}", responseContent.length());
	}

	@Override
	public String getId() {
		return (config.getString("baidu.advertiserId"));
	}

	@Override
	public boolean isClickUrlRequired() {
		return true;
	}
}
