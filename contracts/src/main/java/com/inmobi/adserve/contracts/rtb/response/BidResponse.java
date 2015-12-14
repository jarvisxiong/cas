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
public final class BidResponse {
    @NonNull
    @Required
    private String id;
    @NonNull @Required private List<SeatBid> seatbid;
    private String bidid;
    private String cur;
    private String customdata;
}
