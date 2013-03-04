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
  public synchronized AdNetworkInterface runRtbSecondPriceAuctionEngine() {
    // Do not run auction 2 times.
    if(auctionComplete)
      return rtbResponse == null ? null : rtbResponse.getAdNetworkInterface();

    auctionComplete = true;
    logger.debug("Inside RTB auction engine");
    List<ChannelSegment> rtbList;
    // Apply rtb filters.
    rtbList = rtbFilters(rtbSegments);
    if(rtbList.size() == 0) {
      logger.debug("rtb segments are", Integer.valueOf(rtbList.size()));
      rtbResponse = null;
      logger.debug("returning from auction engine , winner is null");
      return null;
    } else if(rtbList.size() == 1) {
      logger.debug("rtb segments are", Integer.valueOf(rtbList.size()));
      rtbResponse = rtbList.get(0);
      secondBidPrice = Math.min(casInternalRequestParameters.rtbBidFloor, rtbResponse.getAdNetworkInterface()
          .getBidprice() * 0.9);
      rtbResponse.getAdNetworkInterface().setSecondBidPrice(secondBidPrice);
      logger.debug("completed auction and winner is", rtbList.get(0).getAdNetworkInterface().getName()
          + " and secondBidPrice is " + secondBidPrice);
      return rtbList.get(0).getAdNetworkInterface();
    }

    logger.debug("rtb segments are", Integer.valueOf(rtbList.size()));
    for (int i = 0; i < rtbList.size(); i++) {
      for (int j = i + 1; j < rtbList.size(); j++) {
        if(rtbList.get(i).getAdNetworkInterface().getBidprice() < rtbList.get(j).getAdNetworkInterface().getBidprice()) {
          ChannelSegment channelSegment = rtbList.get(i);
          rtbList.set(i, rtbList.get(j));
          rtbList.set(j, channelSegment);
        }
      }
    }
    double maxPrice = rtbList.get(0).getAdNetworkInterface().getBidprice();
    int secondHighestBidNumber = 1;
    int lowestLatency = 0;
    for (int i = 1; i < rtbList.size(); i++) {
      if(rtbList.get(i).getAdNetworkInterface().getBidprice() < maxPrice) {
        secondHighestBidNumber = i;
        break;
      } else if(rtbList.get(i).getAdNetworkInterface().getLatency() < rtbList.get(lowestLatency)
          .getAdNetworkInterface().getLatency())
        lowestLatency = i;
    }
    if(secondHighestBidNumber != 1) {
      double secondHighestBidPrice = rtbList.get(secondHighestBidNumber).getAdNetworkInterface().getBidprice();
      double price = maxPrice * 0.9;
      if(price > secondHighestBidPrice) {
        secondBidPrice = price;
      } else {
        secondBidPrice = secondHighestBidPrice + 0.01;
      }
    } else {
      secondBidPrice = rtbList.get(1).getAdNetworkInterface().getBidprice() + 0.01;
    }
    rtbResponse = rtbList.get(lowestLatency);
    secondBidPrice = Math.min(secondBidPrice, rtbResponse.getAdNetworkInterface().getBidprice());
    rtbResponse.getAdNetworkInterface().setSecondBidPrice(secondBidPrice);
    logger.debug("completed auction and winner is", rtbList.get(lowestLatency).getAdNetworkInterface().getName()
        + " and secondBidPrice is " + secondBidPrice);
    return rtbList.get(lowestLatency).getAdNetworkInterface();
  }

  public List<ChannelSegment> rtbFilters(List<ChannelSegment> rtbSegments) {
    List<ChannelSegment> rtbList = new ArrayList<ChannelSegment>();
    logger.debug("No of rtb partners who sent response are", Integer.valueOf(rtbList.size()));
    // Ad filter.
    for (int i = 0; i < rtbSegments.size(); i++) {
      if(rtbSegments.get(i).getAdNetworkInterface().getAdStatus().equalsIgnoreCase("AD")) {
        logger.debug("Dropped in NO AD filter", rtbSegments.get(i).getAdNetworkInterface().getName());
        rtbList.add(rtbSegments.get(i));
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
    logger
        .debug("No of rtb partners who sent AD response with bid more than bidFloor", Integer.valueOf(rtbList.size()));
    // Bid not zero filter.
    for (int i = 0; i < rtbList.size(); i++) {
      if(rtbList.get(i).getAdNetworkInterface().getBidprice() <= 0) {
        logger.debug("Dropped in bid is zero filter", rtbList.get(i).getAdNetworkInterface().getName());
        rtbList.remove(rtbList.get(i));
      }
    }
    logger.debug("No of rtb partners who sent AD response with bid more than 0", Integer.valueOf(rtbList.size()));
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
    if(rtbSegments == null)
      return false;
    if(rtbSegments.size() == 0)
      return true;
    for (ChannelSegment channelSegment : rtbSegments) {
      if(!channelSegment.getAdNetworkInterface().isRequestCompleted())
        return false;
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
}
