package com.inmobi.adserve.channels.adnetworks.amoad;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.net.URI;

import org.apache.commons.configuration.Configuration;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;


public class DCPAmoAdAdNetwork extends AbstractDCPAdNetworkImpl {
    // Updates the request parameters according to the Ad Network. Returns true on
    // success.i
    private static final Logger LOG = LoggerFactory.getLogger(DCPAmoAdAdNetwork.class);

    private static final String tag = "<!-- AMoAd Zone: [Inmobi ] --><div class=\"amoad_frame sid_62056d310111552c1081c48959720547417af886416a2ebac81d12f901043a9a container_div color_#0000cc-#444444-#ffffff-#0000FF-#009900 sp\"></div>"
                                      + "<script src='http://j.amoad.com/js/aa.js' type='text/javascript' charset='utf-8'></script>";

    public DCPAmoAdAdNetwork(final Configuration config, final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
    }

    @Override
    public boolean configureParameters() {
        LOG.debug("Configure parameters inside AmoAd returned true");
        return true;
    }

    @Override
    public String getName() {
        return "amoad";
    }

    @Override
    public String getId() {
        return (config.getString("AmoAd.advertiserId"));
    }

    @Override
    public void generateJsAdResponse() {
        statusCode = HttpResponseStatus.OK.code();
        VelocityContext context = new VelocityContext();

        context.put(VelocityTemplateFieldConstants.PartnerHtmlCode, tag);
        try {
            responseContent = Formatter.getResponseFromTemplate(TemplateType.HTML, context, sasParams, beaconUrl);
            adStatus = "AD";
        }
        catch (Exception exception) {
            adStatus = "NO_AD";
            LOG.info("Error generating Static Js adtag for AmoAd  : {}", exception);
        }
        LOG.debug("response length is {}", responseContent.length());
    }

    @Override
    public boolean useJsAdTag() {
        return true;
    }

    @Override
    public URI getRequestUri() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }
}