package com.inmobi.adserve.channels.server;

import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.json.JSONException;

import com.inmobi.adserve.channels.util.DebugLogger;


public interface Servlet {
  
  public void handleRequest(HttpRequestHandler hrh, QueryStringDecoder queryStringDecoder, MessageEvent e, DebugLogger logger) throws Exception;

}
