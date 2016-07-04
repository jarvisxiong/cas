package com.inmobi.adserve.channels.server;

import static com.inmobi.adserve.channels.server.ChannelServerErrorMessages.CONTAINER_ID_COULD_NOT_BE_EXTRACTED_IN_PROD_ENVIRONMENT;
import static com.inmobi.adserve.channels.server.ChannelServerErrorMessages.CONTAINER_NAME_MISSING_IN_PROD_ENVIRONMENT;
import static com.inmobi.adserve.channels.server.ChannelServerErrorMessages.DATA_CENTRE_ID_MISSING_IN_PROD_ENVIRONMENT;
import static com.inmobi.adserve.channels.server.ChannelServerErrorMessages.DATA_CENTRE_NAME_MISSING_IN_PROD_ENVIRONMENT;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.AEROSPIKE_FEEDBACK;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.CAU_METADATA_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.CCID_MAP_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.CHANNEL_ADGROUP_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.CHANNEL_FEEDBACK_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.CHANNEL_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.CHANNEL_SEGMENT_FEEDBACK_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.CREATIVE_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.CURRENCY_CONVERSION_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.DATABASE;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.GEO_CITY_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.GEO_REGION_FENCE_MAP_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.GEO_ZIP_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.IX_ACCOUNT_MAP_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.IX_BLOCKLIST_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.IX_PACKAGE_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.NATIVE_AD_TEMPLATE_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.PACKAGE_REPOSITORY_V2;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.PRICING_ENGINE_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.SDK_MRAID_MAP_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.SITE_ECPM_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.SITE_FILTER_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.SITE_METADATA_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.SITE_TAXONOMY_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.SLOT_SIZE_MAP_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.WAP_SITE_UAC_REPOSITORY;
import static com.inmobi.adserve.channels.util.LoggerUtils.configureApplicationLoggers;
import static com.inmobi.adserve.channels.util.Utils.ExceptionBlock.getStackTrace;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.log4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.trackers.DefaultLazyInmobiAdTrackerBuilder;
import com.inmobi.adserve.channels.entity.IXBlocklistRepository;
import com.inmobi.adserve.channels.repository.CAUMetaDataRepository;
import com.inmobi.adserve.channels.repository.CcidMapRepository;
import com.inmobi.adserve.channels.repository.ChannelAdGroupRepository;
import com.inmobi.adserve.channels.repository.ChannelFeedbackRepository;
import com.inmobi.adserve.channels.repository.ChannelRepository;
import com.inmobi.adserve.channels.repository.ChannelSegmentAdvertiserCache;
import com.inmobi.adserve.channels.repository.ChannelSegmentFeedbackRepository;
import com.inmobi.adserve.channels.repository.ChannelSegmentMatchingCache;
import com.inmobi.adserve.channels.repository.CreativeRepository;
import com.inmobi.adserve.channels.repository.CurrencyConversionRepository;
import com.inmobi.adserve.channels.repository.GeoCityRepository;
import com.inmobi.adserve.channels.repository.GeoRegionFenceMapRepository;
import com.inmobi.adserve.channels.repository.GeoZipRepository;
import com.inmobi.adserve.channels.repository.IMEIAerospikeRepository;
import com.inmobi.adserve.channels.repository.IXAccountMapRepository;
import com.inmobi.adserve.channels.repository.IXPackageRepository;
import com.inmobi.adserve.channels.repository.NativeAdTemplateRepository;
import com.inmobi.adserve.channels.repository.PricingEngineRepository;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.repository.SdkMraidMapRepository;
import com.inmobi.adserve.channels.repository.SiteAerospikeFeedbackRepository;
import com.inmobi.adserve.channels.repository.SiteEcpmRepository;
import com.inmobi.adserve.channels.repository.SiteFilterRepository;
import com.inmobi.adserve.channels.repository.SiteMetaDataRepository;
import com.inmobi.adserve.channels.repository.SiteTaxonomyRepository;
import com.inmobi.adserve.channels.repository.SlotSizeMapRepository;
import com.inmobi.adserve.channels.repository.WapSiteUACRepository;
import com.inmobi.adserve.channels.repository.pmp.PackageRepositoryV2;
import com.inmobi.adserve.channels.repository.stats.RepositoryStats;
import com.inmobi.adserve.channels.server.kafkalogging.PhotonCasActivityWriter;
import com.inmobi.adserve.channels.server.module.CasNettyModule;
import com.inmobi.adserve.channels.server.module.ServerModule;
import com.inmobi.adserve.channels.server.requesthandler.Logging;
import com.inmobi.adserve.channels.server.servlet.ServletChangeLogLevel;
import com.inmobi.adserve.channels.util.ConfigurationLoader;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.Utils.ImpressionIdGenerator;
import com.inmobi.casthrift.DataCenter;
import com.inmobi.messaging.publisher.AbstractMessagePublisher;
import com.inmobi.messaging.publisher.MessagePublisherFactory;
import com.inmobi.phoenix.batteries.data.AbstractStatsMaintainingDBRepository;
import com.inmobi.phoenix.exception.InitializationException;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.lifecycle.LifecycleManager;

import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;

/**
 * @author ritwik.kumar<br>
 * <br>
 *         Run jar: <br>
 * <br>
 *         java -Dincoming.connections=100 -Ddcpoutbound.connections=50 -Drtboutbound.connections=50
 *         -DconfigFile=sampleConfigFile -jar cas-server.jar <br>
 * <br>
 *         If these are not specified server will pick these values from channel-server.properties config file. <br>
 *         If configFile is not specified, it takes the DEFAULT_CONFIG_FILE from location
 *         "/opt/inmobi/cas/conf/channel-server.properties"
 */
public class ChannelServer {
    private static int repoLoadRetryCount;

    private static org.slf4j.Logger LOG;
    private static ChannelAdGroupRepository channelAdGroupRepository;
    private static ChannelRepository channelRepository;
    private static ChannelFeedbackRepository channelFeedbackRepository;
    private static ChannelSegmentFeedbackRepository channelSegmentFeedbackRepository;
    private static SiteMetaDataRepository siteMetaDataRepository;
    private static SiteTaxonomyRepository siteTaxonomyRepository;
    private static SiteAerospikeFeedbackRepository siteAerospikeFeedbackRepository;
    private static PricingEngineRepository pricingEngineRepository;
    private static SiteFilterRepository siteFilterRepository;
    private static SiteEcpmRepository siteEcpmRepository;
    private static CurrencyConversionRepository currencyConversionRepository;
    private static WapSiteUACRepository wapSiteUACRepository;
    private static IXAccountMapRepository ixAccountMapRepository;
    private static IXPackageRepository ixPackageRepository;
    private static PackageRepositoryV2 packageRepositoryV2;
    private static CreativeRepository creativeRepository;
    private static NativeAdTemplateRepository nativeAdTemplateRepository;
    private static GeoZipRepository geoZipRepository;
    private static GeoCityRepository geoCityRepository;
    private static SlotSizeMapRepository slotSizeMapRepository;
    private static SdkMraidMapRepository sdkMraidMapRepository;
    private static GeoRegionFenceMapRepository geoRegionFenceMapRepository;
    private static CcidMapRepository ccidMapRepository;
    private static IMEIAerospikeRepository imeiAerospikeRepository;
    private static IXBlocklistRepository ixBlocklistRepository;
    private static CAUMetaDataRepository cauMetaDataRepository;
    private static final String DEFAULT_CONFIG_FILE = "/opt/inmobi/cas/conf/channel-server.properties";
    private static final String SCRIBE_MESSAGE_PUBLISHER_CONFIG_KEY = "scribePublisherConf";
    private static String configFile;

    public static void main(final String[] args) throws Exception {
        // Used to turn off internal logging for ConfigurationLoader
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");

        configFile = System.getProperty("configFile", DEFAULT_CONFIG_FILE);
        try {
            final ConfigurationLoader configurationLoader = ConfigurationLoader.getInstance(configFile);

            // Set the status code for load balancer status.
            ServerStatusInfo.setStatusCodeAndString(200, "OK");

            // Used to turn on internal logging for VelocityEngine and Governator/Guice
            System.clearProperty("org.apache.commons.logging.Log");

            // Initialising all loggers
            configureApplicationLoggers(configurationLoader.getLoggerConfiguration());
            LOG = LoggerFactory.getLogger(ChannelServer.class);
            
            Formatter.init();

            final ChannelServerHelper channelServerHelper = new ChannelServerHelper();

            // Parsing environment variables
            final String dataCentreName = channelServerHelper.getDataCentreName();
            if (null == dataCentreName) {
                ChannelServerHelper.handleChannelServerFailure(DATA_CENTRE_NAME_MISSING_IN_PROD_ENVIRONMENT);
                return;
            }

            final Byte dataCenterIdCode = channelServerHelper.getDataCentreId();
            if (null == dataCenterIdCode) {
                ChannelServerHelper.handleChannelServerFailure(DATA_CENTRE_ID_MISSING_IN_PROD_ENVIRONMENT);
                return;
            }

            final String containerName = channelServerHelper.getContainerName();
            if (null == containerName) {
                ChannelServerHelper.handleChannelServerFailure(CONTAINER_NAME_MISSING_IN_PROD_ENVIRONMENT);
                return;
            }

            final Short containerId = channelServerHelper.getContainerId(containerName);
            if (null == containerId) {
                ChannelServerHelper.handleChannelServerFailure(CONTAINER_ID_COULD_NOT_BE_EXTRACTED_IN_PROD_ENVIRONMENT);
                return;
            }


            // Initialising Internal logger factory for Netty
            InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());

            // Initialising databus logging
            final AbstractMessagePublisher dataBusPublisher = (AbstractMessagePublisher) MessagePublisherFactory
                .create(configurationLoader.getServerConfiguration().getString(SCRIBE_MESSAGE_PUBLISHER_CONFIG_KEY));

            // Initialising ImpressionIdGenerator
            ImpressionIdGenerator.init(containerId, dataCenterIdCode);

            // Initialising InmobiAdTracker Builders
            DefaultLazyInmobiAdTrackerBuilder.init(configurationLoader.getServerConfiguration().subset("clickmaker"));

            final String rrLogKey = configurationLoader.getServerConfiguration().getString("rrLogKey");
            final String advertisementLogKey = configurationLoader.getServerConfiguration().getString("adsLogKey");
            final String umpAdsLogKey = configurationLoader.getServerConfiguration().getString("umpAdsLogKey");
            Logging.init(dataBusPublisher, rrLogKey, advertisementLogKey, umpAdsLogKey,
                    configurationLoader.getServerConfiguration(), containerName, dataCentreName);
            PhotonCasActivityWriter.init(configurationLoader.getServerConfiguration(), channelServerHelper.isProdEnvironment());
            // Initializing graphite stats
            RepositoryStats.init(configurationLoader.getMetricsConfiguration(), containerName);
            InspectorStats.init(configurationLoader.getMetricsConfiguration(), containerName);
            channelAdGroupRepository = new ChannelAdGroupRepository();
            channelRepository = new ChannelRepository();
            channelFeedbackRepository = new ChannelFeedbackRepository();
            channelSegmentFeedbackRepository = new ChannelSegmentFeedbackRepository();
            siteMetaDataRepository = new SiteMetaDataRepository();
            siteTaxonomyRepository = new SiteTaxonomyRepository();
            siteAerospikeFeedbackRepository = new SiteAerospikeFeedbackRepository();
            imeiAerospikeRepository = new IMEIAerospikeRepository();
            pricingEngineRepository = new PricingEngineRepository();
            siteFilterRepository = new SiteFilterRepository();
            siteEcpmRepository = new SiteEcpmRepository();
            currencyConversionRepository = new CurrencyConversionRepository();
            wapSiteUACRepository = new WapSiteUACRepository();
            creativeRepository = new CreativeRepository();
            nativeAdTemplateRepository = new NativeAdTemplateRepository();
            ixAccountMapRepository = new IXAccountMapRepository();
            ixPackageRepository = new IXPackageRepository();
            packageRepositoryV2 = new PackageRepositoryV2();
            geoZipRepository = new GeoZipRepository();
            geoCityRepository = new GeoCityRepository();
            slotSizeMapRepository = new SlotSizeMapRepository();
            sdkMraidMapRepository = new SdkMraidMapRepository();
            geoRegionFenceMapRepository = new GeoRegionFenceMapRepository();
            ccidMapRepository = new CcidMapRepository();
            ixBlocklistRepository = new IXBlocklistRepository();
            cauMetaDataRepository = new CAUMetaDataRepository();

            final RepositoryHelper.Builder repoHelperBuilder = RepositoryHelper.newBuilder();
            repoHelperBuilder.setChannelRepository(channelRepository);
            repoHelperBuilder.setChannelAdGroupRepository(channelAdGroupRepository);
            repoHelperBuilder.setChannelFeedbackRepository(channelFeedbackRepository);
            repoHelperBuilder.setChannelSegmentFeedbackRepository(channelSegmentFeedbackRepository);
            repoHelperBuilder.setSiteMetaDataRepository(siteMetaDataRepository);
            repoHelperBuilder.setSiteTaxonomyRepository(siteTaxonomyRepository);
            repoHelperBuilder.setSiteAerospikeFeedbackRepository(siteAerospikeFeedbackRepository);
            repoHelperBuilder.setImeiAerospikeRepository(imeiAerospikeRepository);
            repoHelperBuilder.setPricingEngineRepository(pricingEngineRepository);
            repoHelperBuilder.setSiteFilterRepository(siteFilterRepository);
            repoHelperBuilder.setSiteEcpmRepository(siteEcpmRepository);
            repoHelperBuilder.setCurrencyConversionRepository(currencyConversionRepository);
            repoHelperBuilder.setWapSiteUACRepository(wapSiteUACRepository);
            repoHelperBuilder.setIxAccountMapRepository(ixAccountMapRepository);
            repoHelperBuilder.setIxPackageRepository(ixPackageRepository);
            repoHelperBuilder.setPackageRepositoryV2(packageRepositoryV2);
            repoHelperBuilder.setCreativeRepository(creativeRepository);
            repoHelperBuilder.setNativeAdTemplateRepository(nativeAdTemplateRepository);
            repoHelperBuilder.setGeoZipRepository(geoZipRepository);
            repoHelperBuilder.setGeoCityRepository(geoCityRepository);

            repoHelperBuilder.setSlotSizeMapRepository(slotSizeMapRepository);
            repoHelperBuilder.setSdkMraidMapRepository(sdkMraidMapRepository);
            repoHelperBuilder.setGeoRegionFenceMapRepository(geoRegionFenceMapRepository);
            repoHelperBuilder.setCcidMapRepository(ccidMapRepository);
            repoHelperBuilder.setIxBlocklistRepository(ixBlocklistRepository);
            repoHelperBuilder.setCauMetaDataRepository(cauMetaDataRepository);

            final RepositoryHelper repositoryHelper = repoHelperBuilder.build();

            wapSiteUACRepository.initOverrides(
                    configurationLoader.getCacheConfiguration().subset(WAP_SITE_UAC_REPOSITORY), Logger.getLogger("repository"));

            instantiateRepository(Logger.getLogger("repository"), configurationLoader, dataCentreName);
            CasConfigUtil.init(configurationLoader, repositoryHelper);

            // Configure the netty server.
            final Injector injector =
                    LifecycleInjector
                            .builder()
                            .withModules(
                                    Modules.combine(new CasNettyModule(configurationLoader.getServerConfiguration()),
                                            new ServerModule(configurationLoader, repositoryHelper, dataCentreName)))
                            .usingBasePackages("com.inmobi.adserve.channels.server.netty",
                                    "com.inmobi.adserve.channels.api.provider").build().createInjector();

            final LifecycleManager manager = injector.getInstance(LifecycleManager.class);
            manager.start();

            ServletChangeLogLevel.init();

            Runtime.getRuntime().addShutdownHook(new Thread() {

                @Override
                public void run() {
                    ixPackageRepository.stop();
                    manager.close();
                }
            });

            System.out.close();
        } catch (final Exception exception) {
            ChannelServerHelper.handleChannelServerFailure(exception);
        }
    }

    public static String getConfigFile() {
        if (configFile != null) {
            return configFile;
        }
        return DEFAULT_CONFIG_FILE;
    }


    private static void instantiateRepository(final Logger logger, final ConfigurationLoader config,
        final String dataCentreName) throws ClassNotFoundException {
        try {
            logger.debug("Starting to instantiate repositories");
            final Configuration databaseConfig = config.getDatabaseConfiguration();
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
            System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");

            // Configuring the dataSource with JDBC
            final InitialContext initialContext = new InitialContext();
            initialContext.createSubcontext("java:");
            initialContext.createSubcontext("java:comp");
            initialContext.createSubcontext("java:comp/env");
            Class.forName("org.postgresql.Driver");

            final Properties props = new Properties();
            props.put("type", "javax.sql.DataSource");
            props.put("driverClassName", "org.postgresql.Driver");
            props.put("user", databaseConfig.getString("username"));
            props.put("password", databaseConfig.getString("password"));

            final String validationQuery = databaseConfig.getString("validationQuery");
            final int maxActive = databaseConfig.getInt("maxActive", 20);
            final int maxIdle = databaseConfig.getInt("maxIdle", 20);
            final int maxWait = databaseConfig.getInt("maxWait", -1);
            final boolean testOnBorrow = databaseConfig.getBoolean("testOnBorrow", true);

            final String connectUri =
                    "jdbc:postgresql://" + databaseConfig.getString("host") + ':' + databaseConfig.getInt("port") + '/'
                            + databaseConfig.getString(DATABASE) + "?socketTimeout="
                            + databaseConfig.getString("socketTimeout") + "&ApplicationName=cas";
            final ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connectUri, props);
            final PoolableConnectionFactory pcFactory = new PoolableConnectionFactory(connectionFactory, null);
            final GenericObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<>(pcFactory);
            connectionPool.setMaxTotal(maxActive);
            connectionPool.setMaxIdle(maxIdle);
            connectionPool.setMaxWaitMillis(maxWait * 1000);
            connectionPool.setTestOnBorrow(testOnBorrow);

            pcFactory.setPool(connectionPool);
            pcFactory.setValidationQuery(validationQuery);
            pcFactory.setDefaultReadOnly(true);
            pcFactory.setDefaultAutoCommit(false);

            final PoolingDataSource<PoolableConnection> ds = new PoolingDataSource<>(connectionPool);
            initialContext.bind("java:comp/env/jdbc", ds);

            ChannelSegmentMatchingCache.init(logger);
            ChannelSegmentAdvertiserCache.init(logger);

            // Reusing the repository from phoenix adserving framework.
            repoLoadRetryCount = config.getServerConfiguration().getInt("repoLoadRetryCount", 1);
            if (repoLoadRetryCount < 1) {
                repoLoadRetryCount = 1;
            }
            logger.error(String.format("*************** Starting repo loading with retry count as %s",
                    repoLoadRetryCount));
            loadRepos(nativeAdTemplateRepository, NATIVE_AD_TEMPLATE_REPOSITORY, config, logger);
            loadRepos(creativeRepository, CREATIVE_REPOSITORY, config, logger);
            loadRepos(currencyConversionRepository, CURRENCY_CONVERSION_REPOSITORY, config, logger);
            loadRepos(wapSiteUACRepository, WAP_SITE_UAC_REPOSITORY, config, logger);
            loadRepos(ixAccountMapRepository, IX_ACCOUNT_MAP_REPOSITORY, config, logger);
            loadRepos(channelAdGroupRepository, CHANNEL_ADGROUP_REPOSITORY, config, logger);
            loadRepos(channelRepository, CHANNEL_REPOSITORY, config, logger);
            loadRepos(channelFeedbackRepository, CHANNEL_FEEDBACK_REPOSITORY, config, logger);
            loadRepos(channelSegmentFeedbackRepository, CHANNEL_SEGMENT_FEEDBACK_REPOSITORY, config, logger);
            loadRepos(siteTaxonomyRepository, SITE_TAXONOMY_REPOSITORY, config, logger);
            loadRepos(siteMetaDataRepository, SITE_METADATA_REPOSITORY, config, logger);
            loadRepos(pricingEngineRepository, PRICING_ENGINE_REPOSITORY, config, logger);
            loadRepos(siteFilterRepository, SITE_FILTER_REPOSITORY, config, logger);
            loadRepos(siteEcpmRepository, SITE_ECPM_REPOSITORY, config, logger);
            loadRepos(geoZipRepository, GEO_ZIP_REPOSITORY, config, logger);
            loadRepos(geoCityRepository, GEO_CITY_REPOSITORY, config, logger);
            loadRepos(slotSizeMapRepository, SLOT_SIZE_MAP_REPOSITORY, config, logger);
            loadRepos(sdkMraidMapRepository, SDK_MRAID_MAP_REPOSITORY, config, logger);
            loadRepos(geoRegionFenceMapRepository, GEO_REGION_FENCE_MAP_REPOSITORY, config, logger);
            loadRepos(ccidMapRepository, CCID_MAP_REPOSITORY, config, logger);
            loadRepos(ixBlocklistRepository, IX_BLOCKLIST_REPOSITORY, config, logger);
            loadRepos(cauMetaDataRepository, CAU_METADATA_REPOSITORY, config, logger);
            ixPackageRepository.init(logger, ds, config.getCacheConfiguration().subset(IX_PACKAGE_REPOSITORY), IX_PACKAGE_REPOSITORY);
            packageRepositoryV2.init(logger, ds, config.getCacheConfiguration().subset(PACKAGE_REPOSITORY_V2), PACKAGE_REPOSITORY_V2);
            final DataCenter dc = getDataCentre(dataCentreName);
            siteAerospikeFeedbackRepository.init(config.getServerConfiguration().subset(AEROSPIKE_FEEDBACK), dc);
            imeiAerospikeRepository.init(config.getServerConfiguration().subset(AEROSPIKE_FEEDBACK), dc);
            logger.error("* * * * Instantiating repository completed * * * *");
            LOG.error("* * * * Instantiating repository completed * * * *");
            config.getCacheConfiguration().subset(SITE_METADATA_REPOSITORY).subset(SITE_METADATA_REPOSITORY);
        } catch (final NamingException exception) {
            logger.error("failed to create binding for postgresql data source " + exception.getMessage());
            ServerStatusInfo.setStatusCodeAndString(404, getStackTrace(exception));
        } catch (final InitializationException exception) {
            logger.error("failed to initialize repository " + exception.getMessage());
            ServerStatusInfo.setStatusCodeAndString(404, getStackTrace(exception));
            if (logger.isDebugEnabled()) {
                logger.debug(getStackTrace(exception));
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private static void loadRepos(final AbstractStatsMaintainingDBRepository repository, final String repoName,
            final ConfigurationLoader config, final Logger logger) throws InitializationException {
        final long startTime = System.currentTimeMillis();
        logger.error(String.format("*************** Started loading %s, at %s", repoName, startTime));
        int tryCount;
        Exception exp = null;
        for (tryCount = 1; tryCount <= repoLoadRetryCount; tryCount++) {
            logger.error(String.format("*************** %s, Try %s", repoName, tryCount));
            try {
                repository.init(logger, config.getCacheConfiguration().subset(repoName), repoName);
                break;
            } catch (final Exception exc) {
                logger.error("*************** Error in loading repo " + repoName, exc);
                exp = exc;
            }
        }
        if (tryCount > repoLoadRetryCount) {
            final String msg =
                    String.format("Tried %s times but still could not load repo %s", repoLoadRetryCount, repoName);
            logger.error(msg);
            throw new InitializationException(msg, exp);
        }
        final long endTime = System.currentTimeMillis();
        logger.error(String.format("*************** Loaded repo %s, in %s ms", repoName, endTime - startTime));
    }

    private static DataCenter getDataCentre(final String dataCentreName) {
        DataCenter colo = DataCenter.ALL;
        if (DataCenter.UJ1.toString().equalsIgnoreCase(dataCentreName)) {
            colo = DataCenter.UJ1;
        } else if (DataCenter.UH1.toString().equalsIgnoreCase(dataCentreName)) {
            colo = DataCenter.UH1;
        } else if (DataCenter.LHR1.toString().equalsIgnoreCase(dataCentreName)) {
            colo = DataCenter.LHR1;
        } else if (DataCenter.HKG1.toString().equalsIgnoreCase(dataCentreName)) {
            colo = DataCenter.HKG1;
        }
        return colo;
    }
}
