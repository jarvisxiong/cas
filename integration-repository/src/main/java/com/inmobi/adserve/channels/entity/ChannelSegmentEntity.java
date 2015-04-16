package com.inmobi.adserve.channels.entity;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.types.AdFormatType;
import com.inmobi.casthrift.ADCreativeType;
import com.inmobi.phoenix.batteries.data.IdentifiableEntity;

import lombok.Getter;
import lombok.Setter;


@Getter
public class ChannelSegmentEntity implements IdentifiableEntity<String> {

    private static final long serialVersionUID = 1L;
    private final static Logger LOG = LoggerFactory.getLogger(ChannelSegmentEntity.class);

    private final String advertiserId;
    private final String adgroupId;
    private final String[] adIds;
    private final String channelId;
    private final long platformTargeting;
    private final Long[] rcList;
    private final Long[] tags;
    private final boolean status;
    private final boolean isTestMode;
    private final String externalSiteKey;
    private final Timestamp modified_on;
    private final String campaignId;
    private final Long[] slotIds;
    private final Long[] incIds;
    private final long adgroupIncId;
    private final boolean allTags;
    private final String pricingModel;
    private final ArrayList<Integer> targetingPlatform;
    private final Integer[] siteRatings;
    private final List<Integer> osIds;
    private final boolean udIdRequired;
    private final boolean zipCodeRequired;
    private final boolean latlongRequired;
    private final boolean restrictedToRichMediaOnly;
    private final boolean appUrlEnabled;
    private final boolean interstitialOnly;
    private final boolean nonInterstitialOnly;
    private final boolean stripUdId;
    private final boolean stripZipCode;
    private final boolean stripLatlong;
    private final JSONObject additionalParams;
    private final Long[] categoryTaxonomy;
    private final Set<String> sitesIE;
    private final boolean isSiteInclusion;
    private final long impressionCeil;
    private final List<Integer> manufModelTargetingList;
    private final double ecpmBoost;
    private final Date ecpmBoostExpiryDate;
    private final Long[] tod;
    private final int dst; // Classify rtbd, ix and dcp ad groups
    private final long campaignIncId;
    private final Integer[] adFormatIds;
    private final String automationTestId;

    public ChannelSegmentEntity(final Builder builder) {
        advertiserId = builder.advertiserId;
        adgroupId = builder.adgroupId;
        adIds = builder.adIds;
        channelId = builder.channelId;
        platformTargeting = builder.platformTargeting;
        rcList = builder.rcList;
        tags = builder.tags;
        status = builder.status;
        isTestMode = builder.isTestMode;
        externalSiteKey = builder.externalSiteKey;
        modified_on = builder.modified_on;
        campaignId = builder.campaignId;
        slotIds = builder.slotIds;
        incIds = builder.incIds;
        adgroupIncId = builder.adgroupIncId;
        allTags = builder.allTags;
        pricingModel = builder.pricingModel;
        final ArrayList<Integer> targetingPlatform = new ArrayList<>();
        if (builder.targetingPlatform == 1 || builder.targetingPlatform > 2) {
            targetingPlatform.add(1);
        }
        if (builder.targetingPlatform >= 2) {
            targetingPlatform.add(2);
        }
        this.targetingPlatform = targetingPlatform;
        siteRatings = builder.siteRatings;
        osIds = builder.osIds;
        udIdRequired = builder.udIdRequired;
        zipCodeRequired = builder.zipCodeRequired;
        latlongRequired = builder.latlongRequired;
        restrictedToRichMediaOnly = builder.restrictedToRichMediaOnly;
        appUrlEnabled = builder.appUrlEnabled;
        interstitialOnly = builder.interstitialOnly;
        nonInterstitialOnly = builder.nonInterstitialOnly;
        stripUdId = builder.stripUdId;
        stripZipCode = builder.stripZipCode;
        stripLatlong = builder.stripLatlong;
        additionalParams = builder.additionalParams;
        categoryTaxonomy = builder.categoryTaxonomy;
        sitesIE = builder.sitesIE;
        isSiteInclusion = builder.isSiteInclusion;
        impressionCeil = builder.impressionCeil;
        manufModelTargetingList = builder.manufModelTargetingList;
        ecpmBoost = builder.ecpmBoost;
        ecpmBoostExpiryDate = builder.ecpmBoostExpiryDate;
        tod = builder.tod;
        dst = builder.dst;
        campaignIncId = builder.campaignIncId;
        adFormatIds = builder.adFormatIds;
        automationTestId = builder.automationTestId;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Setter
    public static class Builder {
        private String advertiserId;
        private String adgroupId;
        private String[] adIds;
        private String channelId;
        private long platformTargeting;
        private Long[] rcList;
        private Long[] tags;
        private boolean status;
        private boolean isTestMode;
        private String externalSiteKey;
        private Timestamp modified_on;
        private String campaignId;
        private Long[] slotIds;
        private Long[] incIds;
        private long adgroupIncId;
        private boolean allTags;
        private String pricingModel;
        private int targetingPlatform;
        private Integer[] siteRatings;
        private List<Integer> osIds;
        private boolean udIdRequired;
        private boolean zipCodeRequired;
        private boolean latlongRequired;
        private boolean restrictedToRichMediaOnly;
        private boolean appUrlEnabled;
        private boolean interstitialOnly;
        private boolean nonInterstitialOnly;
        private boolean stripUdId;
        private boolean stripZipCode;
        private boolean stripLatlong;
        private JSONObject additionalParams;
        private Long[] categoryTaxonomy;
        private Set<String> sitesIE;
        private boolean isSiteInclusion;
        private long impressionCeil;
        private List<Integer> manufModelTargetingList;
        private double ecpmBoost;
        private Date ecpmBoostExpiryDate;
        private Long[] tod;
        private int dst;
        private long campaignIncId;
        private Integer[] adFormatIds;
        private String automationTestId;

        public ChannelSegmentEntity build() {
            return new ChannelSegmentEntity(this);
        }
    }

    @Override
    public String getId() {
        return adgroupId;
    }

    @Override
    public String getJSON() {
        return null; // To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Get the inc_id corresponding to an ad format in the ad_group.
     */
    public long getIncId(final ADCreativeType creativeType) {
        final long notFound = -1L;

        if (null == getAdFormatIds()) {
            return notFound;
        }

        final int requestedAdFormatId = getAdFormatId(creativeType);
        try {
            for (int i = 0; i < getAdFormatIds().length; i++) {
                if (getAdFormatIds()[i] == requestedAdFormatId) {
                    return getIncIds()[i];
                }
            }
        } catch (final ArrayIndexOutOfBoundsException e) {
            LOG.debug("Exception raised in ChannelSegmentEntity {}", e);
            return notFound;
        }
        // This would happen only with inconsistent data.
        return notFound;
    }

    /**
     * Get the AdId corresponding to an ad format in the ad_group.
     */
    public String getAdId(final ADCreativeType creativeType) {
        final String notFound = StringUtils.EMPTY;

        if (null == getAdFormatIds()) {
            return notFound;
        }

        final int requestedAdFormatId = getAdFormatId(creativeType);
        try {
            for (int i = 0; i < getAdFormatIds().length; i++) {
                if (getAdFormatIds()[i] == requestedAdFormatId) {
                    return getAdIds()[i];
                }
            }
        } catch (final ArrayIndexOutOfBoundsException e) {
            LOG.debug("Exception raised in ChannelSegmentEntity {}", e);
            return notFound;
        }
        // This would happen only with inconsistent data.
        return notFound;
    }

    /**
     * Get Ad format id corresponding to a creative type in the ad group.
     */
    private int getAdFormatId(final ADCreativeType creativeType) {
        if (creativeType == ADCreativeType.NATIVE) {
            return AdFormatType.META_JSON.getValue(); // NATIVE
        } else if (creativeType == ADCreativeType.INTERSTITIAL_VIDEO) {
            return AdFormatType.VIDEO.getValue(); // VIDEO
        } else if (creativeType == ADCreativeType.BANNER) {
            return AdFormatType.TEXT.getValue(); // BANNER
        } else {
            return -1; // not found
        }
    }
}
