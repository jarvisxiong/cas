package com.inmobi.adserve.channels.util;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.mockStaticPartial;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.Configuration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.reporting.GraphiteReporter;

@RunWith(PowerMockRunner.class)
@PrepareForTest({InspectorStats.class, GraphiteReporter.class})
public class InspectorStatsTest {

    @Test
    public void testInit() throws Exception {
        final String hostName = "cas1001.ads.uj1.inmobi.com";
        final String expectedMetricProducer = "nonprod.corp.cas-1.app";
        final String graphiteServer = "graphiteServer";
        final int graphitePort = 2020;
        final int graphiteInterval = 1;

        mockStatic(InetAddress.class);
        mockStatic(GraphiteReporter.class);
        mockStatic(System.class);
        expect(System.getProperty("run.environment", "test")).andReturn("prod").anyTimes();

        Configuration mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("host", "cas-metrics-relay.corp.inmobi.com")).andReturn(
                graphiteServer).anyTimes();
        expect(mockConfig.getInt("port", 2020)).andReturn(graphitePort).anyTimes();
        expect(mockConfig.getInt("intervalInMinutes", 1)).andReturn(graphiteInterval).anyTimes();
        expect(mockConfig.getBoolean("shouldLogAdapterLatencies", false)).andReturn(false).anyTimes();
        expect(mockConfig.getString("prefix")).andReturn("nonprod.corp.cas-1.app").anyTimes();

        GraphiteReporter.enable(isA(MetricsRegistry.class), eq((long) graphiteInterval), eq(TimeUnit.MINUTES), eq(graphiteServer), eq(graphitePort), eq(expectedMetricProducer));
        expectLastCall().anyTimes();

        replayAll();
        InspectorStats.init(mockConfig, hostName);

        verifyAll();
    }

    public static void setFinalField(Field field, Object newValue, Object obj) throws Exception {
        field.setAccessible(true);
        // remove final modifier from field
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(obj, newValue);
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
