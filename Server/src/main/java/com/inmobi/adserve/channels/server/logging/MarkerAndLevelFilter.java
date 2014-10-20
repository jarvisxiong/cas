package com.inmobi.adserve.channels.server.logging;

import org.slf4j.Marker;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;

import com.inmobi.adserve.channels.scope.NettyRequestScope;


/**
 * @author abhishek.parwal
 * 
 */
public class MarkerAndLevelFilter extends TurboFilter {
    private static final Marker TRACE_MARKER = NettyRequestScope.TRACE_MAKER;
    private Level levelToEnforce = Level.ERROR;

    private String excludedTurboFilteringLogs = "";

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

        if (level.isGreaterOrEqual(levelToEnforce) || excludedTurboFilteringLogs.contains(logger.getName())) {
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

    /**
     * @param excludedTurboFilteringLogs the excludedTurboFilteringLogs to set
     */
    public void setExcludedTurboFilteringLogs(final String excludedTurboFilteringLogs) {
        if (excludedTurboFilteringLogs != null) {
            this.excludedTurboFilteringLogs = excludedTurboFilteringLogs;
        }
    }

}
