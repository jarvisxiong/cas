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

    private static final String CLASS_NAME = IPRepository.class.getSimpleName();

    private static final Logger LOG = LoggerFactory.getLogger(IPRepository.class);

    public IPRepository() {

        this.map = new ConcurrentHashMap<String, String>();
        updateTimer = new Timer("IPRepository-Update-TimerTask");
        IPRepositoryTimerTask ipRepositoryTimerTask = new IPRepositoryTimerTask();
        updateTimer.schedule(ipRepositoryTimerTask, REFRESH_TIME, REFRESH_TIME);

    }

    public Timer getUpdateTimer() {
        return updateTimer;
    }

    /**
     * Determines the IP address of a host, given the host's name. See also the method
     * {@link InetAddress#getByName(String)}
     * 
     * @param adapterName
     * @param host
     * @param traceMarker
     * @return IP address
     */
    public String getIPAddress(String adapterName, String host, Marker traceMarker) {
        if (host == null) {
            InspectorStats.incrementStatCount(adapterName, InspectorStrings.NULL_HOST_NAME);
            return null;
        }
        LOG.debug(traceMarker, "Doing lookup for {}", host);

        URI uri = null;
        try {
            uri = new URI(host);
        } catch (URISyntaxException e) {
            InspectorStats.incrementStatCount(adapterName, InspectorStrings.URI_SYNTAX_EXCEPTION);
            if (LOG.isErrorEnabled()) {
                LOG.error(traceMarker, "URISyntaxException " + ExceptionBlock.getStackTrace(e), this.getClass()
                        .getSimpleName());
            }
            return host;
        } catch (Exception ex) {
            InspectorStats.incrementStatCount(CLASS_NAME, ex.getClass().getSimpleName());
            if (LOG.isErrorEnabled()) {
                LOG.error(traceMarker, "Exception " + ExceptionBlock.getStackTrace(ex), this.getClass().getSimpleName());
            }
            return host;
        }
        String key = uri.getHost();

        if (!map.containsKey(key)) {
            setIPAddr(key, traceMarker);
        }
        String value = map.get(key);
        host = host.replaceFirst(key, value);
        LOG.debug(traceMarker, "Done lookup and the resolution is {}", host);
        return host;
    }

    private void setIPAddr(String key, Marker traceMarker) {
        if (key == null) {
            InspectorStats.incrementStatCount(CLASS_NAME, InspectorStrings.NULL_IP_ADDRESS);
            return;
        }
        try {
            InetAddress addr = InetAddress.getByName(key);
            String value = addr.getHostAddress();
            map.put(key, value);
            LOG.debug(traceMarker, "key is {} and the value is {}", key, value);
        } catch (UnknownHostException e) {
            InspectorStats.incrementStatCount(CLASS_NAME, InspectorStrings.UNKNOWN_HOST_EXCEPTION);
            if (LOG.isErrorEnabled()) {
                LOG.error(traceMarker, "UnknownHostException " + ExceptionBlock.getStackTrace(e), this.getClass()
                        .getSimpleName());
            }
        } catch (SecurityException ex) {
            InspectorStats.incrementStatCount(CLASS_NAME, ex.getClass().getSimpleName());
            if (LOG.isErrorEnabled()) {
                LOG.error(traceMarker, "UnknownHostException " + ExceptionBlock.getStackTrace(ex), this.getClass()
                        .getSimpleName());
            }
        } catch (Exception ex) {
            InspectorStats.incrementStatCount(CLASS_NAME, ex.getClass().getSimpleName());
            if (LOG.isErrorEnabled()) {
                LOG.error(traceMarker, "Exception " + ExceptionBlock.getStackTrace(ex), this.getClass().getSimpleName());
            }
        } finally {
            if (!map.containsKey(key)) {
                map.put(key, key);
            }
        }
    }

    private final class IPRepositoryTimerTask extends TimerTask {
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
