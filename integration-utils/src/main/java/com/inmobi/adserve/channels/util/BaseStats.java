/**
 *
 */
package com.inmobi.adserve.channels.util;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.metrics.reporting.GraphiteReporter;

/**
 * @author ritwik.kumar
 *
 */
public abstract class BaseStats {
    private static final Logger LOG = LoggerFactory.getLogger(BaseStats.class);
    protected static final String PROD = "prod";
    protected static final String STATS = "stat";
    protected static String boxName;

    /**
     * Init graphite and Stats metrics
     *
     * @param graphiteServer
     * @param graphitePort
     * @param graphiteInterval - set in minutes
     * @param hostName
     */
    public static void init(final String graphiteServer, final int graphitePort, final int graphiteInterval,
            final String hostName) {
        final String metricProducer = getMetricProducer(hostName);
        LOG.error("graphiteServer:{}, graphitePort:{}, graphiteInterval:{}", graphiteServer, graphitePort,
                graphiteInterval);
        LOG.error("metricProducer:{}, boxName:{}", metricProducer, boxName);
        GraphiteReporter.enable(graphiteInterval, TimeUnit.MINUTES, graphiteServer, graphitePort, metricProducer);
    }


    /**
     *
     * @param hostName
     * @return
     */
    protected static String getMetricProducer(String hostName) {
        StringBuilder metricProducer = null;
        String runEnvironment = System.getProperty("run.environment", "test");
        if (StringUtils.isBlank(hostName)) {
            hostName = "unknown-host";
            LOG.error("HostName of box is null while pushing data to graphite");
        }
        if (runEnvironment.equalsIgnoreCase(PROD)) {
            final Pattern prodHostPattern = Pattern.compile("(cas\\d{4})\\.ads\\.(lhr1|uh1|uj1|hkg1)\\.inmobi\\.com");
            final Matcher prodHostMatcher = prodHostPattern.matcher(hostName);
            if (prodHostMatcher.find()) {
                metricProducer =
                        new StringBuilder(PROD).append(".").append(prodHostMatcher.group(2)).append(".cas-1.app");
                boxName = prodHostMatcher.group(1);
            } else {
                runEnvironment = "test";
                LOG.error("HostName of box is not of format cas<4 digits>.ads.<uh1|uj1|lhr1|hkg1>.inmobi.com");
            }
        }
        if (!runEnvironment.equalsIgnoreCase(PROD)) {
            final int dotIndex = hostName.indexOf('.');
            boxName = dotIndex > 0 ? hostName.substring(0, dotIndex) : hostName;
            metricProducer = new StringBuilder("test.cas-1.app");
        }
        return metricProducer.toString();
    }

}
