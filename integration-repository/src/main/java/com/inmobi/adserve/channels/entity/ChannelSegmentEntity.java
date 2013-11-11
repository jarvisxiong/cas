package com.inmobi.adserve.channels.entity;

import com.inmobi.phoenix.batteries.data.IdentifiableEntity;
import lombok.Getter;
import lombok.Setter;
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
    private final String             adId;
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
    private final long               incId;
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

    public ChannelSegmentEntity(Builder builder) {
        this.advertiserId = builder.advertiserId;
        this.adgroupId = builder.adgroupId;
        this.adId = builder.adId;
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
        this.incId = builder.incId;
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
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Setter
    public static class Builder {
        private String        advertiserId;
        private String        adgroupId;
        private String        adId;
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
        private long          incId;
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

}