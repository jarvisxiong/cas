package com.inmobi.adserve.channels.adnetworks.amoad;

import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.Formatter.TemplateType;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.commons.configuration.Configuration;
import org.apache.velocity.VelocityContext;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class DCPAmoAdAdNetwork extends AbstractDCPAdNetworkImpl {
    // Updates the request parameters according to the Ad Network. Returns true
    // on
    // success.i
    private static final Logger LOG = LoggerFactory.getLogger(DCPAmoAdAdNetwork.class);

    private static final String BANNER_TAG =
            "<!-- AMoAd Zone: [Inmobi ] --><div class=\"amoad_frame sid_62056d310111552c1081c48959720547417af886416a2ebac81d12f901043a9a container_div color_#0000cc-#444444-#ffffff-#0000FF-#009900 sp\"></div>"
                    + "<script src='http://j.amoad.com/js/aa.js' type='text/javascript' charset='utf-8'></script>";
    private static final String INTERSTITIAL_TAG =
            "<!-- AMoAd Zone: [Inmobi mega panel] --><div class=\"amoad_frame sid_62056d310111552c1081c48959720547de5a0851cfb70d70576781728f316065 container_div color_#0000CC-#444444-#FFFFFF-#0000FF-#009900 sp wv\"></div><script src='http://j.amoad.com/js/aa.js' type='text/javascript' charset='utf-8'></script>";
    private static Map<Short, String> slotTagMap;
    static {
        slotTagMap = new HashMap<Short, String>();
        slotTagMap.put((short) 9, BANNER_TAG);
        slotTagMap.put((short) 15, BANNER_TAG);
        slotTagMap.put((short) 10, INTERSTITIAL_TAG);
    }

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
        return config.getString("amoad.advertiserId");
    }

    @Override
    public void generateJsAdResponse() {
        statusCode = HttpResponseStatus.OK.code();
        final VelocityContext context = new VelocityContext();

        final String tag = slotTagMap.get(sasParams.getSlot());
        if (StringUtils.isEmpty(tag)) {
            LOG.error("Tag is not configured for this slot: {}", sasParams.getSlot());
            adStatus = "NO_AD";
            return;
        }

        context.put(VelocityTemplateFieldConstants.PARTNER_HTML_CODE, tag);
        try {
            responseContent = Formatter.getResponseFromTemplate(TemplateType.HTML, context, sasParams, beaconUrl);
            adStatus = "AD";
        } catch (final Exception exception) {
            adStatus = "NO_AD";
            LOG.error("Error generating Static Js adtag for AmoAd: {}", exception);
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
