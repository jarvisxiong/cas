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
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
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
        String successfulResponse = "";

        try {
            String appenderName = jObject.getString("appender");
            String levelName    = jObject.getString("level");

            Level level = getLogger(levelName);
            if (null == level) {
                LOG.error("Invalid Logger Level");
                hrh.responseSender.sendResponse(failedResponse, serverChannel);
            }
            if (false == isValidAppender(appenderName)) {
                LOG.error("No such Appender Name");
                hrh.responseSender.sendResponse(failedResponse, serverChannel);
            }

            // Get sub loggers of root
            LogManager.getRootLogger().setLevel(level);
            LOG.error("Successfully changed log level of {} to {}", appenderName, levelName);
            hrh.responseSender.sendResponse(failedResponse, serverChannel);
        }
        catch (Exception e) {

        }
    }

    @Override
    public String getName() {
        return "lbstatus";
    }

    /**
     * Partially mimics functionality of getLogger in log4j 2.x
     * On migrating to log4j 2.x simply replace calls to this function with calls to getLogger present in log4j 2.x
     * @param sArg
     * @return
     */
    private Level getLogger(String sArg) {
        Level level = null;
        switch(sArg) {
            case "OFF": level   = Level.OFF;   break;
            case "DEBUG": level = Level.DEBUG; break;
            case "INFO": level  = Level.INFO;  break;
            case "WARN": level  = Level.WARN;  break;
            case "ERROR": level = Level.ERROR; break;
            default: level = null;
        }
        return level;
    }

    private Boolean isValidAppender(String appender) {
        return null;
    }
}
