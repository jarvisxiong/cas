package com.inmobi.adserve.channels.server;

import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.repository.*;
import com.inmobi.adserve.channels.server.client.BootstrapCreation;
import com.inmobi.adserve.channels.server.client.RtbBootstrapCreation;
import com.inmobi.adserve.channels.server.requesthandler.AsyncRequestMaker;
import com.inmobi.adserve.channels.server.requesthandler.Filters;
import com.inmobi.adserve.channels.server.requesthandler.Logging;
import com.inmobi.adserve.channels.server.requesthandler.MatchSegments;
import com.inmobi.adserve.channels.util.ConfigurationLoader;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.casthrift.DataCenter;
import com.inmobi.messaging.publisher.AbstractMessagePublisher;
import com.inmobi.messaging.publisher.MessagePublisherFactory;
import com.inmobi.phoenix.exception.InitializationException;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Log4JLoggerFactory;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;
import org.postgresql.jdbc3.Jdbc3PoolingDataSource;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ChannelServer {
    private static Logger logger;
    private static ChannelAdGroupRepository channelAdGroupRepository;
    private static ChannelRepository channelRepository;
    private static ChannelFeedbackRepository channelFeedbackRepository;
    private static ChannelSegmentFeedbackRepository channelSegmentFeedbackRepository;
    private static SiteMetaDataRepository siteMetaDataRepository;
    private static SiteTaxonomyRepository siteTaxonomyRepository;
    private static SiteCitrusLeafFeedbackRepository siteCitrusLeafFeedbackRepository;
    private static PricingEngineRepository pricingEngineRepository;
    private static PublisherFilterRepository publisherFilterRepository;
    private static RepositoryHelper repositoryHelper;
    private static final String configFile = "/opt/mkhoj/conf/cas/channel-server.properties";
    public static byte dataCenterIdCode;
    public static short hostIdCode;
    public static String dataCentreName;

    public static void main(String[] args) throws Exception {
        try {
            ConfigurationLoader config = ConfigurationLoader.getInstance(configFile);

            if (!checkLogFolders(config.log4jConfiguration())) {
                System.out.println("Log folders are not available so exiting..");
                return;
            }

            // Set the status code for load balancer status.
            ServerStatusInfo.statusCode = 200;

            DebugLogger.init(config.loggerConfiguration());
            SlotSizeMapping.init();
            Formatter.init();

            PropertyConfigurator.configure(config.loggerConfiguration().getString
                    ("log4jLoggerConf"));
            logger = Logger.getLogger("repository");
            logger.debug("Initializing logger completed");

            // parsing the data center id given in the vm parameters
            ChannelServerHelper channelServerHelper = new ChannelServerHelper(logger);
            dataCenterIdCode = channelServerHelper
                    .getDataCenterId(ChannelServerStringLiterals.DATA_CENTER_ID_KEY);
            hostIdCode = channelServerHelper.getHostId(ChannelServerStringLiterals.HOST_NAME_KEY);
            dataCentreName = channelServerHelper
                    .getDataCentreName(ChannelServerStringLiterals.DATA_CENTRE_NAME_KEY);

            // Initialising Internal logger factory for Netty
            InternalLoggerFactory.setDefaultFactory(new Log4JLoggerFactory());

            // Initialising logging - Write to databus
            AbstractMessagePublisher dataBusPublisher = (AbstractMessagePublisher)
                    MessagePublisherFactory
                    .create(configFile);
            String rrLogKey = config.serverConfiguration().getString("rrLogKey");
            String channelLogKey = config.serverConfiguration().getString("channelLogKey");
            String advertisementLogKey = config.serverConfiguration().getString("adsLogKey");
            Logging.init(dataBusPublisher, rrLogKey, channelLogKey, advertisementLogKey,
                    config.serverConfiguration());

            channelAdGroupRepository = new ChannelAdGroupRepository();
            channelRepository = new ChannelRepository();
            channelFeedbackRepository = new ChannelFeedbackRepository();
            channelSegmentFeedbackRepository = new ChannelSegmentFeedbackRepository();
            siteMetaDataRepository = new SiteMetaDataRepository();
            siteTaxonomyRepository = new SiteTaxonomyRepository();
            siteCitrusLeafFeedbackRepository = new SiteCitrusLeafFeedbackRepository();
            pricingEngineRepository = new PricingEngineRepository();
            publisherFilterRepository = new PublisherFilterRepository();

            repositoryHelper = new RepositoryHelper(channelRepository, channelAdGroupRepository,
                    channelFeedbackRepository,
                    channelSegmentFeedbackRepository, siteMetaDataRepository,
                    siteTaxonomyRepository,
                    siteCitrusLeafFeedbackRepository, pricingEngineRepository,
                    publisherFilterRepository);
            instantiateRepository(logger, config);

            ServletHandler.init(config, repositoryHelper);
            MatchSegments.init(channelAdGroupRepository);
            Filters.init(config.adapterConfiguration());
            SegmentFactory.init(repositoryHelper, config.adapterConfiguration(), logger);

            // Creating netty client for out-bound calls.
            Timer timer = new HashedWheelTimer(5, TimeUnit.MILLISECONDS);
            BootstrapCreation.init(timer);
            RtbBootstrapCreation.init(timer);
            ClientBootstrap clientBootstrap = BootstrapCreation.createBootstrap(logger,
                    config.serverConfiguration());
            ClientBootstrap rtbClientBootstrap = RtbBootstrapCreation.createBootstrap(logger,
                    config.rtbConfiguration());

            // For some partners netty client does not work thus
            // Creating a ning client for out-bound calls
            AsyncHttpClientConfig asyncHttpClientConfig = new AsyncHttpClientConfig.Builder()
                    .setRequestTimeoutInMs(config.serverConfiguration().getInt
                            ("readtimeoutMillis") - 100)
                    .setConnectionTimeoutInMs(600).build();
            AsyncHttpClient asyncHttpClient = new AsyncHttpClient(asyncHttpClientConfig);

            if (null == clientBootstrap) {
                ServerStatusInfo.statusCode = 404;
                ServerStatusInfo.statusString = "StackTrace is: failed to create bootstrap";
                logger.error("failed to create bootstrap");
                return;
            }

            // Configure the netty server.

            // Initialising request handler
            AsyncRequestMaker.init(clientBootstrap, rtbClientBootstrap, asyncHttpClient);
            ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
                    Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
            Timer servertimer = new HashedWheelTimer(5, TimeUnit.MILLISECONDS);
            bootstrap.setPipelineFactory(new ChannelServerPipelineFactory(servertimer, config
                    .serverConfiguration()));
            bootstrap.setOption("child.keepAlive", true);
            bootstrap.setOption("child.tcpNoDelay", true);
            bootstrap.setOption("child.reuseAddress", true);
            bootstrap.setOption("child.connectTimeoutMillis", 5); // FIXME
            bootstrap.bind(new InetSocketAddress(8800));
            // If client bootstrap is not present throwing exception which will
            // set
            // lbStatus as NOT_OK.
        } catch (Exception exception) {
            ServerStatusInfo.statusString = getMyStackTrace(exception);
            ServerStatusInfo.statusCode = 404;
            logger.error("stack trace is " + getMyStackTrace(exception));
            if (logger.isDebugEnabled()) {
                logger.debug(exception.getMessage());
                HttpRequestHandler.sendMail(exception.getMessage(), getMyStackTrace(exception));
            }
        }
    }

    private static String getMyStackTrace(Exception exception) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        exception.printStackTrace(printWriter);
        return ("StackTrace is: " + stringWriter.toString());
    }

    private static void instantiateRepository(Logger logger, ConfigurationLoader config) {
        try {
            logger.debug("Starting to instantiate repository");
            ChannelSegmentMatchingCache.init(logger);
            Configuration databaseConfig = config.databaseConfiguration();
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                    "org.apache.naming.java.javaURLContextFactory");
            System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");

            // Configuring the dataSource with JDBC
            InitialContext initialContext = new InitialContext();
            initialContext.createSubcontext("java:");
            initialContext.createSubcontext("java:comp");
            initialContext.createSubcontext("java:comp/env");

            Jdbc3PoolingDataSource dataSource = new Jdbc3PoolingDataSource();
            dataSource.setServerName(databaseConfig.getString("host"));
            dataSource.setPortNumber(databaseConfig.getInt("port"));
            dataSource.setDatabaseName(databaseConfig.getString(ChannelServerStringLiterals
                    .DATABASE));
            dataSource.setUser(databaseConfig.getString("username"));
            dataSource.setPassword(databaseConfig.getString("password"));

            initialContext.bind("java:comp/env/jdbc", dataSource);

            ChannelSegmentMatchingCache.init(logger);
            // Reusing the repository from phoenix adsering framework.
            channelAdGroupRepository.init(logger,
                    config.cacheConfiguration()
                            .subset(ChannelServerStringLiterals.CHANNEL_ADGROUP_REPOSITORY),
                    ChannelServerStringLiterals.CHANNEL_ADGROUP_REPOSITORY);
            channelRepository.init(logger,
                    config.cacheConfiguration().subset(ChannelServerStringLiterals
                            .CHANNEL_REPOSITORY),
                    ChannelServerStringLiterals.CHANNEL_REPOSITORY);
            channelFeedbackRepository.init(
                    logger,
                    config.cacheConfiguration().subset(
                            ChannelServerStringLiterals.CHANNEL_FEEDBACK_REPOSITORY),
                    ChannelServerStringLiterals.CHANNEL_FEEDBACK_REPOSITORY);
            channelSegmentFeedbackRepository.init(
                    logger,
                    config.cacheConfiguration().subset(
                            ChannelServerStringLiterals.CHANNEL_SEGMENT_FEEDBACK_REPOSITORY),
                    ChannelServerStringLiterals.CHANNEL_SEGMENT_FEEDBACK_REPOSITORY);
            siteTaxonomyRepository.init(logger,
                    config.cacheConfiguration().subset(ChannelServerStringLiterals
                            .SITE_TAXONOMY_REPOSITORY),
                    ChannelServerStringLiterals.SITE_TAXONOMY_REPOSITORY);
            siteMetaDataRepository.init(logger,
                    config.cacheConfiguration().subset(ChannelServerStringLiterals
                            .SITE_METADATA_REPOSITORY),
                    ChannelServerStringLiterals.SITE_METADATA_REPOSITORY);
            pricingEngineRepository.init(logger, config.cacheConfiguration().subset
                    (ChannelServerStringLiterals.PRICING_ENGINE_REPOSITORY),
                    ChannelServerStringLiterals.PRICING_ENGINE_REPOSITORY);
            publisherFilterRepository.init(logger, config.cacheConfiguration().subset
                    (ChannelServerStringLiterals.PUBLISHER_FILTER_REPOSITORY),
                    ChannelServerStringLiterals.PUBLISHER_FILTER_REPOSITORY);
            siteCitrusLeafFeedbackRepository.init(
                    config.serverConfiguration().subset(ChannelServerStringLiterals
                            .CITRUS_LEAF_FEEDBACK),
                    getDataCenter());
            logger.error("* * * * Instantiating repository completed * * * *");
        } catch (NamingException exception) {
            logger
                    .error("failed to creatre binding for postgresql data source " + exception
                            .getMessage());
            ServerStatusInfo.statusCode = 404;
            ServerStatusInfo.statusString = getMyStackTrace(exception);
            return;
        } catch (InitializationException exception) {
            logger.error("failed to initialize repository " + exception.getMessage());
            ServerStatusInfo.statusCode = 404;
            ServerStatusInfo.statusString = getMyStackTrace(exception);
            exception.printStackTrace();
            return;
        }
    }

    private static DataCenter getDataCenter() {
        DataCenter colo = DataCenter.ALL;
        if (DataCenter.UA2.toString().equalsIgnoreCase(ChannelServer.dataCentreName)) {
            colo = DataCenter.UA2;
        } else if (DataCenter.UJ1.toString().equalsIgnoreCase(ChannelServer.dataCentreName)) {
            colo = DataCenter.UJ1;
        } else if (DataCenter.UJ1.toString().equalsIgnoreCase(ChannelServer.dataCentreName)) {
            colo = DataCenter.UJ1;
        } else if (DataCenter.LHR1.toString().equalsIgnoreCase(ChannelServer.dataCentreName)) {
            colo = DataCenter.LHR1;
        } else if (DataCenter.HKG1.toString().equalsIgnoreCase(ChannelServer.dataCentreName)) {
            colo = DataCenter.HKG1;
        }
        return colo;
    }

    // check if all log folders exists
    public static boolean checkLogFolders(Configuration config) {
        String rrLogFolder = config.getString("appender.rr.File");
        String channelLogFolder = config.getString("appender.channel.File");
        String debugLogFolder = config.getString("appender.debug.File");
        String advertiserLogFolder = config.getString("appender.advertiser.File");
        String sampledAdvertiserLogFolder = config.getString("appender.sampledadvertiser.File");
        String repositoryLogFolder = config.getString("appender.repository.File");
        File rrFolder = null;
        File channelFolder = null;
        File debugFolder = null;
        File advertiserFolder = null;
        File sampledAdvertiserFolder = null;
        File repositoryFolder = null;
        if (rrLogFolder != null) {
            rrLogFolder = rrLogFolder.substring(0, rrLogFolder.lastIndexOf('/') + 1);
            rrFolder = new File(rrLogFolder);
        }
        if (repositoryLogFolder != null) {
            repositoryLogFolder = repositoryLogFolder.substring(0,
                    repositoryLogFolder.lastIndexOf('/') + 1);
            repositoryFolder = new File(repositoryLogFolder);
        }
        if (channelLogFolder != null) {
            channelLogFolder = channelLogFolder.substring(0, channelLogFolder.lastIndexOf('/') + 1);
            channelFolder = new File(channelLogFolder);
        }
        if (debugLogFolder != null) {
            debugLogFolder = debugLogFolder.substring(0, debugLogFolder.lastIndexOf('/') + 1);
            debugFolder = new File(debugLogFolder);
        }
        if (advertiserLogFolder != null) {
            advertiserLogFolder = advertiserLogFolder.substring(0,
                    advertiserLogFolder.lastIndexOf('/') + 1);
            advertiserFolder = new File(advertiserLogFolder);
        }
        if (sampledAdvertiserLogFolder != null) {
            sampledAdvertiserLogFolder = sampledAdvertiserLogFolder.substring(0,
                    sampledAdvertiserLogFolder.lastIndexOf('/') + 1);
            sampledAdvertiserFolder = new File(sampledAdvertiserLogFolder);
        }
        if (rrFolder != null && rrFolder.exists() && channelFolder != null && channelFolder.exists()) {
            if (debugFolder != null && debugFolder.exists() && advertiserFolder != null
                    && advertiserFolder.exists()) {
                if (sampledAdvertiserFolder != null && sampledAdvertiserFolder.exists()
                        && repositoryFolder != null && repositoryFolder.exists()) {
                    return true;
                }
            }
        }
        ServerStatusInfo.statusCode = 404;
        ServerStatusInfo.statusString = "StackTrace is: one or more log folders missing";
        return false;
    }
}
