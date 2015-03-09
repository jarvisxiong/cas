package com.inmobi.adserve.channels.api;

import java.net.URI;
import java.util.List;
import java.util.Map;

import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.casthrift.ADCreativeType;
import com.inmobi.casthrift.DemandSourceType;

public interface AdNetworkInterface {

    /**
     * Returns the Adstatus.
     * 
     * @return
     */
    String getAdStatus();

    /**
     * Return the latency.
     * 
     * @return
     */
    long getLatency();

    /**
     * Return the creative id.
     * 
     * @return
     */
    String getCreativeId();

    /**
     * Return the sample image url.
     * 
     * @return
     */
    String getIUrl();

    /**
     * Return the creative attributes.
     * 
     * @return
     */
    List<Integer> getAttribute();

    /**
     * Return the advertiser domains.
     * 
     * @return
     */
    List<String> getADomain();

    // Returns the adMarkup
    String getAdMarkUp();

    /**
     * 
     * @return
     */
    ADCreativeType getCreativeType();

    /**
     * Returns whether to log creative or not
     * 
     * @return
     */
    boolean isLogCreative();

    /**
     * Set whether creative logging is required or not
     * 
     * @param logCreative
     */
    void setLogCreative(final boolean logCreative);

    /**
     * Return the bid price for rtb, for other will return the -1.
     * 
     * @return
     */
    double getBidPriceInUsd();

    /**
     * Returns teh bid price for rtbd in local currency in which partner has bid
     * 
     * @return
     */
    double getBidPriceInLocal();

    /**
     * Sets the secondBid price after running the auction. Sets the price in both local and USD currency(Auction
     * currency)
     * 
     * @param price
     */
    void setSecondBidPrice(final Double price);

    /**
     * Returns the second bid price after auctioning.
     * 
     * @return
     */
    double getSecondBidPriceInUsd();

    /**
     * Returns the second bid price in local currency after auction
     * 
     * @return
     */
    double getSecondBidPriceInLocal();

    /**
     * Return bidder currency
     * 
     * @return
     */
    String getCurrency();

    /**
     * Returns true for rtb partner, false otherwise.
     * 
     * @return
     */
    boolean isRtbPartner();

    /**
     * Returns true for ix partner, false otherwise.
     * 
     * @return
     */
    boolean isIxPartner();

    /**
     * Processes the response
     */
    void processResponse();

    /**
     * Sets the internal AD status
     * 
     * @param adStatus
     */
    void setAdStatus(String adStatus);

    /**
     * Returns auction id sent in the rtb response
     * 
     * @return
     */
    String getAuctionId();

    /**
     * Returns impression Id sent in the rtb response
     * 
     * @return
     */
    String getRtbImpressionId();

    /**
     * Returns seat id sent in the rtb response
     * 
     * @return
     */
    String getSeatId();

    /**
     * Returns the name of the third party ad network.
     * 
     * @return
     */
    String getName();

    /**
     * Returns the Channel Id for the TPAN as in our database.
     * 
     * @return
     */
    String getId();

    /**
     * Updates the request parameters according to the Ad Network. Returns true on success.
     * 
     * @param param
     * @param casInternalRequestParameters
     * @param entity
     * @param clickUrl
     * @param beaconUrl
     * @param SlotId
     * @param repositoryHelper
     * @return
     */
    boolean configureParameters(final SASRequestParameters param,
            final CasInternalRequestParameters casInternalRequestParameters, final ChannelSegmentEntity entity,
            final String clickUrl, final String beaconUrl, final long SlotId, final RepositoryHelper repositoryHelper);

    /**
     * Makes asynchronous request to Ad Network server. Returns true on success.
     * 
     * @return
     */
    boolean makeAsyncRequest();

    /**
     * Constructs the request url
     * 
     * @return
     * @throws Exception
     */
    URI getRequestUri() throws Exception;

    /**
     * whether click url is used by adapter
     * 
     * @return
     */
    boolean isBeaconUrlRequired();

    /**
     * whether click url is used by adapter
     * 
     * @return
     */
    boolean isClickUrlRequired();

    /**
     * Returns true if the adapter is an internal partner.
     * 
     * @return
     */
    boolean isInternal();

    /**
     * Called after the adapter is selected for impression.
     */
    void impressionCallback();

    /**
     * Called after the adapter is not selected for impression.
     */
    void noImpressionCallBack();

    /**
     * get click url
     * 
     * @return
     */
    String getClickUrl();

    /**
     * get Impression Id
     * 
     * @return
     */
    String getImpressionId();

    /**
     * Returns true if request is completed.
     * 
     * @return
     */
    boolean isRequestCompleted();

    /**
     * Constructs the response from status and content.
     * 
     * @return
     */
    ThirdPartyAdResponse getResponseAd();

    /**
     * get request url
     * 
     * @return
     */
    String getRequestUrl();

    /**
     * get Response content
     * 
     * @return
     */
    String getHttpResponseContent();

    /**
     * 
     * @return
     */
    Map<?, ?> getResponseHeaders();

    /**
     * Does the clean up for the channels and closes the port.
     */
    void cleanUp();

    /**
     * return response Struct
     * 
     * @return
     */
    ThirdPartyAdResponse getResponseStruct();

    /**
     * return connection latency
     * 
     * @return
     */
    long getConnectionLatency();

    /**
     * 
     * @return
     */
    boolean useJsAdTag();

    /**
     * 
     * @param encryptedBid
     */
    void setEncryptedBid(final String encryptedBid);

    /**
     * 
     */
    void generateJsAdResponse();

    /**
     * 
     * @param adapterName
     */
    void setName(final String adapterName);

    /**
     * 
     * @return
     */
    RepositoryHelper getRepositoryHelper();

    /**
     * 
     * @return
     */
    Short getSelectedSlotId();

    /**
     * Get Demand Source Type
     * 
     * @return
     */
    DemandSourceType getDst();

    /**
     * Disable IP Resolution
     * 
     * @param isIPResolutionDisabled
     */
    void disableIPResolution(boolean isIPResolutionDisabled);

    /**
     * 
     * @return
     */
    int getHttpResponseStatusCode();
}
