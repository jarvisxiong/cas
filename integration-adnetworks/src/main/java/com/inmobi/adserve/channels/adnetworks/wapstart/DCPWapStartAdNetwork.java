package com.inmobi.adserve.channels.adnetworks.wapstart;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.awt.Dimension;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.velocity.VelocityContext;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;


public class DCPWapStartAdNetwork extends AbstractDCPAdNetworkImpl {
	private static final Logger          LOG           = LoggerFactory.getLogger(DCPWapStartAdNetwork.class);

	private String                       latitude      = null;
	private String                       longitude     = null;
	private int                          width;
	private int                          height;
	private String                       adid = null;


	public DCPWapStartAdNetwork(final Configuration config, final Bootstrap clientBootstrap,
			final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
		super(config, clientBootstrap, baseRequestHandler, serverChannel);

	}

	@Override
	public boolean configureParameters() {
		if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
				|| StringUtils.isBlank(externalSiteId)) {
			LOG.debug("mandatory parameters missing for wapstart so exiting adapter");
			return false;
		}
		host = config.getString("wapstart.host");

		if (null != sasParams.getSlot() && SlotSizeMapping.getDimension((long) sasParams.getSlot()) != null) {
			Dimension dim = SlotSizeMapping.getDimension((long) sasParams.getSlot());
			width = (int) Math.ceil(dim.getWidth());
			height = (int) Math.ceil(dim.getHeight());
		}
		else {
			LOG.debug("mandate parameters missing for WapStart, so returning from adapter");
			return false;
		}

		if (casInternalRequestParameters.latLong != null
				&& StringUtils.countMatches(casInternalRequestParameters.latLong, ",") > 0) {
			String[] latlong = casInternalRequestParameters.latLong.split(",");
			latitude = latlong[0];
			longitude = latlong[1];

		}

		LOG.info("Configure parameters inside wapstart returned true");
		return true;
	}

	@Override
	public String getName() {
		return "wapstart";
	}

	@Override
	public URI getRequestUri() throws Exception {
		try {
			StringBuilder url = new StringBuilder(String.format(host,externalSiteId));
			return (new URI(url.toString()));
		}
		catch (URISyntaxException exception) {
			errorStatus = ThirdPartyAdResponse.ResponseStatus.MALFORMED_URL;
			LOG.info("{}", exception);
		}
		return null;
	}

	private String getRequestParams() {
		User user= new User();
		String gender = sasParams.getGender();
		if(StringUtils.isNotBlank(gender)){
			int gen = "F".equalsIgnoreCase(gender)? 2 : 1;
			user.setGender(gen);
		}
		if (sasParams.getAge() != null) {
			int age = sasParams.getAge();
			int year = Calendar.getInstance().get(Calendar.YEAR);
			int yob = year - age;
			user.setYob(yob);
		}
		if(StringUtils.isNotBlank(casInternalRequestParameters.uid)){
			user.setUid(casInternalRequestParameters.uid);
		}
		Geo geo = new Geo();
		if (StringUtils.isNotBlank(latitude) && StringUtils.isNotBlank(longitude)) {
			geo.setLat(latitude);
			geo.setLon(longitude);
		}
		if(StringUtils.isNotBlank(sasParams.getCountryCode())){
			geo.setCountry(sasParams.getCountryCode());
		}
		Device device= new Device();
		device.setIp(sasParams.getRemoteHostIp());
		device.setUa(sasParams.getUserAgent());
		String gpid = getGPID();
		if (gpid != null) {
			adid = gpid;
			device.setAdid(adid);

		}
		if(StringUtils.isNotBlank(casInternalRequestParameters.uidMd5)){
			device.setAndroid_id(casInternalRequestParameters.uidMd5);
		}
		else if(StringUtils.isNotBlank(casInternalRequestParameters.uidO1)){
			device.setAndroid_id(casInternalRequestParameters.uidO1);
		}
		if (StringUtils.isNotEmpty(casInternalRequestParameters.uidIFA)&& "1".equals(casInternalRequestParameters.uidADT)) {
			device.setIfa(casInternalRequestParameters.uidIFA);
		}
		device.setGeo(geo);

		Publisher publisher = new Publisher();
		publisher.setName(blindedSiteId);
		publisher.setId(sasParams.getSiteIncId());

		Site site = new Site();
		site.setId(Integer.parseInt(externalSiteId));
		site.setPublisher(publisher);
		site.setCtype(1);

		Banner banner = new Banner();
		banner.setH(height);
		banner.setW(width);
		//5:MRAID 2
		banner.setApi(5);
		//Banner type 1: Text and Graphic
		banner.setBtype(1);
		Impression impression = new Impression();
		Banner[] banners =  new Banner[1];
		banners[0]=banner;
		impression.setBanner(banners);
		WapStartAdrequest adRequest = new WapStartAdrequest();
		adRequest.setDevice(device);
		adRequest.setImpression(impression);
		adRequest.setSite(site);
		adRequest.setUser(user);

		ObjectMapper mapper = new ObjectMapper();

		try {

			String requestBody = mapper.writeValueAsString(adRequest);
			LOG.debug(requestBody);
			return requestBody;
		} catch (JsonProcessingException e) {
			LOG.error(e.getMessage());
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
		Request ningRequest = new RequestBuilder("POST").setUrl(uri.toString())
				.setHeader("x-display-metrics", String.format("%sx%s", width, height))
				.setHeader("xplus1-user-agent", sasParams.getUserAgent())
				.setHeader("x-plus1-remote-addr", sasParams.getRemoteHostIp())
				.setHeader(HttpHeaders.Names.USER_AGENT, sasParams.getUserAgent())
				.setHeader(HttpHeaders.Names.ACCEPT_LANGUAGE, "en-us")
				.setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.BYTES)
				.setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/json")
				.setHeader("X-Forwarded-For", sasParams.getRemoteHostIp())
				.setHeader(HttpHeaders.Names.HOST, uri.getHost()).setBody(requestParams).build();
		LOG.debug("WapStart request: {}", ningRequest);
		LOG.debug("WapStart request Body: {}", requestParams);
		return ningRequest;
	}



	@Override
	public void parseResponse(final String response, final HttpResponseStatus status) {
		LOG.debug("response is {} and response length is {}", response, response.length());
		if (status.code() != 200 || StringUtils.isBlank(response)) {
			statusCode = status.code();
			if (200 == statusCode) {
				statusCode = 500;
			}
			responseContent = "";
			return;
		}
		try {
			JSONObject responseJson = new JSONObject(response).getJSONArray("seat").getJSONObject(0);
			;
			TemplateType t;
			VelocityContext context = new VelocityContext();
			String partnerClickUrl = null;
			if(responseJson.has("clink")){
				partnerClickUrl = responseJson.getString("clink");
			}
			else{
				adStatus = "NO_AD";
				statusCode = 500;
				return;
			}
			context.put(VelocityTemplateFieldConstants.PartnerClickUrl, partnerClickUrl);
			context.put(VelocityTemplateFieldConstants.IMClickUrl, clickUrl);
			context.put(VelocityTemplateFieldConstants.PartnerBeaconUrl, responseJson.getString("vlink"));
			if(responseJson.has("graphic")){
				JSONObject textGraphic = responseJson.getJSONObject("textgraphic");
				String imageUrl = textGraphic.getString("name");
				context.put(VelocityTemplateFieldConstants.PartnerImgUrl, imageUrl);
				t = TemplateType.IMAGE;
			}
			else if(responseJson.has("text")){
				JSONObject text = responseJson.getJSONObject("text");
				context.put(VelocityTemplateFieldConstants.AdText, text.getString("title"));
				if(text.has("content")){
					context.put(VelocityTemplateFieldConstants.Description, text.getString("content"));
				}
				String vmTemplate = Formatter.getRichTextTemplateForSlot(slot.toString());
				if (StringUtils.isEmpty(vmTemplate)) {
					t = TemplateType.PLAIN;
				}
				else {
					context.put(VelocityTemplateFieldConstants.Template, vmTemplate);
					t = TemplateType.RICH;
				}

			}
			else{
				adStatus = "NO_AD";
				statusCode = 500;
				return;
			}
			responseContent = Formatter.getResponseFromTemplate(t, context, sasParams, beaconUrl);
			adStatus = "AD";
			statusCode = 200;
		} catch (Exception exception) {
			adStatus = "NO_AD";
			LOG.info("Error parsing response from Wapstart : {}", exception);
			LOG.info("Response from WapStart: {}", response);
			return;
		}
		LOG.debug("response length is {}", responseContent.length());
	}

	@Override
	public String getId() {
		return (config.getString("wapstart.advertiserId"));
	}

	@Override
	public boolean isClickUrlRequired() {
		return true;
	}
}
