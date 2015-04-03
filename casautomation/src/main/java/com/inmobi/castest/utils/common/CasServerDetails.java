package com.inmobi.castest.utils.common;

public class CasServerDetails {

    private final static String CAS_SERVER_IP = System.getProperty("cas_end_point") != null ? System
            .getProperty("cas_end_point") : "localhost:8800";

    private final static String CAS_SERVER_ENDPOINT = "http://" + CAS_SERVER_IP + "/";// "http://10.14.118.66:8800/";

    private final static String LOG_FILE_PATH = "/opt/mkhoj/logs/cas/debug/";

    private final static String LOG_PARSER_URL = CAS_SERVER_ENDPOINT + "logParser";

    private final static String FENDER_DEBUGGER = System.getProperty("fender_debugger") != null
            ? System.getProperty("fender_debugger") : "false";

    private final static String GENERATE_DATA = System.getProperty("data_gen") != null
            ? System.getProperty("data_gen") : "true";

    public static String getLogFilePath() {
        return LOG_FILE_PATH;
    }

    public static String getCasServerEndPoint() {
        return CAS_SERVER_ENDPOINT;
    }

    public static String getLogParserUrl() {
        return LOG_PARSER_URL;
    }
}
