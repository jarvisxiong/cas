package com.inmobi.castest.utils.common;

public class CasDBDetails {
    private static final String DB_HOST =
        System.getProperty("db_host") != null ? System.getProperty("db_host") : "10.14.118.77";
    private static final String DB_PORT =
        System.getProperty("db_port") != null ? System.getProperty("db_port") : "5499";
    private static final String DB_NAME = System.getProperty("db_name") != null ? System.getProperty("db_name") : "ix";
    private static final String DB_USERNAME =
        System.getProperty("db_uname") != null ? System.getProperty("db_uname") : "brand_user";
    private static final String DB_PASSWORD =
        System.getProperty("db_pword") != null ? System.getProperty("db_pword") : "brand@123";

    private static final boolean IS_E2E = System.getProperty("is_e2e") != null ? true : false;

    public static String getDbHost() {
        return DB_HOST;
    }

    public static String getDbPort() {
        return DB_PORT;
    }

    public static String getDbName() {
        return DB_NAME;
    }

    public static String getDbUserName() {
        return DB_USERNAME;
    }

    public static String getDbPassword() {
        return DB_PASSWORD;
    }

    public static boolean isE2e() {
        return IS_E2E;
    }

}
