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
 */
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@GsonContract
public final class SeatBid {
    @NonNull @Required private List<Bid> bid;
    // DSP Ids
    private String buyer;
    // Trading Desk Ids
    private String seat;
    private CommonExtension ext;
}
