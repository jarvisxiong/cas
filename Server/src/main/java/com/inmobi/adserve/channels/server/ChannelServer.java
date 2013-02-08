package com.inmobi.adserve.channels.server;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Log4JLoggerFactory;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;
import org.postgresql.jdbc3.Jdbc3PoolingDataSource;

import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.repository.ChannelAdGroupRepository;
import com.inmobi.adserve.channels.repository.ChannelRepository;
import com.inmobi.adserve.channels.repository.ChannelFeedbackRepository;
import com.inmobi.adserve.channels.repository.ChannelSegmentFeedbackRepository;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.repository.SiteCitrusLeafFeedbackRepository;
import com.inmobi.adserve.channels.repository.SiteMetaDataRepository;
import com.inmobi.adserve.channels.repository.SiteTaxonomyRepository;
import com.inmobi.adserve.channels.util.ConfigurationLoader;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.casthrift.DataCenter;
import com.inmobi.messaging.publisher.AbstractMessagePublisher;
import com.inmobi.messaging.publisher.MessagePublisherFactory;
import com.inmobi.phoenix.exception.InitializationException;

public class ChannelServer {
  private static Logger logger;
  private static ChannelAdGroupRepository channelAdGroupRepository;
  private static ChannelRepository channelRepository;
  private static ChannelFeedbackRepository channelFeedbackRepository;
  private static ChannelSegmentFeedbackRepository channelSegmentFeedbackRepository;
  private static SiteMetaDataRepository siteMetaDataRepository;
  private static SiteTaxonomyRepository siteTaxonomyRepository;
  private static SiteCitrusLeafFeedbackRepository siteCitrusLeafFeedbackRepository;
  private static RepositoryHelper repositoryHelper;
  private static final String configFile = "/opt/mkhoj/conf/cas/channel-server.properties";
  private static String DATACENTERIDKEY = "dc.id";
  private static String HOSTNAMEKEY = "host.name";
  private static String DATACENTRENAMEKEY = "dc.name";
  public static byte dataCenterIdCode;
  public static short hostIdCode;
  public static String dataCentreName;

  public static void main(String[] args) throws Exception {

    ConfigurationLoader config = ConfigurationLoader.getInstance(configFile);
    if(!checkLogFolders(config.log4jConfiguration()))
      return;
    // Set the status code for load balancer status.
    ServerStatusInfo.statusCode = 200;

    // Setting up the logger factory and SlotSizeMapping for the project.
    DebugLogger.init(config.loggerConfiguration());
    SlotSizeMapping.init();

    logger = Logger.getLogger(config.loggerConfiguration().getString("debug"));

    logger.info("Initializing logger completed");
    // parsing the data center id given in the vm parameters
    ChannelServerHelper channelServerHelper = new ChannelServerHelper(logger);
    dataCenterIdCode = channelServerHelper.getDataCenterId(DATACENTERIDKEY);
    hostIdCode = channelServerHelper.getHostId(HOSTNAMEKEY);
    dataCentreName = channelServerHelper.getDataCentreName(DATACENTRENAMEKEY);
    // Initialising Internal logger factory for Netty
    InternalLoggerFactory.setDefaultFactory(new Log4JLoggerFactory());

    // Initialising logging - Write to databus
    AbstractMessagePublisher dataBusPublisher = (AbstractMessagePublisher) MessagePublisherFactory.create(configFile);
    String rrLogKey = config.serverConfiguration().getString("rrLogKey");
    String channelLogKey = config.serverConfiguration().getString("channelLogKey");
    String advertisementLogKey = config.serverConfiguration().getString("adsLogKey");
    Logging.init(dataBusPublisher, rrLogKey, channelLogKey, advertisementLogKey, config.serverConfiguration());

    // Initialising Internal logger factory for Netty
    InternalLoggerFactory.setDefaultFactory(new Log4JLoggerFactory());

    channelAdGroupRepository = new ChannelAdGroupRepository();
    channelRepository = new ChannelRepository();
    channelFeedbackRepository = new ChannelFeedbackRepository();
    channelSegmentFeedbackRepository = new ChannelSegmentFeedbackRepository();
    siteMetaDataRepository = new SiteMetaDataRepository();
    siteTaxonomyRepository = new SiteTaxonomyRepository();
    siteCitrusLeafFeedbackRepository = new SiteCitrusLeafFeedbackRepository();
    
    repositoryHelper = new RepositoryHelper(channelRepository, channelAdGroupRepository, channelFeedbackRepository,
        channelSegmentFeedbackRepository, siteMetaDataRepository, siteTaxonomyRepository, siteCitrusLeafFeedbackRepository);

    MatchSegments.init(channelAdGroupRepository, repositoryHelper);
    InspectorStats.initializeRepoStats("ChannelAdGroupRepository");
    InspectorStats.initializeRepoStats("ChannelFeedbackRepository");
    InspectorStats.initializeRepoStats("ChannelSegmentFeedbackRepository");
    InspectorStats.initializeRepoStats("SiteMetaDataRepository");
    InspectorStats.initializeRepoStats("SiteTaxonomyRepository");
    instantiateRepository(logger, config);
    Filters.init(config.adapterConfiguration(), repositoryHelper);

    // Creating netty client for out-bound calls.
    Timer timer = new HashedWheelTimer(5, TimeUnit.MILLISECONDS);
    BootstrapCreation.init(timer);
    RtbBootstrapCreation.init(timer);
    ClientBootstrap clientBootstrap = BootstrapCreation.createBootstrap(logger, config.serverConfiguration());
    ClientBootstrap rtbClientBootstrap = RtbBootstrapCreation.createBootstrap(logger, config.rtbConfiguration());
    if(null == clientBootstrap) {
      ServerStatusInfo.statusCode = 404;
      ServerStatusInfo.statusString = "StackTrace is: failed to create bootstrap";
      logger.error("failed to create bootstrap");
      return;
    }

    InspectorStats.initializeLbStats();
    InspectorStats.initializeWorkflow("WorkFlow");
    // Configure the netty server.
    try {
      // Initialising request handler
      AsyncRequestMaker.init(clientBootstrap, rtbClientBootstrap);
      ServletHandler.init(config, repositoryHelper);
      SegmentFactory.init(repositoryHelper);
      ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
          Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
      Timer servertimer = new HashedWheelTimer(5, TimeUnit.MILLISECONDS);
      bootstrap.setPipelineFactory(new ChannelServerPipelineFactory(servertimer, config.serverConfiguration()));
      bootstrap.setOption("child.keepAlive", true);
      bootstrap.setOption("child.tcpNoDelay", true);
      bootstrap.setOption("child.reuseAddress", true);
      bootstrap.setOption("child.connectTimeoutMillis", 5); // FIXME Should come
                                                            // from properties
                                                            // file
      bootstrap.bind(new InetSocketAddress(8800));
      // If client bootstrap is not present throwing exception which will set
      // lbStatus as NOT_OK.
    } catch (Exception exception) {
      ServerStatusInfo.statusString = getMyStackTrace(exception);
      ServerStatusInfo.statusCode = 404;
      logger.error("stack trace is " + getMyStackTrace(exception));
      if(logger.isDebugEnabled()) {
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
      Configuration databaseConfig = config.databaseConfiguration();
      System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
      System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");

      // Configuring the dataSource with JDBC
      InitialContext initialContext = new InitialContext();
      initialContext.createSubcontext("java:");
      initialContext.createSubcontext("java:comp");
      initialContext.createSubcontext("java:comp/env");

      Jdbc3PoolingDataSource dataSource = new Jdbc3PoolingDataSource();
      dataSource.setServerName(databaseConfig.getString("host"));
      dataSource.setPortNumber(databaseConfig.getInt("port"));
      dataSource.setDatabaseName(databaseConfig.getString("database"));
      dataSource.setUser(databaseConfig.getString("username"));
      dataSource.setPassword(databaseConfig.getString("password"));
      Configuration repoConfig = config.repoConfiguration();
      Configuration feedbackConfig = config.feedBackConfiguration();
      Configuration segmentFeedbackConfig = config.segmentFeedBackConfiguration();
      Configuration siteTaxonomyConfig = config.siteTaxonomyConfiguration();
      InspectorStats.setStats("ChannelAdGroupRepository", InspectorStrings.isUpdating, 0);
      InspectorStats.setStats("ChannelAdGroupRepository", InspectorStrings.repoSource,
          databaseConfig.getString("database"));
      InspectorStats.setStats("ChannelAdGroupRepository", InspectorStrings.query, repoConfig.getString("query"));
      InspectorStats.setStats("ChannelAdGroupRepository", InspectorStrings.refreshInterval,
          repoConfig.getString("refreshTime"));

      InspectorStats.setStats("ChannelFeedbackRepository", InspectorStrings.isUpdating, 0);
      InspectorStats.setStats("ChannelFeedbackRepository", InspectorStrings.repoSource,
          databaseConfig.getString("database"));
      InspectorStats.setStats("ChannelFeedbackRepository", InspectorStrings.query, feedbackConfig.getString("query"));
      InspectorStats.setStats("ChannelFeedbackRepository", InspectorStrings.refreshInterval,
          feedbackConfig.getString("refreshTime"));

      InspectorStats.setStats("ChannelSegmentFeedbackRepository", InspectorStrings.isUpdating, 0);
      InspectorStats.setStats("ChannelSegmentFeedbackRepository", InspectorStrings.repoSource,
          databaseConfig.getString("database"));
      InspectorStats.setStats("ChannelSegmentFeedbackRepository", InspectorStrings.query,
          segmentFeedbackConfig.getString("query"));
      InspectorStats.setStats("ChannelSegmentFeedbackRepository", InspectorStrings.refreshInterval,
          segmentFeedbackConfig.getString("refreshTime"));
      
      InspectorStats.setStats("SiteMetaDataRepository", InspectorStrings.isUpdating, 0);
      InspectorStats.setStats("SiteMetaDataRepository", InspectorStrings.repoSource,
          databaseConfig.getString("database"));
      InspectorStats.setStats("SiteMetaDataRepository", InspectorStrings.query, repoConfig.getString("query"));
      InspectorStats.setStats("SiteMetaDataRepository", InspectorStrings.refreshInterval,
          repoConfig.getString("refreshTime"));
      
      InspectorStats.setStats("SiteTaxonomyRepository", InspectorStrings.isUpdating, 0);
      InspectorStats.setStats("SiteTaxonomyRepository", InspectorStrings.repoSource,
          databaseConfig.getString("database"));
      InspectorStats.setStats("SiteTaxonomyRepository", InspectorStrings.query,
          siteTaxonomyConfig.getString("query"));
      InspectorStats.setStats("SiteTaxonomyRepository", InspectorStrings.refreshInterval,
          siteTaxonomyConfig.getString("refreshTime"));

      initialContext.bind("java:comp/env/jdbc", dataSource);

      // Reusing the repository from phoenix adsering framework.
      channelAdGroupRepository.init(logger, config.cacheConfiguration().subset("ChannelAdGroupRepository"),
          "ChannelAdGroupRepository");
      channelRepository.init(logger, config.cacheConfiguration().subset("ChannelRepository"), "ChannelRepository");
      channelFeedbackRepository.init(logger, config.cacheConfiguration().subset("ChannelFeedbackRepository"),
          "ChannelFeedbackRepository");
      channelSegmentFeedbackRepository.init(logger,
          config.cacheConfiguration().subset("ChannelSegmentFeedbackRepository"), "ChannelSegmentFeedbackRepository");
      siteTaxonomyRepository.init(logger, config.cacheConfiguration().subset("SiteTaxonomyRepository"),
          "SiteTaxonomyRepository");
      siteMetaDataRepository.init(logger, config.cacheConfiguration().subset("SiteMetaDataRepository"),
          "SiteMetaDataRepository");
      siteCitrusLeafFeedbackRepository.init(config.serverConfiguration(), DataCenter.GLOBAL);

      logger.error("* * * * Instantiating repository completed * * * *");
    } catch (NamingException exception) {
      logger.error("failed to creatre binding for postgresql data source " + exception.getMessage());
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

  // check if all log folders exists
  public static boolean checkLogFolders(Configuration config) {
    String rrLogFolder = config.getString("appender.rr.File");
    String channelLogFolder = config.getString("appender.channel.File");
    String debugLogFolder = config.getString("appender.debug.File");
    String advertiserLogFolder = config.getString("appender.advertiser.File");
    String sampledAdvertiserLogFolder = config.getString("appender.sampledadvertiser.File");
    if(rrLogFolder != null)
      rrLogFolder = rrLogFolder.substring(0, rrLogFolder.lastIndexOf('/') + 1);
    if(channelLogFolder != null)
      channelLogFolder = channelLogFolder.substring(0, channelLogFolder.lastIndexOf('/') + 1);
    if(debugLogFolder != null)
      debugLogFolder = debugLogFolder.substring(0, debugLogFolder.lastIndexOf('/') + 1);
    if(advertiserLogFolder != null)
      advertiserLogFolder = advertiserLogFolder.substring(0, advertiserLogFolder.lastIndexOf('/') + 1);
    if(sampledAdvertiserLogFolder != null)
      sampledAdvertiserLogFolder = sampledAdvertiserLogFolder.substring(0,
          sampledAdvertiserLogFolder.lastIndexOf('/') + 1);
    File rrFolder = new File(rrLogFolder);
    File channelFolder = new File(channelLogFolder);
    File debugFolder = new File(debugLogFolder);
    File advertiserFolder = new File(advertiserLogFolder);
    File sampledAdvertiserFolder = new File(sampledAdvertiserLogFolder);
    if(rrFolder != null && rrFolder.exists() && channelFolder != null && channelFolder.exists() && debugFolder != null
        && debugFolder.exists() && advertiserFolder != null && advertiserFolder.exists()
        && sampledAdvertiserFolder != null && sampledAdvertiserFolder.exists())
      return true;
    ServerStatusInfo.statusCode = 404;
    ServerStatusInfo.statusString = "StackTrace is: one or more log folders missing";
    return false;
  }
}
