package com.inmobi.adserve.channels.server.servlet;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.createNiceMock;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.inmobi.adserve.channels.repository.ChannelAdGroupRepository;
import com.inmobi.adserve.channels.repository.ChannelFeedbackRepository;
import com.inmobi.adserve.channels.repository.ChannelRepository;
import com.inmobi.adserve.channels.repository.ChannelSegmentFeedbackRepository;
import com.inmobi.adserve.channels.repository.CreativeRepository;
import com.inmobi.adserve.channels.repository.CurrencyConversionRepository;
import com.inmobi.adserve.channels.repository.IXAccountMapRepository;
import com.inmobi.adserve.channels.repository.NativeAdTemplateRepository;
import com.inmobi.adserve.channels.repository.PricingEngineRepository;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.repository.SiteAerospikeFeedbackRepository;
import com.inmobi.adserve.channels.repository.SiteEcpmRepository;
import com.inmobi.adserve.channels.repository.SiteFilterRepository;
import com.inmobi.adserve.channels.repository.SiteMetaDataRepository;
import com.inmobi.adserve.channels.repository.SiteTaxonomyRepository;
import com.inmobi.adserve.channels.repository.WapSiteUACRepository;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.server.ChannelServer;
import com.inmobi.adserve.channels.server.ChannelServerStringLiterals;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.requesthandler.ResponseSender;
import com.inmobi.adserve.channels.util.ConfigurationLoader;
import com.inmobi.phoenix.exception.RepositoryException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ConfigurationLoader.class, ServletRepoRefresh.class, ChannelServer.class, ChannelRepository.class,
        ChannelAdGroupRepository.class, ChannelFeedbackRepository.class, ChannelSegmentFeedbackRepository.class,
        SiteMetaDataRepository.class, SiteTaxonomyRepository.class, SiteAerospikeFeedbackRepository.class,
        PricingEngineRepository.class, SiteFilterRepository.class, SiteEcpmRepository.class,
        CurrencyConversionRepository.class, WapSiteUACRepository.class, IXAccountMapRepository.class,
        CreativeRepository.class, NativeAdTemplateRepository.class})
public class ServletRepoRefreshTest {
    private static HttpRequestHandler httpRequestHandler;
    private static QueryStringDecoder mockQueryStringDecoder;
    private static Channel mockChannel;
    private static ConfigurationLoader mockConfigLoader;

    private static final String LAST_UPDATE = "'${last_update}'";
    private static final String REPLACE_STRING = "now() -interval '1 MINUTE'";

    final private static String json1 =
            "{\"repoName\":\"ChannelRepository\",\"DBHost\":\"10.14.118.57\",\"DBPort\":\"5499\",\"DBSnapshot\":\"pratap_dcp_jenkins_dont_delete\",\"DBUser\":\"postgres\",\"DBPassword\":\"mkhoj123\"}";
    final private static String json2 =
            "{\"repoName\":\"ChannelAdGroupRepository\",\"DBHost\":\"10.14.118.57\",\"DBPort\":\"5499\",\"DBSnapshot\":\"pratap_dcp_jenkins_dont_delete\",\"DBUser\":\"postgres\",\"DBPassword\":\"mkhoj123\"}";
    final private static String json3 =
            "{\"repoName\":\"ChannelFeedbackRepository\",\"DBHost\":\"10.14.118.57\",\"DBPort\":\"5499\",\"DBSnapshot\":\"pratap_dcp_jenkins_dont_delete\",\"DBUser\":\"postgres\",\"DBPassword\":\"mkhoj123\"}";
    final private static String json4 =
            "{\"repoName\":\"ChannelSegmentFeedbackRepository\",\"DBHost\":\"10.14.118.57\",\"DBPort\":\"5499\",\"DBSnapshot\":\"pratap_dcp_jenkins_dont_delete\",\"DBUser\":\"postgres\",\"DBPassword\":\"mkhoj123\"}";
    final private static String json5 =
            "{\"repoName\":\"SiteMetaDataRepository\",\"DBHost\":\"10.14.118.57\",\"DBPort\":\"5499\",\"DBSnapshot\":\"pratap_dcp_jenkins_dont_delete\",\"DBUser\":\"postgres\",\"DBPassword\":\"mkhoj123\"}";
    final private static String json6 =
            "{\"repoName\":\"SiteTaxonomyRepository\",\"DBHost\":\"10.14.118.57\",\"DBPort\":\"5499\",\"DBSnapshot\":\"pratap_dcp_jenkins_dont_delete\",\"DBUser\":\"postgres\",\"DBPassword\":\"mkhoj123\"}";
    final private static String json7 =
            "{\"repoName\":\"PricingEngineRepository\",\"DBHost\":\"10.14.118.57\",\"DBPort\":\"5499\",\"DBSnapshot\":\"pratap_dcp_jenkins_dont_delete\",\"DBUser\":\"postgres\",\"DBPassword\":\"mkhoj123\"}";
    final private static String json8 =
            "{\"repoName\":\"SiteFilterRepository\",\"DBHost\":\"10.14.118.57\",\"DBPort\":\"5499\",\"DBSnapshot\":\"pratap_dcp_jenkins_dont_delete\",\"DBUser\":\"postgres\",\"DBPassword\":\"mkhoj123\"}";
    final private static String json9 =
            "{\"repoName\":\"SiteEcpmRepository\",\"DBHost\":\"10.14.118.57\",\"DBPort\":\"5499\",\"DBSnapshot\":\"pratap_dcp_jenkins_dont_delete\",\"DBUser\":\"postgres\",\"DBPassword\":\"mkhoj123\"}";
    final private static String json10 =
            "{\"repoName\":\"CurrencyConversionRepository\",\"DBHost\":\"10.14.118.57\",\"DBPort\":\"5499\",\"DBSnapshot\":\"pratap_dcp_jenkins_dont_delete\",\"DBUser\":\"postgres\",\"DBPassword\":\"mkhoj123\"}";
    final private static String json11 =
            "{\"repoName\":\"WapSiteUACRepository\",\"DBHost\":\"10.14.118.57\",\"DBPort\":\"5499\",\"DBSnapshot\":\"pratap_dcp_jenkins_dont_delete\",\"DBUser\":\"postgres\",\"DBPassword\":\"mkhoj123\"}";
    final private static String json12 =
            "{\"repoName\":\"IXAccountMapRepository\",\"DBHost\":\"10.14.118.57\",\"DBPort\":\"5499\",\"DBSnapshot\":\"pratap_dcp_jenkins_dont_delete\",\"DBUser\":\"postgres\",\"DBPassword\":\"mkhoj123\"}";
    final private static String json13 =
            "{\"repoName\":\"CreativeRepository\",\"DBHost\":\"10.14.118.57\",\"DBPort\":\"5499\",\"DBSnapshot\":\"pratap_dcp_jenkins_dont_delete\",\"DBUser\":\"postgres\",\"DBPassword\":\"mkhoj123\"}";
    final private static String json14 =
            "{\"repoName\":\"NativeAdTemplateRepository\",\"DBHost\":\"10.14.118.57\",\"DBPort\":\"5499\",\"DBSnapshot\":\"pratap_dcp_jenkins_dont_delete\",\"DBUser\":\"postgres\",\"DBPassword\":\"mkhoj123\"}";
    final private static String json15 =
            "{\"repoName\":\"RepositoryThatDoesn'tExist\",\"DBHost\":\"10.14.118.57\",\"DBPort\":\"5499\",\"DBSnapshot\":\"pratap_dcp_jenkins_dont_delete\",\"DBUser\":\"postgres\",\"DBPassword\":\"mkhoj123\"}";

    private static Map<String, List<String>> createMapFromString(final String json) {
        final Map<String, List<String>> params = new HashMap<>();
        params.put("args", Arrays.asList(json));
        return params;
    }

    private static void prepareHttpRequestHandler() {
        final ResponseSender mockResponseSender = createNiceMock(ResponseSender.class);
        httpRequestHandler = new HttpRequestHandler(null, null, mockResponseSender);
        replayAll();
    }

    private static void prepareMockQueryStringDecoder() {
        mockQueryStringDecoder = createMock(QueryStringDecoder.class);
        expect(mockQueryStringDecoder.parameters()).andReturn(createMapFromString(json1)).times(1)
                .andReturn(createMapFromString(json2)).times(1).andReturn(createMapFromString(json3)).times(1)
                .andReturn(createMapFromString(json4)).times(1).andReturn(createMapFromString(json5)).times(1)
                .andReturn(createMapFromString(json6)).times(1).andReturn(createMapFromString(json7)).times(1)
                .andReturn(createMapFromString(json8)).times(1).andReturn(createMapFromString(json9)).times(1)
                .andReturn(createMapFromString(json10)).times(1).andReturn(createMapFromString(json11)).times(1)
                .andReturn(createMapFromString(json12)).times(1).andReturn(createMapFromString(json13)).times(1)
                .andReturn(createMapFromString(json14)).times(1).andReturn(createMapFromString(json15)).times(1);
        replayAll();
    }

    private static void prepareMockChannel() {
        mockChannel = createMock(Channel.class);
        replayAll();
    }

    private static void prepareMockConfigurationLoader() {
        final String configFile = "/opt/mkhoj/conf/cas/channel-server.properties";

        mockStatic(ChannelServer.class);
        mockConfigLoader = createMock(ConfigurationLoader.class);
        final Configuration mockServerConfig = createMock(Configuration.class);
        mockStatic(ConfigurationLoader.class);

        expect(ChannelServer.getConfigFile()).andReturn(configFile).anyTimes();
        expect(ConfigurationLoader.getInstance(configFile)).andReturn(mockConfigLoader).anyTimes();
        expect(mockConfigLoader.getRtbConfiguration()).andReturn(null).anyTimes();
        expect(mockConfigLoader.getLoggerConfiguration()).andReturn(null).anyTimes();
        expect(mockConfigLoader.getServerConfiguration()).andReturn(mockServerConfig).anyTimes();
        expect(mockConfigLoader.getAdapterConfiguration()).andReturn(null).anyTimes();
        expect(mockConfigLoader.getLog4jConfiguration()).andReturn(null).anyTimes();
        expect(mockConfigLoader.getDatabaseConfiguration()).andReturn(null).anyTimes();
        expect(mockServerConfig.getInt("percentRollout", 100)).andReturn(100).anyTimes();
        expect(mockServerConfig.getList("allowedSiteTypes")).andReturn(null).anyTimes();

        expect(mockConfigLoader.getCacheConfiguration()).andReturn(mockServerConfig).anyTimes();
        expect(mockServerConfig.subset(ChannelServerStringLiterals.CHANNEL_ADGROUP_REPOSITORY)).andReturn(
                mockServerConfig).anyTimes();
        expect(mockServerConfig.subset(ChannelServerStringLiterals.CHANNEL_REPOSITORY)).andReturn(mockServerConfig)
                .anyTimes();
        expect(mockServerConfig.subset(ChannelServerStringLiterals.CHANNEL_FEEDBACK_REPOSITORY)).andReturn(
                mockServerConfig).anyTimes();
        expect(mockServerConfig.subset(ChannelServerStringLiterals.CHANNEL_SEGMENT_FEEDBACK_REPOSITORY)).andReturn(
                mockServerConfig).anyTimes();
        expect(mockServerConfig.subset(ChannelServerStringLiterals.SITE_METADATA_REPOSITORY)).andReturn(
                mockServerConfig).anyTimes();
        expect(mockServerConfig.subset(ChannelServerStringLiterals.SITE_TAXONOMY_REPOSITORY)).andReturn(
                mockServerConfig).anyTimes();
        expect(mockServerConfig.subset(ChannelServerStringLiterals.PRICING_ENGINE_REPOSITORY)).andReturn(
                mockServerConfig).anyTimes();
        expect(mockServerConfig.subset(ChannelServerStringLiterals.SITE_FILTER_REPOSITORY)).andReturn(mockServerConfig)
                .anyTimes();
        expect(mockServerConfig.subset(ChannelServerStringLiterals.SITE_ECPM_REPOSITORY)).andReturn(mockServerConfig)
                .anyTimes();
        expect(mockServerConfig.subset(ChannelServerStringLiterals.CURRENCY_CONVERSION_REPOSITORY)).andReturn(
                mockServerConfig).anyTimes();
        expect(mockServerConfig.subset(ChannelServerStringLiterals.WAP_SITE_UAC_REPOSITORY))
                .andReturn(mockServerConfig).anyTimes();
        expect(mockServerConfig.subset(ChannelServerStringLiterals.IX_ACCOUNT_MAP_REPOSITORY)).andReturn(
                mockServerConfig).anyTimes();
        expect(mockServerConfig.subset(ChannelServerStringLiterals.CREATIVE_REPOSITORY)).andReturn(mockServerConfig)
                .anyTimes();
        expect(mockServerConfig.subset(ChannelServerStringLiterals.NATIVE_AD_TEMPLATE_REPOSITORY)).andReturn(
                mockServerConfig).anyTimes();
        expect(mockServerConfig.getString(ChannelServerStringLiterals.QUERY)).andReturn(LAST_UPDATE).anyTimes();
        replayAll();
    }

    private static void prepareMockDriverManager() throws SQLException {
        final String dbUser = "postgres";
        final String dbPassword = "mkhoj123";
        final String dbName = "pratap_dcp_jenkins_dont_delete";
        final String dbHost = "10.14.118.57";
        final String dbPort = "5499";
        final String connectionString = "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName;

        final Connection mockConnection = createMock(Connection.class);
        final Statement mockStatement = createMock(Statement.class);
        mockStatic(DriverManager.class);

        expect(DriverManager.getConnection(connectionString, dbUser, dbPassword)).andReturn(mockConnection).anyTimes();
        expect(mockConnection.createStatement()).andReturn(mockStatement).anyTimes();

        expect(mockStatement.executeQuery(REPLACE_STRING)).andReturn(null).anyTimes();
        mockStatement.close();
        expectLastCall().anyTimes();
        mockConnection.close();
        expectLastCall().anyTimes();
        replayAll();
    }

    private static void prepareMockCasConfigUtil() throws RepositoryException {
        final RepositoryHelper mockRepositoryHelper = createMock(RepositoryHelper.class);
        final ChannelAdGroupRepository mockChannelAdGroupRepository = createMock(ChannelAdGroupRepository.class);
        final ChannelRepository mockChannelRepository = createMock(ChannelRepository.class);
        final ChannelFeedbackRepository mockChannelFeedbackRepository = createMock(ChannelFeedbackRepository.class);
        final ChannelSegmentFeedbackRepository mockChannelSegmentFeedbackRepository =
                createMock(ChannelSegmentFeedbackRepository.class);
        final SiteMetaDataRepository mockSiteMetaDataRepository = createMock(SiteMetaDataRepository.class);
        final SiteTaxonomyRepository mockSiteTaxonomyRepository = createMock(SiteTaxonomyRepository.class);
        final SiteAerospikeFeedbackRepository mockSiteAerospikeFeedbackRepository =
                createMock(SiteAerospikeFeedbackRepository.class);
        final PricingEngineRepository mockPricingEngineRepository = createMock(PricingEngineRepository.class);
        final SiteFilterRepository mockSiteFilterRepository = createMock(SiteFilterRepository.class);
        final SiteEcpmRepository mockSiteEcpmRepository = createMock(SiteEcpmRepository.class);
        final CurrencyConversionRepository mockCurrencyConversionRepository =
                createMock(CurrencyConversionRepository.class);
        final WapSiteUACRepository mockWapSiteUACRepository = createMock(WapSiteUACRepository.class);
        final IXAccountMapRepository mockIxAccountMapRepository = createMock(IXAccountMapRepository.class);
        final CreativeRepository mockCreativeRepository = createMock(CreativeRepository.class);
        final NativeAdTemplateRepository mockNativeAdTemplateRepository = createMock(NativeAdTemplateRepository.class);

        expect(mockRepositoryHelper.getChannelRepository()).andReturn(mockChannelRepository).anyTimes();
        expect(mockRepositoryHelper.getChannelAdGroupRepository()).andReturn(mockChannelAdGroupRepository).anyTimes();
        expect(mockRepositoryHelper.getChannelFeedbackRepository()).andReturn(mockChannelFeedbackRepository).anyTimes();
        expect(mockRepositoryHelper.getChannelSegmentFeedbackRepository()).andReturn(
                mockChannelSegmentFeedbackRepository).anyTimes();
        expect(mockRepositoryHelper.getSiteMetaDataRepository()).andReturn(mockSiteMetaDataRepository).anyTimes();
        expect(mockRepositoryHelper.getSiteTaxonomyRepository()).andReturn(mockSiteTaxonomyRepository).anyTimes();
        expect(mockRepositoryHelper.getPricingEngineRepository()).andReturn(mockPricingEngineRepository).anyTimes();
        expect(mockRepositoryHelper.getSiteFilterRepository()).andReturn(mockSiteFilterRepository).anyTimes();
        expect(mockRepositoryHelper.getSiteEcpmRepository()).andReturn(mockSiteEcpmRepository).anyTimes();
        expect(mockRepositoryHelper.getCurrencyConversionRepository()).andReturn(mockCurrencyConversionRepository)
                .anyTimes();
        expect(mockRepositoryHelper.getWapSiteUACRepository()).andReturn(mockWapSiteUACRepository).anyTimes();
        expect(mockRepositoryHelper.getIxAccountMapRepository()).andReturn(mockIxAccountMapRepository).anyTimes();
        expect(mockRepositoryHelper.getCreativeRepository()).andReturn(mockCreativeRepository).anyTimes();
        expect(mockRepositoryHelper.getNativeAdTemplateRepository()).andReturn(mockNativeAdTemplateRepository)
                .anyTimes();

        expect(mockChannelRepository.newUpdateFromResultSetToOptimizeUpdate(null)).andReturn(null).anyTimes();
        expect(mockChannelAdGroupRepository.newUpdateFromResultSetToOptimizeUpdate(null)).andReturn(null).anyTimes();
        expect(mockChannelFeedbackRepository.newUpdateFromResultSetToOptimizeUpdate(null)).andReturn(null).anyTimes();
        expect(mockChannelSegmentFeedbackRepository.newUpdateFromResultSetToOptimizeUpdate(null)).andReturn(null)
                .anyTimes();
        expect(mockSiteMetaDataRepository.newUpdateFromResultSetToOptimizeUpdate(null)).andReturn(null).anyTimes();
        expect(mockSiteTaxonomyRepository.newUpdateFromResultSetToOptimizeUpdate(null)).andReturn(null).anyTimes();
        expect(mockPricingEngineRepository.newUpdateFromResultSetToOptimizeUpdate(null)).andReturn(null).anyTimes();
        expect(mockSiteFilterRepository.newUpdateFromResultSetToOptimizeUpdate(null)).andReturn(null).anyTimes();
        expect(mockSiteEcpmRepository.newUpdateFromResultSetToOptimizeUpdate(null)).andReturn(null).anyTimes();
        expect(mockCurrencyConversionRepository.newUpdateFromResultSetToOptimizeUpdate(null)).andReturn(null)
                .anyTimes();
        expect(mockWapSiteUACRepository.newUpdateFromResultSetToOptimizeUpdate(null)).andReturn(null).anyTimes();
        expect(mockIxAccountMapRepository.newUpdateFromResultSetToOptimizeUpdate(null)).andReturn(null).anyTimes();
        expect(mockCreativeRepository.newUpdateFromResultSetToOptimizeUpdate(null)).andReturn(null).anyTimes();
        expect(mockNativeAdTemplateRepository.newUpdateFromResultSetToOptimizeUpdate(null)).andReturn(null).anyTimes();

        mockStatic(CasConfigUtil.class);
        CasConfigUtil.repositoryHelper = mockRepositoryHelper;
        replayAll();
    }

    @BeforeClass
    public static void setUp() {
        prepareHttpRequestHandler();
        prepareMockQueryStringDecoder();
        prepareMockChannel();
        prepareMockConfigurationLoader();

        try {
            prepareMockCasConfigUtil();
            prepareMockDriverManager();
        } catch (final SQLException ignored) {

        } catch (final RepositoryException ignored) {}
    }

    // Add -XX:-UseSplitVerifier to VM options if running manually
    @Test
    public void testHandleRequest() throws Exception {
        for (int i = 0; i < 15; ++i) {
            final ServletRepoRefresh servlet = new ServletRepoRefresh();
            servlet.handleRequest(httpRequestHandler, mockQueryStringDecoder, mockChannel);
        }
    }

    @Test
    public void testGetName() {
        final ServletRepoRefresh servlet = new ServletRepoRefresh();
        assertThat(servlet.getName(), is(equalTo("RepoRefresh")));
    }
}
