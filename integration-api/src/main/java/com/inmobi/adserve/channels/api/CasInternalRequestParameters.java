package com.inmobi.adserve.channels.api;

import java.util.List;

import lombok.Data;

import com.inmobi.adserve.channels.types.AccountType;

@Data
public class CasInternalRequestParameters {
    // Rtb Params
    private double highestEcpm;
    private List<String> blockedIabCategories;
    private List<String> blockedAdvertisers;
    private AccountType siteAccountType; // Whether site account is managed or selfserve
    private double auctionBidFloor;
    private String auctionId;
    private boolean traceEnabled;

    private String impressionId;
    private String impressionIdForVideo;

    // Control enrichment params
    private String gpid;
    private String uid; // udid
    private String uidO1;
    private String uidMd5; // UM5
    private String uidIFA; // IDA
    private String uidIFV; // IDV
    private String uidSO1;
    private String uidIDUS1;
    private String uidADT;
    private String uidWC;
    private String uuidFromUidCookie; // imuc_5
    private String zipCode;
    private String latLong;
    private String appUrl;
    private double siteFloor;
}
