package com.inmobi.adserve.channels.server.requesthandler.filters.adgroup;

import java.util.List;

import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.server.beans.CasContext;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.server.requesthandler.filters.ChannelSegmentFilter;


/**
 * @author abhishek.parwal
 * 
 */
public interface AdGroupLevelFilter extends ChannelSegmentFilter {

  /**
   * @param channelSegments
   * @param sasParams
   * @param casContext
   */
  void filter(final List<ChannelSegment> channelSegments, final SASRequestParameters sasParams,
      final CasContext casContext);

}
