package com.inmobi.adserve.channels.server.circuitbreaker;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.mockStaticNice;
import static org.powermock.api.easymock.PowerMock.replayAll;
import junit.framework.TestCase;

import org.apache.commons.configuration.Configuration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.util.InspectorStats;

@RunWith(PowerMockRunner.class)
@PrepareForTest({InspectorStats.class, CasConfigUtil.class, CircuitBreakerImpl.class})
public class CircuitBreakerTest extends TestCase {

    private Configuration mockConfig;
    private String advertiserName = "advertiserName";
    
    public void setUp() throws Exception {
        mockStaticNice(InspectorStats.class);
        
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getLong("circuitbreaker.lengthOfMovingWindowCounterInSeconds")).andReturn(300L).anyTimes();
        expect(mockConfig.getDouble("circuitbreaker.failureThreshold")).andReturn(0.5).anyTimes();
        expect(mockConfig.getLong("circuitbreaker.minimumNumberOfRequests")).andReturn(2L).anyTimes();
        expect(mockConfig.getLong("circuitbreaker.numberOfSecondsUnderObservation")).andReturn(10L).anyTimes();
        
        mockStatic(CasConfigUtil.class);
        expect(CasConfigUtil.getServerConfig()).andReturn(mockConfig).anyTimes();
    }

    @Test
    public void testCircuitBreakerInitialialization() throws Exception {
        replayAll();
        CircuitBreakerImpl circuitBreaker = new CircuitBreakerImpl(advertiserName);
        assertThat(circuitBreaker.canForwardTheRequest(), is(equalTo(true)));
    }
    
    @Test
    public void testCircuitBreakerReinitialialization() throws Exception {
        replayAll();

        CircuitBreakerImpl circuitBreaker = new CircuitBreakerImpl(advertiserName);
        circuitBreaker.increamentRequestCounter(System.currentTimeMillis());

        long currentTimeAddedWithSixMinutes = System.currentTimeMillis() +11 * 60 * 1000; 
        mockStatic(System.class);
        expect(System.currentTimeMillis()).andReturn(currentTimeAddedWithSixMinutes).anyTimes();

        replayAll();
        
        assertThat(circuitBreaker.canForwardTheRequest(), is(equalTo(true)));
    }
    
    @Test
    public void testCircuitBreakerAfterOneRequestForwarding() throws Exception {
        replayAll();
        CircuitBreakerImpl circuitBreaker = new CircuitBreakerImpl(advertiserName);
        circuitBreaker.increamentRequestCounter(System.currentTimeMillis());
        assertThat(circuitBreaker.canForwardTheRequest(), is(equalTo(true)));
    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void testMovingToCircuitOpenState() throws Exception {
        replayAll();
        CircuitBreakerImpl circuitBreaker = new CircuitBreakerImpl(advertiserName);
        circuitBreaker.increamentRequestCounter(System.currentTimeMillis());
        circuitBreaker.increamentFailureCounter(System.currentTimeMillis());
        PowerMock.stub(CircuitBreakerImpl.class.getDeclaredMethod("isTenSecondsOver")).andReturn(true);
        assertThat(circuitBreaker.canForwardTheRequest(), is(equalTo(false)));
        PowerMock.reset(CircuitBreakerImpl.class);
    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void testMovingToCircuitObservationState() throws Exception {
        replayAll();
        CircuitBreakerImpl circuitBreaker = new CircuitBreakerImpl(advertiserName);
        circuitBreaker.increamentRequestCounter(System.currentTimeMillis());
        circuitBreaker.increamentFailureCounter(System.currentTimeMillis());
        
        PowerMock.stub(CircuitBreakerImpl.class.getDeclaredMethod("isTenSecondsOver")).andReturn(true);
        //circuitBreaker has moved to open state
        assertThat(circuitBreaker.canForwardTheRequest(), is(equalTo(false)));
        
        circuitBreaker.increamentRequestCounter(System.currentTimeMillis());
        circuitBreaker.increamentFailureCounter(System.currentTimeMillis());
        
        long currentTimeAddedWithSixMinutes = System.currentTimeMillis() + 6 * 60 * 1000; 
        mockStatic(System.class);
        expect(System.currentTimeMillis()).andReturn(currentTimeAddedWithSixMinutes).anyTimes();
        replayAll();
        
        //circuitBreaker has moved to Observation state
        assertThat(circuitBreaker.canForwardTheRequest(), is(equalTo(true)));
        PowerMock.reset(CircuitBreakerImpl.class);
    }
    
    @Test
    public void testMovingToCircuitOpenFromObservateionState() throws Exception {
        replayAll();
        CircuitBreakerImpl circuitBreaker = new CircuitBreakerImpl(advertiserName);
        circuitBreaker.setCurrentState(CircuitBreakerImpl.State.UNDER_OBSERVATION);
        circuitBreaker.increamentRequestCounter(System.currentTimeMillis());
        circuitBreaker.increamentFailureCounter(System.currentTimeMillis());
        circuitBreaker.increamentRequestCounter(System.currentTimeMillis());
        circuitBreaker.increamentFailureCounter(System.currentTimeMillis());
        
        long currentTimeAddedWithSixMinutes = System.currentTimeMillis() + 6 * 60 * 1000; 
        mockStatic(System.class);
        expect(System.currentTimeMillis()).andReturn(currentTimeAddedWithSixMinutes).anyTimes();
        replayAll();
        
        //Move to open state
        assertThat(circuitBreaker.canForwardTheRequest(), is(equalTo(false)));
    }
    
    @Test
    public void testMovingToCircuitClosedFromObservateionState() throws Exception {
        replayAll();
        CircuitBreakerImpl circuitBreaker = new CircuitBreakerImpl(advertiserName);
        circuitBreaker.setCurrentState(CircuitBreakerImpl.State.UNDER_OBSERVATION);
        circuitBreaker.increamentRequestCounter(System.currentTimeMillis());
        
        long currentTimeAddedWithSixMinutes = System.currentTimeMillis() + 6 * 60 * 1000; 
        mockStatic(System.class);
        expect(System.currentTimeMillis()).andReturn(currentTimeAddedWithSixMinutes).anyTimes();
        replayAll();
        
        //Move to circuit close state
        assertThat(circuitBreaker.canForwardTheRequest(), is(equalTo(true)));
    }
    
    @Test
    public void testSetCurrentStateMethod() throws Exception{
        replayAll();
        CircuitBreakerImpl circuitBreaker = new CircuitBreakerImpl(advertiserName);
        
        circuitBreaker.setCurrentState(CircuitBreakerImpl.State.UNDER_OBSERVATION);
        assertThat(circuitBreaker.canForwardTheRequest(), is(equalTo(true)));
        
        circuitBreaker.setCurrentState(CircuitBreakerImpl.State.CIRCUIT_CLOSED);
        assertThat(circuitBreaker.canForwardTheRequest(), is(equalTo(true)));
        
        circuitBreaker.setCurrentState(CircuitBreakerImpl.State.CIRCUIT_OPEN);
        assertThat(circuitBreaker.canForwardTheRequest(), is(equalTo(false)));
    }
}
