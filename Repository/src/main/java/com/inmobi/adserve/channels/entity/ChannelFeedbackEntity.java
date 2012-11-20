package com.inmobi.adserve.channels.entity;

import com.inmobi.phoenix.batteries.data.IdentifiableEntity;

public class ChannelFeedbackEntity implements IdentifiableEntity<String> {

  private static final long serialVersionUID = 1L;

  private String advertiserId;
  private double totalInflow;
  private double totalBurn;
  private double balance;
  private int totalImpressions;
  private int todayImpressions;
  private double averageLatency;
  private double revenue;

  public ChannelFeedbackEntity(String advertiserId, double totalInflow, double totalBurn, double balance, int totalImpressions, int todayImpressions,
      double averageLatency, double revenue) {
    this.advertiserId = advertiserId;
    this.totalInflow = totalInflow;
    this.totalBurn = totalBurn;
    this.balance = balance;
    this.totalImpressions = totalImpressions;
    this.todayImpressions = todayImpressions;
    this.averageLatency = averageLatency;
    this.revenue = revenue;
  }

  public double getRevenue() {
    return revenue;
  }

  @Override
  public String getId() {
    return advertiserId;
  }

  public String getAdvertiserId() {
    return advertiserId;
  }

  public double getTotalInflow() {
    return totalInflow;
  }

  public double getBurn() {
    return totalBurn;
  }

  public double getAverageLatency() {
    return averageLatency;
  }

  public int getTodayImpressions() {
    return todayImpressions;
  }

  public double getBalance() {
    return balance;
  }

  public int getTotalImpressions() {
    return totalImpressions;
  }

  @Override
  public String getJSON() {
    return null;
  }

}
