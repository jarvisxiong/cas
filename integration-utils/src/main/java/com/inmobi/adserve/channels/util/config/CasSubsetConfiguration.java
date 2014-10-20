package com.inmobi.adserve.channels.util.config;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.SubsetConfiguration;

public class CasSubsetConfiguration extends SubsetConfiguration {

    public CasSubsetConfiguration(final Configuration parent, final String prefix) {
        super(parent, prefix);
    }

    public CasSubsetConfiguration(final Configuration parent, final String prefix, final String delimiter) {
        super(parent, prefix, delimiter);
    }

    @Override
    /**
     * The fix is related to https://jira.corp.inmobi.com/browse/DCP-448
     * {@inheritDoc} says --> Returns the interpolated value. Non String values are returned without change. 
     * It anyway doesn't validate non-string values, String values are anyway validated by the calling code, so this method is redundant and it was causing performance bottle-necks.
     * This was causing our threads to choke and it was taking 17% of our cpu time and is the reason for our threads running into monitor or blocked state.
     * 
     * Note: If one config value points to another config value, it is not supported. 
     * Currently this feature is not used by our code, this is not a must have feature
     * In future if anyone needs it, then this method needs to be udpated.
     */
    protected Object interpolate(final Object value) {
        return value;
    }
}
