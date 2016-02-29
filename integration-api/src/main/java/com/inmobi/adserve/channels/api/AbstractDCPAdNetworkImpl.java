package com.inmobi.adserve.channels.api;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;


/**
 * @author abhishek.parwal
 * @author ritwik.kumar
 * 
 */
public abstract class AbstractDCPAdNetworkImpl extends BaseAdNetworkImpl {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDCPAdNetworkImpl.class);
    protected final Configuration config;
    protected final String DCP_KEY = "DCP";

    /**
     * 
     * @param config
     * @param clientBootstrap
     * @param baseRequestHandler
     * @param serverChannel
     */
    protected AbstractDCPAdNetworkImpl(final Configuration config, final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(baseRequestHandler, serverChannel);
        this.config = config;
        this.clientBootstrap = clientBootstrap;
    }

    /**
     * 
     * @return
     */
    protected boolean isInterstitial() {
        if (10 == selectedSlotId // 300X250
                || 14 == selectedSlotId // 320X480
                || 16 == selectedSlotId // 768X1024
                || 17 == selectedSlotId /* 800x1280 */
                || 32 == selectedSlotId // 480x320
                || 33 == selectedSlotId // 1024x768
                || 34 == selectedSlotId) /* 1280x800 */ {
            return true;
        }
        return false;
    }


    /**
     * 
     * @return
     */
    protected boolean isApp() {
        if (StringUtils.isBlank(sasParams.getSource())) {
            return false;
        } else {
            return APP.equalsIgnoreCase(sasParams.getSource());
        }
    }

    /**
     * 
     * @param param
     * @param format
     * @return
     */
    protected String getURLEncode(final String param, final String format) {
        String encodedString = DEFAULT_EMPTY_STRING;
        String decoded = param;

        if (StringUtils.isNotBlank(param)) {
            try {
                String tobeEndoded = param;
                decoded = URLDecoder.decode(tobeEndoded, format);
                while (!tobeEndoded.equalsIgnoreCase(decoded)) {
                    tobeEndoded = decoded;
                    decoded = URLDecoder.decode(tobeEndoded, format);
                }
            } catch (final UnsupportedEncodingException uee) {
                LOG.debug("Error during decode in getURLEncode() for {} for string {}, exception raised {}", getName(),
                        param, uee);
            }
            try {
                encodedString = URLEncoder.encode(decoded.trim(), format);
            } catch (final UnsupportedEncodingException e) {
                LOG.debug("Error during encode in getURLEncode() for {} for string {}, exception raised {}", getName(),
                        param, e);
            }
        }
        return encodedString;
    }

    protected StringBuilder appendQueryParam(final StringBuilder builder, final String paramName, final int paramValue,
            final boolean isFirstParam) {
        return builder.append(isFirstParam ? '?' : '&').append(paramName).append('=').append(paramValue);
    }

    protected StringBuilder appendQueryParam(final StringBuilder builder, final String paramName,
            final String paramValue, final boolean isFirstParam) {
        return builder.append(isFirstParam ? '?' : '&').append(paramName).append('=').append(paramValue);
    }

    protected StringBuilder appendQueryParam(final StringBuilder builder, final String paramName,
            final double paramValue, final boolean isFirstParam) {
        return builder.append(isFirstParam ? '?' : '&').append(paramName).append('=').append(paramValue);
    }
}
