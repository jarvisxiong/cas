package com.inmobi.adserve.channels.server.servlet;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.logging.MarkerAndLevelFilter;
import com.inmobi.adserve.channels.util.Utils.TestUtils.SampleServletQueries;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.turbo.TurboFilter;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;


@Singleton
@Path("/changeLogLevel")
public class ServletChangeLogLevel implements Servlet {
    private static final Logger LOG = LoggerFactory.getLogger(ServletChangeLogLevel.class);
    private static final String INVALID_LOGGER_NAME =
            "Invalid Logger Name or Incorrect Query Format.\n"
            + "Valid logger names are: debug, advertiser, sampledadvertiser, repository\n"
            + "Valid Query Format: " + SampleServletQueries.servletChangeLogLevel;
    private static final String INVALID_LOGGER_LEVEL =
            "Invalid Logger Level. Valid logger levels are: DEBUG, INFO, WARN, ERROR, OFF";
    private static final String TIMER_ALREADY_RUNNING_ERROR = "Aborting request to change log level as a timer thread "
            + "is already running. Please wait a minute for the timer thread to finish executing.";

    private static ScheduledFuture<?> scheduledFuture = null;

    private static long  defaultDelayTime =  15000;   // 15 seconds
    private static Level defaultAdvertiserLoggerLevel;
    private static Level defaultSampledAdvertiserLoggerLevel;
    private static Level defaultRepositoryLoggerLevel;
    private static Level defaultTurboFilterLevel;


    public static void init() {
        LOG.debug("Initializing servlet ChangeLogLevel...");
        LoggerContext lc = (LoggerContext) org.slf4j.LoggerFactory.getILoggerFactory();
        defaultAdvertiserLoggerLevel = lc.getLogger("advertiser").getLevel();
        defaultSampledAdvertiserLoggerLevel = lc.getLogger("sampledadvertiser").getLevel();
        defaultRepositoryLoggerLevel = lc.getLogger("repository").getLevel();
        defaultTurboFilterLevel = ((MarkerAndLevelFilter) lc.getTurboFilterList().get(0)).getLevelToEnforce();
    }

    public void handleRequest(final HttpRequestHandler hrh, final QueryStringDecoder queryStringDecoder,
                              final Channel serverChannel) throws Exception {
        LOG.error("Inside Change Log Level Servlet");

        try {
            long delayTime = CasConfigUtil.getServerConfig().subset("servletChangeLogLevel")
                    .getLong("timerDelay", defaultDelayTime);
            String timerMessage = null;

            /**
             * Timer Logic:
             *     If a timerThread is already running or is scheduled to run
             *         then abort servlet and display 'TIMER_ALREADY_RUNNING_ERROR' error
             *     Else
             *         change log level and create a timer thread to revert the log level change after a minute
             */

            if (null != scheduledFuture && !scheduledFuture.isDone()) {
                LOG.error(TIMER_ALREADY_RUNNING_ERROR);
                hrh.responseSender.sendResponse(TIMER_ALREADY_RUNNING_ERROR, serverChannel);
                return;
            }

            final Map<String, List<String>> params = queryStringDecoder.parameters();
            final String loggerName;
            final String levelName;

            if (params.containsKey("debug")) {
                loggerName = "com.inmobi.adserve.channels";
                levelName = params.get("debug").get(0).toString();
                timerMessage = ". The log level will be reverted back to " + defaultTurboFilterLevel
                        + " in: " + String.valueOf(delayTime) + "ms";
            } else if (params.containsKey("advertiser")) {
                loggerName = "advertiser";
                levelName = params.get("advertiser").get(0).toString();
                timerMessage = ". The log level will be reverted back to " + defaultAdvertiserLoggerLevel
                        + " in: " + String.valueOf(delayTime) + "ms";
            } else if (params.containsKey("sampledadvertiser")) {
                loggerName = "sampledadvertiser";
                levelName = params.get("sampledadvertiser").get(0).toString();
                timerMessage = ". The log level will be reverted back to " + defaultSampledAdvertiserLoggerLevel
                        + " in: " + String.valueOf(delayTime) + "ms";
            } else if (params.containsKey("repository")) {
                loggerName = "repository";
                levelName = params.get("repository").get(0).toString();
                timerMessage = ". The log level will be reverted back to " + defaultRepositoryLoggerLevel
                        + " in: " + String.valueOf(delayTime) + "ms";
            } else {
                LOG.error(INVALID_LOGGER_NAME);
                hrh.responseSender.sendResponse(INVALID_LOGGER_NAME, serverChannel);
                return;
            }

            final Level level = getLevel(levelName);

            if (null == level) {
                LOG.error(INVALID_LOGGER_LEVEL);
                hrh.responseSender.sendResponse(INVALID_LOGGER_LEVEL, serverChannel);
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

                Iterator<TurboFilter> itr = ((LoggerContext)LoggerFactory.getILoggerFactory())
                        .getTurboFilterList().iterator();
                while(itr.hasNext()) {
                    TurboFilter filter = itr.next();
                    if (filter instanceof MarkerAndLevelFilter) {
                        ((MarkerAndLevelFilter)filter).setLevel(levelName);
                        break;
                    }
                }
            } else {
                ((ch.qos.logback.classic.Logger)LoggerFactory.getLogger(loggerName)).setLevel(level);
            }

            String successMessage = "Successfully changed log level of " + loggerName
                    + " and all it's descendants to " + levelName;

            Runnable servletChangeLogLevelTimerTask = () -> {
                // Resets log levels to default values
                if (params.containsKey("debug")) {
                    Iterator<TurboFilter> itr =
                            ((LoggerContext) LoggerFactory.getILoggerFactory())
                                    .getTurboFilterList().iterator();
                    while (itr.hasNext()) {
                        TurboFilter filter = itr.next();
                        if (filter instanceof MarkerAndLevelFilter) {
                            ((MarkerAndLevelFilter) filter).setLevel(defaultTurboFilterLevel);
                            break;
                        }
                    }
                } else if (params.containsKey("advertiser")){
                    ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(loggerName))
                            .setLevel(defaultAdvertiserLoggerLevel);
                } else if (params.containsKey("sampledadvertiser")){
                    ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(loggerName))
                            .setLevel(defaultSampledAdvertiserLoggerLevel);
                } else if (params.containsKey("repository")){
                    ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(loggerName))
                            .setLevel(defaultRepositoryLoggerLevel);
                }
            };

            ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
            scheduledFuture = timer.schedule(servletChangeLogLevelTimerTask, delayTime, TimeUnit.MILLISECONDS);
            timer.shutdown();

            LOG.error(successMessage + timerMessage);
            hrh.responseSender.sendResponse(successMessage + timerMessage, serverChannel);
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

    private Level getLevel(String sArg) {
        if (null != sArg) {
            sArg = sArg.toUpperCase();
        }
        Level level = null;
        switch(sArg) {
            case "DEBUG": level = Level.DEBUG;   break;
            case "WARN":  level = Level.WARN;    break;
            case "INFO":  level = Level.INFO;    break;
            case "ERROR": level = Level.ERROR;   break;
            case "OFF":   level = Level.OFF;     break;
        }
        return level;
    }
}
