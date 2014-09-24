package com.inmobi.adserve.channels.server;

import java.util.List;
import java.util.Random;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.util.ConfigurationLoader;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;


public class CasConfigUtil {
/*
    private CasConfigUtil(){
        //dummy private constructor to avoid instantiation from other classes
    }
*/
    private static final Logger    LOG                      = LoggerFactory.getLogger(CasConfigUtil.class);

    // TODO: clear up all these responses, configs to separate module
    public static final String     jsonParsingError         = "EJSON";
    public static final String     thriftParsingError       = "ETHRIFT";
    public static final String     processingError          = "ESERVER";
    public static final String     missingSiteId            = "NOSITE";
    public static final String     incompatibleSiteType     = "ESITE";
    public static final String     lowSdkVersion            = "LSDK";
    public static final String     MISSING_CATEGORY         = "MISSINGCATEGORY";
    public static final String     CLOSED_CHANNEL_EXCEPTION = "java.nio.channels.ClosedChannelException";
    public static final String     CONNECTION_RESET_PEER    = "java.io.IOException: Connection reset by peer";

    private static Configuration   serverConfig;
    private static Configuration   rtbConfig;
    private static Configuration   adapterConfig;
    private static Configuration   loggerConfig;
    private static Configuration   log4jConfig;
    private static Configuration   databaseConfig;

    public static RepositoryHelper repositoryHelper;
    public static int              percentRollout;
    public static List<String>     allowedSiteTypes;
    public static int              rollCount                = 0;
    public static final Random     random                   = new Random();

    public static void init(final ConfigurationLoader config, final RepositoryHelper repositoryHelper) {
        CasConfigUtil.rtbConfig = config.getRtbConfiguration();
        CasConfigUtil.loggerConfig = config.getLoggerConfiguration();
        CasConfigUtil.serverConfig = config.getServerConfiguration();
        CasConfigUtil.adapterConfig = config.getAdapterConfiguration();
        CasConfigUtil.log4jConfig = config.getLog4jConfiguration();
        CasConfigUtil.databaseConfig = config.getDatabaseConfiguration();
        CasConfigUtil.repositoryHelper = repositoryHelper;
        percentRollout = CasConfigUtil.serverConfig.getInt("percentRollout", 100);
        allowedSiteTypes = CasConfigUtil.serverConfig.getList("allowedSiteTypes");

        InspectorStats.incrementStatCount(InspectorStrings.PERCENT_ROLL_OUT, percentRollout);
    }

    public static Configuration getServerConfig() {
        return serverConfig;
    }

    public static Configuration getAdapterConfig() {
        return adapterConfig;
    }

    public static Configuration getLoggerConfig() {
        return loggerConfig;
    }

    public static Configuration getLog4jConfig() {
        return log4jConfig;
    }

    public static Configuration getDatabaseConfig() {
        return databaseConfig;
    }

    public static Configuration getRtbConfig() {
        return CasConfigUtil.rtbConfig;
    }

    public static void setRtbConfig(final Configuration rtbConfig) {
        CasConfigUtil.rtbConfig = rtbConfig;
    }

}
