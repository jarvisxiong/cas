package com.inmobi.castest.utils.common;

public class DummyBidderDetails {

    private static final String DUMBID_PORT = System.getProperty("dumbid_port") != null ? System
            .getProperty("dumbid_port") : "8765";

    private static final String DUMBID_TIME_OUT = System.getProperty("dumbid_timeout") != null ? System
            .getProperty("dumbid_timeout") : "1";

    private static final String DUMBID_PERCENT_ADS = System.getProperty("dumbid_percent_ads") != null ? System
            .getProperty("dumbid_percent_ads") : "100";

    private static final String DUMBID_BUDGET = System.getProperty("dumbid_budget") != null ? System
            .getProperty("dumbid_budget") : "infinity";

    private static final String DUMBID_SEAT_ID = System.getProperty("dumbid_seatid") != null ? System
            .getProperty("dumbid_seatid") : "9ab79acef8764348a36a07b05fc3ee64";

    private static final String DUMBID_TOGGLE_UNDERSTRESS = System.getProperty("dumbid_under_stress") != null ? System
            .getProperty("dumbid_under_stress") : "yes";

    public static String getDumbidTimeOut() {
        return DUMBID_TIME_OUT;
    }

    public static String getDumbidPort() {
        return DUMBID_PORT;
    }

    public static String getDumbidPercentAds() {
        return DUMBID_PERCENT_ADS;
    }

    public static String getDumbidSeatId() {
        return DUMBID_SEAT_ID;
    }

    public static String getDumbidBudget() {
        return DUMBID_BUDGET;
    }

    public static String getDumbidToggleUnderstress() {
        return DUMBID_TOGGLE_UNDERSTRESS;
    }

}
