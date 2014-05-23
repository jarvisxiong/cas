package com.inmobi.adserve.channels.server.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import com.inmobi.adserve.channels.scope.NettyRequestScope;
import org.slf4j.Marker;


/**
 * @author abhishek.parwal
 * 
 */
public class TraceFilter extends Filter<ILoggingEvent> {

    private static final Marker TRACE_MARKER = NettyRequestScope.TRACE_MAKER;

    @Override
    public FilterReply decide(final ILoggingEvent event) {
        final Marker marker = event.getMarker();
        if (marker != null && marker.contains(TRACE_MARKER)) {
            return FilterReply.NEUTRAL;
        }
        return FilterReply.DENY;
    }
}