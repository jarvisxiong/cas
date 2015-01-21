package com.inmobi.adserve.channels.api;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.google.inject.Singleton;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.Utils.ExceptionBlock;

@Singleton
public class IPRepository {

    final private Map<String, String> map;
    private Timer updateTimer;
    
    private static final long REFRESH_TIME = 60 * 1000L;
    
    private static final String NAME = IPRepository.class.getSimpleName();
    
    private static final Logger LOG = LoggerFactory.getLogger(IPRepository.class);
    
    public IPRepository(){
        
        this.map = new ConcurrentHashMap<String, String>();
        updateTimer = new Timer("IPRepository-Update-TimerTask");
        IPRepositoryTimerTask ipRepositoryTimerTask = new IPRepositoryTimerTask();
        updateTimer.schedule(ipRepositoryTimerTask, REFRESH_TIME, REFRESH_TIME);
        
    }
    
    public Timer getUpdateTimer() {
        return updateTimer;
    }

    public String getIPAddress(String host, Marker traceMarker){
        LOG.debug(traceMarker, "Doing lookup for " + host);
        
        URI uri = null;
        try {
            uri = new URI(host);
        } catch (URISyntaxException e) {
            InspectorStats.incrementStatCount(NAME, InspectorStrings.URI_SYNTAX_EXCEPTION);
            LOG.error(traceMarker, "URISyntaxException " + ExceptionBlock.getStackTrace(e), this.getClass().getSimpleName());
            return null;
        } catch(Exception ex){
            InspectorStats.incrementStatCount(NAME, InspectorStrings.URI_SYNTAX_EXCEPTION);
            LOG.error(traceMarker, "Exception " + ExceptionBlock.getStackTrace(ex), this.getClass().getSimpleName());
            return null;
        }
        String key = uri.getHost();
        
        if(!map.containsKey(key)){
            setIPAddr(key, traceMarker);
        }
        String value = map.get(key);
        host = host.replaceFirst(key, value);
        LOG.debug(traceMarker, "Done lookup and the resolution is " + host);
        return host;
    }
    
    private void setIPAddr(String key, Marker traceMarker){
        try {
            InetAddress addr = InetAddress.getByName(key);
            String value = addr.getHostAddress();
            map.put(key, value);
            LOG.debug(traceMarker, "key is {} and the value is {}", key, value);
        } catch (UnknownHostException e) {
            InspectorStats.incrementStatCount(NAME, InspectorStrings.UNKNOWN_HOST_EXCEPTION);
            LOG.error(traceMarker, "UnknownHostException " + ExceptionBlock.getStackTrace(e), this.getClass().getSimpleName());
            if(!map.containsKey(key)){
                map.put(key, key);
            }
        } catch(Exception ex){
            InspectorStats.incrementStatCount(NAME, InspectorStrings.UNKNOWN_HOST_EXCEPTION);
            LOG.error(traceMarker, "Exception " + ExceptionBlock.getStackTrace(ex), this.getClass().getSimpleName());
            if(!map.containsKey(key)){
                map.put(key, key);
            }
        } 
    }

    private final class IPRepositoryTimerTask extends TimerTask{
        @Override
        public void run() {
            final Iterator<String> keys = map.keySet().iterator();
            while (keys.hasNext()) {
                final String key = keys.next();
                setIPAddr(key, null);
            }
        }
    }
}
