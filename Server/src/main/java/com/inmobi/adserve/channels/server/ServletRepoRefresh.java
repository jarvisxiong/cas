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

  @Override
  public void handleRequest(HttpRequestHandler hrh, QueryStringDecoder queryStringDecoder, MessageEvent e,
      DebugLogger logger) throws Exception {
    Map<String, List<String>> params = queryStringDecoder.getParameters();
    String requestParam = params.get("args").toString();
    JSONArray jsonArray = new JSONArray(requestParam);
    JSONObject jObject = jsonArray.getJSONObject(0);
    hrh.logger.debug("requestParam",requestParam,"jObject",jObject);
    String repoName = jObject.get("repoName").toString();
    hrh.logger.debug("RepoName is", repoName);
    if(repoName == null) {
      hrh.responseSender.sendResponse("NOTOK", e);
      return;
    }

    try {
      String dbHost = jObject.get("DBHost").toString();
      String dbPort = jObject.get("DBPort").toString();
      String dbName = jObject.get("DBSnapshot").toString();
      String dbUser = jObject.get("DBUser").toString();
      String dbPassword = jObject.get("DBPassword").toString();
      String connectionString = "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName;
      try {
        Connection con;
        ConfigurationLoader config = ConfigurationLoader.getInstance("/opt/mkhoj/conf/cas/channel-server.properties");
        con = DriverManager.getConnection(connectionString, dbUser, dbPassword);
        Statement statement = con.createStatement();
        if(repoName.equalsIgnoreCase("ChannelAdGroupRepository")) {
          ResultSet resultSet = statement.executeQuery(config.cacheConfiguration().subset("ChannelAdGroupRepository")
              .getString("query").replace("'${last_update}'", "now() -interval '1 HOUR'"));
          ServletHandler.repositoryHelper.getChannelAdGroupRepository().newUpdateFromResultSetToOptimizeUpdate(
              resultSet);
         hrh.logger.debug("query is", config.cacheConfiguration().subset("ChannelAdGroupRepository")
              .getString("query").replace("'${last_update}'", "now() -interval '1 MINUTE'"));
          while (resultSet.next()) {
            hrh.logger.debug("Resultset is not null");
            for (int i = 1; i <= 15; i++) {
              if (i > 1) hrh.logger.debug(",  ");
              String columnValue = resultSet.getString(i);
              hrh.logger.debug(columnValue);
            }
            hrh.logger.debug(" ");
          }
        } else if(repoName.equalsIgnoreCase("ChannelRepository")) {
          ResultSet resultSet = statement.executeQuery(config.cacheConfiguration().subset("ChannelRepository")
              .getString("query").replace("'${last_update}'", "now() -interval '1 HOUR'"));
          ServletHandler.repositoryHelper.getChannelAdGroupRepository().newUpdateFromResultSetToOptimizeUpdate(
              resultSet);

        }
        con.close();
      } catch (SQLException e1) {
        hrh.logger.error("error is", e1.getMessage());
        e1.printStackTrace();
        hrh.responseSender.sendResponse("NOTOK", e);
      } catch (RepositoryException e2) {
        hrh.logger.error("error is", e2.getMessage());
        e2.printStackTrace();
        hrh.responseSender.sendResponse("NOTOK", e);
      }
    } catch (Exception e1) {
      hrh.logger.error("error is", e1.getMessage());
      e1.printStackTrace();
      hrh.responseSender.sendResponse("NOTOK", e);
    }
    hrh.logger.debug("Successfully updated repository");
    hrh.responseSender.sendResponse("OK", e);
  }

  @Override
  public String getName() {
    return "RepoRefresh";
  }

}
