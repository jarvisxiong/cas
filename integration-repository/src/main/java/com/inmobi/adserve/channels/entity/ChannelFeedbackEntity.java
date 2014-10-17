package com.inmobi.adserve.channels.entity;

import lombok.Getter;
import lombok.Setter;

import com.inmobi.phoenix.batteries.data.IdentifiableEntity;


@Getter
public class ChannelFeedbackEntity implements IdentifiableEntity<String> {

  private static final long serialVersionUID = 1L;

  private final String advertiserId;
  private final double totalInflow;
  private final double totalBurn;
  private final double balance;
  private final int totalImpressions;
  private final int todayRequests;
  private final int todayImpressions;
  private final double averageLatency;
  private final double revenue;

  public ChannelFeedbackEntity(final Builder builder) {
    advertiserId = builder.advertiserId;
    totalInflow = builder.totalInflow;
    totalBurn = builder.totalBurn;
    balance = builder.balance;
    totalImpressions = builder.totalImpressions;
    todayRequests = builder.todayRequests;
    todayImpressions = builder.todayImpressions;
    averageLatency = builder.averageLatency;
    revenue = builder.revenue;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  @Setter
  public static class Builder {
    private String advertiserId;
    private double totalInflow;
    private double totalBurn;
    private double balance;
    private int totalImpressions;
    private int todayRequests;
    private int todayImpressions;
    private double averageLatency;
    private double revenue;

    public ChannelFeedbackEntity build() {
      return new ChannelFeedbackEntity(this);
    }
  }

  @Override
  public String getId() {
    return advertiserId;
  }

  @Override
  public String getJSON() {
    return null;
  }

}
