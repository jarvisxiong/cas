package com.inmobi.adserve.channels.server;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.configuration.Configuration;
import org.testng.annotations.Test;

import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.SiteTaxonomyEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.util.ConfigurationLoader;
import com.inmobi.adserve.channels.util.DebugLogger;

public class MatchSegmentsTest extends TestCase {

  @Test
  public void testGetCategories() {
    String configFile = "/opt/mkhoj/conf/cas/channel-server.properties";
    ConfigurationLoader config = ConfigurationLoader.getInstance(configFile);
    ServletHandler.init(config, null);
    Configuration mockConfig = createMock(Configuration.class);
    SASRequestParameters sasRequestParameters = new SASRequestParameters();
    sasRequestParameters.setSiteId("1");
    sasRequestParameters.setSiteSegmentId(2);
    expect(mockConfig.getBoolean("isNewCategory", false)).andReturn(true).anyTimes();
    expect(mockConfig.getString("debug")).andReturn("debug").anyTimes();
    expect(mockConfig.getString("slf4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/logger.xml");
    expect(mockConfig.getString("log4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties");    replay(mockConfig);
    List<Long> newCat = new ArrayList<Long>();
    newCat.add(1L);
    newCat.add(2L);
    newCat.add(3L);
    DebugLogger.init(mockConfig);
    DebugLogger debugLogger = new DebugLogger();
    RepositoryHelper repositoryHelper = createMock(RepositoryHelper.class);
    SiteTaxonomyEntity s1 = new SiteTaxonomyEntity("1", "name", "4");
    SiteTaxonomyEntity s2 = new SiteTaxonomyEntity("2", "name", null);
    SiteTaxonomyEntity s3 = new SiteTaxonomyEntity("3", "name", "4");
    SiteTaxonomyEntity s4 =new SiteTaxonomyEntity("4", "name", null);
    expect(repositoryHelper.querySiteTaxonomyRepository("1")).andReturn(s1).anyTimes();
    expect(repositoryHelper.querySiteTaxonomyRepository("2")).andReturn(s2).anyTimes();
    expect(repositoryHelper.querySiteTaxonomyRepository("3")).andReturn(s3).anyTimes();
    expect(repositoryHelper.querySiteTaxonomyRepository("4")).andReturn(s4).anyTimes();
    expect(repositoryHelper.querySiteCitrusLeafFeedbackRepository("1","2",debugLogger)).andReturn(null).anyTimes();
    replay(repositoryHelper);
    MatchSegments.init(null);
    MatchSegments matchSegments = new MatchSegments(repositoryHelper, sasRequestParameters, debugLogger);
    assertEquals(new ArrayList<Long>(), matchSegments.getCategories());
  }
}

