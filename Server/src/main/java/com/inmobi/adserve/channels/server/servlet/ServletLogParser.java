package com.inmobi.adserve.channels.server.servlet;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.Path;

import org.apache.commons.lang.StringUtils;

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


@Path("/logParser")
@Singleton
public class ServletLogParser implements Servlet {

    @Override
    public void handleRequest(final HttpRequestHandler hrh, final QueryStringDecoder queryStringDecoder,
            final Channel serverChannel) throws Exception {
        final Map<String, List<String>> params = queryStringDecoder.parameters();
        final HttpRequest request = hrh.getHttpRequest();
        String targetStrings = StringUtils.EMPTY;
        String logFilePath = StringUtils.EMPTY;

        // Handle POST request
        if (request.getMethod() == HttpMethod.POST) {
            @SuppressWarnings("deprecation")
            final String jObject = URLDecoder.decode(((FullHttpRequest) request).content().toString(CharsetUtil.UTF_8));
            final String[] array = jObject.split("&");
            targetStrings = array[0].split("=")[1];
            logFilePath = array[1].split("=")[1];
        } else {
            // Handle GET request
            if (!params.isEmpty()) {
                for (final Entry<String, List<String>> p : params.entrySet()) {
                    final String key = p.getKey();
                    final List<String> vals = p.getValue();
                    if ("search".equalsIgnoreCase(key)) {
                        targetStrings = vals.get(0);
                    } else if ("logFilePath".equalsIgnoreCase(key)) {
                        logFilePath = vals.get(0);
                    }
                }
            }
        }
        if (logFilePath == null) {
            logFilePath = "/opt/inmobi/cas/logs/debug/";
        }

        final ProcessBuilder pb =
                new ProcessBuilder(CasConfigUtil.getServerConfig().getString("logParserScript"), "-t", targetStrings,
                        "-l", logFilePath);
        final Process process = pb.start();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        StringBuilder response = new StringBuilder();
        while(StringUtils.isNotEmpty(line = bufferedReader.readLine())) {
            response.append(line + "\n");
        }
        bufferedReader.close();

        process.waitFor();
        hrh.responseSender.sendResponse(response.toString(), serverChannel);

    }

    @Override
    public String getName() {
        return "LogParser";
    }

}
