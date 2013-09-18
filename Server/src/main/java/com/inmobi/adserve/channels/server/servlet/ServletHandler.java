package com.inmobi.adserve.channels.server.servlet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.configuration.Configuration;
import org.jboss.netty.handler.codec.http.HttpRequest;

import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.util.ConfigurationLoader;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;

public class ServletHandler {

  public static final String jsonParsingError = "EJSON";
  public static final String processingError = "ESERVER";
  public static final String missingSiteId = "NOSITE";
  public static final String incompatibleSiteType = "ESITE";
  public static final String lowSdkVersion = "LSDK";
  public static final String MISSING_CATEGORY = "MISSINGCATEGORY";
  public static final String CLOSED_CHANNEL_EXCEPTION = "java.nio.channels.ClosedChannelException";
  public static final String CONNECTION_RESET_PEER = "java.io.IOException: Connection reset by peer";
  
  private static Configuration serverConfig;
  private static Configuration rtbConfig;
  private static Configuration adapterConfig;
  private static Configuration loggerConfig;
  private static Configuration log4jConfig;
  private static Configuration databaseConfig;
  
  public static RepositoryHelper repositoryHelper;
  public static int percentRollout;
  public static List<String> allowedSiteTypes;
  public static int rollCount = 0;
  public static final Random random = new Random();
  public static final Map<String, ServletFactory> servletMap = new HashMap<String, ServletFactory>();
  
  public static void init(ConfigurationLoader config, RepositoryHelper repositoryHelper) {
    ServletHandler.rtbConfig = config.rtbConfiguration();
    ServletHandler.loggerConfig = config.loggerConfiguration();
    ServletHandler.serverConfig = config.serverConfiguration();
    ServletHandler.adapterConfig = config.adapterConfiguration();
    ServletHandler.log4jConfig = config.log4jConfiguration();
    ServletHandler.databaseConfig = config.databaseConfiguration();
    ServletHandler.repositoryHelper = repositoryHelper;
    percentRollout = ServletHandler.serverConfig.getInt("percentRollout", 100);
    allowedSiteTypes = ServletHandler.serverConfig.getList("allowedSiteTypes");
    InspectorStats.setStats(InspectorStrings.percentRollout, Long.valueOf(percentRollout));

    servletMap.put("/stat", new ServletFactory() {
      @Override
      public Servlet getServlet() {
        return new ServletStat();
      }
    });

    servletMap.put("/mapsizes", new ServletFactory() {
      @Override
      public Servlet getServlet() {
        return new ServletMapsizes();
      }
    });

    servletMap.put("/changerollout", new ServletFactory() {
      @Override
      public Servlet getServlet() {
        return new ServletChangeRollout();
      }
    });

    servletMap.put("/lbstatus", new ServletFactory() {
      @Override
      public Servlet getServlet() {
        return new ServletLbStatus();
      }
    });

    servletMap.put("/disablelbstatus", new ServletFactory() {
      @Override
      public Servlet getServlet() {
        return new ServletDisableLbStatus();
      }
    });

    servletMap.put("/enablelbstatus", new ServletFactory() {
      @Override
      public Servlet getServlet() {
        return new ServletEnableLbStatus();
      }
    });

    servletMap.put("/configChange", new ServletFactory() {
      @Override
      public Servlet getServlet() {
        return new ServletChangeConfig();
      }
    });
    
    servletMap.put("/backfill", new ServletFactory() {      
      @Override
      public Servlet getServlet() {
       return new ServletBackFill();
      }
    });
    
    servletMap.put("/getsegments", new ServletFactory() {      
      @Override
      public Servlet getServlet() {
        return new ServletGetSegment();
      }
    });
    
    servletMap.put("/logParser", new ServletFactory() {
      @Override
      public Servlet getServlet() {
        return new ServletLogParser();
      }
    });
    
    servletMap.put("/repoRefresh", new ServletFactory() {
      @Override
      public Servlet getServlet() {
        return new ServletRepoRefresh();
      }
    });
    
    servletMap.put("/repostat", new ServletFactory() {
			@Override
			public Servlet getServlet() {
				return new ServletRepoStat();
			}
		});
    
    servletMap.put("/errorDetails", new ServletFactory() {
			@Override
			public Servlet getServlet() {
				return new ServletErrorDetails();
			}
		});

  }

  public static String getHost(HttpRequest request) {
    List<Map.Entry<String, String>> headers = request.getHeaders();
    String host = null;

    for (int index = 0; index < headers.size(); index++) {
      if(((String) ((Map.Entry<String, String>) (headers.get(index))).getKey()).equalsIgnoreCase("Host")) {
        host = (String) ((Map.Entry<String, String>) (headers.get(index))).getValue();
      }
    }
    return host;
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
  
  public static void setRtbConfig(Configuration rtbConfig) {
    ServletHandler.rtbConfig = rtbConfig;
  }
}