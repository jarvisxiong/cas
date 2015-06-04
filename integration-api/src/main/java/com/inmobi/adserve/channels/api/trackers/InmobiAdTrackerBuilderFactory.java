package com.inmobi.adserve.channels.api.trackers;

import com.inmobi.adserve.channels.api.SASRequestParameters;

/**
 * Created by ishanbhatnagar on 12/5/15.
 */

/**
 * Interface for InmobiAdTracker Builder Factories
 * This is should only be used by Guice.
 */
public interface InmobiAdTrackerBuilderFactory {
    public InmobiAdTrackerBuilder getBuilder(final SASRequestParameters sasParams, final String impressionId,
                                             final boolean pricingModel);
}