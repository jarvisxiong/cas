package com.inmobi.adserve.channels.server.requesthandler;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.*;
import com.inmobi.adserve.channels.repository.ChannelAdGroupRepository;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.server.requesthandler.beans.AdvertiserMatchedSegmentDetail;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.annotations.AdvertiserIdNameMap;

import lombok.Getter;

import org.apache.hadoop.thirdparty.guava.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import javax.inject.Singleton;

import java.util.*;


@Singleton
public class MatchSegments {
    private static final Logger                LOG     = LoggerFactory.getLogger(MatchSegments.class);

    private static final String                DEFAULT = "default";
    @Getter
    private final RepositoryHelper             repositoryHelper;
    private final ChannelAdGroupRepository     channelAdGroupRepository;
    private final ChannelEntity                defaultChannelEntity;
    private final ChannelFeedbackEntity        defaultChannelFeedbackEntity;
    private final ChannelSegmentFeedbackEntity defaultChannelSegmentFeedbackEntity;
    private final ChannelSegmentFeedbackEntity defaultChannelSegmentCitrusLeafFeedbackEntity;
    private final Provider<Marker>             traceMarkerProvider;

    private final Map<String, String>          advertiserIdToNameMap;

    @Inject
    public MatchSegments(final RepositoryHelper repositoryHelper, final Provider<Marker> traceMarkerProvider,
            @AdvertiserIdNameMap final Map<String, String> advertiserIdToNameMap) {
        this.traceMarkerProvider = traceMarkerProvider;
        this.repositoryHelper = repositoryHelper;
        this.advertiserIdToNameMap = advertiserIdToNameMap;

        Double defaultEcpm = CasConfigUtil.getServerConfig().getDouble("default.ecpm", 0.1);
        channelAdGroupRepository = repositoryHelper.getChannelAdGroupRepository();

        ChannelEntity.Builder channelEntityBuilder = ChannelEntity.newBuilder();
        channelEntityBuilder.setImpressionCeil(Long.MAX_VALUE);
        channelEntityBuilder.setImpressionFloor(0);
        channelEntityBuilder.setPriority(3);
        channelEntityBuilder.setRequestCap(Long.MAX_VALUE);
        channelEntityBuilder.setSiteInclusion(false);
        channelEntityBuilder.setSitesIE(new HashSet<String>());
        defaultChannelEntity = channelEntityBuilder.build();

        ChannelFeedbackEntity.Builder channelFeedbackEntityBuilder = ChannelFeedbackEntity.newBuilder();
        channelFeedbackEntityBuilder.setAdvertiserId(DEFAULT);
        channelFeedbackEntityBuilder.setBalance(Double.MAX_VALUE);
        defaultChannelFeedbackEntity = channelFeedbackEntityBuilder.build();

        ChannelSegmentFeedbackEntity.Builder channelSegmentFeedbackEntityBuilder = ChannelSegmentFeedbackEntity
                .newBuilder();
        channelSegmentFeedbackEntityBuilder.setAdvertiserId(DEFAULT);
        channelSegmentFeedbackEntityBuilder.setAdGroupId(DEFAULT);
        channelSegmentFeedbackEntityBuilder.setECPM(defaultEcpm);
        channelSegmentFeedbackEntityBuilder.setFillRatio(0.01);
        channelSegmentFeedbackEntityBuilder.setLastHourLatency(400);
        defaultChannelSegmentFeedbackEntity = channelSegmentFeedbackEntityBuilder.build();

        this.defaultChannelSegmentCitrusLeafFeedbackEntity = this.defaultChannelSegmentFeedbackEntity;
    }

    // select channel segment based on specified rules
    public List<AdvertiserMatchedSegmentDetail> matchSegments(final SASRequestParameters sasParams) {

        Marker traceMarker = traceMarkerProvider.get();
        Short slotId = sasParams.getSlot();
        Long countryId = sasParams.getCountryId();
        int osId = sasParams.getOsId();
        String sourceStr = sasParams.getSource();
        String siteRatingStr = sasParams.getSiteType();
        Integer targetingPlatform = (sourceStr == null || sourceStr.equalsIgnoreCase("wap")) ? 2 : 1 /* app */;
        Integer siteRating = -1;
        if (null == siteRatingStr || slotId == null || sasParams.getCategories() == null
                || sasParams.getCategories().isEmpty()) {
            return null;
        }
        if (siteRatingStr.equalsIgnoreCase("performance")) {
            siteRating = 0;
        }
        else if (siteRatingStr.equalsIgnoreCase("mature")) {
            siteRating = 1;
        }
        else if (siteRatingStr.equalsIgnoreCase("family_safe")) {
            siteRating = 2;
        }
        try {
            LOG.debug(
                    traceMarker,
                    "Requesting Parameters :  slot: {} categories: {} country: {} targetingPlatform: {} siteRating: {} osId: {}",
                    slotId, sasParams.getCategories(), countryId, targetingPlatform, siteRating, osId);
            long slot = slotId.longValue();
            long country = -1;
            if (countryId != null) {
                country = countryId;
            }

            return (matchSegments(slot, getCategories(sasParams), country, targetingPlatform, siteRating, osId,
                    sasParams, traceMarker));
        }
        catch (NumberFormatException exception) {
            LOG.error(traceMarker, "Error parsing required arguments {}", exception);
            return null;
        }
    }

    /**
     * repositoryHelper Method which computes categories according to new category taxonomy and returns the category
     * list (old or new) depending upon the config
     */
    private List<Long> getCategories(final SASRequestParameters sasParams) {
        // Computing all the parents for categories in the category list from the
        // request
        Set<Long> categories = Sets.newHashSet();
        List<Long> categoryList = sasParams.getCategories();
        if (null != categoryList) {
            for (Long cat : categoryList) {
                String parentId = cat.toString();
                while (parentId != null) {
                    categories.add(Long.parseLong(parentId));
                    SiteTaxonomyEntity entity = repositoryHelper.querySiteTaxonomyRepository(parentId);
                    if (entity == null) {
                        break;
                    }
                    parentId = entity.getParentId();
                }
            }
        }
        // setting Categories field in sasParams to contain their parentids as well
        categoryList = Lists.newArrayList(categories);
        sasParams.setCategories(categoryList);
        return categoryList;
    }

    private List<AdvertiserMatchedSegmentDetail> matchSegments(final long slotId, final List<Long> categories,
            final long country, final Integer targetingPlatform, final Integer siteRating, final int osId,
            final SASRequestParameters sasParams, final Marker traceMarker) {
        Set<ChannelSegmentEntity> allFilteredEntities = new HashSet<ChannelSegmentEntity>();

        // adding -1 for all categories
        categories.add(-1l);
        // adding -1 for all countries
        long[] countries = { -1 };
        if (country != -1) {
            countries = new long[] { -1, country };
        }
        // adding -1 for all osIds
        int[] osIds = new int[] { -1, osId };

        for (long category : categories) {
            for (long countryId : countries) {
                for (int os : osIds) {
                    Collection<ChannelSegmentEntity> filteredEntities = loadEntities(slotId, category, countryId,
                            targetingPlatform, siteRating, os, sasParams.getDst(), traceMarker);
                    LOG.debug(traceMarker, "Found {} adGroups", filteredEntities.size());
                    allFilteredEntities.addAll(filteredEntities);
                }
            }
        }

        List<AdvertiserMatchedSegmentDetail> result = insertChannelSegmentToResultSet(allFilteredEntities, sasParams,
                traceMarker);

        if (result.size() == 0) {
            LOG.debug(
                    traceMarker,
                    "No matching records for the request - slot: {} categories: {} country: {} targetingPlatform: {} siteRating: {} osId: {}",
                    slotId, categories, country, targetingPlatform, siteRating, osId);
        }
        else {
            LOG.debug(traceMarker, "Final selected list of adGroups : ");
            printSegments(result, traceMarker);
        }
        return result;
    }

    // Loads entities and updates cache if required.
    private Collection<ChannelSegmentEntity> loadEntities(final long slotId, final long category, final long country,
            final Integer targetingPlatform, final Integer siteRating, final int osId, Integer dst, final Marker traceMarker) {
        LOG.debug(traceMarker,
                "Loading adgroups for slot: {} category: {} country: {} targetingPlatform: {} siteRating: {} osId: {} dst: {}",
                slotId, category, country, targetingPlatform, siteRating, osId, dst);
        return channelAdGroupRepository.getEntities(slotId, category, country, targetingPlatform, siteRating, osId, dst);
    }

    private List<AdvertiserMatchedSegmentDetail> insertChannelSegmentToResultSet(
            final Set<ChannelSegmentEntity> allFilteredEntities, final SASRequestParameters sasParams,
            final Marker traceMarker) {

        Map<String, AdvertiserMatchedSegmentDetail> advertiserToMatchedSegmentDetailMap = Maps.newHashMap();

        LOG.debug(traceMarker, "AdGroups are :");
        for (ChannelSegmentEntity channelSegmentEntity : allFilteredEntities) {
            LOG.debug(traceMarker, "AdGroup : {}", channelSegmentEntity.getAdgroupId());

            // select the segment only if advertiserIdToNameMap contains incoming Segment advertiserId
            if (advertiserIdToNameMap.containsKey(channelSegmentEntity.getAdvertiserId())) {

                InspectorStats.incrementStatCount(advertiserIdToNameMap.get(channelSegmentEntity.getAdvertiserId()),
                        InspectorStrings.totalMatchedSegments);

                ChannelSegment channelSegment = createSegment(channelSegmentEntity, sasParams, traceMarker);

                AdvertiserMatchedSegmentDetail advertiserMatchedSegmentDetail = advertiserToMatchedSegmentDetailMap
                        .get(channelSegmentEntity.getAdvertiserId());

                if (advertiserMatchedSegmentDetail == null) {
                    advertiserMatchedSegmentDetail = new AdvertiserMatchedSegmentDetail(new ArrayList<ChannelSegment>());
                    advertiserToMatchedSegmentDetailMap.put(channelSegmentEntity.getAdvertiserId(),
                            advertiserMatchedSegmentDetail);
                }

                advertiserMatchedSegmentDetail.getChannelSegmentList().add(channelSegment);

            }

        }

        return Lists.newArrayList(advertiserToMatchedSegmentDetailMap.values());

    }

    private ChannelSegment createSegment(final ChannelSegmentEntity channelSegmentEntity,
            final SASRequestParameters sasParams, final Marker traceMarker) {
        ChannelEntity channelEntity = repositoryHelper.queryChannelRepository(channelSegmentEntity.getChannelId());
        ChannelFeedbackEntity channelFeedbackEntity = repositoryHelper
                .queryChannelFeedbackRepository(channelSegmentEntity.getAdvertiserId());
        ChannelSegmentFeedbackEntity channelSegmentFeedbackEntity = repositoryHelper
                .queryChannelSegmentFeedbackRepository(channelSegmentEntity.getAdgroupId());
        ChannelSegmentFeedbackEntity channelSegmentCitrusLeafFeedbackEntity = null;
        if (channelEntity == null) {
            LOG.debug(traceMarker, "No channelEntity for found");
            channelEntity = defaultChannelEntity;
        }

        if (channelFeedbackEntity == null) {
            LOG.debug(traceMarker, "No channelFeedbackEntity found");
            channelFeedbackEntity = defaultChannelFeedbackEntity;
        }

        if (channelSegmentFeedbackEntity == null) {
            LOG.debug(traceMarker, "No channelSegmentFeedbackEntity found");
            channelSegmentFeedbackEntity = defaultChannelSegmentFeedbackEntity;
        }

        SegmentAdGroupFeedbackEntity segmentAdGroupFeedbackEntity = repositoryHelper
                .querySiteCitrusLeafFeedbackRepository(sasParams.getSiteId(), sasParams.getSiteSegmentId());

        if (segmentAdGroupFeedbackEntity != null) {
            if (segmentAdGroupFeedbackEntity.getAdGroupFeedbackMap() != null) {
                channelSegmentCitrusLeafFeedbackEntity = segmentAdGroupFeedbackEntity.getAdGroupFeedbackMap().get(
                        channelSegmentEntity.getExternalSiteKey());
            }
        }
        else {
            LOG.debug(traceMarker, "No segmentAdGroupFeedbackEntity found");
        }

        if (channelSegmentCitrusLeafFeedbackEntity == null) {
            LOG.debug(traceMarker, "No channelSegmentCitrusLeafFeedbackEntity");
            channelSegmentCitrusLeafFeedbackEntity = defaultChannelSegmentCitrusLeafFeedbackEntity;
        }

        double pEcpm = channelSegmentCitrusLeafFeedbackEntity.getECPM();
        return new ChannelSegment(channelSegmentEntity, channelEntity, channelFeedbackEntity,
                channelSegmentFeedbackEntity, channelSegmentCitrusLeafFeedbackEntity, null, pEcpm);
    }

    private void printSegments(final List<AdvertiserMatchedSegmentDetail> result, final Marker traceMarker) {
        if (LOG.isDebugEnabled() || null != traceMarker) {
            for (AdvertiserMatchedSegmentDetail advertiserMatchedSegmentDetail : result) {
                for (ChannelSegment channelSegment : advertiserMatchedSegmentDetail.getChannelSegmentList()) {
                    LOG.debug(traceMarker, "Advertiser :{} , AdGroup : {}", channelSegment.getChannelSegmentEntity()
                            .getAdvertiserId(), channelSegment.getChannelSegmentEntity().getAdgroupId());
                }
            }
        }
    }
}
