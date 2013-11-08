package com.inmobi.adserve.channels.util;

import java.util.Random;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;


public class DebugLogger
{
    public static Configuration config;
    public static Logger        debugLogger  = null;
    private String              taskId       = "";
    private boolean             trace;
    private StringBuilder       traceBuilder = new StringBuilder();

    public void setTrace()
    {
        trace = true;
    }

    private static Random randomNumberGenerator;

    public static void init(Configuration config)
    {
        DebugLogger.config = config;
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);
        lc.reset();
        try {
            configurator.doConfigure(config.getString("slf4jLoggerConf"));
        }
        catch (JoranException e) {
            e.printStackTrace();
        }
        debugLogger = LoggerFactory.getLogger("debug");
        randomNumberGenerator = new Random();
    }

    public DebugLogger()
    {
        taskId = System.currentTimeMillis() + "-" + randomNumberGenerator.nextInt(99999999);
    }

    public void trace(Object... logMessages)
    {
        if (trace) {
            for (Object logMessage : logMessages) {
                traceBuilder.append(" ").append(logMessage);
            }
            if (trace) {
                traceBuilder.append("\n");
            }
        }
    }

    public void debug(Object... logMessages)
    {
        if (trace) {
            error(logMessages);
            return;
        }
        if (isDebugEnabled()) {
            Throwable t = new Throwable();
            StackTraceElement[] elements = t.getStackTrace();

            String callerMethodName = elements[1].getMethodName();
            StringBuilder sb = new StringBuilder();
            sb.append(callerMethodName).append(" - [").append(taskId).append("] -");
            for (Object logMessage : logMessages) {
                sb.append(" ").append(logMessage);
            }
            debugLogger.debug(sb.toString());
        }
    }

    public void info(Object... logMessages)
    {
        if (trace) {
            error(logMessages);
            return;
        }
        Throwable t = new Throwable();
        StackTraceElement[] elements = t.getStackTrace();

        String callerMethodName = elements[1].getMethodName();
        StringBuilder sb = new StringBuilder();
        sb.append(callerMethodName).append(" - [").append(taskId).append("] -");
        for (Object logMessage : logMessages) {
            sb.append(" ").append(logMessage);
        }
        debugLogger.info(sb.toString());
    }

    public void error(Object... logMessages)
    {
        Throwable t = new Throwable();
        StackTraceElement[] elements = t.getStackTrace();

        String callerMethodName = elements[1].getMethodName();
        StringBuilder sb = new StringBuilder();
        sb.append(callerMethodName).append(" - [").append(taskId).append("] -");
        for (Object logMessage : logMessages) {
            sb.append(" ").append(logMessage);
        }
        debugLogger.error(sb.toString());
        trace(logMessages);
    }

    public boolean isDebugEnabled()
    {
        return trace || debugLogger.isDebugEnabled();
    }

    public String getTrace()
    {
        return traceBuilder.toString();
    }
}