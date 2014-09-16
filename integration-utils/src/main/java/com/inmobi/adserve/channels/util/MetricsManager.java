package com.inmobi.adserve.channels.util;

import com.yammer.metrics.reporting.GraphiteReporter;

import org.apache.commons.lang.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class MetricsManager {

	private static Map<Integer/* dst */, ConcurrentHashMap<String/* dst */, RealTimeStatsForCountryDst>> realTimeCountryDstStats = new ConcurrentHashMap<Integer, ConcurrentHashMap<String, RealTimeStatsForCountryDst>>();
	private static Map<Integer/* partner */, ConcurrentHashMap<String/* dst */, RealTimeStatsForPartnerRequests>> realTimeCountryPartnerStats = new ConcurrentHashMap<Integer, ConcurrentHashMap<String, RealTimeStatsForPartnerRequests>>();
	private static Map<String, RealTimeStatsForDstLatency> dstRealTimeStats = new HashMap<>();

	public static void init(String graphiteServer, int graphitePort, int graphiteInterval) {
		String metricProducer;
		try {
			metricProducer = metricsPrefix(InetAddress.getLocalHost().getHostName().toLowerCase());
		} catch (UnknownHostException e) {
			metricProducer = "unknown-host";
		}
		GraphiteReporter.enable(graphiteInterval, TimeUnit.MINUTES, graphiteServer, graphitePort, metricProducer);
	}

	private static String metricsPrefix(String hostname) {
		hostname = StringUtils.removeEnd(hostname, ".inmobi.com");
		return StringUtils.reverseDelimited(hostname, '.');
	}

	public static void updateLatency(String dst, long latency) {
		if (null == dstRealTimeStats.get(dst)) {
			RealTimeStatsForDstLatency realTimeStats = new RealTimeStatsForDstLatency(dst);
			dstRealTimeStats.put(dst, realTimeStats);
		}
		RealTimeStatsForDstLatency realTimeStats = dstRealTimeStats.get(dst);
		realTimeStats.getLatency().update(latency);
	}

	public static void updateClientTimerLatency(String dst, long latency) {
		if (null == dstRealTimeStats.get(dst)) {
			RealTimeStatsForDstLatency realTimeStats = new RealTimeStatsForDstLatency(dst);
			dstRealTimeStats.put(dst, realTimeStats);
		}
		RealTimeStatsForDstLatency realTimeStats = dstRealTimeStats.get(dst);
		realTimeStats.getClientTimerLatency().update(latency);
	}

	public static void updateIncomingRequestsStats(String dst, Long countryId, String countryName) {
		if (countryId == null) {
			return;
		}

		if (null == realTimeCountryDstStats.get(countryId.intValue())) {
			realTimeCountryDstStats.put(countryId.intValue(), new ConcurrentHashMap<String, RealTimeStatsForCountryDst>());
		}

		if (null == realTimeCountryDstStats.get(countryId.intValue()).get(dst)) {
			RealTimeStatsForCountryDst realTimeStats = new RealTimeStatsForCountryDst(countryName, dst);
			realTimeCountryDstStats.get(countryId.intValue()).put(dst, realTimeStats);
		}
		realTimeCountryDstStats.get(countryId.intValue()).get(dst).getIncomingRequests().inc();
	}

	public static void updatePartnerRequestStats(String partnerName, Long countryId, String countryName) {
		if (null == realTimeCountryPartnerStats.get(countryId)) {
			realTimeCountryPartnerStats.put(countryId.intValue(), new ConcurrentHashMap<String, RealTimeStatsForPartnerRequests>());
		}

		if (null == realTimeCountryPartnerStats.get(countryId.intValue()).get(partnerName)) {
			RealTimeStatsForPartnerRequests realTimeStats = new RealTimeStatsForPartnerRequests(countryName, partnerName);
			realTimeCountryPartnerStats.get(countryId.intValue()).put(partnerName, realTimeStats);
		}
		realTimeCountryPartnerStats.get(countryId.intValue()).get(partnerName).getPartnerRequests().inc();
	}

	public static void resetTimers(){
	    Iterator<Entry<String, RealTimeStatsForDstLatency>> it = dstRealTimeStats.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        System.out.println(pairs.getKey() + " = " + pairs.getValue());
	        ((RealTimeStatsForDstLatency)pairs.getValue()).clearTimers();
	        it.remove();
	    }
	}
}
