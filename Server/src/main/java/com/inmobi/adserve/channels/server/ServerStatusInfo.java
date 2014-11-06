package com.inmobi.adserve.channels.server;

public class ServerStatusInfo {
    private ServerStatusInfo() {
    }

    private static int statusCode;
    private static String statusString;

    /**
     * @return the statusCode
     */
    public static final int getStatusCode() {
        return statusCode;
    }

    /**
     * @param sCode the statusCode to set
     * @param sString the statusString to set
     */
    public static synchronized final void setStatusCodeAndString(final int sCode, final String sString) {
        statusCode = sCode;
        statusString = sString;
    }

    /**
     * @return the statusString
     */
    public static final String getStatusString() {
        return statusString;
    }

}
