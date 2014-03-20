package com.inmobi.adserve.channels.adnetworks.webmoblink;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;


public class WebmobLinkAdNetwork extends AbstractDCPAdNetworkImpl {
    private static final Logger    LOG         = LoggerFactory.getLogger(WebmobLinkAdNetwork.class);

    private String                 mode;
    private String                 responseFormat;
    private String                 result;
    private String                 country;
    private String                 channels;
    private static Map<Long, Long> channelList = new HashMap<Long, Long>();
    static {
        channelList.put(10l, 1l);
        channelList.put(19l, 3l);
        channelList.put(32l, 3l);
        channelList.put(4l, 3l);
        channelList.put(30l, 3l);
        channelList.put(28l, 3l);
        channelList.put(13l, 10l);
        channelList.put(31l, 10l);
        channelList.put(14l, 11l);
        channelList.put(22l, 14l);
        channelList.put(18l, 14l);
        channelList.put(21l, 14l);
        channelList.put(17l, 14l);
        channelList.put(15l, 16l);
        channelList.put(7l, 20l);
        channelList.put(23l, 20l);
        channelList.put(24l, 20l);
        channelList.put(27l, 20l);
        channelList.put(11l, 23l);
    }

    public WebmobLinkAdNetwork(final Configuration config, final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
    }

    @Override
    public boolean configureParameters() {
        mode = config.getString("webmoblink.mode").toUpperCase();
        responseFormat = config.getString("webmoblink.adformat");
        host = config.getString("webmoblink.host");
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId) || StringUtils.isBlank(mode)
                || StringUtils.isBlank(responseFormat)) {
            LOG.debug("mandate parameters missing for webmoblink so exiting adapter");
            return false;
        }
        if (sasParams.getUserAgent().toUpperCase().contains("OPERA")) {
            LOG.debug("Opera user agent found. So exiting the adapter");
            return false;
        }
        List<Long> categories = sasParams.getCategories();
        if (categories != null && categories.size() > 0) {
            Set<Long> channelSet = new HashSet<Long>();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < categories.size(); i++) {
                channelSet.add(channelList.get(categories.get(i)));
            }
            for (Iterator<Long> iterator = channelSet.iterator(); iterator.hasNext();) {
                Long channelId = iterator.next();
                sb.append(channelId).append(",");
            }
            sb.replace(sb.length() - 1, sb.length(), "");
            channels = sb.toString();
        }
        result = config.getString("webmoblink.resformat");
        country = sasParams.getCountryCode();
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
        try {
            LOG.debug("pid={}, mo={}, ua={}, ip={}, format={}, result={}, country={}, channels={}", externalSiteId,
                    mode, sasParams.getUserAgent(), sasParams.getRemoteHostIp(), responseFormat, result, country,
                    channels);
            StringBuilder url = new StringBuilder();
            url.append(host).append("?pid=").append(externalSiteId).append("&mo=").append(mode).append("&ua=")
                    .append(getURLEncode(sasParams.getUserAgent(), format));
            url.append("&ip=").append(sasParams.getRemoteHostIp()).append("&format=").append(responseFormat)
                    .append("&result=").append(result);
            if (!StringUtils.isBlank(country)) {
                url.append("&cc=").append(country);
            }
            if (!StringUtils.isBlank(channels)) {
                url.append("&channels=").append(channels);
            }
            LOG.debug("Webmoblink uri - {}", url);
            return (new URI(url.toString()));
        }
        catch (URISyntaxException exception) {
            errorStatus = ThirdPartyAdResponse.ResponseStatus.MALFORMED_URL;
            LOG.error("{}", exception);
        }
        return null;
    }

    @Override
    public void parseResponse(final String response, final HttpResponseStatus status) {
        super.parseResponse(response, status);
        if (statusCode == 200 && !StringUtils.isBlank(responseContent)) {
            LOG.debug("Insert beacon url in the Webmoblink ad response.");
            responseContent = responseContent.replace("<body><html>", "<body><html><img src=\"" + beaconUrl
                    + "\" height=1 width=1 />");
            LOG.debug("response length is {} responseContent is {}", responseContent.length(), responseContent);
        }
    }
}