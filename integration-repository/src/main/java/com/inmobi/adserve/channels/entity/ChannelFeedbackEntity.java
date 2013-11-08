package com.inmobi.adserve.channels.entity;

import com.inmobi.phoenix.batteries.data.IdentifiableEntity;
import lombok.Getter;
import lombok.Setter;


@Getter
public class ChannelFeedbackEntity implements IdentifiableEntity<String>
{

    private static final long serialVersionUID = 1L;

    private final String      advertiserId;
    private final double      totalInflow;
    private final double      totalBurn;
    private final double      balance;
    private final int         totalImpressions;
    private final int         todayRequests;
    private final int         todayImpressions;
    private final double      averageLatency;
    private final double      revenue;

    public ChannelFeedbackEntity(Builder builder)
    {
        this.advertiserId = builder.advertiserId;
        this.totalInflow = builder.totalInflow;
        this.totalBurn = builder.totalBurn;
        this.balance = builder.balance;
        this.totalImpressions = builder.totalImpressions;
        this.todayRequests = builder.todayRequests;
        this.todayImpressions = builder.todayImpressions;
        this.averageLatency = builder.averageLatency;
        this.revenue = builder.revenue;
    }

    public static Builder newBuilder()
    {
        return new Builder();
    }

    @Setter
    public static class Builder
    {
        private String advertiserId;
        private double totalInflow;
        private double totalBurn;
        private double balance;
        private int    totalImpressions;
        private int    todayRequests;
        private int    todayImpressions;
        private double averageLatency;
        private double revenue;

        public ChannelFeedbackEntity build()
        {
            return new ChannelFeedbackEntity(this);
        }
    }

    @Override
    public String getId()
    {
        return advertiserId;
    }

    @Override
    public String getJSON()
    {
        return null;
    }

}
