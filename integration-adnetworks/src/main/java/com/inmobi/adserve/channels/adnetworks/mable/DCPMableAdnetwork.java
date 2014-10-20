package com.inmobi.adserve.channels.adnetworks.mable;

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
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;


public class DCPMableAdnetwork extends AbstractDCPAdNetworkImpl {
	private static final Logger LOG = LoggerFactory.getLogger(DCPMableAdnetwork.class);

	private int width;
	private int height;
	private String latitude;
	private String longitude;
	private final String authKey;
	private String uidType = null;
	private static final String SIZE_FORMAT = "%dx%d";
	private static final String UDID_FORMAT = "UDID";
	private static final String ODIN_FORMAT = "ODIN1";
	private static final String SODIN1_FORMAT = "SODIN1";
	private static final String IFA_FORMAT = "IFA";

	public DCPMableAdnetwork(final Configuration config, final Bootstrap clientBootstrap,
			final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
		super(config, clientBootstrap, baseRequestHandler, serverChannel);
		authKey = config.getString("mable.authKey");
		host = config.getString("mable.host");
	}

	@Override
	public boolean configureParameters() {
		if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
				|| StringUtils.isBlank(externalSiteId)) {
			LOG.debug("mandatory parameters missing for Mable so exiting adapter");
			return false;
		}

		if (null != sasParams.getSlot() && SlotSizeMapping.getDimension((long) sasParams.getSlot()) != null) {
			final Dimension dim = SlotSizeMapping.getDimension((long) sasParams.getSlot());
			width = (int) Math.ceil(dim.getWidth());
			height = (int) Math.ceil(dim.getHeight());
		} else {
			LOG.debug("mandate parameters missing for Mable, so returning from adapter");
			return false;
		}

		if (casInternalRequestParameters.getLatLong() != null
				&& StringUtils.countMatches(casInternalRequestParameters.getLatLong(), ",") > 0) {
			final String[] latlong = casInternalRequestParameters.getLatLong().split(",");
			latitude = latlong[0];
			longitude = latlong[1];
		}

		LOG.info("Configure parameters inside Mable returned true");
		return true;
	}

	@Override
	public String getName() {
		return "mable";
	}

	private String getRequestParams() {
		final JSONObject request = new JSONObject();
		try {
			request.put("imp_beacon", "");
			request.put("auth_key", authKey);

			request.put("site_id", externalSiteId);
			request.put("site_category", getCategories(',', true, true));
			request.put("clk_track_url", clickUrl);
			request.put("client_agent", sasParams.getUserAgent());
			request.put("client_ip", sasParams.getRemoteHostIp());
			request.put("blind_id", blindedSiteId);
			final String uid = getUid();
			if (uid != null) {
				request.put("device_id", uid);
				request.put("did_format", uidType);
			}
			request.put("slot_size", String.format(SIZE_FORMAT, width, height));
			if (!StringUtils.isEmpty(latitude)) {
				request.put("lat", latitude);
			}
			if (!StringUtils.isEmpty(longitude)) {
				request.put("long", longitude);
			}

			if (sasParams.getGender() != null) {
				request.put("gender", sasParams.getGender());
			}
			if (sasParams.getAge() != null) {
				request.put("age", sasParams.getAge());
			}
			if (sasParams.getCountryCode() != null) {
				request.put("country", sasParams.getCountryCode());
			}
			if (sasParams.getPostalCode() != null) {
				request.put("zip", sasParams.getPostalCode());
			}

		} catch (final JSONException e) {
			LOG.info("Error while forming request object, exception raised {}", e);
		}
		LOG.debug("Mable request {}", request);
		return request.toString();
	}

	@Override
	public URI getRequestUri() throws Exception {
		try {
			final String host = config.getString("mable.host");
			final StringBuilder url = new StringBuilder(host);
			return new URI(url.toString());
		} catch (final URISyntaxException exception) {
			errorStatus = ThirdPartyAdResponse.ResponseStatus.MALFORMED_URL;
			LOG.error("{}", exception);
		}
		return null;
	}

	@Override
	protected Request getNingRequest() throws Exception {
		URI uri = getRequestUri();
		if (uri.getPort() == -1) {
			uri = new URIBuilder(uri).setPort(80).build();
		}

		final String requestParams = getRequestParams();
		final Request ningRequest =
				new RequestBuilder("POST").setUrl(uri.toString())
						.setHeader(HttpHeaders.Names.USER_AGENT, sasParams.getUserAgent())
						.setHeader(HttpHeaders.Names.ACCEPT_LANGUAGE, "en-us")
						.setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.BYTES)
						.setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/json")
						.setHeader("X-Forwarded-For", sasParams.getRemoteHostIp())
						.setHeader(HttpHeaders.Names.HOST, uri.getHost()).setBody(requestParams).build();
		LOG.info("Mable request: {}", ningRequest);
		LOG.info("Mable request Body: {}", requestParams);
		return ningRequest;
	}

	@Override
	public void parseResponse(final String response, final HttpResponseStatus status) {
		LOG.debug("response is {}", response);
		statusCode = status.code();
		if (null == response || status.code() != 200 || response.trim().isEmpty()) {
			if (200 == statusCode) {
				statusCode = 500;
			}
			responseContent = "";
			return;
		} else {
			final VelocityContext context = new VelocityContext();
			context.put(VelocityTemplateFieldConstants.PARTNER_HTML_CODE, response.trim());
			try {
				responseContent = Formatter.getResponseFromTemplate(TemplateType.HTML, context, sasParams, beaconUrl);
				adStatus = "AD";
			} catch (final Exception exception) {
				adStatus = "NO_AD";
				LOG.error("Error parsing response from Mable : {}", exception);
				LOG.error("Response from Mable: {}", response);
			}
		}
		LOG.debug("response length is {}", responseContent.length());
	}

	@Override
	public String getId() {
		return config.getString("mable.advertiserId");
	}

	@Override
	protected String getUid() {
		if (sasParams.getOsId() == HandSetOS.iOS.getValue()
				&& StringUtils.isNotEmpty(casInternalRequestParameters.getUidIFA())) {
			uidType = IFA_FORMAT;
			return casInternalRequestParameters.getUidIFA();
		}
		if (StringUtils.isNotEmpty(casInternalRequestParameters.getUidMd5())) {
			uidType = UDID_FORMAT;
			return casInternalRequestParameters.getUidMd5();
		}
		if (StringUtils.isNotEmpty(casInternalRequestParameters.getUid())) {
			uidType = UDID_FORMAT;
			return casInternalRequestParameters.getUid();
		}
		if (StringUtils.isNotEmpty(casInternalRequestParameters.getUidO1())) {
			uidType = ODIN_FORMAT;
			return casInternalRequestParameters.getUidO1();
		}
		if (StringUtils.isNotEmpty(casInternalRequestParameters.getUidSO1())) {
			uidType = SODIN1_FORMAT;
			return casInternalRequestParameters.getUidSO1();
		}
		if (StringUtils.isNotEmpty(casInternalRequestParameters.getUidIDUS1())) {
			uidType = UDID_FORMAT;
			return casInternalRequestParameters.getUidIDUS1();
		} else {
			final String gpid = getGPID();
			if (gpid != null) {
				uidType = UDID_FORMAT;
				return gpid;
			}
			return null;
		}
	}
}
