package com.inmobi.adserve.channels.server.requesthandler.filters.advertiser.impl;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.mockStaticNice;
import static org.powermock.api.easymock.PowerMock.replayAll;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.configuration.Configuration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.inmobi.adserve.channels.api.config.AdapterConfig;
import com.inmobi.adserve.channels.entity.ChannelEntity;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.server.requesthandler.filters.advertiser.AbstractAdvertiserLevelThrottler;
import com.inmobi.adserve.channels.util.InspectorStats;

@RunWith(PowerMockRunner.class)
@PrepareForTest({InspectorStats.class, CasConfigUtil.class, AbstractAdvertiserLevelThrottler.class})
public class AdvertiserFailureThrottlerTest extends TestCase {
    
    private String advertiserId;
    private String advertiserName;
    private String dcName;
    private ChannelSegment mockChannelSegment;
    private ChannelEntity mockChannelEntity;
    private Configuration mockConfig;
    private Map<String, AdapterConfig> advertiserIdConfigMap;

    public void setUp() throws Exception {
        advertiserId = "advertiserId";
        advertiserName = "adapterName";
        dcName = "dcName";

        mockChannelSegment = createMock(ChannelSegment.class);
        mockChannelEntity = createMock(ChannelEntity.class);

        mockStaticNice(InspectorStats.class);
        mockConfig = createMock(Configuration.class);
        
        expect(mockChannelSegment.getChannelEntity()).andReturn(mockChannelEntity).anyTimes();
        expect(mockConfig.getString("class")).andReturn("com.inmobi.adserve.channels.adnetworks.rtb.RtbAdNetwork").anyTimes();
        expect(mockConfig.getBoolean("isRtb", false)).andReturn(false).anyTimes();
        expect(mockConfig.getBoolean("isIx", false)).andReturn(false).anyTimes();
        expect(mockConfig.getString("advertiserId")).andReturn(advertiserId).anyTimes();
        expect(mockChannelEntity.getAccountId()).andReturn(advertiserId).anyTimes();
        
        
        mockStatic(CasConfigUtil.class);
        expect(CasConfigUtil.getServerConfig()).andReturn(mockConfig).anyTimes();
        expect(CasConfigUtil.getAdapterConfig()).andReturn(mockConfig).anyTimes();
    }

    @Test
    public void testCircuitBreakerDisabled() throws Exception {
        expect(mockConfig.getBoolean("circuitbreaker.disable", false)).andReturn(true).once();

        replayAll();
        

        AdapterConfig adapterConfig = new AdapterConfig(mockConfig, advertiserName, dcName, null);
        advertiserIdConfigMap = new HashMap<String, AdapterConfig>();
        advertiserIdConfigMap.put(advertiserId, adapterConfig);
        
        AdvertiserFailureThrottler filter = new AdvertiserFailureThrottler(null, advertiserIdConfigMap);
        assertThat(filter.failedInFilter(mockChannelSegment, null), is(equalTo(false)));
    }
    
    @Test
    public void testCircuitBreakerDisabledAtAdapterLevel() throws Exception {
        expect(mockConfig.getBoolean("circuitbreaker.disable", false)).andReturn(false).once();
        expect(mockConfig.getBoolean(advertiserName + ".excludeCircuitBreaker", false)).andReturn(true).once();

        replayAll();
        

        AdapterConfig adapterConfig = new AdapterConfig(mockConfig, advertiserName, dcName, null);
        advertiserIdConfigMap = new HashMap<String, AdapterConfig>();
        advertiserIdConfigMap.put(advertiserId, adapterConfig);
        
        AdvertiserFailureThrottler filter = new AdvertiserFailureThrottler(null, advertiserIdConfigMap);
        assertThat(filter.failedInFilter(mockChannelSegment, null), is(equalTo(false)));
    }
    
    @Test
    public void testCircuitBreakerWithNullCircuitBreaker() throws Exception {
        expect(mockConfig.getBoolean("circuitbreaker.disable", false)).andReturn(false).once();
        expect(mockConfig.getBoolean(advertiserName + ".excludeCircuitBreaker", false)).andReturn(false).once();

        replayAll();
        

        AdapterConfig adapterConfig = new AdapterConfig(mockConfig, advertiserName, dcName, null);
        advertiserIdConfigMap = new HashMap<String, AdapterConfig>();
        advertiserIdConfigMap.put(advertiserId, adapterConfig);
        
        AdvertiserFailureThrottler filter = new AdvertiserFailureThrottler(null, advertiserIdConfigMap);
        assertThat(filter.failedInFilter(mockChannelSegment, null), is(equalTo(false)));
    }
    
    @Test
    public void testCircuitBreakerWithStartTimeLessThanTenMinutes() throws Exception {
        expect(mockConfig.getBoolean("circuitbreaker.disable", false)).andReturn(false).once();
        expect(mockConfig.getBoolean(advertiserName + ".excludeCircuitBreaker", false)).andReturn(false).once();

        replayAll();
        

        AdapterConfig adapterConfig = new AdapterConfig(mockConfig, advertiserName, dcName, null);
        advertiserIdConfigMap = new HashMap<String, AdapterConfig>();
        advertiserIdConfigMap.put(advertiserId, adapterConfig);
        
        AdvertiserFailureThrottler filter = new AdvertiserFailureThrottler(null, advertiserIdConfigMap);
        assertThat(filter.failedInFilter(mockChannelSegment, null), is(equalTo(false)));
    }

    @Test
    public void testCircuitBreakerWithCircuitBreakerEntry() throws Exception {
        expect(mockConfig.getBoolean("circuitbreaker.disable", false)).andReturn(false).once();
        expect(mockConfig.getBoolean(advertiserName + ".excludeCircuitBreaker", false)).andReturn(false).once();
        expect(mockConfig.getString("circuitbreaker.class")).andReturn("com.inmobi.adserve.channels.server.circuitbreaker.CircuitBreaker").once();
        
        expect(mockConfig.getLong("circuitbreaker.lengthOfMovingWindowCounterInSeconds")).andReturn(300L).once();
        expect(mockConfig.getDouble("circuitbreaker.failureThreshold")).andReturn(0.5).once();
        expect(mockConfig.getLong("circuitbreaker.minimumNumberOfRequests")).andReturn(100L).once();
        expect(mockConfig.getLong("circuitbreaker.numberOfSecondsInOpenCircuit")).andReturn(300000L).once();
        expect(mockConfig.getLong("circuitbreaker.numberOfSecondsUnderObservation")).andReturn(10000L).once();
        
        replayAll();
        AdvertiserFailureThrottler.increamentRequestsCounter(advertiserId, System.currentTimeMillis());

        AdapterConfig adapterConfig = new AdapterConfig(mockConfig, advertiserName, dcName, null);
        advertiserIdConfigMap = new HashMap<String, AdapterConfig>();
        advertiserIdConfigMap.put(advertiserId, adapterConfig);
        
        AdvertiserFailureThrottler filter = new AdvertiserFailureThrottler(null, advertiserIdConfigMap);
        assertThat(filter.failedInFilter(mockChannelSegment, null), is(equalTo(false)));
    }

    @Test
    public void testCircuitBreakerWithTenMinutesComplete() throws Exception {
        expect(mockConfig.getBoolean("circuitbreaker.disable", false)).andReturn(false).once();
        expect(mockConfig.getBoolean(advertiserName + ".excludeCircuitBreaker", false)).andReturn(false).once();
        expect(mockConfig.getString("circuitbreaker.class")).andReturn(
                "com.inmobi.adserve.channels.server.circuitbreaker.CircuitBreaker").once();

        expect(mockConfig.getLong("circuitbreaker.lengthOfMovingWindowCounterInSeconds")).andReturn(300L).once();
        expect(mockConfig.getDouble("circuitbreaker.failureThreshold")).andReturn(0.5).once();
        expect(mockConfig.getLong("circuitbreaker.minimumNumberOfRequests")).andReturn(100L).once();
        expect(mockConfig.getLong("circuitbreaker.numberOfSecondsInOpenCircuit")).andReturn(300000L).once();
        expect(mockConfig.getLong("circuitbreaker.numberOfSecondsUnderObservation")).andReturn(10000L).once();
        
        long currentTimeAddedWithTenMinutes = System.currentTimeMillis() + 20 * 60 * 1000; 
        mockStatic(System.class);
        expect(System.currentTimeMillis()).andReturn(currentTimeAddedWithTenMinutes).anyTimes();
        replayAll();
        
        AdvertiserFailureThrottler.increamentRequestsCounter(advertiserId, System.currentTimeMillis());

        AdapterConfig adapterConfig = new AdapterConfig(mockConfig, advertiserName, dcName, null);
        advertiserIdConfigMap = new HashMap<String, AdapterConfig>();
        advertiserIdConfigMap.put(advertiserId, adapterConfig);

        AdvertiserFailureThrottler filter = new AdvertiserFailureThrottler(null, advertiserIdConfigMap);
        assertThat(filter.failedInFilter(mockChannelSegment, null), is(equalTo(false)));
    }
    
}
