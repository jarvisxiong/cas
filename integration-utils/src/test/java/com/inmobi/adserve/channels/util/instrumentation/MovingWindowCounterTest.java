package com.inmobi.adserve.channels.util.instrumentation;

import junit.framework.TestCase;

import org.junit.Test;

public class MovingWindowCounterTest extends TestCase{

    @Test
    public void testMovingWindowCounter() throws Exception {
        MovingWindowCounter movingWindowCounter = new MovingWindowCounter(300);
        assertEquals(0L, movingWindowCounter.getMovingWindowCounter());
    }
    
    @Test
    public void testMovingWindowCounterWithIncrement() throws Exception {
        MovingWindowCounter movingWindowCounter = new MovingWindowCounter(300);
        movingWindowCounter.incrementRequest(System.currentTimeMillis());
        assertEquals(1L, movingWindowCounter.getMovingWindowCounter());
        assertEquals(1L, movingWindowCounter.getTotalCounter());
    }
    
    @Test
    public void testMovingWindowCounterWithIncrementAndReset() throws Exception {
        MovingWindowCounter movingWindowCounter = new MovingWindowCounter(300);
        movingWindowCounter.incrementRequest(System.currentTimeMillis());
        
        assertEquals(1L, movingWindowCounter.getMovingWindowCounter());
        assertEquals(1L, movingWindowCounter.getTotalCounter());
        
        movingWindowCounter.resetTotalCounters();
        assertEquals(0L, movingWindowCounter.getTotalCounter());
    }
}
