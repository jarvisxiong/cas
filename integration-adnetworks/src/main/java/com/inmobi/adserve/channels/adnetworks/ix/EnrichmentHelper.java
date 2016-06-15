package com.inmobi.adserve.channels.adnetworks.ix;

import com.google.inject.Inject;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.user.photon.datatypes.attribute.brand.BrandAttributes;
import com.inmobi.user.photon.datatypes.commons.attribute.IntAttribute;
import com.inmobi.user.photon.datatypes.commons.attribute.ValueProperties;
import com.ning.http.client.ListenableFuture;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.lang.Long.max;

/**
 * Created by avinash.kumar on 6/9/16.
 */
@Slf4j
public class EnrichmentHelper {

    private static int photonTimeout;

    @Inject
    public EnrichmentHelper(final int photonTimeout) {
        this.photonTimeout = photonTimeout;
    }

    public static void enrichCSIIdsWrapper(final SASRequestParameters sasParams) {
        final Pair<Long, ListenableFuture<BrandAttributes>> brandAttrPair = sasParams.getBrandAttrFuturePair();
        if (null != brandAttrPair) {
            final ListenableFuture<BrandAttributes> brandAttrFuture = brandAttrPair.getRight();
            sasParams.setCsiTags(getCSIIds(sasParams.getCsiTags(), brandAttrPair.getLeft(), System.currentTimeMillis(), brandAttrPair.getRight()));
        }
    }

    public static Set<Integer> getCSIIds(Set<Integer> sasCSITags, final long startTime, final long curTime,
            final ListenableFuture<BrandAttributes> futureBrandAttributes) {
        if (null == sasCSITags) {
            sasCSITags = new HashSet<>();
        }
        try {
            final BrandAttributes brandAttr =
                    futureBrandAttributes.get(getWaitTime(startTime, curTime), TimeUnit.MILLISECONDS);
            InspectorStats.incrementStatCount(InspectorStrings.PHOTON, InspectorStrings.PHOTON_LATENCY, (System.currentTimeMillis()-startTime));
            log.debug("CSIds before enrich : {}", sasCSITags);
            if (null != brandAttr) {
                mergeCSITags(sasCSITags, brandAttr.getBluekai_csids());
                mergeCSITags(sasCSITags, brandAttr.getGeocookie_csids());
                mergeCSITags(sasCSITags, brandAttr.getPds_csids());
            }
            log.debug("CSIds after enrich : {}", sasCSITags);
        } catch (InterruptedException e) {
            InspectorStats.incrementStatCount(InspectorStrings.PHOTON, InspectorStrings.TOTAL_INTERRUPTED_EXCEPTION_IN_PHOTON_RESPONSE);
            //log.error("Interrupt Exception occur while fetching photon data from async call : {}", e.getMessage());
        } catch (ExecutionException e) {
            InspectorStats.incrementStatCount(InspectorStrings.PHOTON, InspectorStrings.TOTAL_EXECUTION_EXCEPTION_IN_PHOTON_RESPONSE);
            //log.error("Execution Exception occur while fetching photon data from async call : {}", e.getMessage());
        } catch (TimeoutException e) {
            InspectorStats.incrementStatCount(InspectorStrings.PHOTON, InspectorStrings.TOTAL_TIMEOUT_IN_PHOTON_RESPONSE);
            //log.error("Timeout Exception occur while fetching photon data from async call : {}", e.getMessage());
        }
        return sasCSITags;
    }

    private static void mergeCSITags(final Set<Integer> csiTags, final IntAttribute intAttr) {
        if (null != intAttr) {
            final Map<String, Map<Integer, ValueProperties>> valueMap = intAttr.getValueMap();
            if (null != valueMap) {
                for (Map.Entry<String, Map<Integer, ValueProperties>> entry : valueMap.entrySet())
                    csiTags.addAll(entry.getValue().keySet());
            }
        }
    }

    public static long getWaitTime(final Long startTime, final long curTime) {
        final long spentTime = curTime - startTime;
        InspectorStats.incrementStatCount(InspectorStrings.PHOTON, InspectorStrings.LATENCY_FOR_PHOTON_FUTURE_CALL, spentTime);
        final long remainingTime = photonTimeout - spentTime;
        return max(remainingTime, 0);
    }
}
