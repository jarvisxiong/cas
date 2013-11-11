package com.inmobi.adserve.channels.adnetworks.mullahmedia;

import org.apache.commons.configuration.Configuration;


public class MoolahMediaPremiumReporting extends BaseMoolahMediaReporting {

    public MoolahMediaPremiumReporting(Configuration config) {
        super(config);
        email = config.getString("mmpremium.email");
        password = config.getString("mmpremium.password");
        host = config.getString("mmpremium.host");
        advertiserName = "MMPremium";
        advertiserId = config.getString("mmpremium.advertiserId");
    }

}
