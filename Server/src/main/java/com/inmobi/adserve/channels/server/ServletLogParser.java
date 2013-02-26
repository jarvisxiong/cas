package com.inmobi.adserve.channels.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.jboss.netty.util.CharsetUtil;
import com.inmobi.adserve.channels.util.DebugLogger;

public class ServletLogParser implements Servlet {

  @Override
  public void handleRequest(HttpRequestHandler hrh, QueryStringDecoder queryStringDecoder, MessageEvent e,
      DebugLogger logger) throws Exception {
    Map<String, List<String>> params = queryStringDecoder.getParameters();
    HttpRequest request = (HttpRequest) e.getMessage();
    String targetStrings = "";
    String logFilePath = "";
    //Handle post request
    if(request.getMethod() == HttpMethod.POST) {
      String jObject = URLDecoder.decode(request.getContent().toString(CharsetUtil.UTF_8)).toString();
      String[] array = jObject.split("&");
      targetStrings = array[0].split("=")[1];
      logFilePath = array[1].split("=")[1];
    } else {//handle get request
      if(!params.isEmpty()) {
        for (Entry<String, List<String>> p : params.entrySet()) {
          String key = p.getKey();
          List<String> vals = p.getValue();
          if(key.equalsIgnoreCase("search"))
            targetStrings = vals.get(0);
          else if(key.equalsIgnoreCase("logFilePath"))
            logFilePath = vals.get(0);
        }
      }
    }
    hrh.logger.debug("targetStrings is", targetStrings);
    hrh.logger.debug("logFilePath is", logFilePath);
    if(logFilePath == null)
      logFilePath = "/opt/mkhoj/logs/cas/debug/";

    ProcessBuilder pb = new ProcessBuilder("/opt/bin/mkhoj/parser.sh", "-t", targetStrings, "-l", logFilePath);
    Process process = pb.start();
    BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
    String line = "";
    while ((line = br.readLine()) != null) {
      hrh.logger.debug(line);
    }
    int exitStatus = process.waitFor();

    if(exitStatus == 0)
      hrh.responseSender.sendResponse("PASS", e);
    else
      hrh.responseSender.sendResponse("FAIL", e);
  }

  @Override
  public String getName() {
    return "LogParser";
  }

}
