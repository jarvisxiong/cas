package com.inmobi.adserve.channels.util;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.mockStaticPartial;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.Configuration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.yammer.metrics.reporting.GraphiteReporter;

@RunWith(PowerMockRunner.class)
@PrepareForTest({InspectorStats.class, GraphiteReporter.class})
public class InspectorStatsTest {

    @Test
    public void testInit() throws Exception {
        final String hostName = "cas1001.ads.uj1.inmobi.com";
        final String expectedMetricProducer = "prod.uj1.cas-1.app.cas1001";
        final String graphiteServer = "graphiteServer";
        final int graphitePort = 1234;
        final int graphiteInterval = 1;

        mockStatic(InetAddress.class);
        mockStatic(GraphiteReporter.class);
        mockStatic(System.class);
        expect(System.getProperty("run.environment", "test")).andReturn("prod").anyTimes();

        Configuration mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("graphiteServer.host", "cas-metrics-relay.uj1.inmobi.com")).andReturn(graphiteServer).anyTimes();
        expect(mockConfig.getInt("graphiteServer.port", 1234)).andReturn(graphitePort).anyTimes();
        expect(mockConfig.getInt("graphiteServer.intervalInMinutes", 1)).andReturn(graphiteInterval).anyTimes();
        expect(mockConfig.getBoolean("graphiteServer.shouldLogAdapterLatencies", false)).andReturn(false).anyTimes();

        GraphiteReporter.enable(graphiteInterval, TimeUnit.MINUTES, graphiteServer, graphitePort,
                expectedMetricProducer);
        expectLastCall().times(1);

        replayAll();
        InspectorStats.init(mockConfig, hostName);

        verifyAll();
    }

    @Test
    public void testGetMetricProducer() throws Exception {
        final String uj1ProdUrl = "cas1000.ads.uj1.inmobi.com";
        final String uh1ProdUrl = "cas1004.ads.uh1.inmobi.com";
        final String hkg1ProdUrl = "cas2000.ads.hkg1.inmobi.com";
        final String lhr1ProdUrl = "cas2002.ads.lhr1.inmobi.com";
        final String WrongColoProdUrl = "cas1000.ads.ul1.inmobi.com";
        final String WrongBoxProdUrl = "cas100.ads.ul1.inmobi.com";
        final String WrongRandomProdUrl = "random1.random2";
        final String WrongNoDotProdUrl = "random1random2random3";
        final String WrongNullProdUrl = null;
        final String WrongBlankProdUrl = "";

        final String expectedUj1Metric = "prod.uj1.cas-1.app.cas1000";
        final String expectedUh1Metric = "prod.uh1.cas-1.app.cas1004";
        final String expectedHkg1Metric = "prod.hkg1.cas-1.app.cas2000";
        final String expectedLhr1Metric = "prod.lhr1.cas-1.app.cas2002";
        final String expectedWrongColoMetric = "test.cas-1.app.cas1000";
        final String expectedWrongBoxMetric = "test.cas-1.app.cas100";
        final String expectedWrongRandomMetric = "test.cas-1.app.random1";
        final String expectedWrongNoDotMetric = "test.cas-1.app.random1random2random3";
        final String expectedWrongNullMetric = "test.cas-1.app.unknown-host";
        final String expectedWrongBlankMetric = "test.cas-1.app.unknown-host";

        mockStatic(System.class);
        expect(System.getProperty("run.environment", "test")).andReturn("prod").anyTimes();
        replayAll();

        String uj1MetricProducer = InspectorStats.getMetricProducer(uj1ProdUrl);
        String uh1MetricProducer = InspectorStats.getMetricProducer(uh1ProdUrl);
        String hkg1MetricProducer = InspectorStats.getMetricProducer(hkg1ProdUrl);
        String lhr1MetricProducer = InspectorStats.getMetricProducer(lhr1ProdUrl);
        String wrongColoMetricProducer = InspectorStats.getMetricProducer(WrongColoProdUrl);
        String wrongBoxMetricProducer = InspectorStats.getMetricProducer(WrongBoxProdUrl);
        String wrongRandomMetricProducer = InspectorStats.getMetricProducer(WrongRandomProdUrl);
        String wrongNoDotMetricProducer = InspectorStats.getMetricProducer(WrongNoDotProdUrl);
        String wrongNullMetricProducer = InspectorStats.getMetricProducer(WrongNullProdUrl);
        String wrongBlankMetricProducer = InspectorStats.getMetricProducer(WrongBlankProdUrl);

        assertThat(uj1MetricProducer, is(equalTo(expectedUj1Metric)));
        assertThat(uh1MetricProducer, is(equalTo(expectedUh1Metric)));
        assertThat(hkg1MetricProducer, is(equalTo(expectedHkg1Metric)));
        assertThat(lhr1MetricProducer, is(equalTo(expectedLhr1Metric)));
        assertThat(wrongColoMetricProducer, is(equalTo(expectedWrongColoMetric)));
        assertThat(wrongBoxMetricProducer, is(equalTo(expectedWrongBoxMetric)));
        assertThat(wrongRandomMetricProducer, is(equalTo(expectedWrongRandomMetric)));
        assertThat(wrongNoDotMetricProducer, is(equalTo(expectedWrongNoDotMetric)));
        assertThat(wrongNullMetricProducer, is(equalTo(expectedWrongNullMetric)));
        assertThat(wrongBlankMetricProducer, is(equalTo(expectedWrongBlankMetric)));

        verifyAll();
    }

    @Test
    public void testIncrementStatCountParameterOnly() throws Exception {
        final String key = "WorkFlow";
        final String parameter = "param";
        final long value = 1L;

        mockStaticPartial(InspectorStats.class, "incrementStatCount", String.class, String.class, long.class);

        InspectorStats.incrementStatCount(key, parameter, value);
        expectLastCall().times(1);

        replayAll();

        InspectorStats.incrementStatCount(key, parameter);

        verifyAll();
    }

    @Test
    public void testIncrementStatCountParameterAndValueOnly() throws Exception {
        final String key = "WorkFlow";
        final String parameter = "param";
        final long value = 5L;

        mockStaticPartial(InspectorStats.class, "incrementStatCount", String.class, String.class, long.class);

        InspectorStats.incrementStatCount(key, parameter, value);
        expectLastCall().times(1);

        replayAll();

        InspectorStats.incrementStatCount(parameter, value);

        verifyAll();
    }

    @Test
    public void testIncrementStatCountKeyAndParameterOnly() throws Exception {
        final String key = "WorkFlow";
        final String parameter = "param";
        final long value = 5L;

        mockStaticPartial(InspectorStats.class, "incrementStatCount", String.class, String.class, long.class);

        InspectorStats.incrementStatCount(key, parameter, value);
        expectLastCall().times(1);

        replayAll();

        InspectorStats.incrementStatCount(parameter, value);

        verifyAll();
    }
}
