package com.inmobi.castest.commons.iohelper;

import java.io.File;
import java.io.FileReader;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;



public class JsonDataIOHelper {



    // private static final Exception SuiteConfigurationNotPresentException = new Exception("Suite File Not Found ! ");

    public static Map<String, String> readTestParams(String testCase, final String className) throws Exception {

        testCase = testCase.toUpperCase();
        Map<String, String> testParamMap;
        final String fileName = className.substring(className.lastIndexOf('.') + 1, className.length());
        System.out.println("* * * * * Running Suite : " + fileName + " * * * * *");
        File brandTestData = new File("src/test/resources/" + fileName + ".json");
        if (!brandTestData.exists()) {
            brandTestData = new File("casautomation/src/test/resources/" + fileName + ".json");
        }

        final JSONParser jsonParser = new JSONParser();
        final Object object = jsonParser.parse(new FileReader(brandTestData));

        final JSONObject jsonObject = (JSONObject) object;

        final JSONObject requestJsonObject = (JSONObject) jsonObject.get(testCase);
        testParamMap = (Map) requestJsonObject.get("Request_Params");
        testParamMap.put("site_siteid", requestJsonObject.get("Site_id").toString());

        System.out.println("* * * Request Parametes set for Test case : " + testCase + " * * *");
        System.out.println(testParamMap);

        return testParamMap;
        //
        // final Yaml yml = new Yaml();
        // final Object x = yml.load(input);

        // JsonReader jsonReader = JsonReader
        // return map;
    }

    public static Map<String, String> readValidations(String testCase, final String className) throws Exception {

        testCase = testCase.toUpperCase();

        final String fileName = className.substring(className.lastIndexOf('.') + 1, className.length());
        System.out.println(fileName);
        File brandTestData = new File("src/test/resources/" + fileName + ".json");
        if (!brandTestData.exists()) {
            brandTestData = new File("casautomation/src/test/resources/" + fileName + ".json");
        }

        final JSONParser jsonParser = new JSONParser();
        final Object object = jsonParser.parse(new FileReader(brandTestData));

        final JSONObject jsonObject = (JSONObject) object;

        final JSONObject validationsJsonObject = (JSONObject) jsonObject.get(testCase);
        System.out.println(validationsJsonObject);
        System.out.println(validationsJsonObject.get("Validations"));
        System.out.println(validationsJsonObject.get("Validations"));

        return (Map) validationsJsonObject.get("Validations");
        //
        // final Yaml yml = new Yaml();
        // final Object x = yml.load(input);

        // JsonReader jsonReader = JsonReader
        // return map;
    }

    public static void main(final String[] args) throws Exception {

        readTestParams("TC_1", "BrandTest");

    }
}
