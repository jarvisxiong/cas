package com.inmobi.adserve.channels.server.requesthandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.inmobi.adserve.adpool.ContentType;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelEntity;
import com.inmobi.adserve.channels.entity.ChannelFeedbackEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentFeedbackEntity;
import com.inmobi.adserve.channels.entity.SegmentAdGroupFeedbackEntity;
import com.inmobi.adserve.channels.entity.SiteTaxonomyEntity;
import com.inmobi.adserve.channels.repository.ChannelAdGroupRepository;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.server.requesthandler.beans.AdvertiserMatchedSegmentDetail;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.annotations.AdvertiserIdNameMap;

import lombok.Getter;

@Singleton
public class MatchSegments {
    private static final Logger LOG = LoggerFactory.getLogger(MatchSegments.class);

    private static final String DEFAULT = "default";
    @Getter
    private final RepositoryHelper repositoryHelper;
    private final ChannelAdGroupRepository channelAdGroupRepository;
    private final ChannelEntity defaultChannelEntity;
    private final ChannelFeedbackEntity defaultChannelFeedbackEntity;
    private final ChannelSegmentFeedbackEntity defaultChannelSegmentFeedbackEntity;
    private final ChannelSegmentFeedbackEntity defaultChannelSegmentAerospikeFeedbackEntity;
    private final Provider<Marker> traceMarkerProvider;

    private final Map<String, String> advertiserIdToNameMap;

    @Inject
    public MatchSegments(final RepositoryHelper repositoryHelper, final Provider<Marker> traceMarkerProvider,
            @AdvertiserIdNameMap final Map<String, String> advertiserIdToNameMap) {
        this.traceMarkerProvider = traceMarkerProvider;
        this.repositoryHelper = repositoryHelper;
        this.advertiserIdToNameMap = advertiserIdToNameMap;

        final Double defaultEcpm = CasConfigUtil.getServerConfig().getDouble("default.ecpm", 0.1);
        channelAdGroupRepository = repositoryHelper.getChannelAdGroupRepository();

        final ChannelEntity.Builder channelEntityBuilder = ChannelEntity.newBuilder();
        channelEntityBuilder.setImpressionCeil(Long.MAX_VALUE);
        channelEntityBuilder.setImpressionFloor(0);
        channelEntityBuilder.setPriority(3);
        channelEntityBuilder.setRequestCap(Long.MAX_VALUE);
        channelEntityBuilder.setSiteInclusion(false);
        channelEntityBuilder.setSitesIE(new HashSet<>());
        defaultChannelEntity = channelEntityBuilder.build();

        final ChannelFeedbackEntity.Builder channelFeedbackEntityBuilder = ChannelFeedbackEntity.newBuilder();
        channelFeedbackEntityBuilder.setAdvertiserId(DEFAULT);
        channelFeedbackEntityBuilder.setBalance(Double.MAX_VALUE);
        defaultChannelFeedbackEntity = channelFeedbackEntityBuilder.build();

        final ChannelSegmentFeedbackEntity.Builder channelSegmentFeedbackEntityBuilder =
                ChannelSegmentFeedbackEntity.newBuilder();
        channelSegmentFeedbackEntityBuilder.setAdvertiserId(DEFAULT);
        channelSegmentFeedbackEntityBuilder.setAdGroupId(DEFAULT);
        channelSegmentFeedbackEntityBuilder.setECPM(defaultEcpm);
        channelSegmentFeedbackEntityBuilder.setFillRatio(0.01);
        channelSegmentFeedbackEntityBuilder.setLastHourLatency(400);
        defaultChannelSegmentFeedbackEntity = channelSegmentFeedbackEntityBuilder.build();

        defaultChannelSegmentAerospikeFeedbackEntity = defaultChannelSegmentFeedbackEntity;
    }

    // select channel segment based on specified rules
    public List<AdvertiserMatchedSegmentDetail> matchSegments(final SASRequestParameters sasParams) {

        final Marker traceMarker = traceMarkerProvider.get();
        final List<Long> slotIdsFromUmp = new ArrayList<>();
        for (final Short s : sasParams.getProcessedMkSlot()) {
            slotIdsFromUmp.add(Long.valueOf(s));
        }
        final Long countryId = sasParams.getCountryId();
        final int osId = sasParams.getOsId();
        final String sourceStr = sasParams.getSource();
        final ContentType siteRatingEnum = sasParams.getSiteContentType();
        final Integer targetingPlatform = sourceStr == null || "wap".equalsIgnoreCase(sourceStr) ? 2 : 1 /* app */;
        Integer siteRating = -1;
        if (null == siteRatingEnum || slotIdsFromUmp == null || slotIdsFromUmp.isEmpty()
                || sasParams.getCategories() == null || sasParams.getCategories().isEmpty()) {
            LOG.debug(traceMarker, "MatchSegments failed as categories/siteRatingEnum/slotIdsFromUmp was empty/null");
            return null;
        }
        if (ContentType.PERFORMANCE == siteRatingEnum) {
            siteRating = 0;
        } else if (ContentType.MATURE == siteRatingEnum) {
            siteRating = 1;
        } else if (ContentType.FAMILY_SAFE == siteRatingEnum) {
            siteRating = 2;
        }
        try {
            LOG.debug(
                    traceMarker,
                    "Requesting Parameters :  slots: {} categories: {} country: {} targetingPlatform: {} siteRating: {} osId: {}",
                    slotIdsFromUmp, sasParams.getCategories(), countryId, targetingPlatform, siteRating, osId);

            long country = -1;
            if (countryId != null) {
                country = countryId;
            }

            return matchSegments(slotIdsFromUmp, getCategories(sasParams), country, targetingPlatform, siteRating,
                    osId, sasParams, traceMarker);
        } catch (final NumberFormatException exception) {
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
        final Set<Long> categories = Sets.newHashSet();
        List<Long> categoryList = sasParams.getCategories();
        if (null != categoryList) {
            for (final Long cat : categoryList) {
                String parentId = cat.toString();
                while (parentId != null) {
                    categories.add(Long.parseLong(parentId));
                    final SiteTaxonomyEntity entity = repositoryHelper.querySiteTaxonomyRepository(parentId);
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

    private List<AdvertiserMatchedSegmentDetail> matchSegments(final List<Long> slotIdsFromUmp,
            final List<Long> categories, final long country, final Integer targetingPlatform, final Integer siteRating,
            final int osId, final SASRequestParameters sasParams, final Marker traceMarker) {
        final Set<ChannelSegmentEntity> allFilteredEntities = new HashSet<ChannelSegmentEntity>();

        // adding -1 for all categories
        categories.add(-1L);
        // adding -1 for all countries
        long[] countries = {-1};
        if (country != -1) {
            countries = new long[] {-1, country};
        }
        // adding -1 for all osIds
        final int[] osIds = new int[] {-1, osId};
        final boolean secure = sasParams.isSecureRequest();

        for (final long slotId : slotIdsFromUmp) {
            for (final long category : categories) {
                for (final long countryId : countries) {
                    for (final int os : osIds) {
                        final Collection<ChannelSegmentEntity> filteredEntities =
                                loadEntities(slotId, category, countryId, targetingPlatform, siteRating, os, secure,
                                        sasParams.getDst(), traceMarker);
                        LOG.debug(traceMarker, "Found {} adGroups", filteredEntities.size());
                        allFilteredEntities.addAll(filteredEntities);
                    }
                }
            }
        }

        final List<AdvertiserMatchedSegmentDetail> result =
                insertChannelSegmentToResultSet(allFilteredEntities, sasParams, traceMarker);

        if (result.isEmpty()) {
            LOG.debug(
                    traceMarker,
                    "No matching records for the request - slots: {} categories: {} country: {} targetingPlatform: {} siteRating: {} osId: {}",
                    slotIdsFromUmp, categories, country, targetingPlatform, siteRating, osId);
        } else {
            LOG.debug(traceMarker, "Final selected list of adGroups : ");
            printSegments(result, traceMarker);
        }
        return result;
    }

    // Loads entities and updates cache if required.
    private Collection<ChannelSegmentEntity> loadEntities(final long slotId, final long category, final long country,
            final Integer targetingPlatform, final Integer siteRating, final int osId, final boolean secure, final Integer dst,
            final Marker traceMarker) {
        LOG.debug(
                traceMarker,
                "Loading adgroups for slot: {} category: {} country: {} targetingPlatform: {} siteRating: {} osId: {} "
                        + "secure : {} dst: {}", slotId, category, country, targetingPlatform, siteRating, osId, secure, dst);
        return channelAdGroupRepository
                .getEntities(slotId, category, country, targetingPlatform, siteRating, osId, secure, dst);
    }

    private List<AdvertiserMatchedSegmentDetail> insertChannelSegmentToResultSet(
            final Set<ChannelSegmentEntity> allFilteredEntities, final SASRequestParameters sasParams,
            final Marker traceMarker) {

        final Map<String, AdvertiserMatchedSegmentDetail> advertiserToMatchedSegmentDetailMap = Maps.newHashMap();

        LOG.debug(traceMarker, "AdGroups are :");
        for (final ChannelSegmentEntity channelSegmentEntity : allFilteredEntities) {
            LOG.debug(traceMarker, "AdGroup : {}", channelSegmentEntity.getAdgroupId());

            // select the segment only if advertiserIdToNameMap contains incoming Segment advertiserId
            if (advertiserIdToNameMap.containsKey(channelSegmentEntity.getAdvertiserId())) {

                InspectorStats.incrementStatCount(advertiserIdToNameMap.get(channelSegmentEntity.getAdvertiserId()),
                        InspectorStrings.TOTAL_MATCHED_SEGMENTS);

                final ChannelSegment channelSegment = createSegment(channelSegmentEntity, sasParams, traceMarker);

                AdvertiserMatchedSegmentDetail advertiserMatchedSegmentDetail =
                        advertiserToMatchedSegmentDetailMap.get(channelSegmentEntity.getAdvertiserId());

                if (advertiserMatchedSegmentDetail == null) {
                    advertiserMatchedSegmentDetail =
                            new AdvertiserMatchedSegmentDetail(new ArrayList<ChannelSegment>());
                    advertiserToMatchedSegmentDetailMap.put(channelSegmentEntity.getAdvertiserId(),
                            advertiserMatchedSegmentDetail);
                }

                advertiserMatchedSegmentDetail.getChannelSegmentList().add(channelSegment);

            } else {
                LOG.debug(traceMarker, "No adapter configuration found for adgroup: {} with advertiser_id: {}",
                        channelSegmentEntity.getAdgroupId(), channelSegmentEntity.getAdvertiserId());
            }

        }

        return Lists.newArrayList(advertiserToMatchedSegmentDetailMap.values());

    }

    private ChannelSegment createSegment(final ChannelSegmentEntity channelSegmentEntity,
            final SASRequestParameters sasParams, final Marker traceMarker) {
        ChannelEntity channelEntity = repositoryHelper.queryChannelRepository(channelSegmentEntity.getChannelId());
        ChannelFeedbackEntity channelFeedbackEntity =
                repositoryHelper.queryChannelFeedbackRepository(channelSegmentEntity.getAdvertiserId());
        ChannelSegmentFeedbackEntity channelSegmentFeedbackEntity =
                repositoryHelper.queryChannelSegmentFeedbackRepository(channelSegmentEntity.getAdgroupId());
        ChannelSegmentFeedbackEntity channelSegmentAerospikeFeedbackEntity = null;
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

        final SegmentAdGroupFeedbackEntity segmentAdGroupFeedbackEntity =
                repositoryHelper.querySiteAerospikeFeedbackRepository(sasParams.getSiteId(),
                        sasParams.getSiteSegmentId());

        if (segmentAdGroupFeedbackEntity != null) {
            if (segmentAdGroupFeedbackEntity.getAdGroupFeedbackMap() != null) {
                channelSegmentAerospikeFeedbackEntity =
                        segmentAdGroupFeedbackEntity.getAdGroupFeedbackMap().get(
                                channelSegmentEntity.getExternalSiteKey());
            }
        } else {
            LOG.debug(traceMarker, "No segmentAdGroupFeedbackEntity found");
        }

        if (channelSegmentAerospikeFeedbackEntity == null) {
            LOG.debug(traceMarker, "No channelSegmentAerospikeFeedbackEntity");
            channelSegmentAerospikeFeedbackEntity = defaultChannelSegmentAerospikeFeedbackEntity;
        }

        final double pEcpm = channelSegmentAerospikeFeedbackEntity.getECPM();
        return new ChannelSegment(channelSegmentEntity, channelEntity, channelFeedbackEntity,
                channelSegmentFeedbackEntity, channelSegmentAerospikeFeedbackEntity, null, pEcpm);
    }

    private void printSegments(final List<AdvertiserMatchedSegmentDetail> result, final Marker traceMarker) {
        if (LOG.isDebugEnabled(traceMarker)) {
            for (final AdvertiserMatchedSegmentDetail advertiserMatchedSegmentDetail : result) {
                for (final ChannelSegment channelSegment : advertiserMatchedSegmentDetail.getChannelSegmentList()) {
                    LOG.debug(traceMarker, "Advertiser :{} , AdGroup : {}", channelSegment.getChannelSegmentEntity()
                            .getAdvertiserId(), channelSegment.getChannelSegmentEntity().getAdgroupId());
                }
            }
        }
    }
}
