package com.inmobi.adserve.channels.api.trackers;

import com.inmobi.adserve.channels.api.SASRequestParameters;

import lombok.RequiredArgsConstructor;

/**
 * Created by ishanbhatnagar on 14/5/15.
 */
@RequiredArgsConstructor
public abstract class InmobiAdTrackerBuilder {
    protected final SASRequestParameters sasParams;
    protected final String impressionId;
    protected final boolean isCpc;

    public abstract InmobiAdTracker buildInmobiAdTracker();
}
