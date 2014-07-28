package com.inmobi.adserve.channels.entity;

import java.sql.Timestamp;
import java.util.List;

import lombok.Data;
import lombok.Setter;

import com.inmobi.phoenix.batteries.data.IdentifiableEntity;

/**
 * @author ritwik.kumar
 */
@Data
public class WapSiteUACEntity implements IdentifiableEntity<String> {
  private static final long serialVersionUID = 1L;

  private final String id;
  /**
   * Type of site (Android, IOS etc.)
   */
  private final long siteTypeId;
  /**
   * Content Rating of the App
   */
  private final String contentRating;
  /**
   * Primary category of the App
   */
  private final String appType;
  /**
   * List of secondary categories
   */
  private final List<String> categories;
  private final Timestamp uacModifiedOn;

  public WapSiteUACEntity(final Builder builder) {
    id = builder.id;
    siteTypeId = builder.siteTypeId;
    contentRating = builder.contentRating;
    appType = builder.appType;
    categories = builder.categories;
    uacModifiedOn = builder.uacModifiedOn;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  @Setter
  public static class Builder {
    private String id;
    private long siteTypeId;
    private String contentRating;
    private String appType;
    private List<String> categories;
    private Timestamp uacModifiedOn;

    public WapSiteUACEntity build() {
      return new WapSiteUACEntity(this);
    }
  }

  @Override
  public String getJSON() {
    return null;
  }

  @Override
  public String getId() {
    return id;
  }

}
