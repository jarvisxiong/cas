package com.inmobi.adserve.channels.api;

import java.net.URI;
import java.util.List;
import java.util.Map;

import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.casthrift.ADCreativeType;


public interface AdNetworkInterface {

    // Returns the Adstatus.
    String getAdStatus();

    // Return the latency.
    long getLatency();

    // Return the creative id.
    String getCreativeId();

    // Return the sample image url.
    String getIUrl();

    // Return the creative attributes.
    List<Integer> getAttribute();

    // Return the advertiser domains.
    List<String> getADomain();

    // Returns whether to log creative or not
    String getAdMarkUp();
    
    ADCreativeType getCreativeType();

    // Returns whether to log creative or not
    boolean isLogCreative();

    // Set whether creative logging is required or not
    void setLogCreative(boolean logCreative);

    // Return the bid price for rtb, for other will return the -1.
    double getBidPriceInUsd();

    // Returns teh bid price for rtbd in local currency in which partner has bid
    double getBidPriceInLocal();

    // Sets the secondBid price after running the auction.
    // Sets the price in both local and USD currency(Auction currency)
    void setSecondBidPrice(final Double price);

    // Returns the second bid price after auctioning.
    double getSecondBidPriceInUsd();

    // Returns the second bid price in local currency after auction
    double getSecondBidPriceInLocal();

    // Return bidder currency
    String getCurrency();

    // Returns true for rtb partner, false otherwise.
    boolean isRtbPartner();

    //Returns true for ix partner, false otherwise.
    boolean isIxPartner();

    // Returns auction id sent in the rtb response
    String getAuctionId();

    // Returns impression Id sent in the rtb response
    String getRtbImpressionId();

    // Returns seat id sent in the rtb response
    String getSeatId();

    // Returns the name of the third party ad network.
    String getName();

    // Returns the Channel Id for the TPAN as in our database.
    String getId();

    // Updates the request parameters according to the Ad Network. Returns true on
    // success.
    boolean configureParameters(final SASRequestParameters param,
            final CasInternalRequestParameters casInternalRequestParameters, final ChannelSegmentEntity entity,
            final String clickUrl, final String beaconUrl);

    // Makes asynchronous request to Ad Network server. Returns true on success.
    boolean makeAsyncRequest();

    // Constructs the request url
    URI getRequestUri() throws Exception;

    // whether click url is used by adapter
    boolean isBeaconUrlRequired();

    // whether click url is used by adapter
    boolean isClickUrlRequired();

    // Returns true if the adapter is an internal partner.
    boolean isInternal();

    // Called after the adapter is selected for impression.
    void impressionCallback();

    // Called after the adapter is not selected for impression.
    void noImpressionCallBack();

    // get click url
    String getClickUrl();

    // get Impression Id
    String getImpressionId();

    // Returns true if request is completed.
    boolean isRequestCompleted();

    // Constructs the response from status and content.
    ThirdPartyAdResponse getResponseAd();

    // get request url
    String getRequestUrl();

    // get Response content
    String getHttpResponseContent();

    Map getResponseHeaders();

    // Does the clean up for the channels and closes the port.
    void cleanUp();

    // return response Struct
    ThirdPartyAdResponse getResponseStruct();

    // return connection latency
    long getConnectionLatency();

    boolean useJsAdTag();

    void setEncryptedBid(final String encryptedBid);

    void generateJsAdResponse();

    void setName(final String adapterName);

    // Get Demand Source Type
    int getDst();
}
