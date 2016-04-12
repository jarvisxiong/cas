package com.inmobi.adserve.channels.api;

import static com.inmobi.adserve.channels.util.InspectorStrings.NULL_HOST_NAME;
import static com.inmobi.adserve.channels.util.InspectorStrings.NULL_URI;

import java.net.InetAddress;
import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;
import com.inmobi.adserve.channels.util.InspectorStats;

/**
 * Local cache of Host to IP resolutions which get refreshed automatically. This cache is required to reduce load to DNS
 * server and remove the bottle neck of DNS lookup
 *
 * @author ritwik.kumar
 *
 */
@Singleton
public class IPRepository {
    private static final Logger LOG = LoggerFactory.getLogger(IPRepository.class);
    private static final long REFRESH_TIME = 60 * 1000L;
    private static final String CLASS_NAME = IPRepository.class.getSimpleName();
    private static final String DEFAULT_ADAPTER = "GetSegment";
    private final Map<String, String> ipRepoMap;
    private final Timer updateTimer;

    /**
     * 
     */
    public IPRepository() {
        ipRepoMap = new ConcurrentHashMap<String, String>();
        updateTimer = new Timer("IPRepository-Update-TimerTask");
        final IPRepositoryTimerTask ipRepositoryTimerTask = new IPRepositoryTimerTask();
        updateTimer.schedule(ipRepositoryTimerTask, REFRESH_TIME, REFRESH_TIME);
    }

    /**
     * 
     * @return
     */
    public Timer getUpdateTimer() {
        return updateTimer;
    }

    /**
     *
     * @param uri
     * @return - An URL where Host is replaced with an IP, If there is an issue in resolution URI as it was passed is
     *         returned
     */
    public String replaceHostWitIp(final String uri) {
        return getIPAddress(DEFAULT_ADAPTER, uri);
    }

    /**
     * Determines the IP address of a host, given the host's name. See also the method
     * {@link InetAddress#getByName(String)}
     *
     * @param adapterName - Used only for pushing stats
     * @param uri
     * @return An URL where Host is replaced with an IP, If there is an issue in resolution URI as it was passed is
     *         returned
     */
    public String getIPAddress(final String adapterName, String uri) {
        if (uri == null) {
            InspectorStats.incrementStatCount(CLASS_NAME, adapterName + "-" + NULL_URI);
            return null;
        }
        LOG.debug("Doing lookup for {}", uri);

        URI uriObj = null;
        try {
            uriObj = new URI(uri);
        } catch (final Exception ex) {
            InspectorStats.incrementStatCount(CLASS_NAME, adapterName + "-" + ex.getClass().getSimpleName());
            LOG.error("Error getting URI Object for ->" + uri, ex);
            return uri;
        }

        final String hostname = uriObj.getHost();
        if (!ipRepoMap.containsKey(hostname)) {
            setIPAddr(hostname);
        }
        final String ip = ipRepoMap.get(hostname);
        uri = uri.replaceFirst(hostname, ip);
        LOG.debug("Done lookup and the resolution is {}", uri);
        return uri;
    }

    /**
     *
     * @param key
     */
    private void setIPAddr(final String hostname) {
        if (hostname == null) {
            InspectorStats.incrementStatCount(CLASS_NAME, NULL_HOST_NAME);
            return;
        }
        try {
            final InetAddress addr = InetAddress.getByName(hostname);
            final String ip = addr.getHostAddress();
            ipRepoMap.put(hostname, ip);
            LOG.debug("host is {} and the ip is {}", hostname, ip);
        } catch (final Exception ex) {
            InspectorStats.incrementStatCount(CLASS_NAME, ex.getClass().getSimpleName());
            LOG.error("Error Inetaddress for hostname->" + hostname, ex);
        } finally {
            if (!ipRepoMap.containsKey(hostname)) {
                ipRepoMap.put(hostname, hostname);
            }
        }
    }


    private final class IPRepositoryTimerTask extends TimerTask {
        @Override
        public void run() {
            final Iterator<String> keys = ipRepoMap.keySet().iterator();
            while (keys.hasNext()) {
                final String key = keys.next();
                LOG.debug("Updating ->" + key);
                setIPAddr(key);
            }
        }
    }

}
