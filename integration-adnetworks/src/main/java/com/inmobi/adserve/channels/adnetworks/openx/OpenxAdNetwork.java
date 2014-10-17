package com.inmobi.adserve.channels.adnetworks.openx;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;


public class OpenxAdNetwork extends AbstractDCPAdNetworkImpl {
  // Updates the request parameters according to the Ad Network. Returns true on
  // success.i
  private static final Logger LOG = LoggerFactory.getLogger(OpenxAdNetwork.class);

  private String latitude = null;
  private String longitude = null;

  public OpenxAdNetwork(final Configuration config, final Bootstrap clientBootstrap,
      final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
    super(config, clientBootstrap, baseRequestHandler, serverChannel);
    this.clientBootstrap = clientBootstrap;
  }

  // Configure the request parameters for making the ad call
  @Override
  public boolean configureParameters() {
    if (StringUtils.isBlank(externalSiteId)) {
      LOG.debug("mandate parameters missing for openx, so returning from adapter");
      return false;
    }

    if (casInternalRequestParameters.getLatLong() != null
        && StringUtils.countMatches(casInternalRequestParameters.getLatLong(), ",") > 0) {
      final String[] latlong = casInternalRequestParameters.getLatLong().split(",");
      latitude = latlong[0];
      longitude = latlong[1];
    }
    LOG.debug("Configure parameters inside openx returned true");
    return true;
  }

  @Override
  public String getName() {
    return "openx";
  }

  @Override
  public String getId() {
    return config.getString("openx.advertiserId");
  }

  // get URI
  @Override
  public URI getRequestUri() throws Exception {
    final StringBuilder finalUrl = new StringBuilder(config.getString("openx.host"));
    finalUrl.append(externalSiteId).append("&cnt=").append(sasParams.getCountryCode().toLowerCase()).append("&dma=")
        .append(sasParams.getState());
    finalUrl.append("&net=").append(sasParams.getLocSrc()).append("&age=").append(sasParams.getAge());
    if (sasParams.getGender() != null) {
      finalUrl.append("&gen=").append(sasParams.getGender().toUpperCase());
    }
    finalUrl.append("&ip=").append(sasParams.getRemoteHostIp()).append("&lat=").append(latitude).append("&lon=")
        .append(longitude);
    if (StringUtils.isNotEmpty(latitude)) {
      finalUrl.append("&lt=3");
    }
    finalUrl.append("&zip=").append(casInternalRequestParameters.getZipCode()).append("&c.siteId=")
        .append(blindedSiteId);

    if (HandSetOS.iOS.getValue() == sasParams.getOsId()) {
      finalUrl.append("&did.ia=").append(casInternalRequestParameters.getUidIFA());
      finalUrl.append("&did.iat=").append(casInternalRequestParameters.getUidADT());
      finalUrl.append("&did.o1=").append(casInternalRequestParameters.getUidO1());
      finalUrl.append("&did.ma.md5=").append(casInternalRequestParameters.getUidMd5());
      finalUrl.append("&did.ma.sha1=").append(casInternalRequestParameters.getUidSO1());
    } else if (HandSetOS.Android.getValue() == sasParams.getOsId()) {
      finalUrl.append("&did.ai.md5=").append(casInternalRequestParameters.getUidMd5());
      finalUrl.append("&did.ai.sha1=").append(casInternalRequestParameters.getUidO1());
    }

    finalUrl.append("&did=").append(casInternalRequestParameters.getUid());

    final String[] urlParams = finalUrl.toString().split("&");
    finalUrl.delete(0, finalUrl.length());
    finalUrl.append(urlParams[0]);

    // discarding parameters that have null values
    for (int i = 1; i < urlParams.length; i++) {
      final String[] paramValue = urlParams[i].split("=");
      if (paramValue.length == 2 && !"null".equals(paramValue[1]) && !StringUtils.isEmpty(paramValue[1])) {
        finalUrl.append('&').append(paramValue[0]).append('=').append(paramValue[1]);
      }
    }
    LOG.debug("url inside openx: {}", finalUrl);
    try {
      return new URI(finalUrl.toString());
    } catch (final URISyntaxException exception) {
      errorStatus = ThirdPartyAdResponse.ResponseStatus.MALFORMED_URL;
      LOG.info("Error Forming Url inside openx {}", exception);
    }
    return null;
  }

  // parse the response received from openx
  @Override
  public void parseResponse(final String response, final HttpResponseStatus status) {
    LOG.debug("response is {} and response length is {}", response, response.length());
    if (status.code() != 200 || response.trim().isEmpty()) {
      statusCode = status.code();
      if (200 == statusCode) {
        statusCode = 500;
      }
      responseContent = "";
      return;
    } else {
      statusCode = status.code();
      final VelocityContext context = new VelocityContext();
      context.put(VelocityTemplateFieldConstants.PARTNER_HTML_CODE, response.trim());
      try {
        responseContent = Formatter.getResponseFromTemplate(TemplateType.HTML, context, sasParams, beaconUrl);
      } catch (final Exception exception) {
        adStatus = "NO_AD";
        LOG.info("Error parsing response from openx : {}", exception);
        LOG.info("Response from openx: {}", response);
        try {
          throw exception;
        } catch (final Exception e) {
          LOG.info("Error while rethrowing the exception : {}", e);
        }
      }
      adStatus = "AD";
    }
    LOG.debug("response length is {}", responseContent.length());
  }
}
