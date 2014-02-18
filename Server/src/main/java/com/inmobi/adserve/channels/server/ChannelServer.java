package com.inmobi.adserve.channels.server;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Log4JLoggerFactory;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.repository.ChannelAdGroupRepository;
import com.inmobi.adserve.channels.repository.ChannelFeedbackRepository;
import com.inmobi.adserve.channels.repository.ChannelRepository;
import com.inmobi.adserve.channels.repository.ChannelSegmentFeedbackRepository;
import com.inmobi.adserve.channels.repository.ChannelSegmentMatchingCache;
import com.inmobi.adserve.channels.repository.CurrencyConversionRepository;
import com.inmobi.adserve.channels.repository.PricingEngineRepository;
import com.inmobi.adserve.channels.repository.PublisherFilterRepository;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.repository.SiteCitrusLeafFeedbackRepository;
import com.inmobi.adserve.channels.repository.SiteEcpmRepository;
import com.inmobi.adserve.channels.repository.SiteMetaDataRepository;
import com.inmobi.adserve.channels.repository.SiteTaxonomyRepository;
import com.inmobi.adserve.channels.server.api.ConnectionType;
import com.inmobi.adserve.channels.server.client.BootstrapCreation;
import com.inmobi.adserve.channels.server.client.RtbBootstrapCreation;
import com.inmobi.adserve.channels.server.module.NettyModule;
import com.inmobi.adserve.channels.server.module.ScopeModule;
import com.inmobi.adserve.channels.server.module.ServerModule;
import com.inmobi.adserve.channels.server.netty.NettyServer;
import com.inmobi.adserve.channels.server.requesthandler.AsyncRequestMaker;
import com.inmobi.adserve.channels.server.requesthandler.Logging;
import com.inmobi.adserve.channels.util.ConfigurationLoader;
import com.inmobi.adserve.channels.util.MetricsManager;
import com.inmobi.casthrift.DataCenter;
import com.inmobi.messaging.publisher.AbstractMessagePublisher;
import com.inmobi.messaging.publisher.MessagePublisherFactory;
import com.inmobi.phoenix.exception.InitializationException;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;


/*
 * 
 * Run jar:- java -Dincoming.connections=100 -Ddcpoutbound.connections=50 -Drtboutbound.connections=50 -jar
 * cas-server.jar If these are not specified server will pick these values from channel-server.properties config file.
 */
public class ChannelServer {
    private static Logger                           logger;
    private static ChannelAdGroupRepository         channelAdGroupRepository;
    private static ChannelRepository                channelRepository;
    private static ChannelFeedbackRepository        channelFeedbackRepository;
    private static ChannelSegmentFeedbackRepository channelSegmentFeedbackRepository;
    private static SiteMetaDataRepository           siteMetaDataRepository;
    private static SiteTaxonomyRepository           siteTaxonomyRepository;
    private static SiteCitrusLeafFeedbackRepository siteCitrusLeafFeedbackRepository;
    private static PricingEngineRepository          pricingEngineRepository;
    private static PublisherFilterRepository        publisherFilterRepository;
    private static SiteEcpmRepository               siteEcpmRepository;
    private static CurrencyConversionRepository     currencyConversionRepository;
    private static final String                     configFile = "/opt/mkhoj/conf/cas/channel-server.properties";
    public static byte                              dataCenterIdCode;
    public static short                             hostIdCode;
    public static String                            dataCentreName;

    public static void main(final String[] args) throws Exception {
        try {
            ConfigurationLoader config = ConfigurationLoader.getInstance(configFile);

            if (!checkLogFolders(config.getLog4jConfiguration())) {
                System.out.println("Log folders are not available so exiting..");
                return;
            }

            // Set the status code for load balancer status.
            ServerStatusInfo.statusCode = 200;

            SlotSizeMapping.init();
            Formatter.init();

            PropertyConfigurator.configure(config.getLoggerConfiguration().getString("log4jLoggerConf"));
            logger = Logger.getLogger("repository");
            logger.debug("Initializing logger completed");

            // parsing the data center id given in the vm parameters
            ChannelServerHelper channelServerHelper = new ChannelServerHelper(logger);
            dataCenterIdCode = channelServerHelper.getDataCenterId(ChannelServerStringLiterals.DATA_CENTER_ID_KEY);
            hostIdCode = channelServerHelper.getHostId(ChannelServerStringLiterals.HOST_NAME_KEY);
            dataCentreName = channelServerHelper.getDataCentreName(ChannelServerStringLiterals.DATA_CENTRE_NAME_KEY);

            // Initialising Internal logger factory for Netty
            InternalLoggerFactory.setDefaultFactory(new Log4JLoggerFactory());

            // Initialising logging - Write to databus
            AbstractMessagePublisher dataBusPublisher = (AbstractMessagePublisher) MessagePublisherFactory
                    .create(configFile);
            String rrLogKey = config.getServerConfiguration().getString("rrLogKey");
            String channelLogKey = config.getServerConfiguration().getString("channelLogKey");
            String advertisementLogKey = config.getServerConfiguration().getString("adsLogKey");
            Logging.init(dataBusPublisher, rrLogKey, channelLogKey, advertisementLogKey,
                    config.getServerConfiguration());

            // Initializing graphite stats
            MetricsManager.init(
                    config.getServerConfiguration().getString("graphiteServer.host", "mon02.ads.uj1.inmobi.com"),
                    config.getServerConfiguration().getInt("graphiteServer.port", 2003), config
                            .getServerConfiguration().getInt("graphiteServer.intervalInMinutes", 1));
            channelAdGroupRepository = new ChannelAdGroupRepository();
            channelRepository = new ChannelRepository();
            channelFeedbackRepository = new ChannelFeedbackRepository();
            channelSegmentFeedbackRepository = new ChannelSegmentFeedbackRepository();
            siteMetaDataRepository = new SiteMetaDataRepository();
            siteTaxonomyRepository = new SiteTaxonomyRepository();
            siteCitrusLeafFeedbackRepository = new SiteCitrusLeafFeedbackRepository();
            pricingEngineRepository = new PricingEngineRepository();
            publisherFilterRepository = new PublisherFilterRepository();
            siteEcpmRepository = new SiteEcpmRepository();
            currencyConversionRepository = new CurrencyConversionRepository();

            RepositoryHelper.Builder repoHelperBuilder = RepositoryHelper.newBuilder();
            repoHelperBuilder.setChannelRepository(channelRepository);
            repoHelperBuilder.setChannelAdGroupRepository(channelAdGroupRepository);
            repoHelperBuilder.setChannelFeedbackRepository(channelFeedbackRepository);
            repoHelperBuilder.setChannelSegmentFeedbackRepository(channelSegmentFeedbackRepository);
            repoHelperBuilder.setSiteMetaDataRepository(siteMetaDataRepository);
            repoHelperBuilder.setSiteTaxonomyRepository(siteTaxonomyRepository);
            repoHelperBuilder.setSiteCitrusLeafFeedbackRepository(siteCitrusLeafFeedbackRepository);
            repoHelperBuilder.setPricingEngineRepository(pricingEngineRepository);
            repoHelperBuilder.setPublisherFilterRepository(publisherFilterRepository);
            repoHelperBuilder.setSiteEcpmRepository(siteEcpmRepository);
            repoHelperBuilder.setCurrencyConversionRepository(currencyConversionRepository);
            RepositoryHelper repositoryHelper = repoHelperBuilder.build();

            instantiateRepository(logger, config);
            ServletHandler.init(config, repositoryHelper);
            Integer maxIncomingConnections = channelServerHelper.getMaxConnections(
                    ChannelServerStringLiterals.INCOMING_CONNECTIONS, ConnectionType.INCOMING);
            Integer maxRTbdOutGoingConnections = channelServerHelper.getMaxConnections(
                    ChannelServerStringLiterals.RTBD_OUTGING_CONNECTIONS, ConnectionType.RTBD_OUTGOING);
            Integer maxDCpOutGoingConnections = channelServerHelper.getMaxConnections(
                    ChannelServerStringLiterals.DCP_OUTGOING_CONNECTIONS, ConnectionType.DCP_OUTGOING);
            if (null != maxIncomingConnections) {
                ServletHandler.getServerConfig().setProperty("incomingMaxConnections", maxIncomingConnections);
            }
            if (null != maxIncomingConnections) {
                ServletHandler.getServerConfig().setProperty("rtbOutGoingMaxConnections", maxRTbdOutGoingConnections);
            }
            if (null != maxIncomingConnections) {
                ServletHandler.getServerConfig().setProperty("dcpOutGoingMaxConnections", maxDCpOutGoingConnections);
            }

            // Creating netty client for out-bound calls.
            Timer timer = new HashedWheelTimer(5, TimeUnit.MILLISECONDS);
            BootstrapCreation.init(timer);
            RtbBootstrapCreation.init(timer);
            ClientBootstrap clientBootstrap = BootstrapCreation
                    .createBootstrap(logger, config.getServerConfiguration());
            ClientBootstrap rtbClientBootstrap = RtbBootstrapCreation.createBootstrap(logger,
                    config.getRtbConfiguration());

            // For some partners netty client does not work thus
            // Creating a ning client for out-bound calls
            AsyncHttpClientConfig asyncHttpClientConfig = new AsyncHttpClientConfig.Builder()
                    .setRequestTimeoutInMs(config.getServerConfiguration().getInt("readtimeoutMillis") - 100)
                    .setConnectionTimeoutInMs(600).build();
            AsyncHttpClient asyncHttpClient = new AsyncHttpClient(asyncHttpClientConfig);

            if (null == clientBootstrap) {
                ServerStatusInfo.statusCode = 404;
                ServerStatusInfo.statusString = "StackTrace is: failed to create bootstrap";
                logger.info("failed to create bootstrap");
                return;
            }

            // Configure the netty server.

            // Initialising request handler
            AsyncRequestMaker.init(clientBootstrap, rtbClientBootstrap, asyncHttpClient);

            Injector parentInjector = Guice.createInjector(new ScopeModule());

            Injector serverInjector = parentInjector.createChildInjector(
                    new NettyModule(config.getServerConfiguration(), 8800),
                    new ServerModule(config.getLoggerConfiguration(), config.getAdapterConfiguration(), config
                            .getServerConfiguration(), repositoryHelper));
            final NettyServer server = serverInjector.getInstance(NettyServer.class);
            server.startAndWait();

            Injector statInjector = parentInjector.createChildInjector(
                    new NettyModule(config.getServerConfiguration(), 8801),
                    new ServerModule(config.getLoggerConfiguration(), config.getAdapterConfiguration(), config
                            .getServerConfiguration(), repositoryHelper));
            final NettyServer statusServer = statInjector.getInstance(NettyServer.class);
            statusServer.startAndWait();

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    server.stopAndWait();
                    statusServer.stopAndWait();
                }
            });

            System.out.close();
            // If client bootstrap is not present throwing exception which will
            // set
            // lbStatus as NOT_OK.
        }
        catch (Exception exception) {
            System.out.println(exception);
            ServerStatusInfo.statusString = getMyStackTrace(exception);
            ServerStatusInfo.statusCode = 404;
            logger.info("stack trace is " + getMyStackTrace(exception));
            if (logger.isDebugEnabled()) {
                logger.debug(exception.getMessage());
                HttpRequestHandler.sendMail(exception.getMessage(), getMyStackTrace(exception));
            }
        }
    }

    public static String getMyStackTrace(final Exception exception) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        exception.printStackTrace(printWriter);
        return ("StackTrace is: " + stringWriter.toString());
    }

    private static void instantiateRepository(final Logger logger, final ConfigurationLoader config)
            throws ClassNotFoundException {
        try {
            logger.debug("Starting to instantiate repository");
            ChannelSegmentMatchingCache.init(logger);
            Configuration databaseConfig = config.getDatabaseConfiguration();
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
            System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");

            // Configuring the dataSource with JDBC
            InitialContext initialContext = new InitialContext();
            initialContext.createSubcontext("java:");
            initialContext.createSubcontext("java:comp");
            initialContext.createSubcontext("java:comp/env");

            Class.forName("org.postgresql.Driver");

            Properties props = new Properties();
            props.put("type", "javax.sql.DataSource");
            props.put("driverClassName", "org.postgresql.Driver");
            props.put("user", databaseConfig.getString("username"));
            props.put("password", databaseConfig.getString("password"));

            String validationQuery = databaseConfig.getString("validationQuery");
            int maxActive = databaseConfig.getInt("maxActive", 20);
            int maxIdle = databaseConfig.getInt("maxIdle", 1);
            int maxWait = databaseConfig.getInt("maxWait", -1);
            boolean testWhileIdle = databaseConfig.getBoolean("testWhileIdle", true);
            boolean testOnBorrow = databaseConfig.getBoolean("testOnBorrow", true);
            final GenericObjectPool connectionPool = new GenericObjectPool(null);
            connectionPool.setMaxActive(maxActive);
            connectionPool.setMaxIdle(maxIdle);
            connectionPool.setMaxWait(maxWait);
            connectionPool.setTestWhileIdle(testWhileIdle);
            connectionPool.setTestOnBorrow(testOnBorrow);
            String connectUri = "jdbc:postgresql://" + databaseConfig.getString("host") + ":"
                    + databaseConfig.getInt("port") + "/"
                    + databaseConfig.getString(ChannelServerStringLiterals.DATABASE) + "?socketTimeout="
                    + databaseConfig.getString("socketTimeout");
            final ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connectUri, props);
            PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory,
                    connectionPool, null, validationQuery, true, false);

            final PoolingDataSource ds = new PoolingDataSource(poolableConnectionFactory.getPool());

            initialContext.bind("java:comp/env/jdbc", ds);

            ChannelSegmentMatchingCache.init(logger);
            // Reusing the repository from phoenix adserving framework.
            currencyConversionRepository.init(logger,
                    config.getCacheConfiguration().subset(ChannelServerStringLiterals.CURRENCY_CONVERSION_REPOSITORY),
                    ChannelServerStringLiterals.CURRENCY_CONVERSION_REPOSITORY);
            channelAdGroupRepository.init(logger,
                    config.getCacheConfiguration().subset(ChannelServerStringLiterals.CHANNEL_ADGROUP_REPOSITORY),
                    ChannelServerStringLiterals.CHANNEL_ADGROUP_REPOSITORY);
            channelRepository.init(logger,
                    config.getCacheConfiguration().subset(ChannelServerStringLiterals.CHANNEL_REPOSITORY),
                    ChannelServerStringLiterals.CHANNEL_REPOSITORY);
            channelFeedbackRepository.init(logger,
                    config.getCacheConfiguration().subset(ChannelServerStringLiterals.CHANNEL_FEEDBACK_REPOSITORY),
                    ChannelServerStringLiterals.CHANNEL_FEEDBACK_REPOSITORY);
            channelSegmentFeedbackRepository.init(
                    logger,
                    config.getCacheConfiguration().subset(
                            ChannelServerStringLiterals.CHANNEL_SEGMENT_FEEDBACK_REPOSITORY),
                    ChannelServerStringLiterals.CHANNEL_SEGMENT_FEEDBACK_REPOSITORY);
            siteTaxonomyRepository.init(logger,
                    config.getCacheConfiguration().subset(ChannelServerStringLiterals.SITE_TAXONOMY_REPOSITORY),
                    ChannelServerStringLiterals.SITE_TAXONOMY_REPOSITORY);
            siteMetaDataRepository.init(logger,
                    config.getCacheConfiguration().subset(ChannelServerStringLiterals.SITE_METADATA_REPOSITORY),
                    ChannelServerStringLiterals.SITE_METADATA_REPOSITORY);
            pricingEngineRepository.init(logger,
                    config.getCacheConfiguration().subset(ChannelServerStringLiterals.PRICING_ENGINE_REPOSITORY),
                    ChannelServerStringLiterals.PRICING_ENGINE_REPOSITORY);
            publisherFilterRepository.init(logger,
                    config.getCacheConfiguration().subset(ChannelServerStringLiterals.PUBLISHER_FILTER_REPOSITORY),
                    ChannelServerStringLiterals.PUBLISHER_FILTER_REPOSITORY);
            siteCitrusLeafFeedbackRepository.init(
                    config.getServerConfiguration().subset(ChannelServerStringLiterals.CITRUS_LEAF_FEEDBACK),
                    getDataCenter());
            siteEcpmRepository.init(logger,
                    config.getCacheConfiguration().subset(ChannelServerStringLiterals.SITE_ECPM_REPOSITORY),
                    ChannelServerStringLiterals.SITE_ECPM_REPOSITORY);
            logger.error("* * * * Instantiating repository completed * * * *");
            config.getCacheConfiguration().subset(ChannelServerStringLiterals.SITE_METADATA_REPOSITORY)
                    .subset(ChannelServerStringLiterals.SITE_METADATA_REPOSITORY);

        }
        catch (NamingException exception) {
            logger.error("failed to creatre binding for postgresql data source " + exception.getMessage());
            ServerStatusInfo.statusCode = 404;
            ServerStatusInfo.statusString = getMyStackTrace(exception);
        }
        catch (InitializationException exception) {
            logger.error("failed to initialize repository " + exception.getMessage());
            ServerStatusInfo.statusCode = 404;
            ServerStatusInfo.statusString = getMyStackTrace(exception);
            if (logger.isDebugEnabled()) {
                logger.debug(ChannelServer.getMyStackTrace(exception));
            }
        }
    }

    private static DataCenter getDataCenter() {
        DataCenter colo = DataCenter.ALL;
        if (DataCenter.UA2.toString().equalsIgnoreCase(ChannelServer.dataCentreName)) {
            colo = DataCenter.UA2;
        }
        else if (DataCenter.UJ1.toString().equalsIgnoreCase(ChannelServer.dataCentreName)) {
            colo = DataCenter.UJ1;
        }
        else if (DataCenter.UJ1.toString().equalsIgnoreCase(ChannelServer.dataCentreName)) {
            colo = DataCenter.UJ1;
        }
        else if (DataCenter.LHR1.toString().equalsIgnoreCase(ChannelServer.dataCentreName)) {
            colo = DataCenter.LHR1;
        }
        else if (DataCenter.HKG1.toString().equalsIgnoreCase(ChannelServer.dataCentreName)) {
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
            sampledAdvertiserLogFolder = sampledAdvertiserLogFolder.substring(0,
                    sampledAdvertiserLogFolder.lastIndexOf('/') + 1);
            sampledAdvertiserFolder = new File(sampledAdvertiserLogFolder);
        }
        if (debugFolder != null && debugFolder.exists() && advertiserFolder != null && advertiserFolder.exists()) {
            if (sampledAdvertiserFolder != null && sampledAdvertiserFolder.exists() && repositoryFolder != null
                    && repositoryFolder.exists()) {
                return true;
            }
        }
        ServerStatusInfo.statusCode = 404;
        ServerStatusInfo.statusString = "StackTrace is: one or more log folders missing";
        return false;
    }
}
