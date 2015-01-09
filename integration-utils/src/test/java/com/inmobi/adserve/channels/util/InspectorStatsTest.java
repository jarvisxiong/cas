package com.inmobi.adserve.channels.util;

import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.mockStaticPartial;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import java.net.InetAddress;
import java.net.UnknownHostException;
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
    public void testInitUnknownHostException() throws Exception {
        final String expectedMetricProducer = "unknown-host";
        final String graphiteServer = "graphiteServer";
        final int graphitePort = 1234;
        final int graphiteInterval = 100;

        mockStatic(InetAddress.class);
        mockStatic(GraphiteReporter.class);
        

        expect(InetAddress.getLocalHost()).andThrow(new UnknownHostException()).times(1);
        

        Configuration mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("graphiteServer.host", "mon02.ads.uj1.inmobi.com")).andReturn(graphiteServer).anyTimes();
        expect(mockConfig.getInt("graphiteServer.port", 1234)).andReturn(graphitePort).anyTimes();
        expect(mockConfig.getInt("graphiteServer.intervalInMinutes", 100)).andReturn(graphiteInterval).anyTimes();
        expect(mockConfig.getBoolean("graphiteServer.shouldLogAdapterLatencies", false)).andReturn(false).anyTimes();

        GraphiteReporter.enable(graphiteInterval, TimeUnit.MINUTES, graphiteServer, graphitePort,
            expectedMetricProducer);
        expectLastCall().times(1);

        replayAll();

        InspectorStats.init(mockConfig);

        verifyAll();
    }

    @Test
    public void testInit() throws Exception {
        final String hostName = "something.someThingElse.inmobi.com";
        final String expectedMetricProducer = "somethingelse.something";
        final String graphiteServer = "graphiteServer";
        final int graphitePort = 1234;
        final int graphiteInterval = 100;

        mockStatic(InetAddress.class);
        mockStatic(GraphiteReporter.class);
        final InetAddress mockInetAddress = createMock(InetAddress.class);

        expect(mockInetAddress.getHostName()).andReturn(hostName).times(1);
        expect(InetAddress.getLocalHost()).andReturn(mockInetAddress).times(1);

        
        Configuration mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("graphiteServer.host", "mon02.ads.uj1.inmobi.com")).andReturn(graphiteServer).anyTimes();
        expect(mockConfig.getInt("graphiteServer.port", 1234)).andReturn(graphitePort).anyTimes();
        expect(mockConfig.getInt("graphiteServer.intervalInMinutes", 100)).andReturn(graphiteInterval).anyTimes();
        expect(mockConfig.getBoolean("graphiteServer.shouldLogAdapterLatencies", false)).andReturn(false).anyTimes();
        
        GraphiteReporter.enable(graphiteInterval, TimeUnit.MINUTES, graphiteServer, graphitePort,
            expectedMetricProducer);
        expectLastCall().times(1);
        
        replayAll();
        InspectorStats.init(mockConfig);
        
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
