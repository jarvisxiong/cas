package com.inmobi.adserve.channels.server.servlet;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Path;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;
import com.inmobi.adserve.channels.server.ChannelServerStringLiterals;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.util.ConfigurationLoader;
import com.inmobi.phoenix.exception.RepositoryException;


@Singleton
@Path("/repoRefresh")
public class ServletRepoRefresh implements Servlet {
    private static final Logger LOG            = LoggerFactory.getLogger(ServletRepoRefresh.class);

    private static final String LAST_UPDATE    = "'${last_update}'";
    private static final String REPLACE_STRING = "now() -interval '1 MINUTE'";

    @Override
    public void handleRequest(final HttpRequestHandler hrh, final QueryStringDecoder queryStringDecoder,
            final Channel serverChannel) throws Exception {
        Map<String, List<String>> params = queryStringDecoder.parameters();
        String requestParam = params.get("args").toString();
        JSONArray jsonArray = new JSONArray(requestParam);
        JSONObject jObject = jsonArray.getJSONObject(0);
        LOG.debug("requestParam {} jObject {}", requestParam, jObject);
        String repoName = jObject.get("repoName").toString();
        LOG.debug("RepoName is {}", repoName);

        String dbHost = jObject.get("DBHost").toString();
        String dbPort = jObject.get("DBPort").toString();
        String dbName = jObject.get("DBSnapshot").toString();
        String dbUser = jObject.get("DBUser").toString();
        String dbPassword = jObject.get("DBPassword").toString();
        String connectionString = "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName;
        Connection con = null;
        Statement statement = null;
        ResultSet resultSet;
        try {
            ConfigurationLoader config = ConfigurationLoader
                    .getInstance("/opt/mkhoj/conf/cas/channel-server.properties");
            con = DriverManager.getConnection(connectionString, dbUser, dbPassword);
            statement = con.createStatement();
            if (repoName.equalsIgnoreCase(ChannelServerStringLiterals.CHANNEL_ADGROUP_REPOSITORY)) {
                final String query = config.getCacheConfiguration()
                        .subset(ChannelServerStringLiterals.CHANNEL_ADGROUP_REPOSITORY)
                        .getString(ChannelServerStringLiterals.QUERY).replace(LAST_UPDATE, REPLACE_STRING);
                resultSet = statement.executeQuery(query);
                CasConfigUtil.repositoryHelper.getChannelAdGroupRepository().newUpdateFromResultSetToOptimizeUpdate(
                        resultSet);
            }
            else if (repoName.equalsIgnoreCase(ChannelServerStringLiterals.CHANNEL_REPOSITORY)) {
                final String query = config.getCacheConfiguration()
                        .subset(ChannelServerStringLiterals.CHANNEL_REPOSITORY)
                        .getString(ChannelServerStringLiterals.QUERY).replace(LAST_UPDATE, REPLACE_STRING);
                resultSet = statement.executeQuery(query);
                CasConfigUtil.repositoryHelper.getChannelRepository()
                        .newUpdateFromResultSetToOptimizeUpdate(resultSet);
            }
            else if (repoName.equalsIgnoreCase(ChannelServerStringLiterals.CHANNEL_FEEDBACK_REPOSITORY)) {
                final String query = config.getCacheConfiguration()
                        .subset(ChannelServerStringLiterals.CHANNEL_FEEDBACK_REPOSITORY)
                        .getString(ChannelServerStringLiterals.QUERY).replace(LAST_UPDATE, REPLACE_STRING);
                resultSet = statement.executeQuery(query);
                CasConfigUtil.repositoryHelper.getChannelFeedbackRepository().newUpdateFromResultSetToOptimizeUpdate(
                        resultSet);
            }
            else if (repoName.equalsIgnoreCase(ChannelServerStringLiterals.CHANNEL_SEGMENT_FEEDBACK_REPOSITORY)) {
                final String query = config.getCacheConfiguration()
                        .subset(ChannelServerStringLiterals.CHANNEL_SEGMENT_FEEDBACK_REPOSITORY)
                        .getString(ChannelServerStringLiterals.QUERY).replace(LAST_UPDATE, REPLACE_STRING);
                resultSet = statement.executeQuery(query);
                CasConfigUtil.repositoryHelper.getChannelSegmentFeedbackRepository()
                        .newUpdateFromResultSetToOptimizeUpdate(resultSet);
            }
            else if (repoName.equalsIgnoreCase(ChannelServerStringLiterals.SITE_METADATA_REPOSITORY)) {
                final String query = config.getCacheConfiguration()
                        .subset(ChannelServerStringLiterals.SITE_METADATA_REPOSITORY)
                        .getString(ChannelServerStringLiterals.QUERY).replace(LAST_UPDATE, REPLACE_STRING);
                resultSet = statement.executeQuery(query);
                CasConfigUtil.repositoryHelper.getSiteMetaDataRepository().newUpdateFromResultSetToOptimizeUpdate(
                        resultSet);
            }
            else if (repoName.equalsIgnoreCase(ChannelServerStringLiterals.SITE_TAXONOMY_REPOSITORY)) {
                final String query = config.getCacheConfiguration()
                        .subset(ChannelServerStringLiterals.SITE_TAXONOMY_REPOSITORY)
                        .getString(ChannelServerStringLiterals.QUERY).replace(LAST_UPDATE, REPLACE_STRING);
                resultSet = statement.executeQuery(query);
                CasConfigUtil.repositoryHelper.getSiteTaxonomyRepository().newUpdateFromResultSetToOptimizeUpdate(
                        resultSet);
            }
            else if (repoName.equalsIgnoreCase(ChannelServerStringLiterals.PRICING_ENGINE_REPOSITORY)) {
                final String query = config.getCacheConfiguration()
                        .subset(ChannelServerStringLiterals.PRICING_ENGINE_REPOSITORY)
                        .getString(ChannelServerStringLiterals.QUERY).replace(LAST_UPDATE, REPLACE_STRING);
                resultSet = statement.executeQuery(query);
                CasConfigUtil.repositoryHelper.getPricingEngineRepository().newUpdateFromResultSetToOptimizeUpdate(
                        resultSet);
            }
            else if (repoName.equalsIgnoreCase(ChannelServerStringLiterals.SITE_ECPM_REPOSITORY)) {
                final String query = config.getCacheConfiguration()
                        .subset(ChannelServerStringLiterals.SITE_ECPM_REPOSITORY)
                        .getString(ChannelServerStringLiterals.QUERY).replace(LAST_UPDATE, REPLACE_STRING);
                resultSet = statement.executeQuery(query);
                CasConfigUtil.repositoryHelper.getSiteEcpmRepository().newUpdateFromResultSetToOptimizeUpdate(
                        resultSet);
            }
            LOG.debug("Successfully updated {}", repoName);
            hrh.responseSender.sendResponse("OK", serverChannel);
        }
        catch (SQLException e1) {
            LOG.info("error is {}", e1);
            hrh.responseSender.sendResponse("NOTOK", serverChannel);
        }
        catch (RepositoryException e2) {
            LOG.info("error is {}", e2);
            hrh.responseSender.sendResponse("NOTOK", serverChannel);
        }
        finally {
            if (null != statement) {
                statement.close();
            }
            if (null != con) {
                con.close();
            }
        }
    }

    @Override
    public String getName() {
        return "RepoRefresh";
    }

}
