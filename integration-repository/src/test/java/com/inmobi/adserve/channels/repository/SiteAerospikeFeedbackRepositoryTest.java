package com.inmobi.adserve.channels.repository;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.AerospikeException;
import com.aerospike.client.policy.ClientPolicy;
import com.inmobi.casthrift.DataCenter;
import com.inmobi.phoenix.exception.InitializationException;
import org.apache.commons.configuration.Configuration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.concurrent.Executors;

import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

@RunWith(PowerMockRunner.class)
@PrepareForTest({SiteAerospikeFeedbackRepository.class, DataCenter.class})
public class SiteAerospikeFeedbackRepositoryTest {

    @Test(expected = InitializationException.class)
    public void testAerospikeInitConfigAndColoNull() throws Exception{
        SiteAerospikeFeedbackRepository tested = new SiteAerospikeFeedbackRepository();
        tested.init(null, null);
    }

    @Test(expected = InitializationException.class)
    public void testAerospikeInitConfigNull() throws Exception{
        DataCenter mockColo = createMock(DataCenter.class);
        replayAll();
        SiteAerospikeFeedbackRepository tested = new SiteAerospikeFeedbackRepository();
        tested.init(null, mockColo);
        verifyAll();
    }

    @Test(expected = InitializationException.class)
    public void testAerospikeInitColoNull() throws Exception{
        Configuration mockConfig = createMock(Configuration.class);
        replayAll();
        SiteAerospikeFeedbackRepository tested = new SiteAerospikeFeedbackRepository();
        tested.init(mockConfig, null);
        verifyAll();
    }

    @Test
    public void testAerospikeInit() throws Exception{
        mockStatic(Executors.class);
        ClientPolicy mockClientPolicy = createMock(ClientPolicy.class);
        Configuration mockConfig = createMock(Configuration.class);
        DataCenter mockColo = createMock(DataCenter.class);
        AerospikeClient mockAeroSpikeClient = createMock(AerospikeClient.class);

        expect(mockConfig.getString("namespace")).andReturn("namespace").times(1);
        expect(mockConfig.getString("set")).andReturn("set").times(1);
        expect(mockConfig.getString("host")).andReturn("host").times(2);
        expect(mockConfig.getInt("port")).andReturn(16).times(2);
        expect(mockConfig.getInt("refreshTime")).andReturn(30000).times(1);
        expect(mockConfig.getInt("feedbackTimeFrame", 15)).andReturn(15).times(1);
        expect(mockConfig.getInt("boostTimeFrame", 3)).andReturn(3).times(1);
        expect(mockConfig.getDouble("default.ecpm", 0.25)).andReturn(0.25).times(1);
        expect(Executors.newCachedThreadPool()).andReturn(null).times(1);
        expectNew(ClientPolicy.class).andReturn(mockClientPolicy).times(1);
        replayAll();
        expectNew(AerospikeClient.class, new Class[]{ClientPolicy.class, String.class, int.class}, mockClientPolicy, mockConfig.getString("host"), mockConfig.getInt("port"))
                .andReturn(mockAeroSpikeClient).times(1);
        replayAll();

        SiteAerospikeFeedbackRepository tested = new SiteAerospikeFeedbackRepository();
        tested.init(mockConfig, mockColo);
        verifyAll();
    }

    @Test(expected = InitializationException.class)
    public void testAerospikeInitIntializationException() throws Exception{
        mockStatic(Executors.class);
        ClientPolicy mockClientPolicy = createMock(ClientPolicy.class);
        Configuration mockConfig = createMock(Configuration.class);
        DataCenter mockColo = createMock(DataCenter.class);

        expect(mockConfig.getString("namespace")).andReturn("namespace").times(1);
        expect(mockConfig.getString("set")).andReturn("set").times(1);
        expect(mockConfig.getString("host")).andReturn("host").times(2);
        expect(mockConfig.getInt("port")).andReturn(16).times(2);
        expect(mockConfig.getInt("refreshTime")).andReturn(30000).times(1);
        expect(mockConfig.getInt("feedbackTimeFrame", 15)).andReturn(15).times(1);
        expect(mockConfig.getInt("boostTimeFrame", 3)).andReturn(3).times(1);
        expect(mockConfig.getDouble("default.ecpm", 0.25)).andReturn(0.25).times(1);
        expect(Executors.newCachedThreadPool()).andReturn(null).times(1);
        expectNew(ClientPolicy.class).andReturn(mockClientPolicy).times(1);
        replayAll();
        expectNew(AerospikeClient.class, new Class[]{ClientPolicy.class, String.class, int.class}, mockClientPolicy, mockConfig.getString("host"), mockConfig.getInt("port"))
                .andThrow(new AerospikeException()).times(1);
        replayAll();

        SiteAerospikeFeedbackRepository tested = new SiteAerospikeFeedbackRepository();
        tested.init(mockConfig, mockColo);
        verifyAll();
    }
}