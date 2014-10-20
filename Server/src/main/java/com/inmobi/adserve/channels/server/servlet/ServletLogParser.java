package com.inmobi.adserve.channels.server.servlet;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.CharsetUtil;

import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.api.Servlet;


@Path("/logParser")
@Singleton
public class ServletLogParser implements Servlet {
	private static final Logger LOG = LoggerFactory.getLogger(ServletLogParser.class);

	@Override
	public void handleRequest(final HttpRequestHandler hrh, final QueryStringDecoder queryStringDecoder,
			final Channel serverChannel) throws Exception {
		final Map<String, List<String>> params = queryStringDecoder.parameters();
		final HttpRequest request = hrh.getHttpRequest();
		String targetStrings = "";
		String logFilePath = "";

		// Handle POST request
		if (request.getMethod() == HttpMethod.POST) {
			@SuppressWarnings("deprecation")
			final String jObject =
					URLDecoder.decode(((FullHttpRequest) request).content().toString(CharsetUtil.UTF_8)).toString();
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
			logFilePath = "/opt/mkhoj/logs/cas/debug/";
		}

		final ProcessBuilder pb =
				new ProcessBuilder(CasConfigUtil.getServerConfig().getString("logParserScript"), "-t", targetStrings,
						"-l", logFilePath);
		final Process process = pb.start();
		final int exitStatus = process.waitFor();
		if (exitStatus == 0) {
			hrh.responseSender.sendResponse("PASS", serverChannel);
		} else {
			hrh.responseSender.sendResponse("FAIL", serverChannel);
		}
	}

	@Override
	public String getName() {
		return "LogParser";
	}

}
