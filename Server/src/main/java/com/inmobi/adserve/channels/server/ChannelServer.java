package com.inmobi.adserve.channels.server;

import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
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
import org.apache.log4j.PropertyConfigurator;

import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.repository.ChannelAdGroupRepository;
import com.inmobi.adserve.channels.repository.ChannelFeedbackRepository;
import com.inmobi.adserve.channels.repository.ChannelRepository;
import com.inmobi.adserve.channels.repository.ChannelSegmentAdvertiserCache;
import com.inmobi.adserve.channels.repository.ChannelSegmentFeedbackRepository;
import com.inmobi.adserve.channels.repository.ChannelSegmentMatchingCache;
import com.inmobi.adserve.channels.repository.CreativeRepository;
import com.inmobi.adserve.channels.repository.CurrencyConversionRepository;
import com.inmobi.adserve.channels.repository.GeoZipRepository;
import com.inmobi.adserve.channels.repository.IXAccountMapRepository;
import com.inmobi.adserve.channels.repository.IXPackageRepository;
import com.inmobi.adserve.channels.repository.NativeAdTemplateRepository;
import com.inmobi.adserve.channels.repository.PricingEngineRepository;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.repository.SiteAerospikeFeedbackRepository;
import com.inmobi.adserve.channels.repository.SiteEcpmRepository;
import com.inmobi.adserve.channels.repository.SiteFilterRepository;
import com.inmobi.adserve.channels.repository.SiteMetaDataRepository;
import com.inmobi.adserve.channels.repository.SiteTaxonomyRepository;
import com.inmobi.adserve.channels.repository.SlotSizeMapRepository;
import com.inmobi.adserve.channels.repository.WapSiteUACRepository;
import com.inmobi.adserve.channels.server.module.CasNettyModule;
import com.inmobi.adserve.channels.server.module.ServerModule;
import com.inmobi.adserve.channels.server.requesthandler.Logging;
import com.inmobi.adserve.channels.server.servlet.ServletChangeLogLevel;
import com.inmobi.adserve.channels.util.ConfigurationLoader;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.Utils.ClickUrlsRegenerator;
import com.inmobi.adserve.channels.util.Utils.ImpressionIdGenerator;
import com.inmobi.casthrift.DataCenter;
import com.inmobi.messaging.publisher.AbstractMessagePublisher;
import com.inmobi.messaging.publisher.MessagePublisherFactory;
import com.inmobi.phoenix.batteries.data.AbstractStatsMaintainingDBRepository;
import com.inmobi.phoenix.exception.InitializationException;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.lifecycle.LifecycleManager;


/*
 * 
 * Run jar:- java -Dincoming.connections=100 -Ddcpoutbound.connections=50 -Drtboutbound.connections=50
 * -DconfigFile=sampleConfigFile -jar cas-server.jar If these are not specified server will pick these values from
 * channel-server.properties config file. If configFile is not specified, it takes the DEFAULT_CONFIG_FILE from location
 * "/opt/mkhoj/conf/cas/channel-server.properties"
 */
public class ChannelServer {
    private static int repoLoadRetryCount;
    public static byte dataCenterIdCode;
    public static short hostIdCode;
    public static String dataCentreName;

    private static Logger logger;
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
    private static CreativeRepository creativeRepository;
    private static NativeAdTemplateRepository nativeAdTemplateRepository;
    private static GeoZipRepository geoZipRepository;
    private static SlotSizeMapRepository slotSizeMapRepository;
    private static final String DEFAULT_CONFIG_FILE = "/opt/mkhoj/conf/cas/channel-server.properties";
    private static String configFile;

    public static void main(final String[] args) throws Exception {
        configFile = System.getProperty("configFile", DEFAULT_CONFIG_FILE);
        try {
            final ConfigurationLoader configurationLoader = ConfigurationLoader.getInstance(configFile);

            if (!checkLogFolders(configurationLoader.getLog4jConfiguration())) {
                System.out.println("Log folders are not available so exiting..");
                return;
            }
            // Set the status code for load balancer status.
            ServerStatusInfo.setStatusCodeAndString(200, "OK");

            Formatter.init();

            PropertyConfigurator.configure(configurationLoader.getLoggerConfiguration().getString("log4jLoggerConf"));
            logger = Logger.getLogger("repository");
            logger.debug("Initializing logger completed");

            // parsing the data center id given in the vm parameters
            final ChannelServerHelper channelServerHelper = new ChannelServerHelper();
            dataCenterIdCode = channelServerHelper.getDataCenterId(ChannelServerStringLiterals.DATA_CENTER_ID_KEY);
            hostIdCode = channelServerHelper.getHostId(ChannelServerStringLiterals.HOST_NAME_KEY);
            dataCentreName = channelServerHelper.getDataCentreName(ChannelServerStringLiterals.DATA_CENTRE_NAME_KEY);

            // Initialising Internal logger factory for Netty
            InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());

            // Initialising logging - Write to databus
            final AbstractMessagePublisher dataBusPublisher =
                    (AbstractMessagePublisher) MessagePublisherFactory.create(configFile);

            // Initialising ImpressionIdGenerator
            ImpressionIdGenerator.init(ChannelServer.hostIdCode, ChannelServer.dataCenterIdCode);

            // Initialising ClickUrlsRegenerator
            ClickUrlsRegenerator.init(configurationLoader.getServerConfiguration().subset("clickmaker"));

            final String rrLogKey = configurationLoader.getServerConfiguration().getString("rrLogKey");
            final String advertisementLogKey = configurationLoader.getServerConfiguration().getString("adsLogKey");
            final String umpAdsLogKey = configurationLoader.getServerConfiguration().getString("umpAdsLogKey");
            Logging.init(dataBusPublisher, rrLogKey, advertisementLogKey, umpAdsLogKey,
                    configurationLoader.getServerConfiguration());

            // Initializing graphite stats
            InspectorStats.init(configurationLoader.getServerConfiguration());
            channelAdGroupRepository = new ChannelAdGroupRepository();
            channelRepository = new ChannelRepository();
            channelFeedbackRepository = new ChannelFeedbackRepository();
            channelSegmentFeedbackRepository = new ChannelSegmentFeedbackRepository();
            siteMetaDataRepository = new SiteMetaDataRepository();
            siteTaxonomyRepository = new SiteTaxonomyRepository();
            siteAerospikeFeedbackRepository = new SiteAerospikeFeedbackRepository();
            pricingEngineRepository = new PricingEngineRepository();
            siteFilterRepository = new SiteFilterRepository();
            siteEcpmRepository = new SiteEcpmRepository();
            currencyConversionRepository = new CurrencyConversionRepository();
            wapSiteUACRepository = new WapSiteUACRepository();
            creativeRepository = new CreativeRepository();
            nativeAdTemplateRepository = new NativeAdTemplateRepository();
            ixAccountMapRepository = new IXAccountMapRepository();
            ixPackageRepository = new IXPackageRepository();
            geoZipRepository = new GeoZipRepository();
            slotSizeMapRepository = new SlotSizeMapRepository();

            final RepositoryHelper.Builder repoHelperBuilder = RepositoryHelper.newBuilder();
            repoHelperBuilder.setChannelRepository(channelRepository);
            repoHelperBuilder.setChannelAdGroupRepository(channelAdGroupRepository);
            repoHelperBuilder.setChannelFeedbackRepository(channelFeedbackRepository);
            repoHelperBuilder.setChannelSegmentFeedbackRepository(channelSegmentFeedbackRepository);
            repoHelperBuilder.setSiteMetaDataRepository(siteMetaDataRepository);
            repoHelperBuilder.setSiteTaxonomyRepository(siteTaxonomyRepository);
            repoHelperBuilder.setSiteAerospikeFeedbackRepository(siteAerospikeFeedbackRepository);
            repoHelperBuilder.setPricingEngineRepository(pricingEngineRepository);
            repoHelperBuilder.setSiteFilterRepository(siteFilterRepository);
            repoHelperBuilder.setSiteEcpmRepository(siteEcpmRepository);
            repoHelperBuilder.setCurrencyConversionRepository(currencyConversionRepository);
            repoHelperBuilder.setWapSiteUACRepository(wapSiteUACRepository);
            repoHelperBuilder.setIxAccountMapRepository(ixAccountMapRepository);
            repoHelperBuilder.setIxPackageRepository(ixPackageRepository);

            repoHelperBuilder.setCreativeRepository(creativeRepository);
            repoHelperBuilder.setNativeAdTemplateRepository(nativeAdTemplateRepository);
            repoHelperBuilder.setGeoZipRepository(geoZipRepository);
            repoHelperBuilder.setSlotSizeMapRepository(slotSizeMapRepository);

            final RepositoryHelper repositoryHelper = repoHelperBuilder.build();

            instantiateRepository(logger, configurationLoader);
            CasConfigUtil.init(configurationLoader, repositoryHelper);

            // Configure the netty server.
            final Injector injector =
                    LifecycleInjector
                            .builder()
                            .withModules(
                                    Modules.combine(new CasNettyModule(configurationLoader.getServerConfiguration()),
                                            new ServerModule(configurationLoader, repositoryHelper)))
                            .usingBasePackages("com.inmobi.adserve.channels.server.netty",
                                    "com.inmobi.adserve.channels.api.provider").build().createInjector();

            final LifecycleManager manager = injector.getInstance(LifecycleManager.class);
            manager.start();

            ServletChangeLogLevel.init();

            Runtime.getRuntime().addShutdownHook(new Thread() {

                @Override
                public void run() {
                    manager.close();
                }
            });

            System.out.close();
            // If client bootstrap is not present throwing exception which will set lbStatus as NOT_OK.
        } catch (final Exception exception) {
            logger.error("Exception in Channel Server " + exception);
            ServerStatusInfo.setStatusCodeAndString(404, getMyStackTrace(exception));
            logger.error("stack trace is " + getMyStackTrace(exception));
            if (logger.isDebugEnabled()) {
                logger.debug("{}", exception);
                sendMail(exception.getMessage(), getMyStackTrace(exception));
            }
        }
    }

    /**
     * 
     * @return
     */
    public static String getConfigFile() {
        if (configFile != null) {
            return configFile;
        }
        return DEFAULT_CONFIG_FILE;
    }

    public static String getMyStackTrace(final Exception exception) {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringWriter);
        exception.printStackTrace(printWriter);
        return "StackTrace is: " + stringWriter.toString();
    }

    private static void instantiateRepository(final Logger logger, final ConfigurationLoader config)
            throws ClassNotFoundException {
        try {
            logger.debug("Starting to instantiate repository");
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
                    "jdbc:postgresql://" + databaseConfig.getString("host") + ":" + databaseConfig.getInt("port") + "/"
                            + databaseConfig.getString(ChannelServerStringLiterals.DATABASE) + "?socketTimeout="
                            + databaseConfig.getString("socketTimeout");

            final ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connectUri, props);

            final PoolableConnectionFactory poolableConnectionFactory =
                    new PoolableConnectionFactory(connectionFactory, null);

            final GenericObjectPool<PoolableConnection> connectionPool =
                    new GenericObjectPool<>(poolableConnectionFactory);
            connectionPool.setMaxTotal(maxActive);
            connectionPool.setMaxIdle(maxIdle);
            connectionPool.setMaxWaitMillis(maxWait * 1000);
            connectionPool.setTestOnBorrow(testOnBorrow);

            poolableConnectionFactory.setPool(connectionPool);
            poolableConnectionFactory.setValidationQuery(validationQuery);
            poolableConnectionFactory.setDefaultReadOnly(true);
            poolableConnectionFactory.setDefaultAutoCommit(false);

            final PoolingDataSource<PoolableConnection> ds = new PoolingDataSource<>(connectionPool);

            initialContext.bind("java:comp/env/jdbc", ds);

            ChannelSegmentMatchingCache.init(logger);
            ChannelSegmentAdvertiserCache.init(logger);

            // Reusing the repository from phoenix adserving framework.
            repoLoadRetryCount = config.getServerConfiguration().getInt("repoLoadRetryCount", 1);
            if (repoLoadRetryCount < 1) {
                repoLoadRetryCount = 1;
            }
            logger.info(String.format("************** starting repo load with retry count as %s", repoLoadRetryCount));
            loadRepos(creativeRepository, ChannelServerStringLiterals.CREATIVE_REPOSITORY, config);
            loadRepos(currencyConversionRepository, ChannelServerStringLiterals.CURRENCY_CONVERSION_REPOSITORY, config);
            loadRepos(wapSiteUACRepository, ChannelServerStringLiterals.WAP_SITE_UAC_REPOSITORY, config);
            loadRepos(ixAccountMapRepository, ChannelServerStringLiterals.IX_ACCOUNT_MAP_REPOSITORY, config);
            loadRepos(channelAdGroupRepository, ChannelServerStringLiterals.CHANNEL_ADGROUP_REPOSITORY, config);
            loadRepos(channelRepository, ChannelServerStringLiterals.CHANNEL_REPOSITORY, config);
            loadRepos(channelFeedbackRepository, ChannelServerStringLiterals.CHANNEL_FEEDBACK_REPOSITORY, config);
            loadRepos(channelSegmentFeedbackRepository,
                    ChannelServerStringLiterals.CHANNEL_SEGMENT_FEEDBACK_REPOSITORY, config);
            loadRepos(siteTaxonomyRepository, ChannelServerStringLiterals.SITE_TAXONOMY_REPOSITORY, config);
            loadRepos(siteMetaDataRepository, ChannelServerStringLiterals.SITE_METADATA_REPOSITORY, config);
            loadRepos(pricingEngineRepository, ChannelServerStringLiterals.PRICING_ENGINE_REPOSITORY, config);
            loadRepos(siteFilterRepository, ChannelServerStringLiterals.SITE_FILTER_REPOSITORY, config);
            loadRepos(siteEcpmRepository, ChannelServerStringLiterals.SITE_ECPM_REPOSITORY, config);
            loadRepos(nativeAdTemplateRepository, ChannelServerStringLiterals.NATIVE_AD_TEMPLATE_REPOSITORY, config);
            loadRepos(geoZipRepository, ChannelServerStringLiterals.GEO_ZIP_REPOSITORY, config);
            loadRepos(slotSizeMapRepository, ChannelServerStringLiterals.SLOT_SIZE_MAP_REPOSITORY, config);
            ixPackageRepository.init(logger, ds,
                    config.getCacheConfiguration().subset(ChannelServerStringLiterals.IX_PACKAGE_REPOSITORY),
                    ChannelServerStringLiterals.IX_PACKAGE_REPOSITORY);
            siteAerospikeFeedbackRepository.init(
                    config.getServerConfiguration().subset(ChannelServerStringLiterals.AEROSPIKE_FEEDBACK),
                    getDataCenter());

            logger.error("* * * * Instantiating repository completed * * * *");
            config.getCacheConfiguration().subset(ChannelServerStringLiterals.SITE_METADATA_REPOSITORY)
                    .subset(ChannelServerStringLiterals.SITE_METADATA_REPOSITORY);
        } catch (final NamingException exception) {
            logger.error("failed to creatre binding for postgresql data source " + exception.getMessage());
            ServerStatusInfo.setStatusCodeAndString(404, getMyStackTrace(exception));
        } catch (final InitializationException exception) {
            logger.error("failed to initialize repository " + exception.getMessage());
            ServerStatusInfo.setStatusCodeAndString(404, getMyStackTrace(exception));
            if (logger.isDebugEnabled()) {
                logger.debug(ChannelServer.getMyStackTrace(exception));
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private static void loadRepos(final AbstractStatsMaintainingDBRepository repository, final String repoName,
            final ConfigurationLoader config) throws InitializationException {
        final long startTime = System.currentTimeMillis();
        logger.error(String.format("*************** Started loading repo %s, at %s", repoName, startTime));
        int tryCount;
        Exception exp = null;
        for (tryCount = 0; tryCount < repoLoadRetryCount; tryCount++) {
            logger.error(String.format("*************** Trying to load repo %s for %s time", repoName, tryCount));
            try {
                repository.init(logger, config.getCacheConfiguration().subset(repoName), repoName);
                break;
            } catch (final Exception exc) {
                logger.error("*************** Error in loading repo " + repoName, exc);
                exp = exc;
            }
        }
        if (tryCount >= repoLoadRetryCount) {
            final String msg =
                    String.format("Tried %s times but still could not load repo %s", repoLoadRetryCount, repoName);
            logger.error(msg);
            throw new InitializationException(msg, exp);
        }
        final long endTime = System.currentTimeMillis();
        logger.error(String.format("*************** Loaded repo %s, in %s ms", repoName, (endTime - startTime)));
        return;
    }

    private static DataCenter getDataCenter() {
        DataCenter colo = DataCenter.ALL;
        if (DataCenter.UJ1.toString().equalsIgnoreCase(ChannelServer.dataCentreName)) {
            colo = DataCenter.UJ1;
        } else if (DataCenter.UH1.toString().equalsIgnoreCase(ChannelServer.dataCentreName)) {
            colo = DataCenter.UH1;
        } else if (DataCenter.LHR1.toString().equalsIgnoreCase(ChannelServer.dataCentreName)) {
            colo = DataCenter.LHR1;
        } else if (DataCenter.HKG1.toString().equalsIgnoreCase(ChannelServer.dataCentreName)) {
            colo = DataCenter.HKG1;
        }
        return colo;
    }

    // check if all log folders exists
    public static boolean checkLogFolders(final Configuration config) {
        String debugLogFolder = config.getString("appender.debug.File");
        String advertiserLogFolder = config.getString("appender.advertiser.File");
        String sampledAdvertiserLogFolder = config.getString("appender.sampledadvertiser.File");
        String repositoryLogFolder = config.getString("appender.repository.File");
        File debugFolder = null;
        File advertiserFolder = null;
        File sampledAdvertiserFolder = null;
        File repositoryFolder = null;
        if (repositoryLogFolder != null) {
            repositoryLogFolder = repositoryLogFolder.substring(0, repositoryLogFolder.lastIndexOf('/') + 1);
            repositoryFolder = new File(repositoryLogFolder);
        }
        if (debugLogFolder != null) {
            debugLogFolder = debugLogFolder.substring(0, debugLogFolder.lastIndexOf('/') + 1);
            debugFolder = new File(debugLogFolder);
        }
        if (advertiserLogFolder != null) {
            advertiserLogFolder = advertiserLogFolder.substring(0, advertiserLogFolder.lastIndexOf('/') + 1);
            advertiserFolder = new File(advertiserLogFolder);
        }
        if (sampledAdvertiserLogFolder != null) {
            sampledAdvertiserLogFolder =
                    sampledAdvertiserLogFolder.substring(0, sampledAdvertiserLogFolder.lastIndexOf('/') + 1);
            sampledAdvertiserFolder = new File(sampledAdvertiserLogFolder);
        }
        if (debugFolder != null && debugFolder.exists() && advertiserFolder != null && advertiserFolder.exists()) {
            if (sampledAdvertiserFolder != null && sampledAdvertiserFolder.exists() && repositoryFolder != null
                    && repositoryFolder.exists()) {
                return true;
            }
        }
        ServerStatusInfo.setStatusCodeAndString(404, "StackTrace is: one or more log folders missing");
        return false;
    }

    // send Mail if channel server crashes
    @SuppressWarnings("unchecked")
    private static void sendMail(final String errorMessage, final String stackTrace) {
        final Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", CasConfigUtil.getServerConfig().getString("smtpServer"));
        final Session session = Session.getDefaultInstance(properties);
        try {
            final MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(CasConfigUtil.getServerConfig().getString("sender")));
            final List<String> recipients = CasConfigUtil.getServerConfig().getList("recipients");
            final javax.mail.internet.InternetAddress[] addressTo =
                    new javax.mail.internet.InternetAddress[recipients.size()];

            for (int index = 0; index < recipients.size(); index++) {
                addressTo[index] = new javax.mail.internet.InternetAddress(recipients.get(index));
            }

            message.setRecipients(Message.RecipientType.TO, addressTo);
            final InetAddress addr = InetAddress.getLocalHost();
            message.setSubject("Channel Ad Server Crashed on Host " + addr.getHostName());
            message.setText(errorMessage + stackTrace);
            Transport.send(message);
        } catch (final MessagingException mex) {
            // logger.info("Error while sending mail");
            logger.error("MessagingException raised while sending mail " + mex);
        } catch (final UnknownHostException ex) {
            // logger.debug("could not resolve host inside send mail");
            logger.error("UnknownException raised while sending mail " + ex);
            // ex.printStackTrace();
        }
    }
}
