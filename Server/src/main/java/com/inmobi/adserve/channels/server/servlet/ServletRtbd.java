package com.inmobi.adserve.channels.server.servlet;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.requesthandler.AsyncRequestMaker;
import com.inmobi.adserve.channels.server.requesthandler.MatchSegments;
import com.inmobi.adserve.channels.server.requesthandler.RequestFilters;
import com.inmobi.adserve.channels.server.requesthandler.filters.ChannelSegmentFilterApplier;
import com.inmobi.adserve.channels.server.requesthandler.filters.DcpAndRtbAdGroupLevelFilters;
import com.inmobi.adserve.channels.server.requesthandler.filters.DcpAndRtbdAdvertiserLevelFilters;
import com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.AdGroupLevelFilter;
import com.inmobi.adserve.channels.server.requesthandler.filters.advertiser.AdvertiserLevelFilter;
import com.inmobi.adserve.channels.server.utils.CasUtils;
import com.inmobi.adserve.channels.util.Utils.ImpressionIdGenerator;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import javax.inject.Inject;
import javax.ws.rs.Path;
import java.util.List;


@Singleton
@Path("/rtbdFill")
public class ServletRtbd extends BaseServlet {
    private static final Logger               LOG = LoggerFactory.getLogger(ServletRtbd.class);

    @Inject
    public ServletRtbd(final Provider<Marker> traceMarkerProvider, final MatchSegments matchSegments,
            final RequestFilters requestFilters, final ChannelSegmentFilterApplier channelSegmentFilterApplier,
            final CasUtils casUtils, final AsyncRequestMaker asyncRequestMaker, @DcpAndRtbdAdvertiserLevelFilters final List<AdvertiserLevelFilter> advertiserLevelFilters,
    		@DcpAndRtbAdGroupLevelFilters final List<AdGroupLevelFilter> adGroupLevelFilters) {
    	super(matchSegments, traceMarkerProvider, channelSegmentFilterApplier, casUtils, requestFilters, asyncRequestMaker, advertiserLevelFilters, adGroupLevelFilters);
    }

    @Override
    public void handleRequest(final HttpRequestHandler hrh, final QueryStringDecoder queryStringDecoder,
            final Channel serverChannel) throws Exception {
        Marker traceMarker = traceMarkerProvider.get();
        LOG.debug(traceMarker, "Inside RTBD servlet");
        InspectorStats.incrementStatCount(InspectorStrings.ruleEngineRequests);
        super.handleRequest(hrh, queryStringDecoder, serverChannel);
    }

    @Override
    public String getName() {
        return "rtbdFill";
    }
    
	@Override
	protected Logger getLogger() {
		return LOG;
	}
}
