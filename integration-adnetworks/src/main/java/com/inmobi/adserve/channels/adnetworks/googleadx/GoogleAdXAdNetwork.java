package com.inmobi.adserve.channels.adnetworks.googleadx;

import com.google.common.base.Strings;

import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;

import org.apache.commons.configuration.Configuration;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.net.URI;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

public class GoogleAdXAdNetwork extends AbstractDCPAdNetworkImpl {

  private static final Logger LOG = LoggerFactory.getLogger(GoogleAdXAdNetwork.class);

  private static final String SCRIPT_END_PART = "<script type=\"text/javascript\" "
                                                + "src=\"//pagead2.googlesyndication.com/pagead/show_ads.js\"></script>";

  private String googleInMobiPubID = null;
  private int width, height;

  public GoogleAdXAdNetwork(final Configuration config,
                            final Bootstrap clientBootstrap,
                            final HttpRequestHandlerBase baseRequestHandler,
                            final Channel serverChannel) {
    super(config, clientBootstrap, baseRequestHandler, serverChannel);
  }

  @Override
  public boolean configureParameters() {
    googleInMobiPubID = config.getString("googleadx.googleAdXPublisherID");

    if (sasParams.getSlot() != null
        && SlotSizeMapping.getDimension((long) sasParams.getSlot()) != null) {
      Dimension dim = SlotSizeMapping.getDimension((long) sasParams
          .getSlot());
      width = (int) Math.ceil(dim.getWidth());
      height = (int) Math.ceil(dim.getHeight());
    }

    LOG.debug("Configure parameters inside GoogleAdX returned true");
    return true;
  }

  @Override
  public String getName() {
    return "googleadx";
  }

  @Override
  public String getId() {
    return (config.getString("googleadx.advertiserId"));
  }

  @Override
  public void generateJsAdResponse() {
    statusCode = HttpResponseStatus.OK.code();
    VelocityContext context = new VelocityContext();

    StringBuffer sb = new StringBuffer("<script type=\"text/javascript\">");
    sb.append("google_ad_client = \"").append(googleInMobiPubID).append("\";");
    sb.append("google_ad_slot = \"").append(externalSiteId).append("\";");
    sb.append("google_ad_width = \"").append(width).append("\";");
    sb.append("google_ad_height = \"").append(height).append("\";");
    sb.append("</script>");
    sb.append(SCRIPT_END_PART);

    context.put(VelocityTemplateFieldConstants.PartnerHtmlCode, sb.toString());
    try {
      responseContent = Formatter.getResponseFromTemplate(
          TemplateType.WAP_HTML_JS_AD_TAG, context, sasParams, beaconUrl);
      adStatus = "AD";
    } catch (Exception exception) {
      adStatus = "NO_AD";
      LOG.info("Error generating Static Js adtag for GoogleAdX  : {}",
               exception);
    }
    LOG.debug("response length is {}", responseContent.length());
  }

  @Override
  public boolean useJsAdTag() {
    return true;
  }

  @Override
  public URI getRequestUri() throws Exception {
    return null;
  }
}
