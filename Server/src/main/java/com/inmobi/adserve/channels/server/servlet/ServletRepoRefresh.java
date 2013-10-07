package com.inmobi.adserve.channels.server.servlet;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.ServletHandler;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.json.JSONArray;
import org.json.JSONObject;

import com.inmobi.adserve.channels.server.ChannelServerStringLiterals;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.util.ConfigurationLoader;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.phoenix.exception.RepositoryException;

public class ServletRepoRefresh implements Servlet {
  private static final String LAST_UPDATE = "'${last_update}'";
  private static final String REPLACE_STRING = "now() -interval '1 MINUTE'";

  @Override
  public void handleRequest(HttpRequestHandler hrh, QueryStringDecoder queryStringDecoder, MessageEvent e,
      DebugLogger logger) throws Exception {
    Map<String, List<String>> params = queryStringDecoder.getParameters();
    String requestParam = params.get("args").toString();
    JSONArray jsonArray = new JSONArray(requestParam);
    JSONObject jObject = jsonArray.getJSONObject(0);
    hrh.logger.debug("requestParam", requestParam, "jObject", jObject);
    String repoName = jObject.get("repoName").toString();
    hrh.logger.debug("RepoName is", repoName);

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
      ConfigurationLoader config = ConfigurationLoader.getInstance("/opt/mkhoj/conf/cas/channel-server.properties");
      con = DriverManager.getConnection(connectionString, dbUser, dbPassword);
      statement = con.createStatement();
      if(repoName.equalsIgnoreCase(ChannelServerStringLiterals.CHANNEL_ADGROUP_REPOSITORY)) {
        final String query = config.cacheConfiguration().subset(ChannelServerStringLiterals.CHANNEL_ADGROUP_REPOSITORY)
            .getString(ChannelServerStringLiterals.QUERY).replace(LAST_UPDATE, REPLACE_STRING);
        resultSet = statement.executeQuery(query);
        ServletHandler.repositoryHelper.getChannelAdGroupRepository().newUpdateFromResultSetToOptimizeUpdate(resultSet);
      } else if(repoName.equalsIgnoreCase(ChannelServerStringLiterals.CHANNEL_REPOSITORY)) {
        final String query = config.cacheConfiguration().subset(ChannelServerStringLiterals.CHANNEL_REPOSITORY)
            .getString(ChannelServerStringLiterals.QUERY).replace(LAST_UPDATE, REPLACE_STRING);
        resultSet = statement.executeQuery(query);
        ServletHandler.repositoryHelper.getChannelRepository().newUpdateFromResultSetToOptimizeUpdate(resultSet);
      } else if(repoName.equalsIgnoreCase(ChannelServerStringLiterals.CHANNEL_FEEDBACK_REPOSITORY)) {
        final String query = config.cacheConfiguration().subset(ChannelServerStringLiterals.CHANNEL_FEEDBACK_REPOSITORY)
            .getString(ChannelServerStringLiterals.QUERY).replace(LAST_UPDATE, REPLACE_STRING);
        resultSet = statement.executeQuery(query);
        ServletHandler.repositoryHelper.getChannelFeedbackRepository()
            .newUpdateFromResultSetToOptimizeUpdate(resultSet);
      } else if(repoName.equalsIgnoreCase(ChannelServerStringLiterals.CHANNEL_SEGMENT_FEEDBACK_REPOSITORY)) {
        final String query = config.cacheConfiguration().subset(ChannelServerStringLiterals.CHANNEL_SEGMENT_FEEDBACK_REPOSITORY)
            .getString(ChannelServerStringLiterals.QUERY).replace(LAST_UPDATE, REPLACE_STRING);
        resultSet = statement.executeQuery(query);
        ServletHandler.repositoryHelper.getChannelSegmentFeedbackRepository().newUpdateFromResultSetToOptimizeUpdate(
            resultSet);
      } else if(repoName.equalsIgnoreCase(ChannelServerStringLiterals.SITE_METADATA_REPOSITORY)) {
        final String query = config.cacheConfiguration().subset(ChannelServerStringLiterals.SITE_METADATA_REPOSITORY)
            .getString(ChannelServerStringLiterals.QUERY).replace(LAST_UPDATE, REPLACE_STRING);
        resultSet = statement.executeQuery(query);
        ServletHandler.repositoryHelper.getSiteMetaDataRepository().newUpdateFromResultSetToOptimizeUpdate(resultSet);
      } else if(repoName.equalsIgnoreCase(ChannelServerStringLiterals.SITE_TAXONOMY_REPOSITORY)) {
        final String query = config.cacheConfiguration().subset(ChannelServerStringLiterals.SITE_TAXONOMY_REPOSITORY)
            .getString(ChannelServerStringLiterals.QUERY).replace(LAST_UPDATE, REPLACE_STRING);
        resultSet = statement.executeQuery(query);
        ServletHandler.repositoryHelper.getSiteTaxonomyRepository().newUpdateFromResultSetToOptimizeUpdate(resultSet);
      }
      hrh.logger.debug("Successfully updated", repoName);
      hrh.responseSender.sendResponse("OK", e);
    } catch (SQLException e1) {
      hrh.logger.info("error is", e1.getMessage());
      hrh.responseSender.sendResponse("NOTOK", e);
    } catch (RepositoryException e2) {
      hrh.logger.info("error is", e2.getMessage());
      hrh.responseSender.sendResponse("NOTOK", e);
    } finally {
      if(null != statement) {
        statement.close();
      }
      if(null != con) {
        con.close();
      }
    }
  }

  @Override
  public String getName() {
    return "RepoRefresh";
  }

}
