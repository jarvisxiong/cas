package com.inmobi.adserve.channels.util;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;


// TODO: change the getters using lombok
public class ConfigurationLoader {
    private static final Logger        LOG      = Logger.getLogger(ConfigurationLoader.class);
    private static ConfigurationLoader instance = null;

    private final Configuration        configuration;

    private ConfigurationLoader(final String configFile) {
        try {
            configuration = new PropertiesConfiguration(configFile);
        }
        catch (ConfigurationException e) {
            LOG.error("error loading config {}", e);
            throw new RuntimeException(e);
        }
    }

    public static synchronized ConfigurationLoader getInstance(final String configFile) {
        if (instance == null) {
            instance = new ConfigurationLoader(configFile);
        }
        return instance;
    }

    public Configuration cacheConfiguration() {
        return configuration.subset("Cache");
    }

    public Configuration repoConfiguration() {
        return configuration.subset("Cache.ChannelAdGroupRepository");
    }

    public Configuration feedBackConfiguration() {
        return configuration.subset("Cache.ChannelFeedbackRepository");
    }

    public Configuration segmentFeedBackConfiguration() {
        return configuration.subset("Cache.ChannelSegmentFeedbackRepository");
    }

    public Configuration siteTaxonomyConfiguration() {
        return configuration.subset("Cache.SiteTaxonomyRepository");
    }

    public Configuration siteMetaDataConfiguration() {
        return configuration.subset("Cache.SiteMetaDataRepository");
    }

    public Configuration adapterConfiguration() {
        return configuration.subset("adapter");
    }

    public Configuration databaseConfiguration() {
        return configuration.subset("database");
    }

    public Configuration serverConfiguration() {
        return configuration.subset("server");
    }

    public Configuration loggerConfiguration() {
        return configuration.subset("logger");
    }

    public Configuration reportConfiguration() {
        return configuration.subset("report");
    }

    public Configuration log4jConfiguration() {
        return configuration.subset("log4j");
    }

    public Configuration rtbConfiguration() {
        return configuration.subset("rtb");
    }

}
