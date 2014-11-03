package com.inmobi.adserve.channels.server.servlet;

/**
 * Created by ishanbhatnagar on 31/10/14.
 */

import com.google.inject.Singleton;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.util.Utils.TestUtils;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;
import java.util.List;
import java.util.Map;

// Work in progress

@Singleton
@Path("/changeLogLevel")
public class ServletChangeLogLevel implements Servlet {
    private static final Logger LOG = LoggerFactory.getLogger(ServletChangeLogLevel.class);

    @Override
    public void handleRequest(final HttpRequestHandler hrh, final QueryStringDecoder queryStringDecoder,
                              final Channel serverChannel) throws Exception {
        LOG.error("Inside Change Log Level Servlet");

        final Map<String, List<String>> params = queryStringDecoder.parameters();
        final String requestParam = params.get("args").toString();
        final JSONArray jsonArray = new JSONArray(requestParam);
        final JSONObject jObject = jsonArray.getJSONObject(0);

        String failedResponse = "Incorrect Query Format. Here's an example: " + TestUtils.SampleServletQueries.servletChangeLogLevel;
        String successfulResponse = "Successfully changed logging level.";

        try {
            String appenderName = jObject.getString("appender");
            String levelName    = jObject.getString("level");

            ch.qos.logback.classic.Level level = getLogger(levelName);
            if (null == level) {
                LOG.error("Invalid Logger Level");
                hrh.responseSender.sendResponse(failedResponse, serverChannel);
            }
            if (false == isValidAppender(appenderName)) {
                LOG.error("No such Appender Name");
                hrh.responseSender.sendResponse(failedResponse, serverChannel);
            }

            //((ch.qos.logback.classic.Logger)LoggerFactory.getLogger(ServletChangeLogLevel.class)).setLevel(ch.qos.logback.classic.Level.ERROR);
            ((ch.qos.logback.classic.Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(level);
            /*LogManager.getRootLogger().setLevel(level);
            Enumeration allLoggers = LogManager.getLoggerRepository().getCurrentLoggers();
            while(allLoggers.hasMoreElements()) {
                ((org.apache.log4j.Logger)allLoggers.nextElement()).setLevel(level);
            }*/
            for(ch.qos.logback.classic.Logger logger: ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).getLoggerContext().getLoggerList()) {
                logger.setLevel(level);
            }

            LOG.debug("DEBUG is on");
            LOG.info("INFO is on");
            LOG.error("ERROR is on");

            LOG.error("Successfully changed log level of {} to {}", appenderName, levelName);
            hrh.responseSender.sendResponse(successfulResponse, serverChannel);
        }
        catch (Exception e) {

        }
    }

    @Override
    public String getName() {
        return "lbstatus";
    }

    private ch.qos.logback.classic.Level getLogger(String sArg) {
        ch.qos.logback.classic.Level level = null;
        switch(sArg) {
            case "OFF": level = ch.qos.logback.classic.Level.OFF;   break;
            case "DEBUG": level = ch.qos.logback.classic.Level.DEBUG;   break;
            case "WARN": level = ch.qos.logback.classic.Level.WARN;   break;
            case "ERROR": level = ch.qos.logback.classic.Level.ERROR;   break;
            default: level = null;
        }
        return level;
    }

    private Boolean isValidAppender(String appender) {
        return true;
    }
}
