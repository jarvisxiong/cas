package com.inmobi.adserve.channels.server.requesthandler;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.SiteTaxonomyEntity;
import com.inmobi.adserve.channels.repository.ChannelAdGroupRepository;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.server.module.CasNettyModule;
import com.inmobi.adserve.channels.server.module.ServerModule;
import com.inmobi.adserve.channels.util.ConfigurationLoader;

import junit.framework.TestCase;


public class MatchSegmentsTest extends TestCase {
    private static final String CHANNEL_SERVER_CONFIG_FILE = "channel-server.properties";
    private ConfigurationLoader configurationLoder;
    private MatchSegments matchSegments;

    @Override
    public void setUp() throws ClassNotFoundException, IllegalAccessException {

        configurationLoder = ConfigurationLoader.getInstance(CHANNEL_SERVER_CONFIG_FILE);
        System.out.println(configurationLoder.getAdapterConfiguration());

        final RepositoryHelper repositoryHelper = createMock(RepositoryHelper.class);
        CasConfigUtil.init(configurationLoder, repositoryHelper);

        final Injector injector =
                Guice.createInjector(new CasNettyModule(configurationLoder.getServerConfiguration()), new ServerModule(
                        configurationLoder, repositoryHelper, "containerName"));

        matchSegments = injector.getInstance(MatchSegments.class);

    }

    @Test
    public void testGetCategories() throws SecurityException, NoSuchMethodException, IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {

        CasConfigUtil.init(configurationLoder, null);
        final Configuration mockConfig = createMock(Configuration.class);
        final SASRequestParameters sasRequestParameters = new SASRequestParameters();
        sasRequestParameters.setSiteId("1");
        sasRequestParameters.setSiteSegmentId(2);
        expect(mockConfig.getBoolean("isNewCategory", false)).andReturn(true).anyTimes();
        expect(mockConfig.getString("debug")).andReturn("debug").anyTimes();
        expect(mockConfig.getString("slf4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/logger.xml");
        expect(mockConfig.getString("log4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties");
        replay(mockConfig);
        final List<Long> newCat = new ArrayList<Long>();
        newCat.add(1L);
        newCat.add(2L);
        newCat.add(3L);
        final RepositoryHelper repositoryHelper = createMock(RepositoryHelper.class);
        final SiteTaxonomyEntity s1 = new SiteTaxonomyEntity("1", "name", "4");
        final SiteTaxonomyEntity s2 = new SiteTaxonomyEntity("2", "name", null);
        final SiteTaxonomyEntity s3 = new SiteTaxonomyEntity("3", "name", "4");
        final SiteTaxonomyEntity s4 = new SiteTaxonomyEntity("4", "name", null);
        expect(repositoryHelper.querySiteTaxonomyRepository("1")).andReturn(s1).anyTimes();
        expect(repositoryHelper.querySiteTaxonomyRepository("2")).andReturn(s2).anyTimes();
        expect(repositoryHelper.querySiteTaxonomyRepository("3")).andReturn(s3).anyTimes();
        expect(repositoryHelper.querySiteTaxonomyRepository("4")).andReturn(s4).anyTimes();
        expect(repositoryHelper.querySiteAerospikeFeedbackRepository("1", 2)).andReturn(null).anyTimes();
        expect(repositoryHelper.getChannelAdGroupRepository()).andReturn(createMock(ChannelAdGroupRepository.class))
                .anyTimes();
        replay(repositoryHelper);

        final Method method = MatchSegments.class.getDeclaredMethod("getCategories", SASRequestParameters.class);
        method.setAccessible(true);
        assertEquals(new ArrayList<Long>(), method.invoke(matchSegments, sasRequestParameters));
    }
}
