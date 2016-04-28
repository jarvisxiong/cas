package com.inmobi.adserve.channels.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.AerospikeException;
import com.aerospike.client.Key;
import com.aerospike.client.Record;
import com.aerospike.client.policy.ClientPolicy;
import com.aerospike.client.policy.Policy;
import com.inmobi.adserve.channels.entity.IMEIEntity;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.casthrift.DataCenter;
import com.inmobi.phoenix.exception.InitializationException;


/**
 *
 * @author ritwik.kumar
 *
 */
public class IMEIAerospikeRepository {
    private static final Logger LOG = LoggerFactory.getLogger(IMEIAerospikeRepository.class);
    protected AerospikeClient aerospikeClient;
    private Policy policy;
    private String namespace;
    private String set;
    private DataCenter colo;
    private Map<String, IMEIEntity> imeiCache;
    private ConcurrentHashMap<String, Boolean> currentlyUpdatingIds;
    private ExecutorService executorService;

    public void init(final Configuration config, final DataCenter colo) throws InitializationException {
        if (null == config || null == colo) {
            throw new InitializationException(
                    "null as a value for any input parameters is not acceptable by init method.");
        }
        if (DataCenter.HKG1 != colo) {
            LOG.info("IMEI Lookup only supportd in HKG1");
            return;
        }

        namespace = config.getString("imeiNamespae");
        set = config.getString("imeiSet");
        this.colo = colo;

        imeiCache = new ConcurrentHashMap<>();
        currentlyUpdatingIds = new ConcurrentHashMap<>();
        executorService = Executors.newCachedThreadPool();
        try {
            final ClientPolicy clientPolicy = new ClientPolicy();
            clientPolicy.maxThreads = 10;
            aerospikeClient = new AerospikeClient(clientPolicy, config.getString("host"), config.getInt("port"));
        } catch (final AerospikeException e) {
            LOG.error("Exception while creating Aerospike client: {}", e);
            throw new InitializationException("Could not instantiate Aerospike client");
        }
        policy = new Policy();
    }

    /**
     *
     * @param androidId
     * @return
     */
    public IMEIEntity query(String androidId) {
        if (DataCenter.HKG1 != colo || StringUtils.isBlank(androidId)) {
            return null;
        }
        androidId = StringUtils.lowerCase(androidId);
        final IMEIEntity imeiEntity = imeiCache.get(androidId);
        if (imeiEntity == null) {
            LOG.debug("Cache MISS : Querying aerospike for : androidId: {}", androidId);
            InspectorStats.incrementStatCount(InspectorStrings.IMEI_CACHE_MISS);
            asynchronouslyFetchFeedbackFromAerospike(androidId);
        } else {
            LOG.debug("Got IMEIEntity from cache for query: androidId: {}", androidId);
            InspectorStats.incrementStatCount(InspectorStrings.IMEI_CACHE_HIT);
        }
        return imeiEntity;
    }

    /**
     * Method that asynchronously fetches Entity from aerospike and puts it into the cache.
     */
    private void asynchronouslyFetchFeedbackFromAerospike(final String androidId) {
        final Boolean isSiteGettingUpdated = currentlyUpdatingIds.putIfAbsent(androidId, true);
        if (isSiteGettingUpdated == null) {
            // forking new thread to fetch feedback from Aerospike
            final CacheUpdater cacheUpdater = new CacheUpdater(androidId);
            final Runnable cacheUpdaterThread = new Thread(cacheUpdater);
            executorService.execute(cacheUpdaterThread);
        } else {
            LOG.debug("Not fetching feedback as IMEI is already updating");
        }
    }

    /**
     * Class that performs feedback fetch from aerospike and cache updating tasks asynchronously
     */
    class CacheUpdater implements Runnable {
        private final String androidId;

        protected CacheUpdater(final String androidId) {
            this.androidId = androidId;
        }

        @Override
        public void run() {
            LOG.debug("Getting feedback from the aerospike for query {}", androidId);
            getFeedbackFromAerospike();
        }

        /**
         * Method which gets feedback from aerospike in case of a cache miss and updates the cache
         */
        private void getFeedbackFromAerospike() {
            final Record record = getFromAerospike(androidId);
            if (null == record) {
                LOG.debug("Key not found in aerospike :{}", androidId);
                InspectorStats.incrementStatCount(InspectorStrings.IMEI_FAILED_TO_LOAD_FROM_AEROSPIKE);
                return;
            }
            LOG.debug("Key found in aerospike :{}", androidId);
            final IMEIEntity imeiEntity = processResultFromAerospike(record);
            if (imeiEntity != null) {
                imeiCache.put(androidId, imeiEntity);
            }
            currentlyUpdatingIds.remove(androidId);
        }

        /**
         * Method which makes a call to aerospike to load the complete site info
         */
        private Record getFromAerospike(final String androidId) {
            InspectorStats.incrementStatCount(InspectorStrings.IMEI_REQUESTS_TO_AEROSPIKE);
            long time = System.currentTimeMillis();
            Record record;
            try {
                final Key key = new Key(namespace, set, androidId);
                record = aerospikeClient.get(policy, key);
            } catch (final AerospikeException e) {
                LOG.error("Exception while retrieving record: {}", e);
                record = null;
            }
            time = System.currentTimeMillis() - time;
            InspectorStats.incrementStatCount(InspectorStrings.IMEI_LATENCY, time);
            return record;
        }

        /**
         * Processes feedback , extract global and colo data to get the IMEIEntity object
         *
         * @param record : object containing IMEI
         */
        private IMEIEntity processResultFromAerospike(final Record record) {
            final String imei = (String) record.getValue("imei");
            final IMEIEntity.Builder imeiBuilder = IMEIEntity.newBuilder();
            imeiBuilder.androidId(androidId);
            imeiBuilder.imei(imei);
            return imeiBuilder.build();
        }
    }

}
