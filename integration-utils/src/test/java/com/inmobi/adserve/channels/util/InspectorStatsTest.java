package com.inmobi.adserve.channels.util;

import com.yammer.metrics.reporting.GraphiteReporter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.mockStaticPartial;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

@RunWith(PowerMockRunner.class)
@PrepareForTest({InspectorStats.class, GraphiteReporter.class})
public class InspectorStatsTest {

    @Test
    public void testInitUnknownHostException() throws Exception {
        String expectedMetricProducer = "unknown-host";
        String graphiteServer = "graphiteServer";
        int graphitePort = 1234;
        int graphiteInterval = 100;

        mockStatic(InetAddress.class);
        mockStatic(GraphiteReporter.class);

        expect(InetAddress.getLocalHost())
                .andThrow(new UnknownHostException()).times(1);
        GraphiteReporter.enable(graphiteInterval, TimeUnit.MINUTES, graphiteServer, graphitePort, expectedMetricProducer);
        expectLastCall().times(1);

        replayAll();

        InspectorStats.init(graphiteServer,graphitePort, graphiteInterval);

        verifyAll();
    }

    @Test
    public void testInit() throws Exception {
        String hostName = "something.someThingElse.inmobi.com";
        String expectedMetricProducer = "somethingelse.something";
        String graphiteServer = "graphiteServer";
        int graphitePort = 1234;
        int graphiteInterval = 100;

        mockStatic(InetAddress.class);
        mockStatic(GraphiteReporter.class);
        InetAddress mockInetAddress = createMock(InetAddress.class);

        expect(mockInetAddress.getHostName()).andReturn(hostName).times(1);
        expect(InetAddress.getLocalHost())
                .andReturn(mockInetAddress).times(1);
        GraphiteReporter.enable(graphiteInterval, TimeUnit.MINUTES, graphiteServer, graphitePort, expectedMetricProducer);
        expectLastCall().times(1);

        replayAll();

        InspectorStats.init(graphiteServer,graphitePort, graphiteInterval);

        verifyAll();
    }

    @Test
    public void testIncrementStatCountParameterOnly() throws Exception {
        String key = "WorkFlow";
        String parameter = "param";
        long value = 1L;

        mockStaticPartial(InspectorStats.class, "incrementStatCount", String.class, String.class, long.class);

        InspectorStats.incrementStatCount(key, parameter, value);
        expectLastCall().times(1);

        replayAll();

        InspectorStats.incrementStatCount(key, parameter);

        verifyAll();
    }

    @Test
    public void testIncrementStatCountParameterAndValueOnly() throws Exception {
        String key = "WorkFlow";
        String parameter = "param";
        long value = 5L;

        mockStaticPartial(InspectorStats.class, "incrementStatCount", String.class, String.class, long.class);

        InspectorStats.incrementStatCount(key, parameter, value);
        expectLastCall().times(1);

        replayAll();

        InspectorStats.incrementStatCount(parameter, value);

        verifyAll();
    }

    @Test
    public void testIncrementStatCountKeyAndParameterOnly() throws Exception {
        String key = "WorkFlow";
        String parameter = "param";
        long value = 5L;

        mockStaticPartial(InspectorStats.class, "incrementStatCount", String.class, String.class, long.class);

        InspectorStats.incrementStatCount(key, parameter, value);
        expectLastCall().times(1);

        replayAll();

        InspectorStats.incrementStatCount(parameter, value);

        verifyAll();
    }
}