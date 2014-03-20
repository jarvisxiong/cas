package com.inmobi.adserve.channels.server.requesthandler;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.configuration.Configuration;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.SiteTaxonomyEntity;
import com.inmobi.adserve.channels.repository.ChannelAdGroupRepository;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.ServletHandler;
import com.inmobi.adserve.channels.server.module.CasNettyModule;
import com.inmobi.adserve.channels.server.module.ServerModule;
import com.inmobi.adserve.channels.util.ConfigurationLoader;


public class MatchSegmentsTest extends TestCase {
    private static final String CHANNEL_SERVER_CONFIG_FILE = "/opt/mkhoj/conf/cas/channel-server.properties";
    private ConfigurationLoader configurationLoder;
    private MatchSegments       matchSegments;

    @Override
    public void setUp() throws ClassNotFoundException {

        configurationLoder = ConfigurationLoader.getInstance(CHANNEL_SERVER_CONFIG_FILE);
        System.out.println(configurationLoder.getAdapterConfiguration());

        RepositoryHelper repositoryHelper = createMock(RepositoryHelper.class);

        ServletHandler.init(configurationLoder, repositoryHelper);

        Injector injector = Guice.createInjector(new CasNettyModule(configurationLoder.getServerConfiguration()),
                new ServerModule(configurationLoder, repositoryHelper));

        matchSegments = injector.getInstance(MatchSegments.class);

    }

    @Test
    public void testGetCategories() throws SecurityException, NoSuchMethodException, IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {

        ServletHandler.init(configurationLoder, null);
        Configuration mockConfig = createMock(Configuration.class);
        SASRequestParameters sasRequestParameters = new SASRequestParameters();
        sasRequestParameters.setSiteId("1");
        sasRequestParameters.setSiteSegmentId(2);
        expect(mockConfig.getBoolean("isNewCategory", false)).andReturn(true).anyTimes();
        expect(mockConfig.getString("debug")).andReturn("debug").anyTimes();
        expect(mockConfig.getString("slf4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/logger.xml");
        expect(mockConfig.getString("log4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties");
        replay(mockConfig);
        List<Long> newCat = new ArrayList<Long>();
        newCat.add(1L);
        newCat.add(2L);
        newCat.add(3L);
        RepositoryHelper repositoryHelper = createMock(RepositoryHelper.class);
        SiteTaxonomyEntity s1 = new SiteTaxonomyEntity("1", "name", "4");
        SiteTaxonomyEntity s2 = new SiteTaxonomyEntity("2", "name", null);
        SiteTaxonomyEntity s3 = new SiteTaxonomyEntity("3", "name", "4");
        SiteTaxonomyEntity s4 = new SiteTaxonomyEntity("4", "name", null);
        expect(repositoryHelper.querySiteTaxonomyRepository("1")).andReturn(s1).anyTimes();
        expect(repositoryHelper.querySiteTaxonomyRepository("2")).andReturn(s2).anyTimes();
        expect(repositoryHelper.querySiteTaxonomyRepository("3")).andReturn(s3).anyTimes();
        expect(repositoryHelper.querySiteTaxonomyRepository("4")).andReturn(s4).anyTimes();
        expect(repositoryHelper.querySiteCitrusLeafFeedbackRepository("1", 2)).andReturn(null).anyTimes();
        expect(repositoryHelper.getChannelAdGroupRepository()).andReturn(createMock(ChannelAdGroupRepository.class))
                .anyTimes();
        replay(repositoryHelper);

        Method method = MatchSegments.class.getDeclaredMethod("getCategories", SASRequestParameters.class);
        method.setAccessible(true);
        assertEquals(new ArrayList<Long>(), method.invoke(matchSegments, sasRequestParameters));
    }
}
