package com.inmobi.adserve.channels.entity;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.inmobi.phoenix.batteries.data.IdentifiableEntity;

public class ChannelSegmentEntity implements IdentifiableEntity<String> {

  private static final long serialVersionUID = 1L;

  private String advertiserId;
  private String adgroupId;
  private String adId;
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
  private long incId;
  private boolean allTags;
  private String pricingModel;
  private ArrayList<Integer> targetingPlatform = new ArrayList<Integer>();
  private Integer[] siteRatings;
  private List<Integer> osIds;
  private List<String> siteIds;
  private boolean siteExclusion;
  private boolean udidRequired;
  private boolean zipCodeRequired;
  private boolean richMediEnabled;

  public ChannelSegmentEntity(final String advertiserId, final String adgroupId, final String adId, final String channelId,
      long platformTargeting, Long[] rcList, Long[] tags, boolean status, boolean isTestMode, String externalSiteKey,
      Timestamp modified_on, String campaignId, Long[] slotIds, long incId, boolean allTags, String pricingModel,
      Integer[] siteRatings, int targetingPlatform, ArrayList<Integer> osIds, List<String> siteIds, boolean siteExclusion,
      boolean udidRequired, boolean zipCodeRequired, boolean richMediaEnabled) {
    this.advertiserId = advertiserId;
    this.adgroupId = adgroupId;
    this.adId = adId;
    this.channelId = channelId;
    this.platformTargeting = platformTargeting;
    this.rcList = rcList;
    this.tags = tags;
    this.status = status;
    this.isTestMode = isTestMode;
    this.externalSiteKey = externalSiteKey;
    this.modified_on = modified_on;
    this.campaignId = campaignId;
    this.slotIds = slotIds;
    this.incId = incId;
    this.allTags = allTags;
    this.pricingModel = pricingModel;
    if(targetingPlatform == 1 || targetingPlatform > 2)
      this.targetingPlatform.add(1);
    if(targetingPlatform >= 2)
      this.targetingPlatform.add(2);
    this.siteRatings = siteRatings;
    this.osIds = osIds;
    this.siteIds = siteIds;
    this.siteExclusion = siteExclusion;
    this.udidRequired = udidRequired;
    this.zipCodeRequired = zipCodeRequired;
    this.richMediEnabled = richMediaEnabled;
  }

  public static long getSerialversionuid() {
    return serialVersionUID;
  }

  public String getAdvertiserId() {
    return advertiserId;
  }

  public Timestamp getModified_on() {
    return modified_on;
  }

  public List<String> getSiteIds() {
    return siteIds;
  }

  public boolean isSiteExclusion() {
    return siteExclusion;
  }

  public boolean isUdidRequired() {
    return udidRequired;
  }

  public boolean isZipCodeRequired() {
    return zipCodeRequired;
  }

  public boolean isRichMediEnabled() {
    return richMediEnabled;
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj)
      return true;
    if(obj == null)
      return false;
    if(!(obj instanceof ChannelSegmentEntity))
      return false;
    return true;
  }

  @Override
  public String getJSON() {
    return null;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (int) (prime * result + advertiserId.hashCode());
    if(advertiserId.equals("2345")) {
      System.out.println("ya ya this also");
      return 1;
    } else
      return result;
  }

  @Override
  public String getId() {
    return advertiserId;
  }

  public String getAdgroupId() {
    return this.adgroupId;
  }

  public String getPricingModel() {
    return this.pricingModel.toUpperCase();
  }

  public long getIncId() {
    return this.incId;
  }

  public String getAdId() {
    return this.adId;
  }

  public String getChannelId() {
    return this.channelId;
  }

  public Long[] getRcList() {
    return this.rcList;
  }

  public String getCampaignId() {
    return this.campaignId;
  }

  public Long[] getSlotIds() {
    return this.slotIds;
  }

  public Long[] getTags() {
    return this.tags;
  }

  public long getPlatformTargeting() {
    return this.platformTargeting;
  }

  public Timestamp getModifiedOn() {
    return this.modified_on;
  }

  public String getExternalSiteKey() {
    return this.externalSiteKey;
  }

  public boolean getIsTestMode() {
    return this.isTestMode;
  }

  public boolean getStatus() {
    return this.status;
  }

  public boolean getAllTags() {
    return this.allTags;
  }

  public Integer[] getSiteRatings() {
    return this.siteRatings;
  }

  public ArrayList<Integer> getTargetingPlatform() {
    return this.targetingPlatform;
  }

  public List<Integer> getOsIds() {
    return osIds;
  }
}
