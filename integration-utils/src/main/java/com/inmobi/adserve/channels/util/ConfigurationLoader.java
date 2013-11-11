package com.inmobi.adserve.channels.util;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.tree.OverrideCombiner;
import org.apache.log4j.Logger;


public class ConfigurationLoader {
    private Logger                     logger   = Logger.getLogger(ConfigurationLoader.class);
    private String                     configFile;
    private static ConfigurationLoader instance = null;

    private CombinedConfiguration      c        = new CombinedConfiguration(new OverrideCombiner());

    private ConfigurationLoader(String configFile) {
        this.configFile = configFile;
        c.addConfiguration(loadProvidedConfiguration());
        c.addConfiguration(loadDefaultConfiguration());
    }

    private AbstractConfiguration loadDefaultConfiguration() {
        AbstractConfiguration c = new PropertiesConfiguration();
        try {
            c = new PropertiesConfiguration(configFile);
        }
        catch (ConfigurationException e) {
            logger.error("Error loading default config {}", e);
        }
        return c;
    }

    private AbstractConfiguration loadProvidedConfiguration() {
        AbstractConfiguration c = new PropertiesConfiguration();
        String configFile = System.getProperty("config");
        if (configFile != null) {
            try {
                c = new PropertiesConfiguration(configFile);
            }
            catch (ConfigurationException e) {
                logger.error("Error loading default config {}", e);
            }
        }
        return c;
    }

    public static synchronized ConfigurationLoader getInstance(String configFile) {
        if (instance == null) {
            instance = new ConfigurationLoader(configFile);
        }
        return instance;
    }

    public Configuration cacheConfiguration() {
        return c.subset("Cache");
    }

    public Configuration repoConfiguration() {
        return c.subset("Cache.ChannelAdGroupRepository");
    }

    public Configuration feedBackConfiguration() {
        return c.subset("Cache.ChannelFeedbackRepository");
    }

    public Configuration segmentFeedBackConfiguration() {
        return c.subset("Cache.ChannelSegmentFeedbackRepository");
    }

    public Configuration siteTaxonomyConfiguration() {
        return c.subset("Cache.SiteTaxonomyRepository");
    }

    public Configuration siteMetaDataConfiguration() {
        return c.subset("Cache.SiteMetaDataRepository");
    }

    public Configuration adapterConfiguration() {
        return c.subset("adapter");
    }

    public Configuration databaseConfiguration() {
        return c.subset("database");
    }

    public Configuration serverConfiguration() {
        return c.subset("server");
    }

    public Configuration loggerConfiguration() {
        return c.subset("logger");
    }

    public Configuration reportConfiguration() {
        return c.subset("report");
    }

    public Configuration log4jConfiguration() {
        return c.subset("log4j");
    }

    public Configuration rtbConfiguration() {
        return c.subset("rtb");
    }

}
