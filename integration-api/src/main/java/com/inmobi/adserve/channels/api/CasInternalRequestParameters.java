package com.inmobi.adserve.channels.api;

import java.util.List;

import com.inmobi.adserve.channels.types.AccountType;

import lombok.Data;

@Data
public class CasInternalRequestParameters {
    // Rtb Params
    private List<String> blockedIabCategories;
    private List<String> blockedAdvertisers;
    private AccountType siteAccountType; // Whether site account is managed or selfserve
    private double auctionBidFloor;
    private String auctionId;
    private boolean traceEnabled;

    // Auction Clearing Price Mechanics
    private double demandDensity; // Equivalent to Alpha*Omega
    private double longTermRevenue; // Equivalent to Beta*Omega
    private int publisherYield; // Equivalent to Gamma

    private String impressionId;

    // Control enrichment params
    private String gpid;
    private String uid; // udid
    private String uidO1;
    private String uidMd5; // UM5
    private String uidIFA; // IDA
    private String uidIFV; // IDV
    private String uidSO1; //remove
    private String uidIDUS1; //remove
    private boolean trackingAllowed = true;
    private String uidWC;
    private String uuidFromUidCookie; // imuc_5
    private String zipCode;
    private String latLong;
    private String appUrl;
    private double siteFloor;
    private String imeiMD5;
    private String imeiSHA1;
}
