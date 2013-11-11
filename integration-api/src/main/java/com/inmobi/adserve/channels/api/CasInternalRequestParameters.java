package com.inmobi.adserve.channels.api;

import java.util.List;


public class CasInternalRequestParameters {
    // Rtb Params
    public double       highestEcpm;
    public List<Long>   blockedCategories;
    public List<String> blockedAdvertisers;
    public double       rtbBidFloor;
    public String       auctionId;

    public String       impressionId;
    // Control enrichment params
    public String       uid;               // udid
    public String       uidO1;
    public String       uidMd5;            // UM5
    public String       uidIFA;            // IDA
    public String       uidIFV;            // IDV
    public String       uidSO1;
    public String       uidIDUS1;
    public String       uidADT;
    public String       zipCode;
    public String       latLong;
    public String       appUrl;
}
