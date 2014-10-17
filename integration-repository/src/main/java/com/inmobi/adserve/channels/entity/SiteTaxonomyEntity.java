package com.inmobi.adserve.channels.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import com.inmobi.phoenix.batteries.data.IdentifiableEntity;


@Data
@NoArgsConstructor
public class SiteTaxonomyEntity implements IdentifiableEntity<String> {

  private static final long serialVersionUID = 1L;

  private String id;
  private String name;
  private String parentId;

  public SiteTaxonomyEntity(final String id, final String name, final String parentId) {
    super();
    this.id = id;
    this.name = name;
    this.parentId = parentId;
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
