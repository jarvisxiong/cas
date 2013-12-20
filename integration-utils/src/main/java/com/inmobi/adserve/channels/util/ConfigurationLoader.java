package com.inmobi.adserve.channels.util;

import lombok.Getter;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;


public class ConfigurationLoader {

    private static final Logger        LOG      = Logger.getLogger(ConfigurationLoader.class);

    private static ConfigurationLoader instance = null;

    @Getter
    private final Configuration        configuration;
    @Getter
    private Configuration              cacheConfiguration;
    @Getter
    private Configuration              repoConfiguration;
    @Getter
    private Configuration              feedBackConfiguration;
    @Getter
    private Configuration              segmentFeedBackConfiguration;
    @Getter
    private Configuration              siteTaxonomyConfiguration;
    @Getter
    private Configuration              siteMetaDataConfiguration;
    @Getter
    private Configuration              adapterConfiguration;
    @Getter
    private Configuration              databaseConfiguration;
    @Getter
    private Configuration              serverConfiguration;
    @Getter
    private Configuration              loggerConfiguration;
    @Getter
    private Configuration              reportConfiguration;
    @Getter
    private Configuration              log4jConfiguration;
    @Getter
    private Configuration              rtbConfiguration;

    private ConfigurationLoader(final String configFile) {
        try {
            configuration = new PropertiesConfiguration(configFile);
            cacheConfiguration = configuration.subset("Cache");
            repoConfiguration = configuration.subset("Cache.ChannelAdGroupRepository");
            feedBackConfiguration = configuration.subset("Cache.ChannelFeedbackRepository");
            segmentFeedBackConfiguration = configuration.subset("Cache.ChannelSegmentFeedbackRepository");
            siteTaxonomyConfiguration = configuration.subset("Cache.SiteTaxonomyRepository");
            siteMetaDataConfiguration = configuration.subset("Cache.SiteMetaDataRepository");
            adapterConfiguration = configuration.subset("adapter");
            databaseConfiguration = configuration.subset("database");
            serverConfiguration = configuration.subset("server");
            loggerConfiguration = configuration.subset("logger");
            reportConfiguration = configuration.subset("report");
            log4jConfiguration = configuration.subset("log4j");
            rtbConfiguration = configuration.subset("rtb");

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

}
