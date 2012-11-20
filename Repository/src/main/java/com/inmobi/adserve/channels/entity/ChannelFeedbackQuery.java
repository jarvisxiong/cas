package com.inmobi.adserve.channels.entity;

import java.util.HashSet;
import java.util.Set;

import com.inmobi.phoenix.data.RepositoryQuery;
import com.inmobi.phoenix.batteries.data.HashIndexKeyBuilder;

public class ChannelFeedbackQuery implements RepositoryQuery, HashIndexKeyBuilder<ChannelFeedbackEntity> {
  private String advertiserId;

  public ChannelFeedbackQuery() {
  }

  public ChannelFeedbackQuery(String advertiserId) {
    this.advertiserId = advertiserId;
  }

  @Override
  public Set<RepositoryQuery> createQueriesFromObject(ChannelFeedbackEntity channelFeedbackEntity) {
    Set<RepositoryQuery> queries = new HashSet<RepositoryQuery>();
    queries.add(new ChannelFeedbackQuery(channelFeedbackEntity.getId()));
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
    return result;
  }
}
