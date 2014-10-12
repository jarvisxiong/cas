package com.inmobi.adserve.channels.server.servlet;

import com.google.inject.Singleton;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.api.Servlet;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


@Path("/logParser")
@Singleton
public class ServletLogParser implements Servlet {
    private static final Logger LOG = LoggerFactory.getLogger(ServletLogParser.class);

    @Override
    public void handleRequest(final HttpRequestHandler hrh, final QueryStringDecoder queryStringDecoder,
            final Channel serverChannel) throws Exception {
        Map<String, List<String>> params = queryStringDecoder.parameters();
        HttpRequest request = hrh.getHttpRequest();
        String targetStrings = "";
        String logFilePath = "";

        // Handle POST request
        if (request.getMethod() == HttpMethod.POST) {
            @SuppressWarnings("deprecation")
            String jObject = URLDecoder.decode(((FullHttpRequest) request).content().toString(CharsetUtil.UTF_8))
                    .toString();
            String[] array = jObject.split("&");
            targetStrings = array[0].split("=")[1];
            logFilePath = array[1].split("=")[1];
        }
        else {
            // Handle GET request
            if (!params.isEmpty()) {
                for (Entry<String, List<String>> p : params.entrySet()) {
                    String key = p.getKey();
                    List<String> vals = p.getValue();
                    if (key.equalsIgnoreCase("search")) {
                        targetStrings = vals.get(0);
                    }
                    else if (key.equalsIgnoreCase("logFilePath")) {
                        logFilePath = vals.get(0);
                    }
                }
            }
        }
        if (logFilePath == null) {
            logFilePath = "/opt/mkhoj/logs/cas/debug/";
        }

        ProcessBuilder pb = new ProcessBuilder(CasConfigUtil.getServerConfig().getString("logParserScript"), "-t",
                targetStrings, "-l", logFilePath);
        Process process = pb.start();
        int exitStatus = process.waitFor();
        if (exitStatus == 0) {
            hrh.responseSender.sendResponse("PASS", serverChannel);
        }
        else {
            hrh.responseSender.sendResponse("FAIL", serverChannel);
        }
    }

    @Override
    public String getName() {
        return "LogParser";
    }

}
