package com.inmobi.adserve.channels.repository;

import com.inmobi.adserve.channels.entity.ChannelEntity;
import com.inmobi.adserve.channels.entity.ChannelFeedbackEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentFeedbackEntity;
import com.inmobi.adserve.channels.entity.CreativeEntity;
import com.inmobi.adserve.channels.entity.CurrencyConversionEntity;
import com.inmobi.adserve.channels.entity.IXAccountMapEntity;
import com.inmobi.adserve.channels.entity.NativeAdTemplateEntity;
import com.inmobi.adserve.channels.entity.PricingEngineEntity;
import com.inmobi.adserve.channels.entity.SegmentAdGroupFeedbackEntity;
import com.inmobi.adserve.channels.entity.SiteEcpmEntity;
import com.inmobi.adserve.channels.entity.SiteFeedbackEntity;
import com.inmobi.adserve.channels.entity.SiteFilterEntity;
import com.inmobi.adserve.channels.entity.SiteMetaDataEntity;
import com.inmobi.adserve.channels.entity.SiteTaxonomyEntity;
import com.inmobi.adserve.channels.entity.WapSiteUACEntity;
import com.inmobi.adserve.channels.query.CreativeQuery;
import com.inmobi.adserve.channels.query.PricingEngineQuery;
import com.inmobi.adserve.channels.query.SiteEcpmQuery;
import com.inmobi.adserve.channels.query.SiteFilterQuery;
import com.inmobi.phoenix.exception.RepositoryException;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.replayAll;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RepositoryHelper.class, NativeAdTemplateEntity.class})
public class RepositoryHelperTest {
    public static ChannelEntity                    mockChannelEntity = createMock(ChannelEntity.class);
    public static ChannelSegmentEntity             mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);
    public static ChannelFeedbackEntity            mockChannelFeedbackEntity = createMock(ChannelFeedbackEntity.class);
    public static ChannelSegmentFeedbackEntity     mockChannelSegmentFeedbackEntity = createMock(ChannelSegmentFeedbackEntity.class);
    public static SiteMetaDataEntity               mockSiteMetaDataEntity = createMock(SiteMetaDataEntity.class);
    public static SiteTaxonomyEntity               mockSiteTaxonomyEntity = createMock(SiteTaxonomyEntity.class);
    public static SiteFeedbackEntity               mockSiteFeedbackEntity = createMock(SiteFeedbackEntity.class);
    public static PricingEngineEntity              mockPricingEngineEntity = createMock(PricingEngineEntity.class);
    public static SiteFilterEntity                 mockSiteFilterEntity = createMock(SiteFilterEntity.class);
    public static SiteEcpmEntity                   mockSiteEcpmEntity = createMock(SiteEcpmEntity.class);
    public static CurrencyConversionEntity         mockCurrencyConversionEntity = createMock(CurrencyConversionEntity.class);
    public static WapSiteUACEntity                 mockWapSiteUACEntity = createMock(WapSiteUACEntity.class);
    public static IXAccountMapEntity               mockIxAccountMapEntity = createMock(IXAccountMapEntity.class);
    public static CreativeEntity                   mockCreativeEntity = createMock(CreativeEntity.class);
    public static NativeAdTemplateEntity           mockNativeAdTemplateEntity = createMock(NativeAdTemplateEntity.class);
    public static SegmentAdGroupFeedbackEntity     mockSegmentAdGroupFeedbackEntity = createMock(SegmentAdGroupFeedbackEntity.class);
    public static ChannelRepository                mockChannelRepository = createMock(ChannelRepository.class);
    public static ChannelAdGroupRepository         mockChannelAdGroupRepository = createMock(ChannelAdGroupRepository.class);
    public static ChannelFeedbackRepository        mockChannelFeedbackRepository = createMock(ChannelFeedbackRepository.class);
    public static ChannelSegmentFeedbackRepository mockChannelSegmentFeedbackRepository = createMock(ChannelSegmentFeedbackRepository.class);
    public static SiteMetaDataRepository           mockSiteMetaDataRepository = createMock(SiteMetaDataRepository.class);
    public static SiteTaxonomyRepository           mockSiteTaxonomyRepository = createMock(SiteTaxonomyRepository.class);
    public static SiteAerospikeFeedbackRepository  mockSiteAerospikeFeedbackRepository = createMock(SiteAerospikeFeedbackRepository.class);
    public static PricingEngineRepository          mockPricingEngineRepository = createMock(PricingEngineRepository.class);
    public static SiteFilterRepository             mockSiteFilterRepository = createMock(SiteFilterRepository.class);
    public static SiteEcpmRepository               mockSiteEcpmRepository = createMock(SiteEcpmRepository.class);
    public static CurrencyConversionRepository     mockCurrencyConversionRepository = createMock(CurrencyConversionRepository.class);
    public static WapSiteUACRepository             mockWapSiteUACRepository = createMock(WapSiteUACRepository.class);
    public static IXAccountMapRepository           mockIxAccountMapRepository = createMock(IXAccountMapRepository.class);
    public static CreativeRepository               mockCreativeRepository = createMock(CreativeRepository.class);
    public static NativeAdTemplateRepository       mockNativeAdTemplateRepository = createMock(NativeAdTemplateRepository.class);
    public static PricingEngineQuery               mockPricingEngineQuery = createMock(PricingEngineQuery.class);
    public static CreativeQuery                    mockCreativeQuery = createMock(CreativeQuery.class);
    public static SiteFilterQuery                  mockSiteFilterQuery = createMock(SiteFilterQuery.class);
    public static SiteEcpmQuery                    mockSiteEcpmQuery = createMock(SiteEcpmQuery.class);

    public static RepositoryHelper tested;
    public static String query1 = "query1";
    public static String query2 = "query2";
    public static int    query3 = 5;
    public static long   query4 = 6;

    @BeforeClass
    public static void setUp () throws Exception{
        expectNew(PricingEngineQuery.class, query3, query3).andReturn(mockPricingEngineQuery).times(2);
        expectNew(CreativeQuery.class, query1, query2).andReturn(mockCreativeQuery).times(2);
        expectNew(SiteFilterQuery.class, query1, query3).andReturn(mockSiteFilterQuery).times(2);
        expectNew(SiteEcpmQuery.class, query1, query3, query3).andReturn(mockSiteEcpmQuery).times(2);

        expect(mockChannelRepository.query(query1))
                .andThrow(new RepositoryException("Repository Exception")).times(1)
                .andReturn(mockChannelEntity).times(1);
        expect(mockChannelAdGroupRepository.query(query1))
                .andThrow(new RepositoryException("Repository Exception")).times(1)
                .andReturn(mockChannelSegmentEntity).times(1);
        expect(mockChannelFeedbackRepository.query(query1))
                .andThrow(new RepositoryException("Repository Exception")).times(1)
                .andReturn(mockChannelFeedbackEntity).times(1);
        expect(mockChannelSegmentFeedbackRepository.query(query1))
                .andThrow(new RepositoryException("Repository Exception")).times(1)
                .andReturn(mockChannelSegmentFeedbackEntity).times(1);
        expect(mockSiteMetaDataRepository.query(query1))
                .andThrow(new RepositoryException("Repository Exception")).times(1)
                .andReturn(mockSiteMetaDataEntity).times(1);
        expect(mockSiteTaxonomyRepository.query(query1))
                .andThrow(new RepositoryException("Repository Exception")).times(1)
                .andReturn(mockSiteTaxonomyEntity).times(1);
        expect(mockSiteAerospikeFeedbackRepository.query(query1))
                .andReturn(mockSiteFeedbackEntity).times(1);
        expect(mockSiteAerospikeFeedbackRepository.query(query1, query3))
                .andReturn(mockSegmentAdGroupFeedbackEntity).times(1);
        expect(mockPricingEngineRepository.query(mockPricingEngineQuery))
                .andThrow(new RepositoryException("Repository Exception")).times(1)
                .andReturn(mockPricingEngineEntity).times(1);
        expect(mockSiteFilterRepository.query(mockSiteFilterQuery))
                .andThrow(new RepositoryException("Repository Exception")).times(1)
                .andReturn(mockSiteFilterEntity).times(1);
        expect(mockSiteEcpmRepository.query(mockSiteEcpmQuery))
                .andThrow(new RepositoryException("Repository Exception")).times(1)
                .andReturn(mockSiteEcpmEntity).times(1);
        expect(mockCurrencyConversionRepository.query(query1))
                .andThrow(new RepositoryException("Repository Exception")).times(1)
                .andReturn(mockCurrencyConversionEntity).times(1);
        expect(mockWapSiteUACRepository.query(query1))
                .andThrow(new RepositoryException("Repository Exception")).times(1)
                .andReturn(mockWapSiteUACEntity).times(1);
        expect(mockIxAccountMapRepository.query(query4))
                .andThrow(new RepositoryException("Repository Exception")).times(1)
                .andReturn(mockIxAccountMapEntity).times(1);
        expect(mockCreativeRepository.query(mockCreativeQuery))
                .andThrow(new RepositoryException("Repository Exception")).times(1)
                .andReturn(mockCreativeEntity).times(1);
        expect(mockNativeAdTemplateRepository.query(query1))
                .andThrow(new RepositoryException("Repository Exception")).times(1)
                .andReturn(mockNativeAdTemplateEntity).times(1);
        replayAll();

        RepositoryHelper.Builder builder = RepositoryHelper.newBuilder();
        builder.setChannelAdGroupRepository(mockChannelAdGroupRepository);
        builder.setChannelFeedbackRepository(mockChannelFeedbackRepository);
        builder.setChannelRepository(mockChannelRepository);
        builder.setChannelSegmentFeedbackRepository(mockChannelSegmentFeedbackRepository);
        builder.setSiteMetaDataRepository(mockSiteMetaDataRepository);
        builder.setSiteTaxonomyRepository(mockSiteTaxonomyRepository);
        builder.setSiteAerospikeFeedbackRepository(mockSiteAerospikeFeedbackRepository);
        builder.setPricingEngineRepository(mockPricingEngineRepository);
        builder.setSiteFilterRepository(mockSiteFilterRepository);
        builder.setSiteEcpmRepository(mockSiteEcpmRepository);
        builder.setCurrencyConversionRepository(mockCurrencyConversionRepository);
        builder.setWapSiteUACRepository(mockWapSiteUACRepository);
        builder.setIxAccountMapRepository(mockIxAccountMapRepository);
        builder.setCreativeRepository(mockCreativeRepository);
        builder.setNativeAdTemplateRepository(mockNativeAdTemplateRepository);

        tested = builder.build();
    }

    @Test
    public void testBuilder() {
        assertThat(tested.getChannelAdGroupRepository(), is(equalTo(mockChannelAdGroupRepository)));
        assertThat(tested.getChannelFeedbackRepository(), is(equalTo(mockChannelFeedbackRepository)));
        assertThat(tested.getChannelRepository(), is(equalTo(mockChannelRepository)));
        assertThat(tested.getChannelSegmentFeedbackRepository(), is(equalTo(mockChannelSegmentFeedbackRepository)));
        assertThat(tested.getSiteMetaDataRepository(), is(equalTo(mockSiteMetaDataRepository)));
        assertThat(tested.getSiteTaxonomyRepository(), is(equalTo(mockSiteTaxonomyRepository)));
        assertThat(tested.getSiteAerospikeFeedbackRepository(), is(equalTo(mockSiteAerospikeFeedbackRepository)));
        assertThat(tested.getPricingEngineRepository(), is(equalTo(mockPricingEngineRepository)));
        assertThat(tested.getSiteFilterRepository(), is(equalTo(mockSiteFilterRepository)));
        assertThat(tested.getSiteEcpmRepository(), is(equalTo(mockSiteEcpmRepository)));
        assertThat(tested.getCurrencyConversionRepository(), is(equalTo(mockCurrencyConversionRepository)));
        assertThat(tested.getWapSiteUACRepository(), is(equalTo(mockWapSiteUACRepository)));
        assertThat(tested.getIxAccountMapRepository(), is(equalTo(mockIxAccountMapRepository)));
        assertThat(tested.getCreativeRepository(), is(equalTo(mockCreativeRepository)));
        assertThat(tested.getNativeAdTemplateRepository(), is(equalTo(mockNativeAdTemplateRepository)));
    }

    @Test
    public void testQueryChannelRepository() throws Exception {
        assertThat(tested.queryChannelRepository(query1), is(equalTo(null)));
        assertThat(tested.queryChannelRepository(query1), is(equalTo(mockChannelEntity)));
    }

    @Test
    public void testQueryChannelAdGroupRepository() throws Exception {
        assertThat(tested.queryChannelAdGroupRepository(query1), is(equalTo(null)));
        assertThat(tested.queryChannelAdGroupRepository(query1), is(equalTo(mockChannelSegmentEntity)));
    }

    @Test
    public void testQueryChannelSegmentFeedbackRepository() throws Exception {
        assertThat(tested.queryChannelSegmentFeedbackRepository(query1), is(equalTo(null)));
        assertThat(tested.queryChannelSegmentFeedbackRepository(query1), is(equalTo(mockChannelSegmentFeedbackEntity)));
    }

    @Test
    public void testQueryChannelFeedbackRepository() throws Exception {
        assertThat(tested.queryChannelFeedbackRepository(query1), is(equalTo(null)));
        assertThat(tested.queryChannelFeedbackRepository(query1), is(equalTo(mockChannelFeedbackEntity)));
    }

    @Test
    public void testQuerySiteTaxonomyRepository() throws Exception {
        assertThat(tested.querySiteTaxonomyRepository(query1), is(equalTo(null)));
        assertThat(tested.querySiteTaxonomyRepository(query1), is(equalTo(mockSiteTaxonomyEntity)));
    }

    @Test
    public void testQuerySiteMetaDetaRepository() throws Exception {
        assertThat(tested.querySiteMetaDetaRepository(query1), is(equalTo(null)));
        assertThat(tested.querySiteMetaDetaRepository(query1), is(equalTo(mockSiteMetaDataEntity)));
    }

    @Test
    public void testQuerySiteAerospikeFeedbackRepository() throws Exception {
        assertThat(tested.querySiteAerospikeFeedbackRepository(query1), is(equalTo(mockSiteFeedbackEntity)));
    }

    @Test
    public void testQuerySiteAerospikeFeedbackRepository1() throws Exception {
        assertThat(tested.querySiteAerospikeFeedbackRepository(query1, query3), is(equalTo(mockSegmentAdGroupFeedbackEntity)));
    }

    @Test
    @Ignore
    public void testQueryPricingEngineRepository() throws Exception {
        assertThat(tested.queryPricingEngineRepository(query3, query3), is(equalTo(null)));
        assertThat(tested.queryPricingEngineRepository(query3, query3), is(equalTo(mockPricingEngineEntity)));
    }

    @Test
    @Ignore
    public void testQueryCreativeRepository() throws Exception {
        assertThat(tested.queryCreativeRepository(query1, query2), is(equalTo(null)));
        assertThat(tested.queryCreativeRepository(query1, query2), is(equalTo(mockCreativeEntity)));
    }

    @Ignore
    @Test
    public void testQuerySiteFilterRepository() throws Exception {
        assertThat(tested.querySiteFilterRepository(query1, query3), is(equalTo(null)));
        assertThat(tested.querySiteFilterRepository(query1, query3), is(equalTo(mockSiteFilterEntity)));
    }

    @Ignore
    @Test
    public void testQuerySiteEcpmRepository() throws Exception {
        assertThat(tested.querySiteEcpmRepository(query1, query3, query3), is(equalTo(null)));
        assertThat(tested.querySiteEcpmRepository(query1, query3, query3), is(equalTo(mockSiteEcpmEntity)));
    }

    @Test
    public void testQueryCurrencyConversionRepository() throws Exception {
        assertThat(tested.queryCurrencyConversionRepository(query1), is(equalTo(null)));
        assertThat(tested.queryCurrencyConversionRepository(query1), is(equalTo(mockCurrencyConversionEntity)));
    }

    @Test
    public void testQueryWapSiteUACRepository() throws Exception {
        assertThat(tested.queryWapSiteUACRepository(query1), is(equalTo(null)));
        assertThat(tested.queryWapSiteUACRepository(query1), is(equalTo(mockWapSiteUACEntity)));
    }

    @Test
    public void testQueryIXAccountMapRepository() throws Exception {
        assertThat(tested.queryIXAccountMapRepository(query4), is(equalTo(null)));
        assertThat(tested.queryIXAccountMapRepository(query4), is(equalTo(mockIxAccountMapEntity)));
    }

    @Test
    public void testQueryNativeAdTemplateRepository() throws Exception {
        assertThat(tested.queryNativeAdTemplateRepository(query1), is(equalTo(null)));
        assertThat(tested.queryNativeAdTemplateRepository(query1), is(equalTo(mockNativeAdTemplateEntity)));
    }
}