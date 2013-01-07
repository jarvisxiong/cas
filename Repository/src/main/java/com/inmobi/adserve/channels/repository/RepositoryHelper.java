package com.inmobi.adserve.channels.repository;

import com.inmobi.adserve.channels.entity.*;
import com.inmobi.phoenix.exception.RepositoryException;

public class RepositoryHelper {
  private ChannelRepository channelRepository;
  private ChannelAdGroupRepository channelAdGroupRepository;
  private ChannelFeedbackRepository channelFeedbackRepository;
  private ChannelSegmentFeedbackRepository channelSegmentFeedbackRepository;

  public RepositoryHelper(ChannelRepository channelRepository, ChannelAdGroupRepository channelAdGroupRepository,
      ChannelFeedbackRepository channelFeedbackRepository,
      ChannelSegmentFeedbackRepository channelSegmentFeedbackRepository) {
    this.channelRepository = channelRepository;
    this.channelAdGroupRepository = channelAdGroupRepository;
    this.channelFeedbackRepository = channelFeedbackRepository;
    this.channelSegmentFeedbackRepository = channelSegmentFeedbackRepository;
  }

  public ChannelEntity queryChannelRepository(String channelId) {
    ChannelEntity channelEntity;
    try {
      channelEntity = channelRepository.query(channelId);
    } catch (RepositoryException e) {
      channelEntity = null;
    }
    return channelEntity;
  }

  public ChannelSegmentEntity queryChannelAdGroupRepository(String adGroupId) {
    ChannelSegmentEntity channelSegmentEntity;
    try {
      channelSegmentEntity = channelAdGroupRepository.query(adGroupId);
    } catch (RepositoryException e) {
      channelSegmentEntity = null;
    }
    return channelSegmentEntity;
  }

  public ChannelSegmentFeedbackEntity queryChannelSegmentFeedbackRepository(String adGroupId) {
    ChannelSegmentFeedbackEntity channelSegmentFeedbackEntity;
    try {
      channelSegmentFeedbackEntity = channelSegmentFeedbackRepository.query(adGroupId);
    } catch (RepositoryException e) {
      channelSegmentFeedbackEntity = null;
    }
    return channelSegmentFeedbackEntity;
  }

  public ChannelFeedbackEntity queryChannelFeedbackRepository(String advertiserId) {
    ChannelFeedbackEntity channelFeedbackEntity;
    try {
      channelFeedbackEntity = channelFeedbackRepository.query(advertiserId);
    } catch (RepositoryException e) {
      channelFeedbackEntity = null;
    }
    return channelFeedbackEntity;
  }

}
