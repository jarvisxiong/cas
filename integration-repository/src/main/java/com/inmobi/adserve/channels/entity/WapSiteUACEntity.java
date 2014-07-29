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
  // Type of site (Android, IOS etc.)
  private final long siteTypeId;
  // Content Rating of the App
  private final String contentRating;
  // Primary category of the App
  private final String appType;
  // List of secondary categories
  private final List<String> categories;
  // Coppa enabled flag
  private final boolean isCoppaEnabled;
  // last modified time of this table
  private final Timestamp modifiedOn;

  public WapSiteUACEntity(final Builder builder) {
    id = builder.id;
    siteTypeId = builder.siteTypeId;
    contentRating = builder.contentRating;
    appType = builder.appType;
    categories = builder.categories;
    isCoppaEnabled = builder.isCoppaEnabled;
    modifiedOn = builder.modifiedOn;
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
    private Timestamp modifiedOn;
    private boolean isCoppaEnabled;

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
