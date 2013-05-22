package com.inmobi.adserve.channels.server;

import java.util.ArrayList;
import java.util.List;

import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.AuctionEngineInterface;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.util.DebugLogger;

/***
 * Auction Engine to run different types of auctions in rtb.
 * 
 * @author Devi Chand(devi.chand@inmobi.com)
 */
public class AuctionEngine implements AuctionEngineInterface {
  private boolean auctionComplete = false;
  private ChannelSegment rtbResponse;
  private double secondBidPrice;
  public SASRequestParameters sasParams;
  public CasInternalRequestParameters casInternalRequestParameters;
  private List<ChannelSegment> rtbSegments;
  private DebugLogger logger;

  public AuctionEngine(DebugLogger logger) {
    this.logger = logger;
  }

  /***
   * RunRtbSecondPriceAuctionEngine returns the adnetwork selected after
   * auctioning If no of rtb segments selected after filtering is zero it
   * returns the null If no of rtb segments selected after filtering is one it
   * returns the rtb adapter for the segment BidFloor is maximum of lowestEcpm
   * and siteFloor If only 2 rtb are selected, highest bid will win and would be
   * charged the secondHighest price If only 1 rtb is selected, it will be
   * selected for sending response and will be charged the highest of
   * secondHighest price or 90% of bidFloor
   */
  public AdNetworkInterface runRtbSecondPriceAuctionEngine() {
    // Do not run auction 2 times.
    synchronized (this) {
      if(auctionComplete) {
        return rtbResponse == null ? null : rtbResponse.getAdNetworkInterface();
      }
      auctionComplete = true;
    }
    
    
    logger.debug("Inside RTB auction engine");
    List<ChannelSegment> rtbList;
    // Apply rtb filters.
    rtbList = rtbFilters(rtbSegments);
    
    
    // Send null as auction response in case of 0 rtb responses.
    if(rtbList.size() == 0) {
      logger.debug("RTB segments are", Integer.valueOf(rtbList.size()));
      rtbResponse = null;
      logger.debug("Returning from auction engine , winner is none");
      return null;
    } else if(rtbList.size() == 1) {
      logger.debug("RTB segments are", Integer.valueOf(rtbList.size()));
      rtbResponse = rtbList.get(0);
      // Take minimum of rtbFloor+0.01 and bid as secondBidprice if no of rtb
      // response are 1.
      secondBidPrice = Math.min(casInternalRequestParameters.rtbBidFloor + 0.01,
          rtbResponse.getAdNetworkInterface().getBidprice());
      //Set encrypted bid price.
      rtbResponse.getAdNetworkInterface().setEncryptedBid(getEncryptedBid(secondBidPrice));
      rtbResponse.getAdNetworkInterface().setSecondBidPrice(secondBidPrice);
      logger.debug("Completed auction, winner is", rtbList.get(0).getAdNetworkInterface().getName(), "and secondBidPrice is", secondBidPrice);
      //Return as there is no need to iterate over the list.
      return rtbList.get(0).getAdNetworkInterface();
    }

    
    //Sort the list by their bid prices.
    logger.debug("RTB segments are", Integer.valueOf(rtbList.size()));
    for (int i = 0; i < rtbList.size(); i++) {
      for (int j = i + 1; j < rtbList.size(); j++) {
        if(rtbList.get(i).getAdNetworkInterface().getBidprice() < rtbList.get(j).getAdNetworkInterface().getBidprice()) {
          ChannelSegment channelSegment = rtbList.get(i);
          rtbList.set(i, rtbList.get(j));
          rtbList.set(j, channelSegment);
        }
      }
    }
    
    
    //Calculates the max price of all rtb responses.
    double maxPrice = rtbList.get(0).getAdNetworkInterface().getBidprice();
    int secondHighestBid = 1;//Keep secondHighestBidPrice number from rtb response list.
    int lowestLatencyBid = 0;//Keep winner number from rtb response list.
    for (int i = 1; i < rtbList.size(); i++) {
      if(rtbList.get(i).getAdNetworkInterface().getBidprice() < maxPrice) {
        secondHighestBid = i;
        break;
      } else if(rtbList.get(i).getAdNetworkInterface().getLatency() < rtbList.get(lowestLatencyBid)
          .getAdNetworkInterface().getLatency()) {
        lowestLatencyBid = i;
      }
    }
    
    //Set rtb response for the auction ran.
    rtbResponse = rtbList.get(lowestLatencyBid);
    
    
    //Calculates the secondHighestBidPrice if no of rtb responses are more than 1.
    secondBidPrice = rtbList.get(secondHighestBid).getAdNetworkInterface().getBidprice();
    double winnerBid = rtbList.get(lowestLatencyBid).getAdNetworkInterface().getBidprice();
    if(winnerBid == secondBidPrice) {
      secondBidPrice = casInternalRequestParameters.rtbBidFloor + 0.01;
    } else {
      secondBidPrice = secondBidPrice + 0.01;
    }
    
    //Ensure secondHighestBidPrice never crosses response bid.
    secondBidPrice = Math.min(secondBidPrice, rtbResponse.getAdNetworkInterface().getBidprice());
    rtbResponse.getAdNetworkInterface().setEncryptedBid(getEncryptedBid(secondBidPrice));
    rtbResponse.getAdNetworkInterface().setSecondBidPrice(secondBidPrice);
    logger.debug("Completed auction, winner is", rtbList.get(lowestLatencyBid).getAdNetworkInterface().getName(),
        "and secondBidPrice is", secondBidPrice);
    return rtbList.get(lowestLatencyBid).getAdNetworkInterface();
  }

  public List<ChannelSegment> rtbFilters(List<ChannelSegment> rtbSegments) {
    List<ChannelSegment> rtbList = new ArrayList<ChannelSegment>();
    logger.debug("No of rtb partners who sent response are", Integer.valueOf(rtbList.size()));
    // Ad filter.
    for (int i = 0; i < rtbSegments.size(); i++) {
      if(rtbSegments.get(i).getAdNetworkInterface().getAdStatus().equalsIgnoreCase("AD")) {
        rtbList.add(rtbSegments.get(i));
      } else {
        logger.debug("Dropped in NO AD filter", rtbSegments.get(i).getAdNetworkInterface().getName());
      }
    }
    logger.debug("No of rtb partners who sent AD response are", Integer.valueOf(rtbList.size()));
    // BidFloor filter.
    for (int i = 0; i < rtbList.size(); i++) {
      if(rtbList.get(i).getAdNetworkInterface().getBidprice() < casInternalRequestParameters.rtbBidFloor) {
        logger.debug("Dropped in bidfloor filter", rtbList.get(i).getAdNetworkInterface().getName());
        rtbList.remove(rtbList.get(i));
      }
    }
    logger.debug("No of rtb partners who sent AD response with bid more than bidFloor", rtbList.size());

    return rtbList;

  }

  public boolean isAuctionComplete() {
    return auctionComplete;
  }

  public ChannelSegment getRtbResponse() {
    return rtbResponse;
  }

  public double getSecondBidPrice() {
    return secondBidPrice;
  }

  @Override
  public boolean isAllRtbComplete() {
    if(rtbSegments == null) {
      return false;
    }
    if(rtbSegments.size() == 0) {
      return true;
    }
    for (ChannelSegment channelSegment : rtbSegments) {
      if(!channelSegment.getAdNetworkInterface().isRequestCompleted()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean isRtbResponseNull() {
    return rtbResponse == null ? true : false;
  }

  public List<ChannelSegment> getRtbSegments() {
    return rtbSegments;
  }

  public void setRtbSegments(List<ChannelSegment> rtbSegments) {
    this.rtbSegments = rtbSegments;
  }

  public String getEncryptedBid(Double bid) {
    long winBid = (long) (bid * Math.pow(10, 6));
    return AsyncRequestMaker.getImpressionId(winBid);
  }
  
  public double calculateRTBFloor(double segmentFloor, double countryFloor) {
    double rtbFloor = 0.0;
    rtbFloor = Math.max(sasParams.getSiteFloor(), casInternalRequestParameters.highestEcpm);
    rtbFloor = Math.max(rtbFloor, segmentFloor);
    rtbFloor = Math.max(rtbFloor, countryFloor);
    return rtbFloor;
  }
}
