package com.inmobi.adserve.channels.util;

import lombok.Getter;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;

import com.inmobi.adserve.channels.util.config.CasBaseConfiguration;


public class ConfigurationLoader {

	private static final Logger LOG = Logger.getLogger(ConfigurationLoader.class);

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
	private final Configuration log4jConfiguration;
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
			LOG.error("error loading config {}", e);
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
		log4jConfiguration = configuration.subset("log4j");
		rtbConfiguration = configuration.subset("rtb");


	}

	public static synchronized ConfigurationLoader getInstance(final String configFile) {
		if (instance == null) {
			instance = new ConfigurationLoader(configFile);
		}
		return instance;
	}

}
