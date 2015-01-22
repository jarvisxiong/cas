package com.inmobi.adserve.channels.util;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;

import com.inmobi.adserve.channels.util.config.CasBaseConfiguration;

import lombok.Getter;

public class ConfigurationLoader {
    private static ConfigurationLoader instance = null;

    @Getter
    private final Configuration configuration;
    @Getter
    private final Configuration cacheConfiguration;
    @Getter
    private final Configuration repoConfiguration;
    @Getter
    private final Configuration feedBackConfiguration;
    @Getter
    private final Configuration segmentFeedBackConfiguration;
    @Getter
    private final Configuration siteTaxonomyConfiguration;
    @Getter
    private final Configuration siteMetaDataConfiguration;
    @Getter
    private final Configuration adapterConfiguration;
    @Getter
    private final Configuration databaseConfiguration;
    @Getter
    private final Configuration serverConfiguration;
    @Getter
    private final Configuration loggerConfiguration;
    @Getter
    private final Configuration rtbConfiguration;

    private ConfigurationLoader(final String configFile) {

        try {
            if (configFile.startsWith("/")) {
                configuration = new CasBaseConfiguration(configFile);
            } else {
                configuration =
                        new CasBaseConfiguration(ConfigurationLoader.class.getClassLoader().getResource(configFile));
            }
        } catch (final ConfigurationException e) {
            System.out.println("Error loading config file. Exception: " + e);
            throw new RuntimeException(e);
        }

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
        rtbConfiguration = configuration.subset("rtb");
    }

    public static synchronized ConfigurationLoader getInstance(final String configFile) {
        if (instance == null) {
            instance = new ConfigurationLoader(configFile);
        }
        return instance;
    }

}
