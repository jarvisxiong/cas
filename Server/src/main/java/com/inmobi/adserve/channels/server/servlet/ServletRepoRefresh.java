package com.inmobi.adserve.channels.server.servlet;

import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.CAU_METADATA_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.CCID_MAP_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.CHANNEL_ADGROUP_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.CHANNEL_FEEDBACK_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.CHANNEL_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.CHANNEL_SEGMENT_FEEDBACK_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.CREATIVE_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.CURRENCY_CONVERSION_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.GEO_REGION_FENCE_MAP_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.GEO_ZIP_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.IX_ACCOUNT_MAP_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.IX_BLOCKLIST_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.NATIVE_AD_TEMPLATE_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.PRICING_ENGINE_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.QUERY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.SDK_VIEWABILITY_ELIGIBILITY_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.SITE_ECPM_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.SITE_FILTER_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.SITE_METADATA_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.SITE_TAXONOMY_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.SLOT_SIZE_MAP_REPOSITORY;
import static com.inmobi.adserve.channels.server.ChannelServerStringLiterals.WAP_SITE_UAC_REPOSITORY;

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
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.server.ChannelServer;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.util.ConfigurationLoader;
import com.inmobi.phoenix.exception.RepositoryException;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;


@Singleton
@Path("/repoRefresh")
public class ServletRepoRefresh implements Servlet {
    private static final Logger LOG = LoggerFactory.getLogger(ServletRepoRefresh.class);

    private static final String LAST_UPDATE = "'${last_update}'";
    private static final String REPLACE_STRING = "now() -interval '1 MINUTE'";

    @Override
    public void handleRequest(final HttpRequestHandler hrh, final QueryStringDecoder queryStringDecoder,
            final Channel serverChannel) throws Exception {

        final Map<String, List<String>> params = queryStringDecoder.parameters();
        final String requestParam = params.get("args").toString();
        final JSONArray jsonArray = new JSONArray(requestParam);
        final JSONObject jObject = jsonArray.getJSONObject(0);
        LOG.debug("requestParam {} jObject {}", requestParam, jObject);

        final String repoName = jObject.get("repoName").toString();
        final String dbHost = jObject.get("DBHost").toString();
        final String dbPort = jObject.get("DBPort").toString();
        final String dbName = jObject.get("DBSnapshot").toString();
        final String dbUser = jObject.get("DBUser").toString();
        final String dbPassword = jObject.get("DBPassword").toString();
        LOG.debug("RepoName is {}", repoName);

        final String connectionString = "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName;
        Connection con = null;
        Statement statement = null;
        ResultSet resultSet;
        Boolean foundMatch = true;

        try {
            final String configFile = ChannelServer.getConfigFile();
            final ConfigurationLoader config = ConfigurationLoader.getInstance(configFile);
            con = DriverManager.getConnection(connectionString, dbUser, dbPassword);
            statement = con.createStatement();

            if (repoName.equalsIgnoreCase(CHANNEL_ADGROUP_REPOSITORY)) {
                final String query =
                        config.getCacheConfiguration().subset(CHANNEL_ADGROUP_REPOSITORY).getString(QUERY)
                                .replace(LAST_UPDATE, REPLACE_STRING);
                resultSet = statement.executeQuery(query);
                CasConfigUtil.repositoryHelper.getChannelAdGroupRepository().newUpdateFromResultSetToOptimizeUpdate(
                        resultSet);
            } else if (repoName.equalsIgnoreCase(CHANNEL_REPOSITORY)) {
                final String query =
                        config.getCacheConfiguration().subset(CHANNEL_REPOSITORY).getString(QUERY)
                                .replace(LAST_UPDATE, REPLACE_STRING);
                resultSet = statement.executeQuery(query);
                CasConfigUtil.repositoryHelper.getChannelRepository().newUpdateFromResultSetToOptimizeUpdate(resultSet);
            } else if (repoName.equalsIgnoreCase(CHANNEL_FEEDBACK_REPOSITORY)) {
                final String query =
                        config.getCacheConfiguration().subset(CHANNEL_FEEDBACK_REPOSITORY).getString(QUERY)
                                .replace(LAST_UPDATE, REPLACE_STRING);
                resultSet = statement.executeQuery(query);
                CasConfigUtil.repositoryHelper.getChannelFeedbackRepository().newUpdateFromResultSetToOptimizeUpdate(
                        resultSet);
            } else if (repoName.equalsIgnoreCase(CHANNEL_SEGMENT_FEEDBACK_REPOSITORY)) {
                final String query =
                        config.getCacheConfiguration().subset(CHANNEL_SEGMENT_FEEDBACK_REPOSITORY).getString(QUERY)
                                .replace(LAST_UPDATE, REPLACE_STRING);
                resultSet = statement.executeQuery(query);
                CasConfigUtil.repositoryHelper.getChannelSegmentFeedbackRepository()
                        .newUpdateFromResultSetToOptimizeUpdate(resultSet);
            } else if (repoName.equalsIgnoreCase(SITE_METADATA_REPOSITORY)) {
                final String query =
                        config.getCacheConfiguration().subset(SITE_METADATA_REPOSITORY).getString(QUERY)
                                .replace(LAST_UPDATE, REPLACE_STRING);
                resultSet = statement.executeQuery(query);
                CasConfigUtil.repositoryHelper.getSiteMetaDataRepository().newUpdateFromResultSetToOptimizeUpdate(
                        resultSet);
            } else if (repoName.equalsIgnoreCase(SITE_TAXONOMY_REPOSITORY)) {
                final String query =
                        config.getCacheConfiguration().subset(SITE_TAXONOMY_REPOSITORY).getString(QUERY)
                                .replace(LAST_UPDATE, REPLACE_STRING);
                resultSet = statement.executeQuery(query);
                CasConfigUtil.repositoryHelper.getSiteTaxonomyRepository().newUpdateFromResultSetToOptimizeUpdate(
                        resultSet);
            } else if (repoName.equalsIgnoreCase(PRICING_ENGINE_REPOSITORY)) {
                final String query =
                        config.getCacheConfiguration().subset(PRICING_ENGINE_REPOSITORY).getString(QUERY)
                                .replace(LAST_UPDATE, REPLACE_STRING);
                resultSet = statement.executeQuery(query);
                CasConfigUtil.repositoryHelper.getPricingEngineRepository().newUpdateFromResultSetToOptimizeUpdate(
                        resultSet);
            } else if (repoName.equalsIgnoreCase(SITE_FILTER_REPOSITORY)) {
                final String query =
                        config.getCacheConfiguration().subset(SITE_FILTER_REPOSITORY).getString(QUERY)
                                .replace(LAST_UPDATE, REPLACE_STRING);
                resultSet = statement.executeQuery(query);
                CasConfigUtil.repositoryHelper.getSiteFilterRepository().newUpdateFromResultSetToOptimizeUpdate(
                        resultSet);
            } else if (repoName.equalsIgnoreCase(SITE_ECPM_REPOSITORY)) {
                final String query =
                        config.getCacheConfiguration().subset(SITE_ECPM_REPOSITORY).getString(QUERY)
                                .replace(LAST_UPDATE, REPLACE_STRING);
                resultSet = statement.executeQuery(query);
                CasConfigUtil.repositoryHelper.getSiteEcpmRepository()
                        .newUpdateFromResultSetToOptimizeUpdate(resultSet);
            } else if (repoName.equalsIgnoreCase(CURRENCY_CONVERSION_REPOSITORY)) {
                final String query =
                        config.getCacheConfiguration().subset(CURRENCY_CONVERSION_REPOSITORY).getString(QUERY)
                                .replace(LAST_UPDATE, REPLACE_STRING);
                resultSet = statement.executeQuery(query);
                CasConfigUtil.repositoryHelper.getCurrencyConversionRepository()
                        .newUpdateFromResultSetToOptimizeUpdate(resultSet);
            } else if (repoName.equalsIgnoreCase(WAP_SITE_UAC_REPOSITORY)) {
                final String query =
                        config.getCacheConfiguration().subset(WAP_SITE_UAC_REPOSITORY).getString(QUERY)
                                .replace(LAST_UPDATE, REPLACE_STRING);
                resultSet = statement.executeQuery(query);
                CasConfigUtil.repositoryHelper.getWapSiteUACRepository().newUpdateFromResultSetToOptimizeUpdate(
                        resultSet);
            } else if (repoName.equalsIgnoreCase(IX_ACCOUNT_MAP_REPOSITORY)) {
                final String query =
                        config.getCacheConfiguration().subset(IX_ACCOUNT_MAP_REPOSITORY).getString(QUERY)
                                .replace(LAST_UPDATE, REPLACE_STRING);
                resultSet = statement.executeQuery(query);
                CasConfigUtil.repositoryHelper.getIxAccountMapRepository().newUpdateFromResultSetToOptimizeUpdate(
                        resultSet);
            } else if (repoName.equalsIgnoreCase(CREATIVE_REPOSITORY)) {
                final String query =
                        config.getCacheConfiguration().subset(CREATIVE_REPOSITORY).getString(QUERY)
                                .replace(LAST_UPDATE, REPLACE_STRING);
                resultSet = statement.executeQuery(query);
                CasConfigUtil.repositoryHelper.getCreativeRepository()
                        .newUpdateFromResultSetToOptimizeUpdate(resultSet);
            } else if (repoName.equalsIgnoreCase(NATIVE_AD_TEMPLATE_REPOSITORY)) {
                final String query =
                        config.getCacheConfiguration().subset(NATIVE_AD_TEMPLATE_REPOSITORY).getString(QUERY)
                                .replace(LAST_UPDATE, REPLACE_STRING);
                resultSet = statement.executeQuery(query);
                CasConfigUtil.repositoryHelper.getNativeAdTemplateRepository().newUpdateFromResultSetToOptimizeUpdate(
                        resultSet);
            } else if (repoName.equalsIgnoreCase(GEO_ZIP_REPOSITORY)) {
                final String query =
                        config.getCacheConfiguration().subset(GEO_ZIP_REPOSITORY).getString(QUERY)
                                .replace(LAST_UPDATE, REPLACE_STRING);
                resultSet = statement.executeQuery(query);
                CasConfigUtil.repositoryHelper.getGeoZipRepository().newUpdateFromResultSetToOptimizeUpdate(resultSet);
            } else if (repoName.equalsIgnoreCase(CAU_METADATA_REPOSITORY)) {
                final String query =
                        config.getCacheConfiguration().subset(CAU_METADATA_REPOSITORY).getString(QUERY)
                                .replace(LAST_UPDATE, REPLACE_STRING);
                resultSet = statement.executeQuery(query);
                CasConfigUtil.repositoryHelper.getCauMetaDataRepository().newUpdateFromResultSetToOptimizeUpdate(
                        resultSet);
            } else if (repoName.equalsIgnoreCase(SLOT_SIZE_MAP_REPOSITORY)) {
                final String query =
                        config.getCacheConfiguration().subset(SLOT_SIZE_MAP_REPOSITORY).getString(QUERY)
                                .replace(LAST_UPDATE, REPLACE_STRING);
                resultSet = statement.executeQuery(query);
                CasConfigUtil.repositoryHelper.getSlotSizeMapRepository().newUpdateFromResultSetToOptimizeUpdate(
                        resultSet);
            } else if (repoName.equalsIgnoreCase(GEO_REGION_FENCE_MAP_REPOSITORY)) {
                final String query =
                        config.getCacheConfiguration().subset(GEO_REGION_FENCE_MAP_REPOSITORY).getString(QUERY)
                                .replace(LAST_UPDATE, REPLACE_STRING);
                resultSet = statement.executeQuery(query);
                CasConfigUtil.repositoryHelper.getGeoRegionFenceMapRepository().newUpdateFromResultSetToOptimizeUpdate(
                        resultSet);
            } else if (repoName.equalsIgnoreCase(CCID_MAP_REPOSITORY)) {
                final String query =
                        config.getCacheConfiguration().subset(CCID_MAP_REPOSITORY).getString(QUERY)
                                .replace(LAST_UPDATE, REPLACE_STRING);
                resultSet = statement.executeQuery(query);
                CasConfigUtil.repositoryHelper.getCcidMapRepository().newUpdateFromResultSetToOptimizeUpdate(resultSet);
            } else if (repoName.equalsIgnoreCase(IX_BLOCKLIST_REPOSITORY)) {
                final String query =
                        config.getCacheConfiguration().subset(IX_BLOCKLIST_REPOSITORY).getString(QUERY)
                                .replace(LAST_UPDATE, REPLACE_STRING);
                resultSet = statement.executeQuery(query);
                CasConfigUtil.repositoryHelper.getIxBlocklistRepository().newUpdateFromResultSetToOptimizeUpdate(
                        resultSet);
            } else if (repoName.equalsIgnoreCase(SDK_VIEWABILITY_ELIGIBILITY_REPOSITORY)) {
                final String query =
                        config.getCacheConfiguration().subset(SDK_VIEWABILITY_ELIGIBILITY_REPOSITORY).getString(QUERY)
                                .replace(LAST_UPDATE, REPLACE_STRING);
                resultSet = statement.executeQuery(query);
                CasConfigUtil.repositoryHelper.getSdkViewabilityEligibilityRepository()
                    .newUpdateFromResultSetToOptimizeUpdate(resultSet);
            } else {
                // RepoName could not be matched
                LOG.debug("RepoName: {} could not be matched", repoName);
                hrh.responseSender.sendResponse("NOTOK RepoName could not be matched", serverChannel);
                foundMatch = false;
            }

            if (foundMatch) {
                LOG.debug("Successfully updated {}", repoName);
                hrh.responseSender.sendResponse("OK", serverChannel);
            }
        } catch (final SQLException e1) {
            LOG.info("error is {}", e1);
            hrh.responseSender.sendResponse("NOTOK", serverChannel);
        } catch (final RepositoryException e2) {
            LOG.info("error is {}", e2);
            hrh.responseSender.sendResponse("NOTOK", serverChannel);
        } finally {
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
