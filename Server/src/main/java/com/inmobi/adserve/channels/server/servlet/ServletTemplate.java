package com.inmobi.adserve.channels.server.servlet;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.List;
import java.util.Map;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;
import com.inmobi.adserve.channels.entity.NativeAdTemplateEntity;
import com.inmobi.adserve.channels.repository.NativeAdTemplateRepository;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.api.Servlet;


@Singleton
@Path("/template")
public class ServletTemplate implements Servlet {
	private static final Logger LOG = LoggerFactory.getLogger(ServletTemplate.class);

	@Override
	public void handleRequest(final HttpRequestHandler hrh, final QueryStringDecoder queryStringDecoder,
			final Channel serverChannel) throws Exception {
		LOG.debug("Inside template servlet");

		final Map<String, List<String>> params = queryStringDecoder.parameters();
		final List<String> siteIdList = params.get("siteId");
		String message = "Invalid siteId";

		if (null != siteIdList && !siteIdList.isEmpty()) {
			final String siteId = siteIdList.get(0);
			final NativeAdTemplateRepository templateRepository =
					CasConfigUtil.repositoryHelper.getNativeAdTemplateRepository();
			final NativeAdTemplateEntity entity = templateRepository.query(siteId);
			if (null != entity) {
				message = entity.getJSON();
			} else {
				message = "No template found for site Id " + siteId;
			}
		}

		hrh.responseSender.sendResponse(message, serverChannel);
	}

	@Override
	public String getName() {
		return "template";
	}
}
