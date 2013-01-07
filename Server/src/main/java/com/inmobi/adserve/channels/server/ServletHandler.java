package com.inmobi.adserve.channels.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.netty.handler.codec.http.HttpRequest;


public class ServletHandler {
  
  public static HashMap<String,ServletFactory> servletMap = new HashMap<String, ServletFactory>();
  
  public static void init() {
    
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
