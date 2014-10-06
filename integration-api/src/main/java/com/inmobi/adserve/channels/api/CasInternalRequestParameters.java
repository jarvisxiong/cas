package com.inmobi.adserve.channels.api;

import com.inmobi.adserve.channels.types.AccountType;

import java.util.HashMap;
import java.util.List;


public class CasInternalRequestParameters {
    // Rtb Params
    public double       highestEcpm;
    public List<String> blockedIabCategories;
    public List<String> blockedAdvertisers;
    public AccountType  siteAccountType; //Whether site account is managed or selfserve
    public double       auctionBidFloor;
    public String       auctionId;
    public boolean      traceEnabled;

    public String       impressionId;
    public String       impressionIdForVideo;

    // Control enrichment params
    public String       gpid;
    public String       uid;               // udid
    public String       uidO1;
    public String       uidMd5;            // UM5
    public String       uidIFA;            // IDA
    public String       uidIFV;            // IDV
    public String       uidSO1;
    public String       uidIDUS1;
    public String       uidADT;
    public String       uidWC;
    public String       uuidFromUidCookie; //imuc_5
    public String       zipCode;
    public String       latLong;
    public String       appUrl;
}
