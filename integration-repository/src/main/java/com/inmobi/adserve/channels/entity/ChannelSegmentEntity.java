package com.inmobi.adserve.channels.entity;

import com.inmobi.adserve.channels.types.AdCreativeType;
import com.inmobi.casthrift.ADCreativeType;
import com.inmobi.phoenix.batteries.data.IdentifiableEntity;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.ArrayUtils;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;


@Getter
public class ChannelSegmentEntity implements IdentifiableEntity<String> {

    private static final long        serialVersionUID = 1L;

    private final String             advertiserId;
    private final String             adgroupId;
    private final String[]           adIds;
    private final String             channelId;
    private final long               platformTargeting;
    private final Long[]             rcList;
    private final Long[]             tags;
    private final boolean            status;
    private final boolean            isTestMode;
    private final String             externalSiteKey;
    private final Timestamp          modified_on;
    private final String             campaignId;
    private final Long[]             slotIds;
    private final Long[]             incIds;
    private final long               adgroupIncId;
    private final boolean            allTags;
    private final String             pricingModel;
    private final ArrayList<Integer> targetingPlatform;
    private final Integer[]          siteRatings;
    private final List<Integer>      osIds;
    private final boolean            udIdRequired;
    private final boolean            zipCodeRequired;
    private final boolean            latlongRequired;
    private final boolean            restrictedToRichMediaOnly;
    private final boolean            appUrlEnabled;
    private final boolean            interstitialOnly;
    private final boolean            nonInterstitialOnly;
    private final boolean            stripUdId;
    private final boolean            stripZipCode;
    private final boolean            stripLatlong;
    private final JSONObject         additionalParams;
    private final Long[]             categoryTaxonomy;
    private final Set<String>        sitesIE;
    private final boolean            isSiteInclusion;
    private final long               impressionCeil;
    private final List<Integer>      manufModelTargetingList;
    private final double             ecpmBoost;
    private final Date               ecpmBoostExpiryDate;
    private final Long[]             tod;
    private final int                dst;                      // Classify rtbd and dcp ad groups
    private final long               campaignIncId;
    private final Integer[]          creativeTypes;

    public ChannelSegmentEntity(Builder builder) {
        this.advertiserId = builder.advertiserId;
        this.adgroupId = builder.adgroupId;
        this.adIds = builder.adIds;
        this.channelId = builder.channelId;
        this.platformTargeting = builder.platformTargeting;
        this.rcList = builder.rcList;
        this.tags = builder.tags;
        this.status = builder.status;
        this.isTestMode = builder.isTestMode;
        this.externalSiteKey = builder.externalSiteKey;
        this.modified_on = builder.modified_on;
        this.campaignId = builder.campaignId;
        this.slotIds = builder.slotIds;
        this.incIds = builder.incIds;
        this.adgroupIncId = builder.adgroupIncId;
        this.allTags = builder.allTags;
        this.pricingModel = builder.pricingModel;
        ArrayList<Integer> targetingPlatform = new ArrayList();
        if (builder.targetingPlatform == 1 || builder.targetingPlatform > 2) {
            targetingPlatform.add(1);
        }
        if (builder.targetingPlatform >= 2) {
            targetingPlatform.add(2);
        }
        this.targetingPlatform = targetingPlatform;
        this.siteRatings = builder.siteRatings;
        this.osIds = builder.osIds;
        this.udIdRequired = builder.udIdRequired;
        this.zipCodeRequired = builder.zipCodeRequired;
        this.latlongRequired = builder.latlongRequired;
        this.restrictedToRichMediaOnly = builder.restrictedToRichMediaOnly;
        this.appUrlEnabled = builder.appUrlEnabled;
        this.interstitialOnly = builder.interstitialOnly;
        this.nonInterstitialOnly = builder.nonInterstitialOnly;
        this.stripUdId = builder.stripUdId;
        this.stripZipCode = builder.stripZipCode;
        this.stripLatlong = builder.stripLatlong;
        this.additionalParams = builder.additionalParams;
        this.categoryTaxonomy = builder.categoryTaxonomy;
        this.sitesIE = builder.sitesIE;
        this.isSiteInclusion = builder.isSiteInclusion;
        this.impressionCeil = builder.impressionCeil;
        this.manufModelTargetingList = builder.manufModelTargetingList;
        this.ecpmBoost = builder.ecpmBoost;
        this.ecpmBoostExpiryDate = builder.ecpmBoostExpiryDate;
        this.tod = builder.tod;
        this.dst = builder.dst;
        this.campaignIncId = builder.campaignIncId;
        this.creativeTypes = builder.creativeTypes;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Setter
    public static class Builder {
        private String        advertiserId;
        private String        adgroupId;
        private String[]      adIds;
        private String        channelId;
        private long          platformTargeting;
        private Long[]        rcList;
        private Long[]        tags;
        private boolean       status;
        private boolean       isTestMode;
        private String        externalSiteKey;
        private Timestamp     modified_on;
        private String        campaignId;
        private Long[]        slotIds;
        private Long[]        incIds;
        private long          adgroupIncId;
        private boolean       allTags;
        private String        pricingModel;
        private int           targetingPlatform;
        private Integer[]     siteRatings;
        private List<Integer> osIds;
        private boolean       udIdRequired;
        private boolean       zipCodeRequired;
        private boolean       latlongRequired;
        private boolean       restrictedToRichMediaOnly;
        private boolean       appUrlEnabled;
        private boolean       interstitialOnly;
        private boolean       nonInterstitialOnly;
        private boolean       stripUdId;
        private boolean       stripZipCode;
        private boolean       stripLatlong;
        private JSONObject    additionalParams;
        private Long[]        categoryTaxonomy;
        private Set<String>   sitesIE;
        private boolean       isSiteInclusion;
        private long          impressionCeil;
        private List<Integer> manufModelTargetingList;
        private double        ecpmBoost;
        private Date          ecpmBoostExpiryDate;
        private Long[]        tod;
        private int           dst;
        private long          campaignIncId;
        private Integer[]     creativeTypes;

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
    public long getIncId(ADCreativeType creativeType) {
        long notFound = -1L;
        int creativeFormatId = getCreativeFormatId(creativeType);
        try {
            for (int i = 0; i < getCreativeTypes().length; i++) {
                if (getCreativeTypes()[i] == creativeFormatId) {
                    return getIncIds()[i];
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            return notFound;
        }
        return notFound;
    }

    /**
     * Get the AdId corresponding to an ad format in the ad_group.
     */
    public String getAdId(ADCreativeType creativeType) {
        String notFound = "";
        int creativeFormatId = getCreativeFormatId(creativeType);
        try {
            for (int i = 0; i < getCreativeTypes().length; i++) {
                if (getCreativeTypes()[i] == creativeFormatId) {
                    return getAdIds()[i];
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            return notFound;
        }
        return notFound;
    }

    /**
     * Get the creative format id corresponding to an ad format in the ad group
     */
    private int getCreativeFormatId(ADCreativeType creativeType) {

        // NOTE: The supplied creativeType will be validated against an ad of the respective format in the ad group.
        // If not found, it will fall back to BANNER ad. This is done for backward compalibility.
        if (creativeType == ADCreativeType.NATIVE && containsAdFormat(AdCreativeType.META_JSON)) {
            return AdCreativeType.META_JSON.getValue();     // NATIVE
        } else if (creativeType == ADCreativeType.INTERSTITIAL_VIDEO && containsAdFormat(AdCreativeType.VIDEO)) {
            return AdCreativeType.VIDEO.getValue();         // VIDEO
        } else {
            return AdCreativeType.TEXT.getValue();  // BANNER
        }
    }

    /**
     * Check if this entity contains a native ad.
     */
    private boolean containsAdFormat(AdCreativeType adCreativeType) {
        if (this.getCreativeTypes() == null) {
            return false;
        }
        return ArrayUtils.contains(this.getCreativeTypes(), adCreativeType.getValue());
    }

}