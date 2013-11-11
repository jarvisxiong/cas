package com.inmobi.adserve.channels.repository;

import com.inmobi.adserve.channels.entity.ChannelSegmentFeedbackEntity;
import com.inmobi.adserve.channels.entity.SegmentAdGroupFeedbackEntity;
import com.inmobi.adserve.channels.entity.SiteFeedbackEntity;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.casthrift.AdGroupFeedback;
import com.inmobi.casthrift.DataCenter;
import com.inmobi.casthrift.Feedback;
import com.inmobi.casthrift.SiteFeedback;
import net.citrusleaf.CitrusleafClient;
import net.citrusleaf.CitrusleafClient.ClResult;
import net.citrusleaf.CitrusleafClient.ClResultCode;
import org.apache.commons.configuration.Configuration;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class SiteCitrusLeafFeedbackRepository {

    private static CitrusleafClient                                   citrusleafClient;
    private String                                                    namespace;
    private String                                                    set;
    private DataCenter                                                colo;
    // Cache to store segment feedback entities loaded form the citrus leaf.
    private ConcurrentHashMap<String/* siteId */, SiteFeedbackEntity> siteSegmentFeedbackCache;
    private ConcurrentHashMap<String, Boolean>                        currentlyUpdatingSites;
    private int                                                       refreshTime;
    private ExecutorService                                           executorService;
    private int                                                       feedbackTimeFrame;
    private int                                                       boostTimeFrame;
    private double                                                    defaultECPM;

    public void init(Configuration config, DataCenter colo) {
        this.namespace = config.getString("namespace");
        this.set = config.getString("set");
        SiteCitrusLeafFeedbackRepository.citrusleafClient = new CitrusleafClient(config.getString("host"),
                config.getInt("port"));
        this.siteSegmentFeedbackCache = new ConcurrentHashMap<String, SiteFeedbackEntity>();
        this.currentlyUpdatingSites = new ConcurrentHashMap<String, Boolean>();
        this.refreshTime = config.getInt("refreshTime");
        this.feedbackTimeFrame = config.getInt("feedbackTimeFrame", 15);
        this.boostTimeFrame = config.getInt("boostTimeFrame", 3);
        this.defaultECPM = config.getDouble("default.ecpm", 0.25);
        this.colo = colo;
        this.executorService = Executors.newCachedThreadPool();
    }

    /**
     * Method to get SegmentAdGroupFeedbackEntity for the request site and segment combination. Looks first in cache
     * with a configurable refresh time if hits within the refresh time , returns the hit entity otherwise makes a call
     * to citrusleaf to load the fresh data while returning the stale entity for the current request.
     * 
     * @return : returns te entity matching for the site, segment and adgroup combination
     */
    public SegmentAdGroupFeedbackEntity query(String siteId, Integer segmentId, DebugLogger logger) {
        SiteFeedbackEntity siteFeedbackEntity = siteSegmentFeedbackCache.get(siteId);
        if (siteFeedbackEntity != null) {
            logger.debug("got the siteFeedback entity from cache for query", siteId, segmentId);
            if (System.currentTimeMillis() - siteFeedbackEntity.getLastUpdated() < refreshTime) {
                logger.debug("siteFeedback entity is fresh for query", siteId, segmentId);
                InspectorStats.incrementStatCount(InspectorStrings.siteFeedbackCacheHit);
                return siteFeedbackEntity.getSegmentAdGroupFeedbackMap() == null ? null : siteFeedbackEntity
                        .getSegmentAdGroupFeedbackMap()
                            .get(segmentId);
            }
            logger.debug("siteFeedback entity is stale for query", siteId, "_", segmentId);
        }
        else {
            logger.debug("siteFeedback not found for siteId:", siteId);
        }
        logger.debug("Returning default/old siteFeedback entity", "and Fetching new data from citrus leaf for siteId:",
            siteId);
        InspectorStats.incrementStatCount(InspectorStrings.siteFeedbackCacheMiss);
        asynchronouslyFetchFeedbackFromCitrusLeaf(siteId, logger);
        siteFeedbackEntity = siteSegmentFeedbackCache.get(siteId);
        return siteFeedbackEntity == null ? null : (siteFeedbackEntity.getSegmentAdGroupFeedbackMap() == null ? null
                : siteFeedbackEntity.getSegmentAdGroupFeedbackMap().get(segmentId));
    }

    /**
     * Method that asynchronously fetches feedback from the citrusleaf and puts it into the cache
     */
    private void asynchronouslyFetchFeedbackFromCitrusLeaf(String siteId, DebugLogger logger) {
        Boolean isSiteGettingUpdated = this.currentlyUpdatingSites.putIfAbsent(siteId, true);
        if (isSiteGettingUpdated == null) {
            // forking new thread to fetch feedback from citrusleaf
            CacheUpdater cacheUpdater = new CacheUpdater(siteId, logger);
            Thread cacheUpdaterThread = new Thread(cacheUpdater);
            executorService.execute(cacheUpdaterThread);
        }
        else {
            logger.debug("Not fetching feedback as site is already updating");
        }
    }

    /**
     * Class that performs feedback fetch from citrusleaf and cache updating tasks asynchronously
     * 
     */
    class CacheUpdater implements Runnable {
        private String      siteId;
        private DebugLogger logger;

        public CacheUpdater(String siteId, DebugLogger logger) {
            this.siteId = siteId;
            this.logger = logger;
        }

        @Override
        public void run() {
            logger.debug("getting feedback form the citrus leaf for query", siteId);
            getFeedbackFromCitrusleaf(siteId);
        }

        /**
         * Method which gets feedback form citrusleaf in case of a cache miss and updates the cache
         */
        void getFeedbackFromCitrusleaf(String siteId) {
            // getting all data for the site
            ClResult clResult = getFromCitrusLeaf(siteId);
            if (!clResult.resultCode.equals(ClResultCode.OK)) {
                logger.debug("key not found in citrus leaf");
                InspectorStats.incrementStatCount(InspectorStrings.siteFeedbackFailedToLoadFromCitrusLeaf);
                return;
            }
            logger.debug("key found in citrus leaf");
            updateCache(processResultFromCitrusLeaf(clResult));
        }

        /**
         * Method which makes a call to citrus leaf to load the complete site info
         */
        ClResult getFromCitrusLeaf(String site) {
            InspectorStats.incrementStatCount(InspectorStrings.siteFeedbackRequestsToCitrusLeaf);
            long time = System.currentTimeMillis();
            ClResult clResult = citrusleafClient.getAll(namespace, set, site, null);
            time = System.currentTimeMillis() - time;
            InspectorStats.incrementStatCount(InspectorStrings.siteFeedbackLatency, time);
            return clResult;
        }

        /**
         * Processes feedback , extract global and colo data to get the siteFeedbackEntity object
         * 
         * @param clResult
         *            : ClResult object containing the feedback
         */
        SiteFeedbackEntity processResultFromCitrusLeaf(ClResult clResult) {
            if (clResult.results != null) {
                Map<Integer, SegmentAdGroupFeedbackEntity> segmentAdGroupFeedbackEntityMap = new HashMap<Integer, SegmentAdGroupFeedbackEntity>();
                for (Map.Entry<String, Object> binValuePair : clResult.results.entrySet()) {
                    String bin = binValuePair.getKey();
                    if (bin.startsWith(DataCenter.ALL.toString())) {
                        String segmentId = bin.split("\u0001")[1];
                        SiteFeedback globalFeedback = new SiteFeedback();
                        try {
                            TDeserializer tDeserializer = new TDeserializer(new TBinaryProtocol.Factory());
                            tDeserializer.deserialize(globalFeedback, (byte[]) binValuePair.getValue());
                        }
                        catch (TException exception) {
                            logger.debug("Error in deserializing thrift for global feedback for " + "segment",
                                segmentId, exception);
                            globalFeedback = null;
                        }
                        bin = DataCenter.RCT.toString() + "\u0001" + segmentId;
                        SiteFeedback rctFeedback = new SiteFeedback();
                        try {
                            TDeserializer tDeserializer = new TDeserializer(new TBinaryProtocol.Factory());
                            Object byteArray = clResult.results.get(bin);
                            if (byteArray == null) {
                                throw new TException("No rct data");
                            }
                            tDeserializer.deserialize(rctFeedback, (byte[]) byteArray);
                        }
                        catch (TException exception) {
                            logger.debug("Error in deserializing thrift for rct feedback for " + "segment", segmentId,
                                exception);
                            rctFeedback = null;
                        }
                        bin = colo.toString() + "\u0001" + segmentId;
                        SiteFeedback coloFeedback = new SiteFeedback();
                        try {
                            TDeserializer tDeserializer = new TDeserializer(new TBinaryProtocol.Factory());
                            Object byteArray = clResult.results.get(bin);
                            if (byteArray == null) {
                                throw new TException("No colo data");
                            }
                            tDeserializer.deserialize(coloFeedback, (byte[]) byteArray);
                        }
                        catch (TException exception) {
                            logger.debug("Error in deserializing thrift for local feedback for " + "segment",
                                segmentId, exception);
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
            logger.debug("No result set for this site in citrusleaf");
            return null;
        }

        /**
         * Builds the siteFeedbackEntity object form the global and colo feedback objects(thrift genereated) fetched
         * from citrus leaf
         */
        SegmentAdGroupFeedbackEntity buildSiteFeedbackEntity(SiteFeedback globalFeedback, SiteFeedback rctFeedback,
                SiteFeedback coloFeedback) {
            if (globalFeedback == null && rctFeedback == null && coloFeedback == null) {
                return null;
            }
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = new Date();
            String today = dateFormat.format(date);
            HashMap<String, ChannelSegmentFeedbackEntity.Builder> adGroupFeedbackBuilderMap = new HashMap<String, ChannelSegmentFeedbackEntity.Builder>();

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

            HashMap<String, ChannelSegmentFeedbackEntity> adGroupFeedbackMap = new HashMap<String, ChannelSegmentFeedbackEntity>();
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
                AdGroupFeedback adGroupFeedback, DateFormat dateFormat, Date date, String today) {
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
        void updateCache(SiteFeedbackEntity siteFeedbackEntity) {
            if (siteFeedbackEntity != null) {
                siteSegmentFeedbackCache.put(this.siteId, siteFeedbackEntity);
            }
            currentlyUpdatingSites.remove(this.siteId);
        }
    }

    /**
     * For lookup into the cache
     */
    public SiteFeedbackEntity query(String siteId) {
        return this.siteSegmentFeedbackCache.get(siteId);
    }

}
