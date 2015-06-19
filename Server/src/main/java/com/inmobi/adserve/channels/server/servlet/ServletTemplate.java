package com.inmobi.adserve.channels.server.servlet;

import java.util.List;
import java.util.Map;

import javax.ws.rs.Path;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;
import com.inmobi.adserve.channels.entity.NativeAdTemplateEntity;
import com.inmobi.adserve.channels.repository.NativeAdTemplateRepository;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.api.Servlet;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;


@Singleton
@Path("/template")
public class ServletTemplate implements Servlet {
    private static final Logger LOG = LoggerFactory.getLogger(ServletTemplate.class);

    @Override
    public void handleRequest(final HttpRequestHandler hrh, final QueryStringDecoder queryStringDecoder,
            final Channel serverChannel) throws Exception {
        LOG.debug("Inside template servlet");

        final Map<String, List<String>> params = queryStringDecoder.parameters();
        final List<String> placementIds = params.get("placementId");
        String message = "Invalid placementId";

        if (CollectionUtils.isNotEmpty(placementIds)) {
            final String placementId = placementIds.get(0);
            final NativeAdTemplateRepository templateRepository =
                    CasConfigUtil.repositoryHelper.getNativeAdTemplateRepository();
            final NativeAdTemplateEntity entity;
            try {
                entity = templateRepository.query(Long.parseLong(placementId));
                if (null != entity) {
                    message = entity.getJSON();
                } else {
                    message = "No template found for placement Id " + placementId;
                }
            } catch (NumberFormatException nfe) {
                message = "Placement Id is not a long";
            }
        }

        hrh.responseSender.sendResponse(message, serverChannel);
    }

    @Override
    public String getName() {
        return "template";
    }
}
