package com.inmobi.adserve.channels.api;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.pmp.DealAttributionMetadata;
import com.inmobi.adserve.channels.entity.pmp.DealEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.util.demand.enums.AuctionType;
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
     * Sets the internal AD status
     *
     * @param adStatus
     */
    void setAdStatus(String adStatus);

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
     * Returns the bid price for rtbd in local currency in which partner has bid
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
     * @param adapterName
     */
    void setName(final String adapterName);

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
     * @param casParams
     * @param entity
     * @param SlotId
     * @param repositoryHelper
     * @return
     */
    boolean configureParameters(final SASRequestParameters param, final CasInternalRequestParameters casParams,
            final ChannelSegmentEntity entity, final long SlotId, final RepositoryHelper repositoryHelper);

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
     * Called after the adapter is selected for impression.
     */
    void impressionCallback();

    /**
     * get click url
     *
     * @return
     */
    String getClickUrl();

    /**
     * get beacon url
     *
     * @return
     */
    String getBeaconUrl();

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
     * @return
     */
    boolean useJsAdTag();

    /**
     * @param encryptedBid
     */
    void setEncryptedBid(final String encryptedBid);

    /**
     *
     */
    void generateJsAdResponse();

    /**
     * @return
     */
    RepositoryHelper getRepositoryHelper();

    /**
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
     * @return
     */
    int getHttpResponseStatusCode();

    /**
     * Returns the bid floor that was forwarded to the partner. Returns null if no value was forwarded.
     */
    Double getForwardedBidFloor();

    /**
     * Returns the bid guidance that was forwarded to the partner. Returns null if no value was forwarded.
     */
    Double getForwardedBidGuidance();

    /**
     * Returns the auction type of the bid
     */
    AuctionType getAuctionType();

    /**
     * Returns the winning deal entity
     */
    DealEntity getDeal();

    /**
     * Returns the deal attribution metadata object
     */
    DealAttributionMetadata getDealAttributionMetadata();

    /**
     * Returns the list of packages that were forwarded in the bid request.
     */
    Set<Integer> getForwardedPackageIds();

    /**
     * Returns the list of all targeting segments that were shorlisted
     */
    Set<Long> getShortlistedTargetingSegmentIds();

    /**
     * Returns the list of deals that were forwarded in the bid request.
     */
    Set<String> getForwardedDealIds();
    
    /**
     * Get App Bundle Id from UAC.
     * 
     * @param useAppBundleIdOfRequest - If UAC Bundle Id not available fall back to request bundle id
     * @return
     */
    String getAppBundleId(boolean useAppBundleIdOfRequest);

    /**
     * Returns the host name
     */
    String getHostName();
}
