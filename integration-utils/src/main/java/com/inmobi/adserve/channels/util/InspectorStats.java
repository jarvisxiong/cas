package com.inmobi.adserve.channels.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.reporting.GraphiteReporter;


public class InspectorStats {

    private static Map<String, ConcurrentHashMap<String, ConcurrentHashMap<String, AtomicLong>>> ingrapherCounterStats = new ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, AtomicLong>>>();
    private static Map<String, ConcurrentHashMap<String, ConcurrentHashMap<String, Counter>>> yammerCounterStats = new ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, Counter>>>();
    private static Map<String, ConcurrentHashMap<String, Histogram>> yammerTimerStats = new ConcurrentHashMap<String, ConcurrentHashMap<String, Histogram>>();
    
    private static final String STATS = "stats";
    private static final String WORK_FLOW = "WorkFlow";
    
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

    public static void incrementStatCount(final String parameter, final long value) {
        incrementStatCount(WORK_FLOW, parameter, value);
    }

    public static void incrementStatCount(final String parameter) {
        incrementStatCount(WORK_FLOW, parameter, 1L);
    }

    public static void incrementStatCount(final String key, final String parameter) {
        incrementStatCount(key, parameter, 1L);
    }

    public static void incrementStatCount(final String key, final String parameter, final long value) {
        if (ingrapherCounterStats.get(key) == null) {
        	synchronized (parameter) {
        		if (ingrapherCounterStats.get(key) == null) {
        			ingrapherCounterStats.put(key, new ConcurrentHashMap<String, ConcurrentHashMap<String, AtomicLong>>());	
        		}
                
			}
        }
        
        if (ingrapherCounterStats.get(key).get(STATS) == null) {
        	synchronized (parameter) {
        		if (ingrapherCounterStats.get(key).get(STATS) == null) {
        			ingrapherCounterStats.get(key).put(STATS, new ConcurrentHashMap<String, AtomicLong>());
        		}
                
        	}
        }
        
        if (ingrapherCounterStats.get(key).get(STATS).get(parameter) == null) {
        	synchronized (parameter) {
        		if (ingrapherCounterStats.get(key).get(STATS).get(parameter) == null) {
        			ingrapherCounterStats.get(key).get(STATS).put(parameter, new AtomicLong(0L));
        		}
                
        	}
        }
        ingrapherCounterStats.get(key).get(STATS).get(parameter).getAndAdd(value);
        incrementYammerCount(key, parameter, value);
    }
    
    public static void incrementYammerCount(final String key, final String parameter, final long value) {
    	String fullKey = key + "." + parameter;
        if (yammerCounterStats.get(key) == null) {
        	synchronized (parameter) {
        		if (yammerCounterStats.get(key) == null) {
                    yammerCounterStats.put(key, new ConcurrentHashMap<String, ConcurrentHashMap<String, Counter>>());
                }
        	}
        }
        
        if (yammerCounterStats.get(key).get(STATS) == null) {
        	synchronized (parameter) {
        		if (yammerCounterStats.get(key).get(STATS) == null) {
        			yammerCounterStats.get(key).put(STATS, new ConcurrentHashMap<String, Counter>());
        		}
        	}
        }
        
        if (yammerCounterStats.get(key).get(STATS).get(parameter) == null) {
        	synchronized (parameter) {
        		if (yammerCounterStats.get(key).get(STATS).get(parameter) == null) {
        			yammerCounterStats.get(key).get(STATS).put(parameter, Metrics.newCounter(InspectorStats.class, fullKey));	
        		}
        	}
        }
        
        yammerCounterStats.get(key).get(STATS).get(parameter).inc(value);
    }

    public static void updateYammerTimerStats(final String dst, final String parameter, final long value) {
    	String fullKey = dst + "." + parameter;
        if (yammerTimerStats.get(dst) == null) {
        	synchronized (parameter) {
        		if (yammerTimerStats.get(dst) == null) {
        			yammerTimerStats.put(dst,  new ConcurrentHashMap<String, Histogram>());	
        		}
        	}
        }
        
        if (yammerTimerStats.get(dst).get(parameter) == null) {
        	synchronized (parameter) {
        		if (yammerTimerStats.get(dst).get(parameter) == null) {
        			yammerTimerStats.get(dst).put(parameter, Metrics.newHistogram(InspectorStats.class, fullKey));	
        		}
        	}
        }
        
        yammerTimerStats.get(dst).get(parameter).update(value);

    }
    
	public static void resetTimers(){
	    Iterator<Entry<String,ConcurrentHashMap<String,Histogram>>> dstIterator = yammerTimerStats.entrySet().iterator();
	    while (dstIterator.hasNext()) {
			Entry<String, ConcurrentHashMap<String, Histogram>> dstPair = (Map.Entry<String, ConcurrentHashMap<String, Histogram>>)dstIterator.next();
			Iterator<Entry<String,Histogram>> timerIterator = dstPair.getValue().entrySet().iterator();
			while(timerIterator.hasNext()){
				timerIterator.next().getValue().clear();
				timerIterator.remove();
			}
	        dstIterator.remove();
	    }
	}


    public static String getStats() {
        return (new JSONObject(ingrapherCounterStats).toString());
    }
    
    
    

}
