package com.inmobi.castest.utils.common;

public class BrandServerDetails {

    private final static String BRAND_SERVER_IP = System.getProperty("brand_end_point") != null ? System
            .getProperty("brand_end_point") : "10.14.117.6";

    private final static String BRAND_SERVER_ENDPOINT = "http://" + BRAND_SERVER_IP + ":8080/";
    // private final static String CAS_SERVER_ENDPOINT = "http://10.14.118.66:8800/";

    private final static String FENDER_DEBUGGER = System.getProperty("fender_debugger") != null ? System
            .getProperty("fender_debugger") : "false";

    private final static String GENERATE_DATA = System.getProperty("data_gen") != null
            ? System.getProperty("data_gen")
            : "true";

    public static String getBrandServerEndPoint() {
        return BRAND_SERVER_ENDPOINT;
    }
}
