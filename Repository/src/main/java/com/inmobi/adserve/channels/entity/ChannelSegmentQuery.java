package com.inmobi.adserve.channels.entity;

import java.util.HashSet;
import java.util.Set;

import com.inmobi.phoenix.data.RepositoryQuery;
import com.inmobi.phoenix.batteries.data.HashIndexKeyBuilder;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;

public class ChannelSegmentQuery implements RepositoryQuery, HashIndexKeyBuilder<ChannelSegmentEntity> {
  private String advertiserId;

  public ChannelSegmentQuery() {
  }

  public ChannelSegmentQuery(String advertiserId) {
    this.advertiserId = advertiserId;
  }

  @Override
  public Set<RepositoryQuery> createQueriesFromObject(ChannelSegmentEntity channelSegment) {
    Set<RepositoryQuery> queries = new HashSet<RepositoryQuery>();
    queries.add(new ChannelSegmentQuery(channelSegment.getId()));
    return queries;
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj)
      return true;
    if(obj == null)
      return false;
    if(!(obj instanceof ChannelSegmentQuery))
      return false;
    return true;

  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + advertiserId.hashCode();
    if(advertiserId.equals("2345")) {
      System.out.println("yes they are equal");
      return 1;
    } else
      return result;
  }
}
