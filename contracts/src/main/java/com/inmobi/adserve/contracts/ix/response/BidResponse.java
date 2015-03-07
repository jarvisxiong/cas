package com.inmobi.adserve.contracts.ix.response;

import java.util.List;

import com.inmobi.adserve.contracts.ix.common.CommonExtension;
import com.inmobi.template.gson.GsonContract;
import com.inmobi.template.gson.Required;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * @author ritwik.kumar
 * Based on Exchange API Specification v1.12
 */
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@GsonContract
public final class BidResponse {
    @NonNull @Required private String id;
    @NonNull @Required private List<SeatBid> seatbid;
    private String bidid;
    private Integer statuscode;
    private CommonExtension ext;
}
