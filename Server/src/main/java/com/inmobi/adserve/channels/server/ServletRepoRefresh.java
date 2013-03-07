package com.inmobi.adserve.channels.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.json.JSONArray;
import org.json.JSONObject;

import com.inmobi.adserve.channels.util.ConfigurationLoader;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.phoenix.exception.RepositoryException;

public class ServletRepoRefresh implements Servlet {
  private static final String LASTUPDATE = "'${last_update}'";
  private static final String REPLACESTRING = "now() -interval '1 MINUTE'";

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
    ResultSet resultSet = null;
    try {
      ConfigurationLoader config = ConfigurationLoader.getInstance("/opt/mkhoj/conf/cas/channel-server.properties");
      con = DriverManager.getConnection(connectionString, dbUser, dbPassword);
      statement = con.createStatement();
      if(repoName.equalsIgnoreCase(StringLiterals.CHANNELADGROUPREPOSITORY)) {
        final String query = config.cacheConfiguration().subset(StringLiterals.CHANNELADGROUPREPOSITORY)
            .getString(StringLiterals.QUERY).replace(LASTUPDATE, REPLACESTRING);
        resultSet = statement.executeQuery(query);
        ServletHandler.repositoryHelper.getChannelAdGroupRepository().newUpdateFromResultSetToOptimizeUpdate(resultSet);
      } else if(repoName.equalsIgnoreCase(StringLiterals.CHANNELREPOSITORY)) {
        final String query = config.cacheConfiguration().subset(StringLiterals.CHANNELREPOSITORY)
            .getString(StringLiterals.QUERY).replace(LASTUPDATE, REPLACESTRING);
        resultSet = statement.executeQuery(query);
        ServletHandler.repositoryHelper.getChannelAdGroupRepository().newUpdateFromResultSetToOptimizeUpdate(resultSet);
      } else if(repoName.equalsIgnoreCase(StringLiterals.CHANNELFEEDBACKREPOSITORY)) {
        final String query = config.cacheConfiguration().subset(StringLiterals.CHANNELFEEDBACKREPOSITORY)
            .getString(StringLiterals.QUERY).replace(LASTUPDATE, REPLACESTRING);
        resultSet = statement.executeQuery(query);
        ServletHandler.repositoryHelper.getChannelFeedbackRepository()
            .newUpdateFromResultSetToOptimizeUpdate(resultSet);
      } else if(repoName.equalsIgnoreCase(StringLiterals.CHANNELSEGMENTFEEDBACKREPOSITORY)) {
        final String query = config.cacheConfiguration().subset(StringLiterals.CHANNELSEGMENTFEEDBACKREPOSITORY)
            .getString(StringLiterals.QUERY).replace(LASTUPDATE, REPLACESTRING);
        resultSet = statement.executeQuery(query);
        ServletHandler.repositoryHelper.getChannelSegmentFeedbackRepository().newUpdateFromResultSetToOptimizeUpdate(
            resultSet);
      } else if(repoName.equalsIgnoreCase(StringLiterals.SITEMETADATAREPOSITORY)) {
        final String query = config.cacheConfiguration().subset(StringLiterals.SITEMETADATAREPOSITORY)
            .getString(StringLiterals.QUERY).replace(LASTUPDATE, REPLACESTRING);
        resultSet = statement.executeQuery(query);
        ServletHandler.repositoryHelper.getSiteMetaDataRepository().newUpdateFromResultSetToOptimizeUpdate(resultSet);
      } else if(repoName.equalsIgnoreCase(StringLiterals.SITETAXONOMYREPOSITORY)) {
        final String query = config.cacheConfiguration().subset(StringLiterals.SITETAXONOMYREPOSITORY)
            .getString(StringLiterals.QUERY).replace(LASTUPDATE, REPLACESTRING);
        resultSet = statement.executeQuery(query);
        ServletHandler.repositoryHelper.getSiteTaxonomyRepository().newUpdateFromResultSetToOptimizeUpdate(resultSet);
      }
      hrh.logger.debug("Successfully updated", repoName);
      hrh.responseSender.sendResponse("OK", e);
    } catch (SQLException e1) {
      hrh.logger.error("error is", e1.getMessage());
      hrh.responseSender.sendResponse("NOTOK", e);
    } catch (RepositoryException e2) {
      hrh.logger.error("error is", e2.getMessage());
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
