// Copyright 2012. InMobi. All Rights reserved

package com.inmobi.adserve.channels.util.instrumentation;

import java.util.concurrent.atomic.AtomicLong;


/**
 * Represents the notation of moving window for a given granularity, for a specific duration, represented as number of
 * buckets.
 */
/* package private */class CounterBucket {

    private final AtomicLong buckets[];

    private final long granularity;


    private long lastBucketTime;

    public CounterBucket(final long granularity, final int numBuckets) {
        buckets = new AtomicLong[numBuckets];
        for (int i = 0; i < buckets.length; i++) {
            buckets[i] = new AtomicLong();
        }
        this.granularity = granularity;
        lastBucketTime = 0;
    }

    private synchronized void clearBuckets(final long timeInGranularity) {
        final int start = (int) (lastBucketTime % buckets.length);
        for (int i = 1; i <= Math.min(timeInGranularity - lastBucketTime, buckets.length); ++i) {
            buckets[(start + i) % buckets.length].set(0L);
        }
        lastBucketTime = timeInGranularity;
    }

    void increment(final long currentTimeInMillis) {
        final long time = currentTimeInMillis / granularity;
        clearBuckets(time);
        buckets[(int) (time % buckets.length)].incrementAndGet();
    }

    long value() {
        clearBuckets(System.currentTimeMillis() / granularity);
        long sum = 0;
        for (final AtomicLong bucket : buckets) {
            sum += bucket.longValue();
        }
        return sum;
    }
}
