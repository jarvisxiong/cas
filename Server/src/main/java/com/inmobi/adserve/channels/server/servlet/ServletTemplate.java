package com.inmobi.adserve.channels.server.servlet;

import java.util.List;
import java.util.Map;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;

import javax.ws.rs.Path;

import org.apache.velocity.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;
import com.inmobi.adserve.channels.entity.NativeAdTemplateEntity;
import com.inmobi.adserve.channels.repository.NativeAdTemplateRepository;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.template.formatter.TemplateManager;


@Singleton
@Path("/template")
public class ServletTemplate implements Servlet {
    private static final Logger LOG = LoggerFactory.getLogger(ServletTemplate.class);
    
    

    @Override
    public void handleRequest(final HttpRequestHandler hrh, final QueryStringDecoder queryStringDecoder,
            final Channel serverChannel) throws Exception {
        LOG.debug("Inside template servlet");
       
        Map<String, List<String>> params = queryStringDecoder.parameters();
        List<String> siteIdList = params.get("siteId");
        String message = "Invalid siteId";
        if (siteIdList != null && (!siteIdList.isEmpty())) {
        	 String siteId = siteIdList.get(0);
        	NativeAdTemplateRepository templateRepository = CasConfigUtil.repositoryHelper.getNativeAdTemplateRepository();
        	NativeAdTemplateEntity  entity = templateRepository.query(siteId);
        	if (entity!=null) {
        		message = entity.getJSON();
        	} else {
        		message = "No template found for site Id "+siteId;
        	}
        	 
        }
        
        hrh.responseSender.sendResponse(message, serverChannel);
        
    }

    @Override
    public String getName() {
        return "template";
    }
}
