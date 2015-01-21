package com.inmobi.adserve.channels.api;

import static org.powermock.api.easymock.PowerMock.mockStaticNice;
import static org.powermock.api.easymock.PowerMock.replayAll;
import junit.framework.TestCase;

import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.testng.annotations.Test;

import com.inmobi.adserve.channels.util.InspectorStats;

@RunWith(PowerMockRunner.class)
@PrepareForTest({InspectorStats.class})
public class IPRepositoryTest  extends TestCase{
    private IPRepository ipRepository;
    
    public void setUp() {

        ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
    }

    @Test
    public void testGetter() {
        mockStaticNice(InspectorStats.class);
        replayAll();
        String actualUrlAddr = ipRepository.getIPAddress("http://localhost:8080/getBid", null);
        String expectedUrlAddr = "http://127.0.0.1:8080/getBid";
        assertEquals(expectedUrlAddr, actualUrlAddr);
    }
    
    @Test
    public void testGetterWithSpecialCharacterInUrl() {
        mockStaticNice(InspectorStats.class);
        replayAll();
        String actualUrlAddr = ipRepository.getIPAddress("http://localhost:8080/%s/getBid", null);
        String expectedUrlAddr = null;
        assertEquals(expectedUrlAddr, actualUrlAddr);
    }
    
    @Test
    public void testGetterWithInvalidHost() {
        mockStaticNice(InspectorStats.class);
        replayAll();
        String actualUrlAddr = ipRepository.getIPAddress("http://localho:8080/getBid", null);
        String expectedUrlAddr = "http://localho:8080/getBid";;
        assertEquals(expectedUrlAddr, actualUrlAddr);
    }
}
