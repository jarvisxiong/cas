package com.inmobi.adserve.channels.server.servlet;

import ch.qos.logback.classic.turbo.TurboFilter;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.logging.MarkerAndLevelFilter;
import com.inmobi.adserve.channels.util.Utils.TestUtils;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by ishanbhatnagar on 31/10/14.
 */

@Singleton
@Path("/changeLogLevel")
public class ServletChangeLogLevel implements Servlet {
    private static final Logger LOG = LoggerFactory.getLogger(ServletChangeLogLevel.class);
    private static final String invalidLoggerName =
            "Invalid Logger Name or Incorrect Query Format.\n"
            + "Valid logger names are: debug, advertiser, sampledadvertiser\n"
            + "Valid Query Format: " + TestUtils.SampleServletQueries.servletChangeLogLevel;
    private static final String invalidLoggerLevel =
            "Invalid Logger Level. Valid logger levels are: DEBUG, INFO, WARN, ERROR, OFF";

    public void handleRequest(final HttpRequestHandler hrh, final QueryStringDecoder queryStringDecoder,
                              final Channel serverChannel) throws Exception {
        LOG.error("Inside Change Log Level Servlet");

        try {
            final Map<String, List<String>> params = queryStringDecoder.parameters();
            String loggerName = null;
            String levelName = null;

            if (params.containsKey("debug")) {
                loggerName = "com.inmobi.adserve.channels";
                levelName = params.get("debug").get(0).toString();
            } else if (params.containsKey("advertiser")) {
                loggerName = "advertiser";
                levelName = params.get("advertiser").get(0).toString();
            } else if (params.containsKey("sampledadvertiser")) {
                loggerName = "sampledadvertiser";
                levelName = params.get("sampledadvertiser").get(0).toString();
            } else {
                LOG.error(invalidLoggerName);
                hrh.responseSender.sendResponse(invalidLoggerName, serverChannel);
                return;
            }

            ch.qos.logback.classic.Level level = getLevel(levelName);
            if (null == level) {
                LOG.error(invalidLoggerLevel);
                hrh.responseSender.sendResponse(invalidLoggerLevel, serverChannel);
                return;
            }

            if(params.containsKey("debug")) {
                /**
                 * Why special handling of debug logs was needed?
                 * Debug logs and trace logs both depend on the "com.inmobi.adserve.channels" logger. In order to
                 * change the debug logs level while keeping the trace logs level the same (i.e. DEBUG),
                 * a turboFilter(MarkerAndLevelFilter) was being used to enforce the log level of debug logs separately.
                 *
                 * So in this case, we change the level of the turboFilter instead of changing the level of the logger.
                 */

                Iterator<TurboFilter> itr = ((ch.qos.logback.classic.LoggerContext)LoggerFactory.getILoggerFactory()).getTurboFilterList().iterator();
                while(itr.hasNext()) {
                    TurboFilter filter = itr.next();
                    if (filter instanceof MarkerAndLevelFilter) {
                        ((MarkerAndLevelFilter)filter).setLevel(levelName);
                    }
                }
            } else {
                ((ch.qos.logback.classic.Logger)LoggerFactory.getLogger(loggerName)).setLevel(level);
            }

            String successMessage = "Successfully changed log level of " + loggerName
                    + " and all it's descendants to " + levelName;
            LOG.error(successMessage);

            hrh.responseSender.sendResponse(successMessage, serverChannel);
        }
        catch (Exception e) {
            LOG.error("Exception caught: {}", e.toString());
            hrh.responseSender.sendResponse(e.toString(), serverChannel);
            return;
        }
    }

    @Override
    public String getName() {
        return "changeLogLevel";
    }

    private ch.qos.logback.classic.Level getLevel(String sArg) {
        if (null != sArg) {
            sArg = sArg.toUpperCase();
        }
        ch.qos.logback.classic.Level level = null;
        switch(sArg) {
            case "DEBUG": level = ch.qos.logback.classic.Level.DEBUG;   break;
            case "WARN":  level = ch.qos.logback.classic.Level.WARN;    break;
            case "INFO":  level = ch.qos.logback.classic.Level.INFO;    break;
            case "ERROR": level = ch.qos.logback.classic.Level.ERROR;   break;
            case "OFF":   level = ch.qos.logback.classic.Level.OFF;     break;
        }
        return level;
    }
}
