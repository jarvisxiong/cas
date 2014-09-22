package com.inmobi.adserve.channels.repository;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.AerospikeException;
import com.aerospike.client.Key;
import com.aerospike.client.Record;
import com.aerospike.client.policy.ClientPolicy;
import com.aerospike.client.policy.Policy;
import com.inmobi.adserve.channels.entity.ChannelSegmentFeedbackEntity;
import com.inmobi.adserve.channels.entity.SegmentAdGroupFeedbackEntity;
import com.inmobi.adserve.channels.entity.SiteFeedbackEntity;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.casthrift.AdGroupFeedback;
import com.inmobi.casthrift.DataCenter;
import com.inmobi.casthrift.Feedback;
import com.inmobi.casthrift.SiteFeedback;
import org.apache.commons.configuration.Configuration;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import com.inmobi.phoenix.exception.InitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Assumptions:
// siteId is never null

public class SiteAerospikeFeedbackRepository {

    private AerospikeClient                                           aerospikeClient;
    private Policy                                                    policy;
    private String                                                    namespace;
    private String                                                    set;
    private DataCenter                                                colo;
    // Cache to store segment feedback entities loaded from aerospike.
    private ConcurrentHashMap<String/* siteId */, SiteFeedbackEntity> siteSegmentFeedbackCache;
    private ConcurrentHashMap<String, Boolean>                        currentlyUpdatingSites;
    private int                                                       refreshTime;
    private ExecutorService                                           executorService;
    private int                                                       feedbackTimeFrame;
    private int                                                       boostTimeFrame;
    private double                                                    defaultECPM;
    private static final Logger                                       LOG = LoggerFactory
                                                                                  .getLogger(SiteAerospikeFeedbackRepository.class);

    public void init(final Configuration config, final DataCenter colo) throws InitializationException{
        if(null == config || null == colo) {
            throw new InitializationException("null as a value for any input parameters is not acceptable by init method.");
        }

        this.namespace         = config.getString("namespace");
        this.set               = config.getString("set");
        this.refreshTime       = config.getInt("refreshTime");
        this.feedbackTimeFrame = config.getInt("feedbackTimeFrame", 15);
        this.boostTimeFrame    = config.getInt("boostTimeFrame", 3);
        this.defaultECPM       = config.getDouble("default.ecpm", 0.25);

        this.siteSegmentFeedbackCache = new ConcurrentHashMap<>();
        this.currentlyUpdatingSites = new ConcurrentHashMap<>();

        this.colo = colo;
        this.executorService = Executors.newCachedThreadPool();

        try {
            ClientPolicy clientPolicy = new ClientPolicy();
            clientPolicy.maxThreads = 10;
            
            this.aerospikeClient = new AerospikeClient(clientPolicy, config.getString("host"), config.getInt("port"));
        } catch (AerospikeException e) {
            LOG.error("Exception while creating Aerospike client: {}", e.getMessage());
            throw new InitializationException("Could not instantiate Aerospike client");
        }

        policy = new Policy();
    }

    /**
     * Method to get SegmentAdGroupFeedbackEntity for the request site and segment combination. Looks first in cache
     * with a configurable refresh time if hits within the refresh time , returns the hit entity otherwise makes a call
     * to aerospike to load the fresh data while returning the stale entity for the current request.
     *
     * @return : returns the entity matching for the site, segment and adgroup combination
     */
    public SegmentAdGroupFeedbackEntity query(final String siteId, final Integer segmentId) {
        SiteFeedbackEntity siteFeedbackEntity = siteSegmentFeedbackCache.get(siteId);
        if (siteFeedbackEntity != null) {
            LOG.debug("Got the siteFeedback entity from cache for query: siteId: {}, segmentId: {}", siteId, segmentId);
            if (System.currentTimeMillis() - siteFeedbackEntity.getLastUpdated() < refreshTime) {
                LOG.debug("siteFeedback entity is fresh for query: siteId: {}, segmentId: {}", siteId, segmentId);
                InspectorStats.incrementStatCount(InspectorStrings.siteFeedbackCacheHit);
                return siteFeedbackEntity.getSegmentAdGroupFeedbackMap() == null ? null : siteFeedbackEntity
                        .getSegmentAdGroupFeedbackMap().get(segmentId);
            }
            LOG.debug("siteFeedback entity is stale for query: siteId: {}, segmentId: {}", siteId, segmentId);
        }
        else {
            LOG.debug("siteFeedback not found for siteId: {}", siteId);
        }
        LOG.debug("Returning default/old siteFeedback entity and fetching new data from aerospike for siteId: {}",
                siteId);
        InspectorStats.incrementStatCount(InspectorStrings.siteFeedbackCacheMiss);
        asynchronouslyFetchFeedbackFromAerospike(siteId);
        siteFeedbackEntity = siteSegmentFeedbackCache.get(siteId);
        return siteFeedbackEntity == null ? null : (siteFeedbackEntity.getSegmentAdGroupFeedbackMap() == null ? null
                : siteFeedbackEntity.getSegmentAdGroupFeedbackMap().get(segmentId));
    }

    /**
     * Method that asynchronously fetches feedback from aerospike and puts it into the cache.
     */
    private void asynchronouslyFetchFeedbackFromAerospike(final String siteId) {
        Boolean isSiteGettingUpdated = this.currentlyUpdatingSites.putIfAbsent(siteId, true);
        if (isSiteGettingUpdated == null) {
            // forking new thread to fetch feedback from Aerospike
            CacheUpdater cacheUpdater = new CacheUpdater(siteId);
            Thread cacheUpdaterThread = new Thread(cacheUpdater);
            executorService.execute(cacheUpdaterThread);
        }
        else {
            LOG.debug("Not fetching feedback as site is already updating");
        }
    }

    /**
     * Class that performs feedback fetch from aerospike and cache updating tasks asynchronously
     */
    class CacheUpdater implements Runnable {
        private final String siteId;

        public CacheUpdater(final String siteId) {
            this.siteId = siteId;
        }

        @Override
        public void run() {
            LOG.debug("Getting feedback from the aerospike for query {}", siteId);
            getFeedbackFromAerospike(siteId);
        }

        /**
         * Method which gets feedback from aerospike in case of a cache miss and updates the cache
         */
        void getFeedbackFromAerospike(final String siteId) {
            // getting all data for the site
            Record record = getFromAerospike(siteId);
            if (null == record) {
                LOG.debug("Key not found in aerospike");
                InspectorStats.incrementStatCount(InspectorStrings.siteFeedbackFailedToLoadFromAerospike);
                return;
            }
            LOG.debug("Key found in aerospike");
            updateCache(processResultFromAerospike(record));
        }

        /**
         * Method which makes a call to aerospike to load the complete site info
         */
        Record getFromAerospike(final String site) {
            InspectorStats.incrementStatCount(InspectorStrings.siteFeedbackRequestsToAerospike);
            long time = System.currentTimeMillis();
            Record record;

            try {
                final Key key = new Key(namespace, set, site);
                record = aerospikeClient.get(policy, key);
            } catch (AerospikeException e) {
                LOG.error("Exception while retrieving record: {}", e.getMessage());
                record = null;
            }
            time = System.currentTimeMillis() - time;
            InspectorStats.incrementStatCount(InspectorStrings.siteFeedbackLatency, time);
            return record;
        }

        /**
         * Processes feedback , extract global and colo data to get the siteFeedbackEntity object
         * 
         * @param record
         *            : ClResult object containing the feedback
         */
        SiteFeedbackEntity processResultFromAerospike(final Record record) {
            if (null != record) {
                Map<Integer, SegmentAdGroupFeedbackEntity> segmentAdGroupFeedbackEntityMap = new HashMap<>();
                for (Map.Entry<String, Object> binValuePair : record.bins.entrySet()) {
                    String bin = binValuePair.getKey();
                    if (bin.startsWith(DataCenter.ALL.toString())) {
                        String segmentId = bin.split("\u0001")[1];
                        SiteFeedback globalFeedback = new SiteFeedback();
                        try {
                            TDeserializer tDeserializer = new TDeserializer(new TBinaryProtocol.Factory());
                            tDeserializer.deserialize(globalFeedback, (byte[]) binValuePair.getValue());
                        }
                        catch (TException exception) {
                            LOG.debug("Error in deserializing thrift for global feedback for segment {} {}", segmentId,
                                    exception);
                            globalFeedback = null;
                        }
                        bin = DataCenter.RCT.toString() + "\u0001" + segmentId;
                        SiteFeedback rctFeedback = new SiteFeedback();
                        try {
                            TDeserializer tDeserializer = new TDeserializer(new TBinaryProtocol.Factory());
                            Object byteArray = record.getValue(bin);
                            if (byteArray == null) {
                                throw new TException("No rct data");
                            }
                            tDeserializer.deserialize(rctFeedback, (byte[]) byteArray);
                        }
                        catch (TException exception) {
                            LOG.debug("Error in deserializing thrift for rct feedback for segment {} {}", segmentId,
                                    exception);
                            rctFeedback = null;
                        }
                        bin = colo.toString() + "\u0001" + segmentId;
                        SiteFeedback coloFeedback = new SiteFeedback();
                        try {
                            TDeserializer tDeserializer = new TDeserializer(new TBinaryProtocol.Factory());
                            Object byteArray = record.getValue(bin);
                            if (byteArray == null) {
                                throw new TException("No colo data");
                            }
                            tDeserializer.deserialize(coloFeedback, (byte[]) byteArray);
                        }
                        catch (TException exception) {
                            LOG.debug("Error in deserializing thrift for local feedback for segment {} {}", segmentId,
                                    exception);
                            coloFeedback = null;
                        }
                        SegmentAdGroupFeedbackEntity segmentAdGroupFeedbackEntity = buildSiteFeedbackEntity(
                                globalFeedback, rctFeedback, coloFeedback);
                        if (segmentAdGroupFeedbackEntity != null) {
                            segmentAdGroupFeedbackEntityMap.put(segmentAdGroupFeedbackEntity.getSegmentId(),
                                    segmentAdGroupFeedbackEntity);
                        }
                    }
                }
                SiteFeedbackEntity.Builder builder = SiteFeedbackEntity.newBuilder();
                builder.setLastUpdated(System.currentTimeMillis());
                builder.setSiteGuId(siteId);
                builder.setSegmentAdGroupFeedbackMap(segmentAdGroupFeedbackEntityMap);
                return builder.build();
            }
            LOG.debug("No result set for this site in aerospike");
            return null;
        }

        /**
         * Builds the siteFeedbackEntity object from the global and colo feedback objects(thrift generated) fetched
         * from aerospike
         */
        SegmentAdGroupFeedbackEntity buildSiteFeedbackEntity(final SiteFeedback globalFeedback,
                final SiteFeedback rctFeedback, final SiteFeedback coloFeedback) {
            if (globalFeedback == null && rctFeedback == null && coloFeedback == null) {
                return null;
            }
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = new Date();
            String today = dateFormat.format(date);
            HashMap<String, ChannelSegmentFeedbackEntity.Builder> adGroupFeedbackBuilderMap = new HashMap<>();

            // getting global data
            if (globalFeedback != null) {
                for (AdGroupFeedback adGroupFeedback : globalFeedback.getAdGroupFeedbacks()) {
                    ChannelSegmentFeedbackEntity.Builder globalBuilder = buildChannelSegmentFeedbackEntityBuilder(
                            adGroupFeedback, dateFormat, date, today);
                    // adding feedback at global level
                    adGroupFeedbackBuilderMap.put(adGroupFeedback.getExternalSiteKey(), globalBuilder);
                }
            }
            if (rctFeedback != null) {
                for (AdGroupFeedback adGroupFeedback : rctFeedback.getAdGroupFeedbacks()) {
                    ChannelSegmentFeedbackEntity.Builder rctBuilder = buildChannelSegmentFeedbackEntityBuilder(
                            adGroupFeedback, dateFormat, date, today);
                    if (adGroupFeedbackBuilderMap.containsKey(adGroupFeedback.getExternalSiteKey())) {
                        ChannelSegmentFeedbackEntity.Builder builder = adGroupFeedbackBuilderMap.get(adGroupFeedback
                                .getExternalSiteKey());
                        builder.setBeacons(builder.getBeacons() + rctBuilder.getBeacons());
                        adGroupFeedbackBuilderMap.put(adGroupFeedback.getExternalSiteKey(), builder);
                    }
                    // else adding feedback entity with rct data
                    else {
                        adGroupFeedbackBuilderMap.put(adGroupFeedback.getExternalSiteKey(), rctBuilder);
                    }
                }
            }
            // getting local data
            if (coloFeedback != null) {
                for (AdGroupFeedback adGroupFeedback : coloFeedback.getAdGroupFeedbacks()) {
                    ChannelSegmentFeedbackEntity.Builder coloBuilder = buildChannelSegmentFeedbackEntityBuilder(
                            adGroupFeedback, dateFormat, date, today);
                    // Setting latency and Fill Ratio to colo local if global level
                    // feedback
                    // already there
                    if (adGroupFeedbackBuilderMap.containsKey(adGroupFeedback.getExternalSiteKey())) {
                        ChannelSegmentFeedbackEntity.Builder builder = adGroupFeedbackBuilderMap.get(adGroupFeedback
                                .getExternalSiteKey());
                        builder.setFillRatio(coloBuilder.getFillRatio());
                        builder.setLastHourLatency(coloBuilder.getLastHourLatency());
                        adGroupFeedbackBuilderMap.put(adGroupFeedback.getExternalSiteKey(), builder);
                    }
                    // else adding feedback entity with colo local data
                    else {
                        adGroupFeedbackBuilderMap.put(adGroupFeedback.getExternalSiteKey(), coloBuilder);
                    }
                }
            }

            HashMap<String, ChannelSegmentFeedbackEntity> adGroupFeedbackMap = new HashMap<>();
            for (Map.Entry<String, ChannelSegmentFeedbackEntity.Builder> entry : adGroupFeedbackBuilderMap.entrySet()) {
                adGroupFeedbackMap.put(entry.getKey(), entry.getValue().build());
            }
            Integer segmentId;
            if (globalFeedback != null) {
                segmentId = globalFeedback.getInventorySegmentId();
            }
            else if (rctFeedback != null) {
                segmentId = rctFeedback.getInventorySegmentId();
            }
            else {
                segmentId = coloFeedback.getInventorySegmentId();
            }
            return new SegmentAdGroupFeedbackEntity(segmentId, adGroupFeedbackMap);
        }

        /**
         * Method that constructs channel segment feedback entity from adgroupFeedback
         */
        private ChannelSegmentFeedbackEntity.Builder buildChannelSegmentFeedbackEntityBuilder(
                final AdGroupFeedback adGroupFeedback, final DateFormat dateFormat, final Date date, final String today) {
            int impressionRendered = 0;
            double weightedImpressionsRendered = 0;
            double weighedRevenue = 0.0;
            double eCPM = 0.0;

            for (Map.Entry<String, Feedback> entry : adGroupFeedback.getDailyFeedback().entrySet()) {
                Feedback feedback = entry.getValue();
                long age;
                try {
                    age = (date.getTime() - dateFormat.parse(entry.getKey().split(" ")[0]).getTime())
                            / (1000 * 60 * 60 * 24);
                }
                catch (ParseException e) {
                    age = feedbackTimeFrame / 2;
                }
                if (age >= 0 && age < feedbackTimeFrame) {
                    weightedImpressionsRendered += feedback.getBeacons() / (age + 1 + 0.0);
                    weighedRevenue += feedback.getRevenue() / (age + 1);
                }
                if (age >= 0 && age < boostTimeFrame) {
                    impressionRendered += feedback.getBeacons();
                }
            }
            if (weightedImpressionsRendered > 0) {
                eCPM = (weighedRevenue / weightedImpressionsRendered) * 1000;
            }
            if (eCPM == 0) {
                eCPM = defaultECPM;
            }
            String todayDateTime = today + "00:00:00";

            Feedback todaysFeedback = adGroupFeedback.getDailyFeedback().get(todayDateTime);
            int todaysclicks = 0;
            if (todaysFeedback != null) {
                todaysclicks = todaysFeedback.getClicks();
            }
            // default latency and fill ratio
            double lastHoursLatency = 400;
            double fillRatio = 0.01;
            Feedback recentHourFeedback = adGroupFeedback.getRecentHourFeedback();
            int requests = recentHourFeedback.getRequests();
            int fills = recentHourFeedback.getFills();
            if (requests > 0) {
                lastHoursLatency = recentHourFeedback.getLatency() / requests;
                fillRatio = fills / (requests + 0.0);
            }
            ChannelSegmentFeedbackEntity.Builder builder = ChannelSegmentFeedbackEntity.newBuilder();
            builder.setAdvertiserId(adGroupFeedback.getAdvertiserId());
            builder.setAdGroupId(adGroupFeedback.getAdGroupGuid());
            builder.setECPM(eCPM);
            builder.setFillRatio(fillRatio);
            builder.setLastHourLatency(lastHoursLatency);
            builder.setTodayRequests(requests);
            builder.setBeacons(impressionRendered);
            builder.setClicks(todaysclicks);
            return builder;
        }

        /**
         * Update the cache with fetched site feedback for the requested segment and its all adgroups
         */
        void updateCache(final SiteFeedbackEntity siteFeedbackEntity) {
            if (siteFeedbackEntity != null) {
                siteSegmentFeedbackCache.put(this.siteId, siteFeedbackEntity);
            }
            currentlyUpdatingSites.remove(this.siteId);
        }
    }

    /**
     * For lookup into the cache
     */
    public SiteFeedbackEntity query(final String siteId) {
        return this.siteSegmentFeedbackCache.get(siteId);
    }

}