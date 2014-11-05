package com.inmobi.adserve.channels.adnetworks.webmoblink;

import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.Data;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

public class DCPWebmoblinkAdNetwork extends AbstractDCPAdNetworkImpl {
  private static final Logger LOG = LoggerFactory.getLogger(DCPWebmoblinkAdNetwork.class);

  private final String adFormat;
  private final String accountId;
  private final String resultFormat;
  private final String mode;
  private boolean isApp;
  private static final String RESPONSE_FORMAT = "format";
  private static final String ACCOUNT_ID = "aid";
  private static final String OPERATING_MODE = "mo";
  private static final String PUBLISHERID = "pid";
  private static final String SITEID = "sid";
  private static final String USERAGENT = "ua";
  private static final String IP = "ip";
  private static final String RESULT = "result";
  private static final String DEVICE_ID = "did";
  private static final String DID_TYPE = "didtype";
  private static final String COUNTRY_CODE = "cc";

  public DCPWebmoblinkAdNetwork(final Configuration config, final Bootstrap clientBootstrap,
      final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
    super(config, clientBootstrap, baseRequestHandler, serverChannel);
    mode = config.getString("webmoblink.mode").toUpperCase();
    adFormat = config.getString("webmoblink.adformat");
    accountId = config.getString("webmoblink.accountId");
    host = config.getString("webmoblink.host");
    resultFormat = config.getString("webmoblink.resformat");
  }

  @Override
  public boolean configureParameters() {
    if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())) {
      LOG.debug("mandate parameters missing for webmoblink so exiting adapter");
      return false;
    }
    isApp = (StringUtils.isBlank(sasParams.getSource()) || WAP.equalsIgnoreCase(sasParams.getSource())) ? false : true;
    LOG.debug("Configure parameters inside webmoblink returned true");
    return true;
  }

  @Override
  public String getId() {
    return (config.getString("webmoblink.advertiserId"));
  }

  @Override
  public boolean isClickUrlRequired() {
    return true;
  }

  @Override
  public String getName() {
    return "webmoblink";
  }

  @Override
  public URI getRequestUri() throws Exception {

    StringBuilder url = new StringBuilder(host);

    appendQueryParam(url, ACCOUNT_ID, accountId, true);
    appendQueryParam(url, PUBLISHERID, externalSiteId, false);
    appendQueryParam(url, SITEID, blindedSiteId, false);
    appendQueryParam(url, OPERATING_MODE, mode, false);
    appendQueryParam(url, USERAGENT, getURLEncode(sasParams.getUserAgent(), format), false);
    appendQueryParam(url, IP, sasParams.getRemoteHostIp(), false);
    appendQueryParam(url, RESPONSE_FORMAT, adFormat, false);
    appendQueryParam(url, RESULT, resultFormat, false);

    if (sasParams.getCountryCode() != null) {
      appendQueryParam(url, COUNTRY_CODE, sasParams.getCountryCode(), false);
    }

    if (isApp) {
      if (sasParams.getOsId() == HandSetOS.Android.getValue()) {
        // if android : o1,uid,um5

        if (StringUtils.isNotBlank(casInternalRequestParameters.getUidMd5())) {

          appendQueryParam(url, DEVICE_ID, casInternalRequestParameters.getUidMd5(), false);
          appendQueryParam(url, DID_TYPE, 4, false);
        } else if (casInternalRequestParameters.getUidO1() != null) {
          appendQueryParam(url, DEVICE_ID, casInternalRequestParameters.getUidO1(), false);
          appendQueryParam(url, DID_TYPE, 4, false);
        } else if (!StringUtils.isBlank(casInternalRequestParameters.getUid())) {
          appendQueryParam(url, DEVICE_ID, casInternalRequestParameters.getUid(), false);
          appendQueryParam(url, DID_TYPE, 4, false);
        }
      } else if (sasParams.getOsId() == HandSetOS.iOS.getValue()) {
        // ios : ifa,so1 and o1 is odin1,idus as already added
        if (StringUtils.isNotBlank(casInternalRequestParameters.getUidIFA())
            && "1".equals(casInternalRequestParameters.getUidADT())) {
          appendQueryParam(url, DEVICE_ID, casInternalRequestParameters.getUidIFA(), false);
          appendQueryParam(url, DID_TYPE, 1, false);

        } else if (StringUtils.isNotBlank(casInternalRequestParameters.getUidIDUS1())) {
          appendQueryParam(url, DEVICE_ID, casInternalRequestParameters.getUidIDUS1(), false);
          appendQueryParam(url, DID_TYPE, 3, false);
        } else if (casInternalRequestParameters.getUidSO1() != null) {
          appendQueryParam(url, DEVICE_ID, casInternalRequestParameters.getUidSO1(), false);
          appendQueryParam(url, DID_TYPE, 3, false);

        } else if (StringUtils.isNotBlank(casInternalRequestParameters.getUidO1())) {
          appendQueryParam(url, DEVICE_ID, casInternalRequestParameters.getUidO1(), false);
          appendQueryParam(url, DID_TYPE, 6, false);
        } else {
          String gpid = getGPID();
          if (null != gpid) {
            appendQueryParam(url, DEVICE_ID, gpid, false);
            appendQueryParam(url, DID_TYPE, 7, false);
          }
        }
      }

    }

    LOG.debug("webmoblink url is {}", url);

    return new URI(url.toString());
  }

  @Override
  public void parseResponse(final String response, final HttpResponseStatus status) {
    LOG.debug("response is {}", response);

    if (null == response || status.code() != 200 || response.trim().isEmpty()) {
      statusCode = status.code();
      if (200 == statusCode) {
        statusCode = 500;
      }
      responseContent = "";
      return;
    } else {
      statusCode = status.code();
      VelocityContext context = new VelocityContext();
      try {
        ADResponse adResponse = jaxbHelper.unmarshal(response, ADResponse.class);
        if (adResponse.getStatus() != 0) {
          statusCode = status.code();
          {
            adStatus = "NO_AD";
            statusCode = 500;
            responseContent = "";
            return;
          }
        }
        TemplateType t = null;
        context.put(VelocityTemplateFieldConstants.PARTNER_CLICK_URL, adResponse.getClickUrl());
        if (StringUtils.isNotBlank(adResponse.getAdText())) {
          context.put(VelocityTemplateFieldConstants.AD_TEXT, adResponse.getAdText());
        }
        context.put(VelocityTemplateFieldConstants.IM_CLICK_URL, clickUrl);
        if (StringUtils.isNotBlank(adResponse.getFirepixel())) {
          context.put(VelocityTemplateFieldConstants.PARTNER_BEACON_URL, adResponse.getFirepixel().trim());
        }
        if (StringUtils.isNotBlank(adResponse.getImageUrl())) {
          context.put(VelocityTemplateFieldConstants.PARTNER_IMG_URL, adResponse.getImageUrl().trim());
          t = TemplateType.IMAGE;

        } else {
          String vmTemplate = Formatter.getRichTextTemplateForSlot(slot.toString());
          if (StringUtils.isEmpty(vmTemplate)) {
            t = TemplateType.PLAIN;
          } else {
            context.put(VelocityTemplateFieldConstants.TEMPLATE, vmTemplate);
            t = TemplateType.RICH;
          }
        }

        responseContent = Formatter.getResponseFromTemplate(t, context, sasParams, beaconUrl);
        adStatus = "AD";
      } catch (Exception exception) {
        adStatus = "NO_AD";
        LOG.info("Error parsing response from Webmoblink: response: {}", response);
        InspectorStats.incrementStatCount(getName(), InspectorStrings.PARSE_RESPONSE_EXCEPTION);
      }
    }
  }

  @XmlRootElement(name = "adResponse")
  @XmlAccessorType(XmlAccessType.FIELD)
  @Data
  public static class ADResponse {
    @XmlElement(required = true)
    private int status;
    @XmlElement(required = true)
    private String clickUrl;
    @XmlElement(required = false)
    private String imageUrl;
    @XmlElement(required = false)
    private String adText;
    @XmlElement(required = false)
    private String firepixel;
  }
}
