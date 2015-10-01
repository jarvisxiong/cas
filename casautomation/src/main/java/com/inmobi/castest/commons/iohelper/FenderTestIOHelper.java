package com.inmobi.castest.commons.iohelper;

import java.io.FileNotFoundException;
import java.util.Map;

public class FenderTestIOHelper {

    public static Map<String, String> setTestParams(final String testName) throws FileNotFoundException {

        return YamlDataIOHelper.readTestParams(testName);

    }

    public static String getPartnerDetails(final String testCaseName) throws FileNotFoundException {
        return YamlDataIOHelper.readTestIndex().get(testCaseName.toUpperCase());
    }

    public static Map<String, String> setDGTestParams(final String testName, final String className) throws Exception {

        return JsonDataIOHelper.readTestParams(testName, className);


    }
}
