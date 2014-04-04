package com.inmobi.adserve.channels.server.logging;

import lombok.Getter;

import org.slf4j.Marker;

import com.inmobi.adserve.channels.server.handler.TraceMarkerhandler;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;


/**
 * @author abhishek.parwal
 * 
 */
public class MarkerAndLevelFilter extends TurboFilter {
    private static final Marker TRACE_MARKER   = TraceMarkerhandler.TRACE_MAKER;
    @Getter
    private static Level        levelToEnforce = Level.ERROR;

    @Override
    public void start() {
        super.start();
    }

    @Override
    public FilterReply decide(final Marker marker, final Logger logger, final Level level, final String format,
            final Object[] params, final Throwable t) {
        if (!isStarted()) {
            return FilterReply.NEUTRAL;
        }

        if (marker != null && marker.contains(TRACE_MARKER)) {
            return FilterReply.NEUTRAL;
        }

        if (level.isGreaterOrEqual(levelToEnforce)) {
            return FilterReply.NEUTRAL;
        }
        return FilterReply.DENY;
    }

    /**
     * The Logger level to enforce in the event.
     * 
     * @param levelStr
     */
    public void setLevel(final String levelStr) {
        if (levelStr != null) {
            levelToEnforce = Level.toLevel(levelStr);
        }
    }
}