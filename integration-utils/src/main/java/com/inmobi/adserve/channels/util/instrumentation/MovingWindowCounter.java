package com.inmobi.adserve.channels.util.instrumentation;

import java.util.concurrent.atomic.AtomicLong;


/**
 * @author aman.gupta
 * @author rajashekhar.c
 * 
 *         A Counter which gives the number of increments that happened in the last 10 sec, last 5 min, and in total.
 * 
 *         Note that if number of increment calls exceed the max positive value of long, then the results of this
 *         counter cannot be trusted. The 'total' count will be the first one to be wrong, then the others.
 * 
 *         This class is thread safe
 */
public class MovingWindowCounter {

    private final CounterBucket movingWindowBucket;

    /**
     * Fields for total request count
     */
    volatile AtomicLong totalCounter = new AtomicLong(0);

    public MovingWindowCounter(final long numberOfSeconds) {
        movingWindowBucket = new CounterBucket(numberOfSeconds, 1000);
    }

    /**
     * The increment request call.
     */
    public void incrementRequest(final long currentTimeInMillis) {
        movingWindowBucket.increment(currentTimeInMillis);
        totalCounter.incrementAndGet();
    }


    /**
     * @return Number of total increments.
     */
    public void resetTotalCounters() {
        totalCounter.set(0);
    }

    /**
     * 
     * @return number of increments in the moving window of last 5 minutes
     */
    public long getMovingWindowCounter() {
        return movingWindowBucket.value();
    }

    /**
     * 
     * @return get total number of increments
     */
    public long getTotalCounter() {
        return totalCounter.get();
    }
}
