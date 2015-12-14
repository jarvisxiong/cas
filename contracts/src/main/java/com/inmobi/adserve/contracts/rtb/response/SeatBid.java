package com.inmobi.adserve.contracts.rtb.response;

import java.util.List;

import com.inmobi.template.gson.GsonContract;
import com.inmobi.template.gson.Required;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Created by avinash.kumar on 12/14/15.
 */
@Data
@NoArgsConstructor
@RequiredArgsConstructor
@GsonContract
public final class SeatBid {
    @NonNull
    @Required
    private List<Bid> bid;
    // Trading Desk Ids
    private String seat;
    private Integer group;
}
