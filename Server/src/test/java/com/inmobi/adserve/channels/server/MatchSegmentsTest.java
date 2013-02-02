package com.inmobi.adserve.channels.server;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;

import java.sql.Timestamp;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.SiteTaxonomyEntity;
import com.inmobi.adserve.channels.repository.ChannelAdGroupRepository;
import com.inmobi.adserve.channels.repository.ChannelRepository;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.InspectorStats;

public class MatchSegmentsTest extends TestCase {
/*
  MatchSegments segments;
  ChannelSegmentCache cache;
  private DebugLogger logger;
  ChannelAdGroupRepository channelAdGroupRepository;
  Configuration mockConfig;
  ChannelSegmentEntity channelSegmentEntity;
  ChannelSegmentEntity channelSegmentEntity1;
  ChannelSegmentEntity channelSegmentEntity2;
  ChannelEntity channelEntity;
  private RepositoryHelper repoHelper;

  public void setUp() {
    mockConfig = createMock(Configuration.class);
    repoHelper = createMock(RepositoryHelper.class);
    channelEntity = new ChannelEntity();
    channelEntity.setName("Channel");
    expect(repoHelper.queryChannelRepository("channelId")).andReturn(channelEntity).anyTimes();
    replay(repoHelper);
    expect(mockConfig.getString("debug")).andReturn("debug").anyTimes();
    expect(mockConfig.getString("loggerConf")).andReturn("/tmp/channel-server.properties").anyTimes();
    replay(mockConfig);
    DebugLogger.init(mockConfig);
    logger = new DebugLogger();
    //cache = createMock(ChannelSegmentCache.class);

    channelAdGroupRepository = createMock(ChannelAdGroupRepository.class);
    MatchSegments.init(channelAdGroupRepository, repoHelper, new InspectorStats());
    segments = new MatchSegments(logger);
    Long[] rcList = null;
    Long[] tags = null;
    Timestamp modified_on = null;
    Long[] slotIds = null;
    Integer[] siteRatings = null;
    channelSegmentEntity = new ChannelSegmentEntity("advertiserId", "adgroupId", "adId", "channelId", (long) 1, rcList, tags, true, true, "externalSiteKey",
        modified_on, "campaignId", slotIds, (long) 1, true, "pricingModel", siteRatings, 1, null);
    channelSegmentEntity1 = new ChannelSegmentEntity("advertiserId", "adgroupId", "adId", "channelId", (long) 0, rcList, tags, false, true, "externalSiteKey",
        modified_on, "campaignId", slotIds, (long) 1, true, "pricingModel", siteRatings, 1, null);
    channelSegmentEntity2 = new ChannelSegmentEntity("advertiserId", "adgroupId", "adId", "channelId", (long) 1, rcList, tags, false, false, "externalSiteKey",
        modified_on, "campaignId", slotIds, (long) 0, false, "pricingModel", siteRatings, 0, null);

  }*/
/*
  @Test
  public void testMatchSegmentsCacheHit() throws Exception {
    assertNotNull(segments);
    long[] categories = { 1 };
    ArrayList<ChannelSegmentEntity> entities = new ArrayList<ChannelSegmentEntity>();
    expect(cache.query(logger, 1, 1, 1, 1, 1, 1, -1)).andReturn(entities);
    expect(cache.query(logger, 1, -1, 1, 1, 1, 1, -1)).andReturn(entities);
    expect(cache.query(logger, 1, 1, -1, 1, 1, 1, -1)).andReturn(entities);
    expect(cache.query(logger, 1, -1, -1, 1, 1, 1, -1)).andReturn(entities);
    replay(cache);
    segments.matchSegments(logger, 1, categories, 1, 1, 1, 1, -1);
    verify();
  }

  @Test
  public void testMatchSegmentsCacheHitMultipleCategories() throws Exception {
    long[] categories = { 1, 2 };
    ArrayList<ChannelSegmentEntity> entities = new ArrayList<ChannelSegmentEntity>();
    entities.add(channelSegmentEntity);
    entities.add(channelSegmentEntity1);
    expect(cache.query(logger, 1, 1, 1, 1, 1, 1, -1)).andReturn(entities);
    expect(cache.query(logger, 1, -1, 1, 1, 1, 1, -1)).andReturn(entities);
    expect(cache.query(logger, 1, 1, -1, 1, 1, 1, -1)).andReturn(entities);
    expect(cache.query(logger, 1, -1, -1, 1, 1, 1, -1)).andReturn(entities);
    expect(cache.query(logger, 1, 2, 1, 1, 1, 1, -1)).andReturn(entities);
    expect(cache.query(logger, 1, 2, -1, 1, 1, 1, -1)).andReturn(entities);
    replay(cache);
    segments.matchSegments(logger, 1, categories, 1, 1, 1, 1, -1);
  }

  @Test
  public void testMatchSegmentsCacheMiss() throws Exception {
    long[] categories = { 1, 2 };
    ArrayList<ChannelSegmentEntity> emptyEntityArray = new ArrayList<ChannelSegmentEntity>();
    expect(cache.query(logger, 1, 1, 1, 1, 1, 1, -1)).andReturn(null);
    expect(cache.query(logger, 1, -1, 1, 1, 1, 1, -1)).andReturn(emptyEntityArray);
    expect(cache.query(logger, 1, 1, -1, 1, 1, 1, -1)).andReturn(emptyEntityArray);
    expect(cache.query(logger, 1, -1, -1, 1, 1, 1, -1)).andReturn(emptyEntityArray);
    expect(cache.query(logger, 1, 2, 1, 1, 1, 1, -1)).andReturn(emptyEntityArray);
    expect(cache.query(logger, 1, 2, -1, 1, 1, 1, -1)).andReturn(emptyEntityArray);
    cache.addOrUpdate(logger, 1, 1, 1, 1, 1, 1, -1, emptyEntityArray);
    EasyMock.expectLastCall();
    ArrayList<ChannelSegmentEntity> nonEmptyArray = new ArrayList<ChannelSegmentEntity>();
    nonEmptyArray.add(channelSegmentEntity1);
    cache.addOrUpdate(logger, 1, 1, 1, 1, 1, 1, -1, nonEmptyArray);
    EasyMock.expectLastCall();
    expect(channelAdGroupRepository.getEntities((short) 1, 1, (short) 1, 1, 1)).andReturn(nonEmptyArray);
    replay(channelAdGroupRepository);
    replay(cache);
    segments.matchSegments(logger, 1, categories, 1, 1, 1, 1, -1);
  }
*/
/*
  @Test
  public void testMatchSegmentsPlatformMatching() throws Exception {
    long[] categories = { 1, 2 };
    ArrayList<ChannelSegmentEntity> emptyEntityArray = new ArrayList<ChannelSegmentEntity>();
    expect(cache.query(logger, 1, 1, 1, 1, 1, 1, -1)).andReturn(null);
    expect(cache.query(logger, 1, -1, 1, 1, 1, 1, -1)).andReturn(emptyEntityArray);
    expect(cache.query(logger, 1, 1, -1, 1, 1, 1, -1)).andReturn(emptyEntityArray);
    expect(cache.query(logger, 1, -1, -1, 1, 1, 1, -1)).andReturn(emptyEntityArray);
    expect(cache.query(logger, 1, 2, 1, 1, 1, 1, -1)).andReturn(emptyEntityArray);
    expect(cache.query(logger, 1, 2, -1, ServletHandler.config1, 1, 1, -1)).andReturn(emptyEntityArray);
    cache.addOrUpdate(logger, 1, 1, 1, 1, 1, 1, -1, emptyEntityArray);
    EasyMock.expectLastCall();
    ArrayList<ChannelSegmentEntity> nonEmptyArray = new ArrayList<ChannelSegmentEntity>();
    nonEmptyArray.add(channelSegmentEntity2);
    cache.addOrUpdate(logger, 1, 1, 1, 1, 1, 1, -1, nonEmptyArray);
    EasyMock.expectLastCall();
    expect(channelAdGroupRepository.getEntities((short) 1, 1, (short) 1, 1, 1)).andReturn(nonEmptyArray);
    replay(channelAdGroupRepository);
    replay(cache);
    segments.matchSegments(logger, 1, categories, 1, 1, 1, 1, -1);
  }
  
  @Test
  public void testMatchSegments() throws Exception {
    long[] categeries = {1,2};
    ArrayList<ChannelSegmentEntity> nonEmptyArray = new ArrayList<ChannelSegmentEntity>();
    expect(channelAdGroupRepository.getEntities(1, -1, 1, 1, 1)).andReturn(nonEmptyArray);
    expect(channelAdGroupRepository.getEntities(1, 1, -1, 1, 1)).andReturn(nonEmptyArray);
    expect(channelAdGroupRepository.getEntities(1, -1, -1, 1, 1)).andReturn(nonEmptyArray);
    
  }

  @Test
  public void testParseOsIds() {
    // if(channelAdGroupRepository.parseOsIds("")!=null)
    ChannelAdGroupRepository channelAdGroupRepository1 = new ChannelAdGroupRepository();
    assertEquals(channelAdGroupRepository1.parseOsIds(""), null);
    assertEquals(channelAdGroupRepository1.parseOsIds(null), null);
    assertEquals(
        channelAdGroupRepository1.parseOsIds(
            "{\"os\": [{\"id\": 1,\"min\": 2.2, \"max\": 4, \"incl\" : true },{\"id\": 1,\"min\": 2.2, \"max\": 4, \"incl\" : true }] }").size(), 2);
  }
  */
  
  @Test
  public void testGetCategories() {
    Configuration mockConfig = createMock(Configuration.class);
    SASRequestParameters sasRequestParameters = new SASRequestParameters();
    expect(mockConfig.getBoolean("isNewCategory")).andReturn(true).anyTimes();
    expect(mockConfig.getString("debug")).andReturn("debug").anyTimes();
    expect(mockConfig.getString("loggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties").anyTimes();
    replay(mockConfig);
    long [] newCat = {1,2,3};
    sasRequestParameters.newCategories = newCat;
    
    RepositoryHelper repositoryHelper = createMock(RepositoryHelper.class);
    SiteTaxonomyEntity s1 = new SiteTaxonomyEntity("1", "name", "4");
    SiteTaxonomyEntity s2 = new SiteTaxonomyEntity("2", "name", null);
    SiteTaxonomyEntity s3 = new SiteTaxonomyEntity("3", "name", "4");
    SiteTaxonomyEntity s4 =new SiteTaxonomyEntity("4", "name", null);
    expect(repositoryHelper.querySiteTaxonomyRepository("1")).andReturn(s1).anyTimes();
    expect(repositoryHelper.querySiteTaxonomyRepository("2")).andReturn(s2).anyTimes();
    expect(repositoryHelper.querySiteTaxonomyRepository("3")).andReturn(s3).anyTimes();
    expect(repositoryHelper.querySiteTaxonomyRepository("4")).andReturn(s4).anyTimes();
    replay(repositoryHelper);
    
    MatchSegments.init(null, repositoryHelper);
    DebugLogger.init(mockConfig);
    MatchSegments matchSegments = new MatchSegments(new DebugLogger());

    long [] cat = matchSegments.getCategories(sasRequestParameters, mockConfig);
    System.out.println(cat);
  }
}

