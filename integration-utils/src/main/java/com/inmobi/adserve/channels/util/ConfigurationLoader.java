package com.inmobi.adserve.channels.util;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;


public class ConfigurationLoader {
    private static final Logger        LOG      = Logger.getLogger(ConfigurationLoader.class);
    private static ConfigurationLoader instance = null;

    private final Configuration        configfuration;

    private ConfigurationLoader(final String configFile) {
        try {
            configfuration = new PropertiesConfiguration(configFile);
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
        return configfuration.subset("Cache");
    }

    public Configuration repoConfiguration() {
        return configfuration.subset("Cache.ChannelAdGroupRepository");
    }

    public Configuration feedBackConfiguration() {
        return configfuration.subset("Cache.ChannelFeedbackRepository");
    }

    public Configuration segmentFeedBackConfiguration() {
        return configfuration.subset("Cache.ChannelSegmentFeedbackRepository");
    }

    public Configuration siteTaxonomyConfiguration() {
        return configfuration.subset("Cache.SiteTaxonomyRepository");
    }

    public Configuration siteMetaDataConfiguration() {
        return configfuration.subset("Cache.SiteMetaDataRepository");
    }

    public Configuration adapterConfiguration() {
        return configfuration.subset("adapter");
    }

    public Configuration databaseConfiguration() {
        return configfuration.subset("database");
    }

    public Configuration serverConfiguration() {
        return configfuration.subset("server");
    }

    public Configuration loggerConfiguration() {
        return configfuration.subset("logger");
    }

    public Configuration reportConfiguration() {
        return configfuration.subset("report");
    }

    public Configuration log4jConfiguration() {
        return configfuration.subset("log4j");
    }

    public Configuration rtbConfiguration() {
        return configfuration.subset("rtb");
    }

}
