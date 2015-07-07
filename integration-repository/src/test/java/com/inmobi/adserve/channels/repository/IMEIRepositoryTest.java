package com.inmobi.adserve.channels.repository;

import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import java.util.concurrent.Executors;

import org.apache.commons.configuration.Configuration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.AerospikeException;
import com.aerospike.client.policy.ClientPolicy;
import com.inmobi.casthrift.DataCenter;
import com.inmobi.phoenix.exception.InitializationException;

/**
 * 
 * @author ritwik.kumar
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({IMEIAerospikeRepository.class, DataCenter.class})
public class IMEIRepositoryTest {

    @Test(expected = InitializationException.class)
    public void testAerospikeInitConfigAndColoNull() throws Exception {
        final IMEIAerospikeRepository tested = new IMEIAerospikeRepository();
        tested.init(null, null);
    }

    @Test(expected = InitializationException.class)
    public void testAerospikeInitConfigNull() throws Exception {
        final DataCenter mockColo = createMock(DataCenter.class);
        replayAll();
        final IMEIAerospikeRepository tested = new IMEIAerospikeRepository();
        tested.init(null, mockColo);
        verifyAll();
    }

    @Test(expected = InitializationException.class)
    public void testAerospikeInitColoNull() throws Exception {
        final Configuration mockConfig = createMock(Configuration.class);
        replayAll();
        final IMEIAerospikeRepository tested = new IMEIAerospikeRepository();
        tested.init(mockConfig, null);
        verifyAll();
    }

    @Test
    public void testAerospikeInitInNonHKG() throws Exception {
        mockStatic(Executors.class);
        final Configuration mockConfig = createMock(Configuration.class);
        replayAll();

        final IMEIAerospikeRepository tested = new IMEIAerospikeRepository();
        tested.init(mockConfig, DataCenter.UH1);
        org.junit.Assert.assertNull(tested.aerospikeClient);
        verifyAll();
    }

    @Test(expected = InitializationException.class)
    public void testAerospikeInitIntializationException() throws Exception {
        mockStatic(Executors.class);
        final ClientPolicy mockClientPolicy = createMock(ClientPolicy.class);
        final Configuration mockConfig = createMock(Configuration.class);

        expect(mockConfig.getString("imeiNamespae")).andReturn("imeiNamespae").times(1);
        expect(mockConfig.getString("imeiSet")).andReturn("imeiSet").times(1);
        expect(mockConfig.getString("host")).andReturn("host").times(2);
        expect(mockConfig.getInt("port")).andReturn(16).times(2);
        expect(Executors.newCachedThreadPool()).andReturn(null).times(1);
        expectNew(ClientPolicy.class).andReturn(mockClientPolicy).times(1);
        replayAll();
        expectNew(AerospikeClient.class, new Class[] {ClientPolicy.class, String.class, int.class}, mockClientPolicy,
                mockConfig.getString("host"), mockConfig.getInt("port")).andThrow(new AerospikeException()).times(1);
        replayAll();

        final IMEIAerospikeRepository tested = new IMEIAerospikeRepository();
        tested.init(mockConfig, DataCenter.HKG1);
        verifyAll();
    }
}
