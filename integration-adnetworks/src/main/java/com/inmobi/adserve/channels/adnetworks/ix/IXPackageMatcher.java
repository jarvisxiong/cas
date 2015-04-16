package com.inmobi.adserve.channels.adnetworks.ix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.googlecode.cqengine.resultset.ResultSet;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.GeoRegionFenceMapEntity;
import com.inmobi.adserve.channels.entity.IXPackageEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.config.GlobalConstant;
import com.inmobi.segment.Segment;
import com.inmobi.segment.impl.CarrierId;
import com.inmobi.segment.impl.City;
import com.inmobi.segment.impl.ConnectionType;
import com.inmobi.segment.impl.ConnectionTypeEnum;
import com.inmobi.segment.impl.Country;
import com.inmobi.segment.impl.DeviceOs;
import com.inmobi.segment.impl.GeoSourceType;
import com.inmobi.segment.impl.GeoSourceTypeEnum;
import com.inmobi.segment.impl.InventoryType;
import com.inmobi.segment.impl.InventoryTypeEnum;
import com.inmobi.segment.impl.LatlongPresent;
import com.inmobi.segment.impl.SiteCategory;
import com.inmobi.segment.impl.SiteId;
import com.inmobi.segment.impl.SlotId;
import com.inmobi.segment.impl.UidPresent;
import com.inmobi.segment.impl.ZipCodePresent;

public class IXPackageMatcher {

    public static final int PACKAGE_MAX_LIMIT = 30;

    public static List<Integer> findMatchingPackageIds(final SASRequestParameters sasParams,
                                                      final RepositoryHelper repositoryHelper, final Short selectedSlotId) {
        List<Integer> matchedPackageIds = new ArrayList<>();

        Segment requestSegment = createRequestSegment(sasParams, selectedSlotId);
        ResultSet<IXPackageEntity> resultSet =
                repositoryHelper.queryIXPackageRepository(sasParams.getOsId(), sasParams.getSiteId(), sasParams
                        .getCountryId().intValue(), selectedSlotId);

        int matchedPackagesCount = 0;
        for (IXPackageEntity packageEntity : resultSet) {
            if (requestSegment.isSubsetOf(packageEntity.getSegment())) {

                // Add to matchedPackageIds only if csId's match
                if (CollectionUtils.isNotEmpty(packageEntity.getDmpFilterSegmentExpression())
                        && !checkForCsidMatch(sasParams.getCsiTags(), packageEntity.getDmpFilterSegmentExpression())) {
                    continue;
                }

                // Add to matchedPackageIds only if at least one fence from the set of request fence ids
                // is present in the set of fence ids defined in the package.
                if (StringUtils.isNotEmpty(packageEntity.getGeoFenceRegion())) {
                    String geoRegionNameCountryCombo = packageEntity.getGeoFenceRegion() + "_"
                            + sasParams.getCountryId();
                    GeoRegionFenceMapEntity geoRegionFenceMapEntity = repositoryHelper.
                            queryGeoRegionFenceMapRepositoryByRegionNameCountryCombo(geoRegionNameCountryCombo);

                    if (null == geoRegionFenceMapEntity ||
                            (null != geoRegionFenceMapEntity.getFenceIdsList() &&
                                    (null == sasParams.getGeoFenceIds() ||
                                            !CollectionUtils.containsAny(sasParams.getGeoFenceIds(),
                                                    geoRegionFenceMapEntity.getFenceIdsList())))) {
                        continue;
                    }
                }

                matchedPackageIds.add(packageEntity.getId());
                // Break the loop if we reach the threshold.
                if (++matchedPackagesCount == PACKAGE_MAX_LIMIT) {
                    InspectorStats.incrementStatCount(InspectorStrings.IX_PACKAGE_THRESHOLD_EXCEEDED_COUNT);
                    break;
                }
            }
        }
        return matchedPackageIds;
    }

    private static boolean checkForCsidMatch(final Set<Integer> csiReqTags, final Set<Set<Integer>> dmpFilterExpression) {
        if (CollectionUtils.isEmpty(csiReqTags)) {
            return false;
        } else {
            for (Set<Integer> smallSet : dmpFilterExpression) {
                if (Collections.disjoint(smallSet, csiReqTags)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static Segment createRequestSegment(final SASRequestParameters sasParams, final Short selectedSlotId) {
        Country reqCountry = new Country();
        DeviceOs reqDeviceOs = new DeviceOs();
        SiteId reqSiteId = new SiteId();
        SlotId reqSlotId = new SlotId();
        CarrierId reqCarrierId = new CarrierId();
        LatlongPresent reqLatlongPresent = new LatlongPresent();
        ZipCodePresent reqZipCodePresent = new ZipCodePresent();
        UidPresent reqUidPresent = new UidPresent();
        InventoryType reqInventoryType = new InventoryType();

        // Below are optional params and requires NULL check.
        ConnectionType reqConnectionType = null;
        SiteCategory reqSiteCategory = null;
        City reqCity = null;
        GeoSourceType reqGeoSource = null;

        reqCountry.init(Collections.singleton(sasParams.getCountryId().intValue()));
        reqDeviceOs.init(Collections.singleton(sasParams.getOsId()));
        reqSiteId.init(Collections.singleton(sasParams.getSiteId()));
        reqSlotId.init(Collections.singleton(selectedSlotId.intValue()));
        reqLatlongPresent.init(StringUtils.isNotEmpty(sasParams.getLatLong()));
        reqZipCodePresent.init(StringUtils.isNotEmpty(sasParams.getPostalCode()));
        reqUidPresent.init(isUdIdPresent(sasParams));
        reqCarrierId.init(Collections.singleton((long) sasParams.getCarrierId())); // TODO: fix long->int cast in ThriftRequestParser

        InventoryTypeEnum reqInventoryEnum =
                GlobalConstant.APP.equalsIgnoreCase(sasParams.getSource()) ? InventoryTypeEnum.APP : InventoryTypeEnum.BROWSER;
        reqInventoryType.init(Collections.singleton(reqInventoryEnum));

        if (null != sasParams.getConnectionType()) {
            reqConnectionType = new ConnectionType();
            ConnectionTypeEnum connectionTypeEnum = ConnectionTypeEnum.valueOf(sasParams.getConnectionType().name());
            reqConnectionType.init(Collections.singleton(connectionTypeEnum));
        }

        if (sasParams.getSiteContentType() != null) {
            reqSiteCategory = new SiteCategory();
            reqSiteCategory.init(sasParams.getSiteContentType().name());
        }

        if (sasParams.getCity() != null) {
            reqCity = new City();
            reqCity.init(Collections.singleton(sasParams.getCity()));
        }

        if (sasParams.getLocationSource() != null) {
            reqGeoSource = new GeoSourceType();
            GeoSourceTypeEnum geoSourceTypeEnum = GeoSourceTypeEnum.valueOf(sasParams.getLocationSource().name());
            reqGeoSource.init(Collections.singleton(geoSourceTypeEnum));
        }

        Segment.Builder requestSegmentBuilder = new Segment.Builder();
        requestSegmentBuilder
                .addSegmentParameter(reqCountry)
                .addSegmentParameter(reqDeviceOs)
                .addSegmentParameter(reqSiteId)
                .addSegmentParameter(reqSlotId)
                .addSegmentParameter(reqLatlongPresent)
                .addSegmentParameter(reqZipCodePresent)
                .addSegmentParameter(reqCarrierId)
                .addSegmentParameter(reqUidPresent)
                .addSegmentParameter(reqInventoryType);

        if (reqConnectionType != null) {
            requestSegmentBuilder.addSegmentParameter(reqConnectionType);
        }
        if (reqSiteCategory != null) {
            requestSegmentBuilder.addSegmentParameter(reqSiteCategory);
        }
        if (reqCity != null) {
            requestSegmentBuilder.addSegmentParameter(reqCity);
        }
        if (reqGeoSource != null) {
            requestSegmentBuilder.addSegmentParameter(reqGeoSource);
        }

        Segment requestSegment = requestSegmentBuilder.build();

        return requestSegment;
    }

    private static boolean isUdIdPresent(SASRequestParameters sasParams) {
        return (StringUtils.isNotEmpty(sasParams.getUidParams()) && !"{}".equals(sasParams.getUidParams()))
                || (null != sasParams.getTUidParams() && !sasParams.getTUidParams().isEmpty());
    }
}
