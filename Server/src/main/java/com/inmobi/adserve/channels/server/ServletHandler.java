package com.inmobi.adserve.channels.server;

import com.google.inject.Singleton;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.servlet.ServletInvalid;
import com.inmobi.adserve.channels.util.ConfigurationLoader;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import org.apache.commons.configuration.Configuration;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Random;


@Sharable
@Singleton
public class ServletHandler extends SimpleChannelUpstreamHandler {
    private static final Logger    LOG                      = LoggerFactory.getLogger(ServletHandler.class);

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
        ServletHandler.rtbConfig = config.getRtbConfiguration();
        ServletHandler.loggerConfig = config.getLoggerConfiguration();
        ServletHandler.serverConfig = config.getServerConfiguration();
        ServletHandler.adapterConfig = config.getAdapterConfiguration();
        ServletHandler.log4jConfig = config.getLog4jConfiguration();
        ServletHandler.databaseConfig = config.getDatabaseConfiguration();
        ServletHandler.repositoryHelper = repositoryHelper;
        percentRollout = ServletHandler.serverConfig.getInt("percentRollout", 100);
        allowedSiteTypes = ServletHandler.serverConfig.getList("allowedSiteTypes");
        InspectorStats.setStats(InspectorStrings.percentRollout, percentRollout);
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
        return ServletHandler.rtbConfig;
    }

    public static void setRtbConfig(final Configuration rtbConfig) {
        ServletHandler.rtbConfig = rtbConfig;
    }

    private final SimpleScope          scope;
    private final Map<String, Servlet> pathToServletMap;

    private final ServletInvalid       invalidServlet;

    @Inject
    ServletHandler(final SimpleScope scope, final Map<String, Servlet> pathToServletMap,
            final ServletInvalid invalidServlet) {
        this.scope = scope;
        this.pathToServletMap = pathToServletMap;
        this.invalidServlet = invalidServlet;
    }

    @Override
    public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {

        HttpRequest request = (HttpRequest) e.getMessage();

        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());
        String path = queryStringDecoder.getPath();

        Servlet servlet = pathToServletMap.get(path);

        if (servlet == null) {
            servlet = invalidServlet;
        }

        LOG.debug("Request servlet is {} for path {}", servlet, path);

        scope.seed(Servlet.class, servlet);

        ctx.sendUpstream(e);

    }
}
