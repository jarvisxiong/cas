package com.inmobi.adserve.channels.server;

import java.util.List;
import java.util.Random;

import org.apache.commons.configuration.Configuration;

import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.util.ConfigurationLoader;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;


public class CasConfigUtil {
    // TODO: clear up all these responses, configs to separate module
    public static final String JSON_PARSING_ERROR = "EJSON";
    public static final String THRIFT_PARSING_ERROR = "ETHRIFT";
    public static final String PROCESSING_ERROR = "ESERVER";
    public static final String MISSING_SITE_ID = "NOSITE";
    public static final String INCOMPATIBLE_SITE_TYPE = "ESITE";
    public static final String LOW_SDK_VERSION = "LSDK";
    public static final String MISSING_CATEGORY = "MISSINGCATEGORY";
    public static final String BANNER_NOT_ALLOWED = "BANNERNOTALLOWED";
    public static final String CHINA_MOBILE_TARGETING = "NONCHINAMOBILE";
    public static final String NO_SUPPORTED_SLOTS = "NOSUPPORTEDSLOTS";
    public static final String NO_SAS_PARAMS = "NOSASPARAMS";
    public static final String MISSING_MRAID_PATH = "NOMRAIDPATH";
    public static final String UNKNOWN_SDK_VERSION = "NOSDKVERSION";
    public static final String CLOSED_CHANNEL_EXCEPTION = "java.nio.channels.ClosedChannelException";
    public static final String CONNECTION_RESET_PEER = "java.io.IOException: Connection reset by peer";
    public static final Random RANDOM = new Random();

    public static RepositoryHelper repositoryHelper;
    public static List<String> allowedSiteTypes;
    public static int rollCount = 0;

    private static Configuration serverConfig;
    private static Configuration rtbConfig;
    private static Configuration adapterConfig;
    private static Configuration loggerConfig;
    private static Configuration databaseConfig;
    
    @SuppressWarnings("unchecked")
    public static void init(final ConfigurationLoader config, final RepositoryHelper repositoryHelper) {
        CasConfigUtil.rtbConfig = config.getRtbConfiguration();
        CasConfigUtil.loggerConfig = config.getLoggerConfiguration();
        CasConfigUtil.serverConfig = config.getServerConfiguration();
        CasConfigUtil.adapterConfig = config.getAdapterConfiguration();
        CasConfigUtil.databaseConfig = config.getDatabaseConfiguration();
        CasConfigUtil.repositoryHelper = repositoryHelper;
        allowedSiteTypes = CasConfigUtil.serverConfig.getList("allowedSiteTypes");

        InspectorStats.incrementStatCount(InspectorStrings.PERCENT_ROLL_OUT,
                CasConfigUtil.serverConfig.getInt("percentRollout", 100));
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
