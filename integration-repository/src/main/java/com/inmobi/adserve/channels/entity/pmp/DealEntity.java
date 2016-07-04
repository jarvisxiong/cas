package com.inmobi.adserve.channels.entity.pmp;

import static com.inmobi.adserve.channels.util.demand.enums.DealType.RIGHT_TO_FIRST_REFUSAL;

import java.util.Map;

import com.inmobi.adserve.channels.util.demand.enums.AuctionType;
import com.inmobi.adserve.channels.util.demand.enums.DealType;
import com.inmobi.casthrift.DemandSourceType;

import lombok.Builder;
import lombok.Getter;


@Getter
@Builder(builderClassName = "Builder", builderMethodName = "newBuilder")
public final class DealEntity {
    private final String id;
    private final Integer packageId;
    private final DemandSourceType dst;
    private String dsp;
    private double floor = 0.0;
    private String currency;
    private final DealType dealType;
    private AuctionType auctionType;
    private Map<String, String> thirdPartyTrackersMap;

    private boolean toBeBilledOnViewability;
    private Double agencyRebatePercentage;
    private Integer externalAgencyId;

    public final boolean isAgencyRebateToBeApplied() {
        return null != agencyRebatePercentage && agencyRebatePercentage > 0 && agencyRebatePercentage <= 100 && null != externalAgencyId;
    }

    public final boolean isTrumpDeal() {
        return RIGHT_TO_FIRST_REFUSAL == dealType;
    }
}
