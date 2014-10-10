package com.inmobi.adserve.channels.server.servlet;

import com.inmobi.adserve.channels.repository.ChannelAdGroupRepository;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.server.ChannelServerStringLiterals;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.requesthandler.ResponseSender;
import com.inmobi.adserve.channels.util.ConfigurationLoader;
import com.inmobi.phoenix.exception.RepositoryException;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.apache.commons.configuration.Configuration;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.createNiceMock;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ConfigurationLoader.class, ServletRepoRefresh.class, ChannelAdGroupRepository.class})
public class ServletRepoRefreshTest {
    private static HttpRequestHandler  httpRequestHandler;
    private static QueryStringDecoder  mockQueryStringDecoder;
    private static Channel             mockChannel;
    private static ConfigurationLoader mockConfigLoader;

    private static final String LAST_UPDATE    = "'${last_update}'";
    private static final String REPLACE_STRING = "now() -interval '1 MINUTE'";

    final private static String json1 = "{\"repoName\":\"ChannelAdGroupRepository\",\"DBHost\":\"10.14.118.57\",\"DBPort\":\"5499\",\"DBSnapshot\":\"pratap_dcp_jenkins_dont_delete\",\"DBUser\":\"postgres\",\"DBPassword\":\"mkhoj123\"}";

    private static Map<String, List<String>> createMapFromString(String json) {
        Map<String, List<String>> params = new HashMap<>();
        params.put("args", Arrays.asList(json));
        return params;
    }

    private static void prepareHttpRequestHandler() {
        ResponseSender mockResponseSender = createNiceMock(ResponseSender.class);
        httpRequestHandler = new HttpRequestHandler(null, null, mockResponseSender);
        replayAll();
    }

    private static void prepareMockQueryStringDecoder() {
        mockQueryStringDecoder = createMock(QueryStringDecoder.class);
        expect(mockQueryStringDecoder.parameters())
                .andReturn(createMapFromString(json1)).times(1);
        replayAll();
    }

    private static void prepareMockChannel() {
        mockChannel = createMock(Channel.class);
        replayAll();
    }

    private static void prepareMockConfigurationLoader() {
        mockConfigLoader               = createMock(ConfigurationLoader.class);
        Configuration mockServerConfig = createMock(Configuration.class);
        mockStatic(ConfigurationLoader.class);

        expect(ConfigurationLoader.getInstance("/opt/mkhoj/conf/cas/channel-server.properties"))
                .andReturn(mockConfigLoader).anyTimes();
        expect(mockConfigLoader.getRtbConfiguration()).andReturn(null).anyTimes();
        expect(mockConfigLoader.getLoggerConfiguration()).andReturn(null).anyTimes();
        expect(mockConfigLoader.getServerConfiguration()).andReturn(mockServerConfig).anyTimes();
        expect(mockConfigLoader.getAdapterConfiguration()).andReturn(null).anyTimes();
        expect(mockConfigLoader.getLog4jConfiguration()).andReturn(null).anyTimes();
        expect(mockConfigLoader.getDatabaseConfiguration()).andReturn(null).anyTimes();
        expect(mockServerConfig.getInt("percentRollout", 100)).andReturn(100).anyTimes();
        expect(mockServerConfig.getList("allowedSiteTypes")).andReturn(null).anyTimes();

        expect(mockConfigLoader.getCacheConfiguration()).andReturn(mockServerConfig).anyTimes();
        expect(mockServerConfig.subset(ChannelServerStringLiterals.CHANNEL_ADGROUP_REPOSITORY)).andReturn(mockServerConfig).anyTimes();
        expect(mockServerConfig.getString(ChannelServerStringLiterals.QUERY)).andReturn(LAST_UPDATE).anyTimes();
        replayAll();
    }

    private static void prepareMockDriverManager() throws SQLException {
        final String dbUser           = "postgres";
        final String dbPassword       = "mkhoj123";
        final String dbName           = "pratap_dcp_jenkins_dont_delete";
        final String dbHost           = "10.14.118.57";
        final String dbPort           = "5499";
        final String connectionString = "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName;

        Connection mockConnection = createMock(Connection.class);
        Statement mockStatement   = createMock(Statement.class);
        mockStatic(DriverManager.class);

        expect(DriverManager.getConnection(connectionString, dbUser, dbPassword)).andReturn(mockConnection).anyTimes();
        expect(mockConnection.createStatement()).andReturn(mockStatement).anyTimes();

        expect(mockStatement.executeQuery(REPLACE_STRING)).andReturn(null).anyTimes();
        mockStatement.close();
        expectLastCall();
        mockConnection.close();
        expectLastCall();
        replayAll();
    }

    private static void prepareMockCasConfigUtil() throws RepositoryException {
        RepositoryHelper mockRepositoryHelper = createMock(RepositoryHelper.class);
        ChannelAdGroupRepository mockChannelAdGroupRepository = createMock(ChannelAdGroupRepository.class);

        expect(mockRepositoryHelper.getChannelAdGroupRepository()).andReturn(mockChannelAdGroupRepository).anyTimes();
        expect(mockChannelAdGroupRepository.newUpdateFromResultSetToOptimizeUpdate(null)).andReturn(null).anyTimes();

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
        }
        catch (SQLException ignored) {

        }
        catch (RepositoryException ignored) {
        }
    }

    // TODO: Change to parameterised
    // Add -XX:-UseSplitVerifier to VM options if running manually
    @Test
    public void testHandleRequest() throws Exception {
        ServletRepoRefresh servlet = new ServletRepoRefresh();

        servlet.handleRequest(httpRequestHandler, mockQueryStringDecoder, mockChannel);
    }

    @Test
    public void testGetName() {
        ServletRepoRefresh servlet = new ServletRepoRefresh();
        assertThat(servlet.getName(), is(equalTo("RepoRefresh")));
    }
}