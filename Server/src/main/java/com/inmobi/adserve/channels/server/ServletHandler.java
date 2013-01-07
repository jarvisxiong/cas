package com.inmobi.adserve.channels.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.json.JSONException;
import org.json.JSONObject;

import com.inmobi.adserve.channels.api.ChannelsClientHandler;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;


public class ServletHandler {
  
  public static HashMap<String,ServletFactory> servletMap = new HashMap<String, ServletFactory>();
  
  public static void init() {
    
    servletMap.put("/stat", new ServletFactory() {      
      @Override
      public Servlet getServlet() {
        return new Servlet() {          
          @Override
          public void handleRequest(HttpRequestHandler hrh, QueryStringDecoder queryStringDecoder, MessageEvent e,
              DebugLogger logger) throws Exception {
            logger.debug("Inside stat servelet");
            hrh.responseSender.sendResponse(InspectorStats.getStats(BootstrapCreation.getMaxConnections(), BootstrapCreation.getDroppedConnections()), e);            
          }          
          @Override
          public String getName() {
            return "stat";
          }
        };
      }
    });
    
    servletMap.put("/mapsizes", new ServletFactory() {      
      @Override
      public Servlet getServlet() {
        return new Servlet() {          
          @Override
          public void handleRequest(HttpRequestHandler hrh, QueryStringDecoder queryStringDecoder, MessageEvent e, DebugLogger logger) throws Exception{
            JSONObject mapsizes = new JSONObject();
            mapsizes.put("ResponseMap", ChannelsClientHandler.responseMap.size());
            mapsizes.put("StatusMap", ChannelsClientHandler.responseMap.size());
            mapsizes.put("AdStatusMap", ChannelsClientHandler.responseMap.size());
            mapsizes.put("SampledAdvertiserLog", Logging.sampledAdvertiserLogNos.size());
            mapsizes.put("ActiveOutboundConnections", BootstrapCreation.getActiveOutboundConnections());
            mapsizes.put("MaxConnections", BootstrapCreation.getMaxConnections());
            mapsizes.put("DroppedConnections", BootstrapCreation.getDroppedConnections());
            hrh.responseSender.sendResponse(mapsizes.toString(), e);
          }
          @Override
          public String getName() {
            return "mapsizes";
          }
        };
      }
    });
    
    servletMap.put("/changerollout", new ServletFactory() {      
      @Override
      public Servlet getServlet() {
        return new Servlet() {          
          @Override
          public void handleRequest(HttpRequestHandler hrh, QueryStringDecoder queryStringDecoder, MessageEvent e, DebugLogger logger) throws Exception {
            try {
              List<String> rollout = (queryStringDecoder.getParameters().get("percentRollout"));
              HttpRequestHandler.setPercentRollout(Integer.parseInt(rollout.get(0)));
            } catch (NumberFormatException ex) {
              logger.error("invalid attempt to change rollout percentage " + ex);
              hrh.responseSender.sendResponse("INVALIDPERCENT", e);
            }
            InspectorStats.setWorkflowStats(InspectorStrings.percentRollout, Long.valueOf(HttpRequestHandler.getPercentRollout()));
            logger.debug("new roll out percentage is " + HttpRequestHandler.getPercentRollout());
            hrh.responseSender.sendResponse("OK", e);
          }
          @Override
          public String getName() {
            return "changerollout";
          }
        };
      }
    });
    
    servletMap.put("/lbstatus", new ServletFactory() {  
      @Override
      public Servlet getServlet() {
        return new Servlet() {          
          @Override
          public void handleRequest(HttpRequestHandler hrh, QueryStringDecoder queryStringDecoder, MessageEvent e,
              DebugLogger logger) throws Exception {
            hrh.sendLbStatus(e);
        }
          @Override
          public String getName() {
            return "lbstatus";
          }
      };
    }
  });
    
    servletMap.put("/disablelbstatus", new ServletFactory() {      
      @Override
      public Servlet getServlet() {
       return new Servlet() {        
        @Override
        public void handleRequest(HttpRequestHandler hrh, QueryStringDecoder queryStringDecoder, MessageEvent e,
            DebugLogger logger) throws Exception {
          HttpRequest request = (HttpRequest) e.getMessage();
          String host = getHost(request);
          if(host != null && host.startsWith("localhost")) {
            hrh.responseSender.sendResponse("OK", e);
            ServerStatusInfo.statusCode = 404;
            ServerStatusInfo.statusString = "NOT_OK";
            logger.debug("asked to shut down the server");
          } else {
            hrh.responseSender.sendResponse("NOT AUTHORIZED", e);
          }
        }
        @Override
        public String getName() {
          return "disablelbstatus";
        }
      };
      }
    });
    
    servletMap.put("/enablelbstatus", new ServletFactory() {      
      @Override
      public Servlet getServlet() {
        return new Servlet() {          
          @Override
          public void handleRequest(HttpRequestHandler hrh, QueryStringDecoder queryStringDecoder, MessageEvent e,
              DebugLogger logger) throws JSONException {
            HttpRequest request = (HttpRequest) e.getMessage();
            String host = getHost(request);
            if(host != null && host.startsWith("localhost")) {
              hrh.responseSender.sendResponse("OK", e);
              ServerStatusInfo.statusCode = 200;
              ServerStatusInfo.statusString = "OK";
              logger.debug("asked to shut down the server");
            } else {
              hrh.responseSender.sendResponse("NOT AUTHORIZED", e);
            }
          }
          @Override
          public String getName() {
            return "enaablelbstatus";
          }
        };
      }
    });
    
    servletMap.put("/configChange", new ServletFactory() {      
      @Override
      public Servlet getServlet() {
        return new Servlet() {
          @Override
          public void handleRequest(HttpRequestHandler hrh, QueryStringDecoder queryStringDecoder, MessageEvent e,
              DebugLogger logger) throws Exception{
            Map<String, List<String>> params = queryStringDecoder.getParameters();
            JSONObject jObject;
            try {
              jObject = RequestParser.extractParams(params, "update", logger);
            } catch (JSONException exeption) {
              jObject = new JSONObject();
              logger.debug("Encountered Json Error while creating json object inside HttpRequest Handler");
              hrh.setTerminationReason(HttpRequestHandler.jsonParsingError);
              InspectorStats.incrementStatCount(InspectorStrings.jsonParsingError, InspectorStrings.count);
            }
            hrh.changeConfig(e, jObject);            
          }
          @Override
          public String getName() {
            return "configchange";
          }
        };
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
  
}
