package com.inmobi.adserve.channels.server;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.testng.annotations.Test;

import com.inmobi.adserve.channels.entity.*;
import com.inmobi.adserve.channels.repository.*;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.phoenix.exception.RepositoryException;
import com.inmobi.adserve.channels.server.HttpRequestHandler.ChannelSegment;
import junit.framework.TestCase;

public class FilterTest extends TestCase {

  Configuration mockConfig;
  Configuration mockAdapterConfig;
  private DebugLogger logger;
  private RepositoryHelper repositoryHelper;
  private ChannelEntity cE1;
  private ChannelEntity cE2;
  private ChannelEntity cE3;
  private ChannelFeedbackEntity cFE1;
  private ChannelFeedbackEntity cFE2;
  private ChannelFeedbackEntity cFE3;
  private ChannelSegmentFeedbackEntity cSFE1;
  private ChannelSegmentFeedbackEntity cSFE2;
  private ChannelSegmentFeedbackEntity cSFE3;
  private ChannelSegmentFeedbackEntity cSFE4;
  private ChannelSegmentFeedbackEntity cSFE5;
  private ChannelSegmentFeedbackEntity cSFE6;
  private ChannelSegmentEntity channelSegmentEntity1;
  private ChannelSegmentEntity channelSegmentEntity2;
  private ChannelSegmentEntity channelSegmentEntity3;
  private ChannelSegmentEntity channelSegmentEntity4;
  private ChannelSegmentEntity channelSegmentEntity5;
  private ChannelSegmentEntity channelSegmentEntity6;

  public void setUp() {
    mockConfig = createMock(Configuration.class);
    mockAdapterConfig = createMock(Configuration.class);
    ArrayList<String> arr = new ArrayList<String>();
    arr.add("atnt.advertiserId");
    arr.add("mobilecommerce.advertiserId");
    arr.add("drawbridge.advertiserId");
    Iterator<String> itr = arr.iterator();
    expect(mockAdapterConfig.getKeys()).andReturn(itr).anyTimes();
    expect(mockAdapterConfig.getString("atnt.advertiserId")).andReturn("1").anyTimes();
    expect(mockAdapterConfig.getInt("atnt.partnerSegmentNo", 2)).andReturn(1).anyTimes();
    expect(mockAdapterConfig.getString("mobilecommerce.advertiserId")).andReturn("3").anyTimes();
    expect(mockAdapterConfig.getInt("mobilecommerce.partnerSegmentNo", 2)).andReturn(3).anyTimes();
    expect(mockAdapterConfig.getString("drawbridge.advertiserId")).andReturn("4").anyTimes();
    expect(mockAdapterConfig.getInt("drawbridge.partnerSegmentNo", 2)).andReturn(4).anyTimes();
    expect(mockAdapterConfig.getString("mullahmedia.advertiserId")).andReturn("5").anyTimes();
    expect(mockAdapterConfig.getInt("mullahmedia.partnerSegmentNo", 2)).andReturn(5).anyTimes();
    expect(mockAdapterConfig.getString("openx.advertiserId")).andReturn("6").anyTimes();
    expect(mockAdapterConfig.getInt("openx.partnerSegmentNo", 2)).andReturn(6).anyTimes();
    expect(mockAdapterConfig.getString("ifd.advertiserId")).andReturn("7").anyTimes();
    expect(mockAdapterConfig.getInt("ifd.partnerSegmentNo", 2)).andReturn(7).anyTimes();
    expect(mockAdapterConfig.getString("tapit.advertiserId")).andReturn("8").anyTimes();
    expect(mockAdapterConfig.getInt("tapit.partnerSegmentNo", 2)).andReturn(8).anyTimes();
    expect(mockAdapterConfig.getString("ifc.advertiserId")).andReturn("9").anyTimes();
    expect(mockAdapterConfig.getInt("ifc.partnerSegmentNo", 2)).andReturn(9).anyTimes();
    expect(mockAdapterConfig.getString("webmoblink.advertiserId")).andReturn("10").anyTimes();
    expect(mockAdapterConfig.getInt("webmoblink.partnerSegmentNo", 2)).andReturn(10).anyTimes();
    expect(mockAdapterConfig.getString("siquis.advertiserId")).andReturn("11").anyTimes();
    expect(mockAdapterConfig.getInt("siquis.partnerSegmentNo", 2)).andReturn(11).anyTimes();
    expect(mockAdapterConfig.getString("huntmads.advertiserId")).andReturn("12").anyTimes();
    expect(mockAdapterConfig.getInt("huntmads.partnerSegmentNo", 2)).andReturn(12).anyTimes();
    expect(mockAdapterConfig.getString("httpool.advertiserId")).andReturn("13").anyTimes();
    expect(mockAdapterConfig.getInt("httpool.partnerSegmentNo", 2)).andReturn(13).anyTimes();    
    replay(mockAdapterConfig);
    repositoryHelper = createMock(RepositoryHelper.class);
    cE1 = new ChannelEntity();
    cE1.setId("advertiserId1").setPriority(1).setImpressionCeil(90).setName("name1");
    cE2 = new ChannelEntity();
    cE2.setId("advertiserId2").setPriority(1).setImpressionCeil(90).setName("name2");
    cE3 = new ChannelEntity();
    cE3.setId("advertiserId3").setPriority(1).setImpressionCeil(90).setName("name3");
    cFE1 = new ChannelFeedbackEntity("advertiserId1", 100.0, 50.0, 50.0, 100, 95, 1.0, 4.0);
    cFE2 = new ChannelFeedbackEntity("advertiserId2", 100.0, 95.0, 5.0, 100, 55, 2.0, 0.6);
    cFE3 = new ChannelFeedbackEntity("advertiserId2", 100.0, 50.0, 50.0, 100, 55, 1.0, 4.0);
    cSFE1 = new ChannelSegmentFeedbackEntity("advertiserId1", "adgroupId1", 0.29, 0.1);
    cSFE2 = new ChannelSegmentFeedbackEntity("advertiserId1", "adgroupId2", 0.9, 0.1);
    cSFE3 = new ChannelSegmentFeedbackEntity("advertiserId1", "adgroupId3", 0.4, 0.1);
    cSFE4 = new ChannelSegmentFeedbackEntity("advertiserId2", "adgroupId4", 0.2, 0.1);
    cSFE5 = new ChannelSegmentFeedbackEntity("advertiserId2", "adgroupId5", 0.5, 0.1);
    cSFE6 = new ChannelSegmentFeedbackEntity("advertiserId3", "adgroupId6", 0.7, 0.1);
    expect(mockConfig.getString("debug")).andReturn("debug").anyTimes();
    expect(mockConfig.getString("loggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties").anyTimes();
    expect(mockConfig.getInt("partnerSegmentNo", 3)).andReturn(2).anyTimes();
    expect(mockConfig.getInt("totalSegmentNo")).andReturn(5).anyTimes();
    expect(mockConfig.getDouble("revenueWindow", 0.33)).andReturn(10.0).anyTimes();
    expect(mockConfig.getDouble("ecpmShift", 0.1)).andReturn(0.0).anyTimes();
    expect(mockConfig.getDouble("feedbackPower", 2.0)).andReturn(1.0).anyTimes();
    expect(repositoryHelper.queryChannelRepository("channelId1")).andReturn(cE1).anyTimes();
    expect(repositoryHelper.queryChannelRepository("channelId2")).andReturn(cE2).anyTimes();
    expect(repositoryHelper.queryChannelRepository("channelId3")).andReturn(cE3).anyTimes();

    expect(repositoryHelper.queryChannelFeedbackRepository("advertiserId1")).andReturn(cFE1).anyTimes();
    expect(repositoryHelper.queryChannelFeedbackRepository("advertiserId2")).andReturn(cFE2).anyTimes();
    expect(repositoryHelper.queryChannelFeedbackRepository("advertiserId3")).andReturn(cFE3).anyTimes();
    expect(repositoryHelper.queryChannelSegmentFeedbackRepository("adgroupId1")).andReturn(cSFE1).anyTimes();
    expect(repositoryHelper.queryChannelSegmentFeedbackRepository("adgroupId2")).andReturn(cSFE2).anyTimes();
    expect(repositoryHelper.queryChannelSegmentFeedbackRepository("adgroupId3")).andReturn(cSFE3).anyTimes();
    expect(repositoryHelper.queryChannelSegmentFeedbackRepository("adgroupId4")).andReturn(cSFE4).anyTimes();
    expect(repositoryHelper.queryChannelSegmentFeedbackRepository("adgroupId5")).andReturn(cSFE5).anyTimes();
    expect(repositoryHelper.queryChannelSegmentFeedbackRepository("adgroupId6")).andReturn(cSFE6).anyTimes();

    Long[] rcList = null;
    Long[] tags = null;
    Timestamp modified_on = null;
    Long[] slotIds = null;
    Integer[] siteRatings = null;
    channelSegmentEntity1 = new ChannelSegmentEntity("advertiserId1", "adgroupId1", "adId", "channelId1", (long) 1, rcList, tags, true, true,
        "externalSiteKey", modified_on, "campaignId", slotIds, (long) 1, true, "pricingModel", siteRatings, 1, null);
    channelSegmentEntity2 = new ChannelSegmentEntity("advertiserId1", "adgroupId2", "adId", "channelId1", (long) 0, rcList, tags, false, true,
        "externalSiteKey", modified_on, "campaignId", slotIds, (long) 1, true, "pricingModel", siteRatings, 1, null);
    channelSegmentEntity3 = new ChannelSegmentEntity("advertiserId1", "adgroupId3", "adId", "channelId1", (long) 1, rcList, tags, false, false,
        "externalSiteKey", modified_on, "campaignId", slotIds, (long) 0, false, "pricingModel", siteRatings, 0, null);
    channelSegmentEntity4 = new ChannelSegmentEntity("advertiserId2", "adgroupId4", "adId", "channelId2", (long) 1, rcList, tags, true, true,
        "externalSiteKey", modified_on, "campaignId", slotIds, (long) 1, true, "pricingModel", siteRatings, 1, null);
    channelSegmentEntity5 = new ChannelSegmentEntity("advertiserId2", "adgroupId5", "adId", "channelId2", (long) 1, rcList, tags, true, true,
        "externalSiteKey", modified_on, "campaignId", slotIds, (long) 1, true, "pricingModel", siteRatings, 1, null);
    channelSegmentEntity6 = new ChannelSegmentEntity("advertiserId3", "adgroupId6", "adId", "channelId3", (long) 1, rcList, tags, true, true,
        "externalSiteKey", modified_on, "campaignId", slotIds, (long) 1, true, "pricingModel", siteRatings, 1, null);

    expect(repositoryHelper.queryChannelAdGroupRepository("adgroupId1")).andReturn(channelSegmentEntity1).anyTimes();
    expect(repositoryHelper.queryChannelAdGroupRepository("adgroupId2")).andReturn(channelSegmentEntity2).anyTimes();
    expect(repositoryHelper.queryChannelAdGroupRepository("adgroupId3")).andReturn(channelSegmentEntity3).anyTimes();
    expect(repositoryHelper.queryChannelAdGroupRepository("adgroupId4")).andReturn(channelSegmentEntity4).anyTimes();
    expect(repositoryHelper.queryChannelAdGroupRepository("adgroupId5")).andReturn(channelSegmentEntity5).anyTimes();
    expect(repositoryHelper.queryChannelAdGroupRepository("adgroupId6")).andReturn(channelSegmentEntity6).anyTimes();

    replay(repositoryHelper);
    replay(mockConfig);
    DebugLogger.init(mockConfig);
    logger = new DebugLogger();
    Filters.init(mockConfig, mockAdapterConfig, repositoryHelper, new InspectorStats());
  }

  @Test
  public void testImpressionBurnFilter() {
    Long[] rcList = null;
    Long[] tags = null;
    Timestamp modified_on = null;
    Long[] slotIds = null;
    Integer[] siteRatings = null;
    ChannelSegmentEntity channelSegmentEntity1 = new ChannelSegmentEntity("advertiserId1", "adgroupId1", "adId", "channelId1", (long) 1, rcList, tags, true,
        true, "externalSiteKey", modified_on, "campaignId", slotIds, (long) 1, true, "pricingModel", siteRatings, 1, null);
    ChannelSegmentEntity channelSegmentEntity2 = new ChannelSegmentEntity("advertiserId1", "adgroupId2", "adId", "channelId1", (long) 0, rcList, tags, false,
        true, "externalSiteKey", modified_on, "campaignId", slotIds, (long) 1, true, "pricingModel", siteRatings, 1, null);
    ChannelSegmentEntity channelSegmentEntity3 = new ChannelSegmentEntity("advertiserId1", "adgroupId3", "adId", "channelId1", (long) 1, rcList, tags, false,
        false, "externalSiteKey", modified_on, "campaignId", slotIds, (long) 0, false, "pricingModel", siteRatings, 0, null);
    ChannelSegmentEntity channelSegmentEntity4 = new ChannelSegmentEntity("advertiserId2", "adgroupId4", "adId", "channelId2", (long) 1, rcList, tags, true,
        true, "externalSiteKey", modified_on, "campaignId", slotIds, (long) 1, true, "pricingModel", siteRatings, 1, null);
    ChannelSegmentEntity channelSegmentEntity5 = new ChannelSegmentEntity("advertiserId2", "adgroupId5", "adId", "channelId2", (long) 1, rcList, tags, true,
        true, "externalSiteKey", modified_on, "campaignId", slotIds, (long) 1, true, "pricingModel", siteRatings, 1, null);
    ChannelSegmentEntity channelSegmentEntity6 = new ChannelSegmentEntity("advertiserId3", "adgroupId5", "adId", "channelId3", (long) 1, rcList, tags, true,
        true, "externalSiteKey", modified_on, "campaignId", slotIds, (long) 1, true, "pricingModel", siteRatings, 1, null);

    HashMap<String, HashMap<String, ChannelSegmentEntity>> matchedSegments = new HashMap<String, HashMap<String, ChannelSegmentEntity>>();
    HashMap<String, ChannelSegmentEntity> adv1 = new HashMap<String, ChannelSegmentEntity>();
    HashMap<String, ChannelSegmentEntity> adv2 = new HashMap<String, ChannelSegmentEntity>();
    HashMap<String, ChannelSegmentEntity> adv3 = new HashMap<String, ChannelSegmentEntity>();

    adv1.put(channelSegmentEntity1.getAdgroupId(), channelSegmentEntity1);
    adv1.put(channelSegmentEntity2.getAdgroupId(), channelSegmentEntity2);
    adv1.put(channelSegmentEntity3.getAdgroupId(), channelSegmentEntity3);
    adv2.put(channelSegmentEntity4.getAdgroupId(), channelSegmentEntity4);
    adv2.put(channelSegmentEntity5.getAdgroupId(), channelSegmentEntity5);
    adv3.put(channelSegmentEntity6.getAdgroupId(), channelSegmentEntity6);
    matchedSegments.put(channelSegmentEntity1.getId(), adv1);
    matchedSegments.put(channelSegmentEntity4.getId(), adv2);
    matchedSegments.put(channelSegmentEntity6.getId(), adv3);

    matchedSegments = Filters.impressionBurnFilter(matchedSegments, logger);

    assertEquals(false, matchedSegments.containsKey(channelSegmentEntity1.getId()));
    assertEquals(false, matchedSegments.containsKey(channelSegmentEntity4.getId()));
    assertEquals(true, matchedSegments.containsKey(channelSegmentEntity6.getId()));
  }

  @Test
  public void testPartnerSegmentCountFilter() {
    Long[] rcList = null;
    Long[] tags = null;
    Timestamp modified_on = null;
    Long[] slotIds = null;
    Integer[] siteRatings = null;
    ChannelSegmentEntity channelSegmentEntity1 = new ChannelSegmentEntity("advertiserId1", "adgroupId1", "adId", "channelId", (long) 1, rcList, tags, true,
        true, "externalSiteKey", modified_on, "campaignId", slotIds, (long) 1, true, "pricingModel", siteRatings, 1, null);
    ChannelSegmentEntity channelSegmentEntity2 = new ChannelSegmentEntity("advertiserId1", "adgroupId2", "adId", "channelId", (long) 0, rcList, tags, false,
        true, "externalSiteKey", modified_on, "campaignId", slotIds, (long) 1, true, "pricingModel", siteRatings, 1, null);
    ChannelSegmentEntity channelSegmentEntity3 = new ChannelSegmentEntity("advertiserId1", "adgroupId3", "adId", "channelId", (long) 1, rcList, tags, false,
        false, "externalSiteKey", modified_on, "campaignId", slotIds, (long) 0, false, "pricingModel", siteRatings, 0, null);
    ChannelSegmentEntity channelSegmentEntity4 = new ChannelSegmentEntity("advertiserId2", "adgroupId4", "adId", "channelId", (long) 1, rcList, tags, true,
        true, "externalSiteKey", modified_on, "campaignId", slotIds, (long) 1, true, "pricingModel", siteRatings, 1, null);
    ChannelSegmentEntity channelSegmentEntity5 = new ChannelSegmentEntity("advertiserId2", "adgroupId5", "adId", "channelId", (long) 1, rcList, tags, true,
        true, "externalSiteKey", modified_on, "campaignId", slotIds, (long) 1, true, "pricingModel", siteRatings, 1, null);
    ChannelSegmentEntity channelSegmentEntity6 = new ChannelSegmentEntity("advertiserId3", "adgroupId6", "adId", "channelId", (long) 1, rcList, tags, true,
        true, "externalSiteKey", modified_on, "campaignId", slotIds, (long) 1, true, "pricingModel", siteRatings, 1, null);

    HashMap<String, HashMap<String, ChannelSegmentEntity>> matchedSegments = new HashMap<String, HashMap<String, ChannelSegmentEntity>>();
    HashMap<String, ChannelSegmentEntity> adv1 = new HashMap<String, ChannelSegmentEntity>();
    HashMap<String, ChannelSegmentEntity> adv2 = new HashMap<String, ChannelSegmentEntity>();
    HashMap<String, ChannelSegmentEntity> adv3 = new HashMap<String, ChannelSegmentEntity>();

    adv1.put(channelSegmentEntity1.getAdgroupId(), channelSegmentEntity1);
    adv1.put(channelSegmentEntity2.getAdgroupId(), channelSegmentEntity2);
    adv1.put(channelSegmentEntity3.getAdgroupId(), channelSegmentEntity3);
    adv2.put(channelSegmentEntity4.getAdgroupId(), channelSegmentEntity4);
    adv2.put(channelSegmentEntity5.getAdgroupId(), channelSegmentEntity5);
    adv3.put(channelSegmentEntity6.getAdgroupId(), channelSegmentEntity6);
    matchedSegments.put(channelSegmentEntity1.getId(), adv1);
    matchedSegments.put(channelSegmentEntity4.getId(), adv2);
    matchedSegments.put(channelSegmentEntity6.getId(), adv3);

    matchedSegments = Filters.partnerSegmentCountFilter(matchedSegments, 0.3, logger);

    assertEquals(false, matchedSegments.get("advertiserId1").containsKey("adgroupId1"));
    assertEquals(2, matchedSegments.get("advertiserId1").size());
    assertEquals(1, matchedSegments.get("advertiserId2").size());
    assertEquals(1, matchedSegments.get("advertiserId3").size());
    assertEquals(false, matchedSegments.get("advertiserId1").containsKey("adgroupId1"));
    assertEquals(false, matchedSegments.get("advertiserId2").containsKey("adgroupId4"));
  }

  @Test
  public void testSegmentsPerRequestFilter() {

    HashMap<String, HashMap<String, ChannelSegmentEntity>> matchedSegments = new HashMap<String, HashMap<String, ChannelSegmentEntity>>();
    HashMap<String, ChannelSegmentEntity> adv1 = new HashMap<String, ChannelSegmentEntity>();
    HashMap<String, ChannelSegmentEntity> adv2 = new HashMap<String, ChannelSegmentEntity>();
    HashMap<String, ChannelSegmentEntity> adv3 = new HashMap<String, ChannelSegmentEntity>();

    adv1.put(channelSegmentEntity1.getAdgroupId(), channelSegmentEntity1);
    adv1.put(channelSegmentEntity2.getAdgroupId(), channelSegmentEntity2);
    adv1.put(channelSegmentEntity3.getAdgroupId(), channelSegmentEntity3);
    adv2.put(channelSegmentEntity4.getAdgroupId(), channelSegmentEntity4);
    adv2.put(channelSegmentEntity5.getAdgroupId(), channelSegmentEntity5);
    adv3.put(channelSegmentEntity6.getAdgroupId(), channelSegmentEntity6);
    matchedSegments.put(channelSegmentEntity1.getId(), adv1);
    matchedSegments.put(channelSegmentEntity4.getId(), adv2);
    matchedSegments.put(channelSegmentEntity6.getId(), adv3);

    List<ChannelSegmentEntity> rows = new ArrayList<ChannelSegmentEntity>();
    for (String advertiserId : matchedSegments.keySet()) {
      for (String adgroupId : matchedSegments.get(advertiserId).keySet()) {
        rows.add(matchedSegments.get(advertiserId).get(adgroupId));
      }
    }
    ChannelSegmentEntity[] finalRow = (ChannelSegmentEntity[]) rows.toArray(new ChannelSegmentEntity[0]);
    finalRow = Filters.segmentsPerRequestFilter(matchedSegments, finalRow, logger);

    assertEquals(5, finalRow.length);
    assertEquals("adgroupId2", finalRow[0].getAdgroupId());
    assertEquals("adgroupId1", finalRow[4].getAdgroupId());

    /*
     * List<ChannelSegment> channelSegments = new ArrayList<ChannelSegment>();
     * for (ChannelSegmentEntity channelSegmentEntity : finalRow) {
     * HttpRequestHandler httpRequestHandler = new HttpRequestHandler();
     * channelSegments.add(httpRequestHandler.new
     * ChannelSegment(channelSegmentEntity, null,
     * repositoryHelper.queryChannelRepository
     * (channelSegmentEntity.getChannelId()), repositoryHelper
     * .queryChannelSegmentFeedbackRepository
     * (channelSegmentEntity.getAdgroupId()))); }
     * 
     * channelSegments = Filters.rankAdapters(channelSegments, logger);
     * 
     * for(ChannelSegment channelSegment : channelSegments ) {
     * logger.debug("AdvertiserId: " +
     * channelSegment.channelSegmentFeedbackEntity.getAdvertiserId() +
     * " AdgroupId " + channelSegment.channelSegmentFeedbackEntity.getId() +
     * " ecpm " + channelSegment.channelSegmentFeedbackEntity.geteCPM() +
     * " Pecpm " +
     * channelSegment.channelSegmentFeedbackEntity.getPrioritisedECPM() +
     * " Low priority range " + channelSegment.lowerPriorityRange +
     * " high priority range " + channelSegment.higherPriorityRange); }
     */
  }
  @Test
  public void testGetPartnerSpecificSegmentNo() {
    assertEquals(1, Filters.getPartnerSpecificSegmentNo("1"));
    assertEquals(3, Filters.getPartnerSpecificSegmentNo("2"));
    assertEquals(3, Filters.getPartnerSpecificSegmentNo("3"));
    assertEquals(4, Filters.getPartnerSpecificSegmentNo("4"));
  }
}
